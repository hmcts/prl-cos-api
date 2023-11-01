package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.CourtOrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildCourtOrderElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.bothLiveWithAndSpendTimeWithOrder;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.liveWithOrder;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.spendTimeWithOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.prohibitedStepsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.specificIssueOrder;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper.COMMA_SEPARATOR;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper.HYPHEN_SEPARATOR;

public class CaseDataTypeOfOrderElementsMapper {

    private CaseDataTypeOfOrderElementsMapper() {
    }

    private static final String SHORT_STATEMENT_INFO = "Short Statement Information";
    private static final String WHO_THE_CHILD_LIVE_WITH = "whoChildLiveWith";
    private static final String CHILD_TIME_SPENT = "childTimeSpent";
    private static final String STOP_OTHER_DOING_SOMETHING = "stopOtherPeopleDoingSomething";
    private static final String RESOLVE_SPECIFIC_ISSUE = "resolveSpecificIssue";

    public static void updateTypeOfOrderElementsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                      C100RebuildCourtOrderElements c100RebuildCourtOrderElements) {
        caseDataBuilder
                .ordersApplyingFor(buildOrdersApplyingFor(c100RebuildCourtOrderElements))
                .typeOfChildArrangementsOrder(buildTypeOfChildArrangementsOrder(c100RebuildCourtOrderElements))
                .natureOfOrder(buildNatureOfOrder(c100RebuildCourtOrderElements));
    }

    private static String buildNatureOfOrder(C100RebuildCourtOrderElements c100RebuildCourtOrderElements) {

        List<String> prohibitedOrderList = nonNull(c100RebuildCourtOrderElements.getReasonsOfHearingWithoutNotice())
                ? Arrays.stream(c100RebuildCourtOrderElements.getReasonsOfHearingWithoutNotice())
                .map(element -> CourtOrderTypeEnum.valueOf(element).getDisplayedValue())
                        .toList() : Collections.emptyList();

        List<String> specificIssueOrderList = nonNull(c100RebuildCourtOrderElements.getResolveSpecificIssueSubField())
                ? Arrays.stream(c100RebuildCourtOrderElements.getResolveSpecificIssueSubField())
                .map(element -> CourtOrderTypeEnum.valueOf(element).getDisplayedValue())
                        .toList() : Collections.emptyList();

        String natureOfOrder = Stream.concat(prohibitedOrderList.stream(), specificIssueOrderList.stream())
                .collect(Collectors.joining(COMMA_SEPARATOR));

        if (isNotEmpty(natureOfOrder) && isNotEmpty(c100RebuildCourtOrderElements.getShortStatement())) {
            return natureOfOrder + COMMA_SEPARATOR + SHORT_STATEMENT_INFO + HYPHEN_SEPARATOR
                    + c100RebuildCourtOrderElements.getShortStatement();
        } else if (isNotEmpty(c100RebuildCourtOrderElements.getShortStatement())) {
            return SHORT_STATEMENT_INFO + HYPHEN_SEPARATOR
                    + c100RebuildCourtOrderElements.getShortStatement();
        } else {
            return natureOfOrder;
        }
    }

    private static ChildArrangementOrderTypeEnum buildTypeOfChildArrangementsOrder(C100RebuildCourtOrderElements
                                                                                    c100RebuildCourtOrderElements) {
        List<String> courtOrderList = Arrays.asList(c100RebuildCourtOrderElements.getCourtOrder());
        ChildArrangementOrderTypeEnum childArrangementOrderTypeEnum = null;

        if (courtOrderList.contains(WHO_THE_CHILD_LIVE_WITH)  && courtOrderList.contains(CHILD_TIME_SPENT)) {
            childArrangementOrderTypeEnum = bothLiveWithAndSpendTimeWithOrder;
        } else if (courtOrderList.contains(CHILD_TIME_SPENT)) {
            childArrangementOrderTypeEnum = spendTimeWithOrder;
        } else if (courtOrderList.contains(WHO_THE_CHILD_LIVE_WITH)) {
            childArrangementOrderTypeEnum = liveWithOrder;
        }
        return childArrangementOrderTypeEnum;
    }

    private static List<OrderTypeEnum> buildOrdersApplyingFor(C100RebuildCourtOrderElements c100RebuildCourtOrderElements) {
        List<OrderTypeEnum> orderTypeEnums = new ArrayList<>();
        List<String> courtOrderList = Arrays.asList(c100RebuildCourtOrderElements.getCourtOrder());
        if (courtOrderList.contains(WHO_THE_CHILD_LIVE_WITH)  || courtOrderList.contains(CHILD_TIME_SPENT)) {
            orderTypeEnums.add(childArrangementsOrder);
        }
        if (courtOrderList.contains(STOP_OTHER_DOING_SOMETHING)) {
            orderTypeEnums.add(prohibitedStepsOrder);
        }
        if (courtOrderList.contains(RESOLVE_SPECIFIC_ISSUE)) {
            orderTypeEnums.add(specificIssueOrder);
        }
        return orderTypeEnums;
    }
}

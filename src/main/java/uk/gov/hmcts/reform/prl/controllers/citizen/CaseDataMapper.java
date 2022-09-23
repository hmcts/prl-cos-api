package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.CourtOrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildCourtOrderElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildHearingWithoutNoticeElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildInternationalElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Arrays;
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
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Component
public class CaseDataMapper {

    private static final String COMMA_SEPARATOR = ", ";
    private static final String HYPHEN_SEPARATOR = " - ";
    private static final String SHORT_STATEMENT_INFO = "Short Statement Information";
    private static final String WHO_THE_CHILD_LIVE_WITH = "whoChildLiveWith";
    private static final String CHILD_TIME_SPENT = "childTimeSpent";
    private static final String STOP_OTHER_DOING_SOMETHING = "stopOtherPeopleDoingSomething";
    private static final String RESOLVE_SPECIFIC_ISSUE = "resolveSpecificIssue";
    private static final String DETAILS_OF_NOTICE_OTHER_PEOPLE_WILL_DO_SOMETHING = "Details of without notice "
            + "hearing because the other person or people may do something that would obstruct the order";

    public CaseData buildUpdatedCaseData(CaseData caseData) throws JsonProcessingException {
        C100RebuildInternationalElements c100RebuildInternationalElements = new ObjectMapper()
                .readValue(caseData.getC100RebuildInternationalElements(), C100RebuildInternationalElements.class);

        C100RebuildHearingWithoutNoticeElements c100RebuildHearingWithoutNoticeElements = new ObjectMapper()
                .readValue(caseData.getC100RebuildHearingWithoutNotice(), C100RebuildHearingWithoutNoticeElements.class);

        C100RebuildCourtOrderElements c100RebuildCourtOrderElements = new ObjectMapper()
                .readValue(caseData.getC100RebuildTypeOfOrder(), C100RebuildCourtOrderElements.class);

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        updateInternationalElementsForCaseData(caseDataBuilder, c100RebuildInternationalElements);
        updateTypeOfOrderElementsForCaseData(caseDataBuilder, c100RebuildCourtOrderElements);
        updateHearingWithoutNoticeElementsForCaseData(caseDataBuilder, c100RebuildHearingWithoutNoticeElements);
        return caseDataBuilder.build();
    }

    private void updateInternationalElementsForCaseData(CaseData.CaseDataBuilder caseDataBuilder,
                                                        C100RebuildInternationalElements c100RebuildInternationalElements) {
        caseDataBuilder
                .habitualResidentInOtherState(buildHabitualResidentInOtherState(c100RebuildInternationalElements))
                .habitualResidentInOtherStateGiveReason(buildHabitualResidentInOtherStateReason(
                        c100RebuildInternationalElements))
                .jurisdictionIssue(c100RebuildInternationalElements.getDoesApplicationLinkedPeopleHaveInternationalOrder())
                .jurisdictionIssueGiveReason(c100RebuildInternationalElements
                        .getApplicationLinkedPeopleHaveInternationalOrderDetails())
                .requestToForeignAuthority(c100RebuildInternationalElements
                        .getHasAnotherCountryRequestedChildInformation())
                .requestToForeignAuthorityGiveReason(c100RebuildInternationalElements
                        .getAnotherCountryRequestedChildInformationDetails());
    }

    private void updateHearingWithoutNoticeElementsForCaseData(CaseData.CaseDataBuilder caseDataBuilder,
                              C100RebuildHearingWithoutNoticeElements c100RebuildHearingWithoutNoticeElements) {
        caseDataBuilder
                .doYouNeedAWithoutNoticeHearing(c100RebuildHearingWithoutNoticeElements.getDoYouNeedHearingWithoutNotice())
                .reasonsForApplicationWithoutNotice(buildReasonsForApplicationWithoutNotice(
                        c100RebuildHearingWithoutNoticeElements))
                .doYouRequireAHearingWithReducedNotice(c100RebuildHearingWithoutNoticeElements
                        .getDoYouNeedHearingWithoutNoticeWithoutReducedNotice())
                .setOutReasonsBelow(c100RebuildHearingWithoutNoticeElements
                        .getDoYouNeedHearingWithoutNoticeWithoutReducedNoticeDetails());
    }

    private String buildReasonsForApplicationWithoutNotice(
            C100RebuildHearingWithoutNoticeElements c100RebuildHearingWithoutNoticeElements) {
        if (c100RebuildHearingWithoutNoticeElements.getDoYouNeedHearingWithoutNoticeAsOtherPplDoSomething().equals(Yes)) {
            return c100RebuildHearingWithoutNoticeElements.getReasonsOfHearingWithoutNotice()
                    + COMMA_SEPARATOR + DETAILS_OF_NOTICE_OTHER_PEOPLE_WILL_DO_SOMETHING
                    + HYPHEN_SEPARATOR +  c100RebuildHearingWithoutNoticeElements
                    .getDoYouNeedHearingWithoutNoticeAsOtherPplDoSomethingDetails();
        } else {
            return c100RebuildHearingWithoutNoticeElements.getReasonsOfHearingWithoutNotice();
        }
    }

    private void updateTypeOfOrderElementsForCaseData(CaseData.CaseDataBuilder caseDataBuilder,
                                                      C100RebuildCourtOrderElements c100RebuildCourtOrderElements) {
        caseDataBuilder
                .ordersApplyingFor(buildOrdersApplyingFor(c100RebuildCourtOrderElements))
                .typeOfChildArrangementsOrder(buildTypeOfChildArrangementsOrder(c100RebuildCourtOrderElements))
                .natureOfOrder(buildNatureOfOrder(c100RebuildCourtOrderElements));
    }

    private String buildNatureOfOrder(C100RebuildCourtOrderElements c100RebuildCourtOrderElements) {
        List<String> prohibitedOrderList = Arrays.stream(c100RebuildCourtOrderElements.getReasonsOfHearingWithoutNotice())
                .map(element -> CourtOrderTypeEnum.valueOf(element).getDisplayedValue()).collect(Collectors.toList());
        List<String> specificIssueOrderList = Arrays.stream(c100RebuildCourtOrderElements.getResolveSpecificIssueSubField())
                .map(element -> CourtOrderTypeEnum.valueOf(element).getDisplayedValue()).collect(Collectors.toList());
        String natureOfOrder = Stream.concat(prohibitedOrderList.stream(), specificIssueOrderList.stream())
                .collect(Collectors.joining(COMMA_SEPARATOR));
        if (isNotEmpty(c100RebuildCourtOrderElements.getShortStatement())) {
            return natureOfOrder + COMMA_SEPARATOR + SHORT_STATEMENT_INFO + HYPHEN_SEPARATOR
                    + c100RebuildCourtOrderElements.getShortStatement();
        } else {
            return natureOfOrder;
        }
    }

    private ChildArrangementOrderTypeEnum buildTypeOfChildArrangementsOrder(C100RebuildCourtOrderElements
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

    private List<OrderTypeEnum> buildOrdersApplyingFor(C100RebuildCourtOrderElements c100RebuildCourtOrderElements) {
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

    private String buildHabitualResidentInOtherStateReason(C100RebuildInternationalElements c100RebuildInternationalElements) {
        return Stream.of(c100RebuildInternationalElements.getChildInternationalResidenceDetails(),
                        c100RebuildInternationalElements.getChildsParentHaveInternationalResidenceDetails())
                .filter(s -> nonNull(s) && !s.isEmpty())
                .collect(Collectors.joining(COMMA_SEPARATOR));
    }

    private YesOrNo buildHabitualResidentInOtherState(C100RebuildInternationalElements c100RebuildInternationalElements) {
        if (Yes.equals(c100RebuildInternationalElements.getDoChildHaveInternationalResidence())
                || Yes.equals(c100RebuildInternationalElements.getDoChildsParentHaveInternationalResidence())) {
            return Yes;
        } else {
            return No;
        }
    }
}

package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Slf4j
public class ManageOrdersUtils {

    private static final String[] HEARING_ORDER_IDS_NEED_SINGLE_HEARING =
        {"noticeOfProceedingsParties","noticeOfProceedingsNonParties","noticeOfProceedings"};

    public static List<String> getHearingScreenValidations(List<Element<HearingData>> ordersHearingDetails,
                                                           CallbackRequest callbackRequest,
                                                           CreateSelectOrderOptionsEnum selectedOrderType) {
        log.info("### Create select order options {}", selectedOrderType);
        List<String> errorList = new ArrayList<>();
        //For C6, C6a & FL402 - restrict to only one hearing, throw error if no hearing or more than one hearing.
        singleHearingValidations(ordersHearingDetails, errorList, callbackRequest, selectedOrderType);

        //hearingType is mandatory for all except dateConfirmedInHearingsTab
        hearingTypeAndEstimatedTimingsValidations(ordersHearingDetails, errorList);

        return errorList;
    }

    private static void singleHearingValidations(List<Element<HearingData>> ordersHearingDetails,
                                                 List<String> errorList,
                                                 CallbackRequest callbackRequest,
                                                 CreateSelectOrderOptionsEnum selectedOrderType) {
        if (Arrays.stream(HEARING_ORDER_IDS_NEED_SINGLE_HEARING).anyMatch(
            orderId -> orderId.equalsIgnoreCase(String.valueOf(selectedOrderType)))
            && !isRequestFromCommonPage(callbackRequest)) {
            if (isEmpty(ordersHearingDetails)
                || ObjectUtils.isEmpty(ordersHearingDetails.get(0)
                                           .getValue().getHearingDateConfirmOptionEnum())) {
                errorList.add("Please provide at least one hearing details");
            } else if (ordersHearingDetails.size() > 1) {
                errorList.add("Only one hearing can be created");
            }
        }
    }

    private static boolean isRequestFromCommonPage(CallbackRequest callbackRequest) {
        return ObjectUtils.isNotEmpty(callbackRequest.getCaseDetailsBefore())
            && ObjectUtils.isNotEmpty(callbackRequest.getCaseDetailsBefore().getData())
            && ObjectUtils.isEmpty(callbackRequest.getCaseDetailsBefore().getData().get("isTheOrderByConsent"));
    }

    private static void hearingTypeAndEstimatedTimingsValidations(List<Element<HearingData>> ordersHearingDetails,
                                                                  List<String> errorList) {
        if (isNotEmpty(ordersHearingDetails)) {
            ordersHearingDetails.stream()
                .map(Element::getValue)
                .forEach(hearingData -> {
                    //hearingType validation
                    if (ObjectUtils.isNotEmpty(hearingData.getHearingDateConfirmOptionEnum())
                        && HearingDateConfirmOptionEnum.dateReservedWithListAssit
                        .equals(hearingData.getHearingDateConfirmOptionEnum())
                        && (ObjectUtils.isEmpty(hearingData.getHearingTypes())
                        || ObjectUtils.isEmpty(hearingData.getHearingTypes().getValue()))) {
                        errorList.add("HearingType cannot be empty, please select a hearingType");
                    }
                    //numeric estimated timings validation
                    if ((StringUtils.isNotEmpty(hearingData.getHearingEstimatedDaysText())
                        && !StringUtils.isNumeric(hearingData.getHearingEstimatedDaysText()))
                        || (StringUtils.isNotEmpty(hearingData.getHearingEstimatedHoursText())
                        && !StringUtils.isNumeric(hearingData.getHearingEstimatedHoursText()))
                        || (StringUtils.isNotEmpty(hearingData.getHearingEstimatedMinutesText())
                        && !StringUtils.isNumeric(hearingData.getHearingEstimatedMinutesText()))) {
                        errorList.add("Please enter numeric values for estimated hearing timings");
                    }
                });
        }
    }
}

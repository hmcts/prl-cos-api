package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;

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
                                                           CreateSelectOrderOptionsEnum selectedOrderType) {
        log.info("### Create select order options {}", selectedOrderType);
        List<String> errorList = new ArrayList<>();
        //For C6, C6a & FL402 - restrict to only one hearing, throw error if no hearing or more than one hearing.
        singleHearingValidations(ordersHearingDetails, errorList, selectedOrderType);

        //hearingType is mandatory for all except dateConfirmedInHearingsTab
        hearingTypeAndEstimatedTimingsValidations(ordersHearingDetails, errorList);

        return errorList;
    }

    public static List<String> getErrorForOccupationScreen(CaseData casedata) {
        List<String> errorList = new ArrayList<>();
        FL404 fl404CustomFields = casedata.getManageOrders().getFl404CustomFields();
        if (CollectionUtils
            .isNotEmpty(fl404CustomFields.getFl404bApplicantIsEntitledToOccupy())
            && CollectionUtils
                .isNotEmpty(fl404CustomFields.getFl404bApplicantAllowedToOccupy())) {
            return errorList;
        } else {
            errorList.add("Please enter either applicant or respondent section");
        }
        return errorList;
    }

    private static void singleHearingValidations(List<Element<HearingData>> ordersHearingDetails,
                                                 List<String> errorList,
                                                 CreateSelectOrderOptionsEnum selectedOrderType) {
        if (Arrays.stream(HEARING_ORDER_IDS_NEED_SINGLE_HEARING).anyMatch(
            orderId -> orderId.equalsIgnoreCase(String.valueOf(selectedOrderType)))) {
            if (isEmpty(ordersHearingDetails)
                || ObjectUtils.isEmpty(ordersHearingDetails.get(0)
                                           .getValue().getHearingDateConfirmOptionEnum())) {
                errorList.add("Please provide at least one hearing details");
            } else if (ordersHearingDetails.size() > 1) {
                errorList.add("Only one hearing can be created");
            }
        }
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
                    validateHearingEstimatedTimings(errorList, hearingData);
                });
        }
    }

    private static void validateHearingEstimatedTimings(List<String> errorList, HearingData hearingData) {
        if (StringUtils.isNotEmpty(hearingData.getHearingEstimatedDays())
            && !StringUtils.isNumeric(hearingData.getHearingEstimatedDays())) {
            errorList.add("Please enter numeric value for Hearing estimated days");
        }
        if (StringUtils.isNotEmpty(hearingData.getHearingEstimatedHours())
            && !StringUtils.isNumeric(hearingData.getHearingEstimatedHours())) {
            errorList.add("Please enter numeric value for Hearing estimated hours");
        }
        if (StringUtils.isNotEmpty(hearingData.getHearingEstimatedMinutes())
            && !StringUtils.isNumeric(hearingData.getHearingEstimatedMinutes())) {
            errorList.add("Please enter numeric value for Hearing estimated minutes");
        }
        //Add validations for hearingMustTakePlaceAtHour & hearingMustTakePlaceAtMinute later when enabled in XUI
    }

    public static List<String> getHearingScreenValidationsForSdo(StandardDirectionOrder standardDirectionOrder) {
        List<String> errorList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(standardDirectionOrder.getSdoHearingsAndNextStepsList())
            && standardDirectionOrder.getSdoHearingsAndNextStepsList().contains(SdoHearingsAndNextStepsEnum.urgentHearing)) {
            validateHearingEstimatedTimings(errorList, standardDirectionOrder.getSdoUrgentHearingDetails());
        }
        if (CollectionUtils.isNotEmpty(standardDirectionOrder.getSdoHearingsAndNextStepsList())
            && standardDirectionOrder.getSdoHearingsAndNextStepsList().contains(SdoHearingsAndNextStepsEnum.fhdra)) {
            validateHearingEstimatedTimings(errorList, standardDirectionOrder.getSdoFhdraHearingDetails());
        }
        if (CollectionUtils.isNotEmpty(standardDirectionOrder.getSdoHearingsAndNextStepsList())
            && standardDirectionOrder.getSdoHearingsAndNextStepsList().contains(SdoHearingsAndNextStepsEnum.permissionHearing)) {
            validateHearingEstimatedTimings(errorList, standardDirectionOrder.getSdoPermissionHearingDetails());
        }
        if (CollectionUtils.isNotEmpty(standardDirectionOrder.getSdoHearingsAndNextStepsList())
            && standardDirectionOrder.getSdoHearingsAndNextStepsList().contains(SdoHearingsAndNextStepsEnum.directionForDra)) {
            validateHearingEstimatedTimings(errorList, standardDirectionOrder.getSdoDraHearingDetails());
        }
        if (CollectionUtils.isNotEmpty(standardDirectionOrder.getSdoHearingsAndNextStepsList())
            && standardDirectionOrder.getSdoHearingsAndNextStepsList().contains(SdoHearingsAndNextStepsEnum.settlementConference)) {
            validateHearingEstimatedTimings(errorList, standardDirectionOrder.getSdoSettlementHearingDetails());
        }
        if (CollectionUtils.isNotEmpty(standardDirectionOrder.getSdoHearingsAndNextStepsList())
            && standardDirectionOrder.getSdoHearingsAndNextStepsList().contains(SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping)) {
            validateHearingEstimatedTimings(errorList, standardDirectionOrder.getSdoSecondHearingDetails());
        }
        return errorList;
    }
}

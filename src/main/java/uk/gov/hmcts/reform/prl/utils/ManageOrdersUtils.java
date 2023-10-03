package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

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

    public static void addHearingScreenFieldShowParams(HearingData hearingData,
                                                       Map<String, Object> caseDataUpdated) {

        if (null != hearingData) {
            //Cafcass or Cafacass Cymru
            caseDataUpdated.put("isCafcassCymru", hearingData.getIsCafcassCymru());
            //FL401
            caseDataUpdated.put("isFL401ApplicantPresent", null != hearingData.getApplicantName() ? Yes : No);
            caseDataUpdated.put("isFL401ApplicantSolicitorPresent", null != hearingData.getApplicantSolicitor() ? Yes : No);
            caseDataUpdated.put("isFL401RespondentPresent", null != hearingData.getRespondentName() ? Yes : No);
            caseDataUpdated.put("isFL401RespondentSolicitorPresent", null != hearingData.getRespondentSolicitor() ? Yes : No);
            //C100
            caseDataUpdated.put("isApplicant1Present", null != hearingData.getApplicantName1() ? Yes : No);
            caseDataUpdated.put("isApplicant2Present", null != hearingData.getApplicantName2() ? Yes : No);
            caseDataUpdated.put("isApplicant3Present", null != hearingData.getApplicantName3() ? Yes : No);
            caseDataUpdated.put("isApplicant4Present", null != hearingData.getApplicantName4() ? Yes : No);
            caseDataUpdated.put("isApplicant5Present", null != hearingData.getApplicantName5() ? Yes : No);
            caseDataUpdated.put("isApplicant1SolicitorPresent", null != hearingData.getApplicantSolicitor1() ? Yes : No);
            caseDataUpdated.put("isApplicant2SolicitorPresent", null != hearingData.getApplicantSolicitor2() ? Yes : No);
            caseDataUpdated.put("isApplicant3SolicitorPresent", null != hearingData.getApplicantSolicitor3() ? Yes : No);
            caseDataUpdated.put("isApplicant4SolicitorPresent", null != hearingData.getApplicantSolicitor4() ? Yes : No);
            caseDataUpdated.put("isApplicant5SolicitorPresent", null != hearingData.getApplicantSolicitor5() ? Yes : No);
            caseDataUpdated.put("isRespondent1Present", null != hearingData.getRespondentName1() ? Yes : No);
            caseDataUpdated.put("isRespondent2Present", null != hearingData.getRespondentName2() ? Yes : No);
            caseDataUpdated.put("isRespondent3Present", null != hearingData.getRespondentName3() ? Yes : No);
            caseDataUpdated.put("isRespondent4Present", null != hearingData.getRespondentName4() ? Yes : No);
            caseDataUpdated.put("isRespondent5Present", null != hearingData.getRespondentName5() ? Yes : No);
            caseDataUpdated.put("isRespondent1SolicitorPresent", null != hearingData.getRespondentSolicitor1() ? Yes : No);
            caseDataUpdated.put("isRespondent2SolicitorPresent", null != hearingData.getRespondentSolicitor2() ? Yes : No);
            caseDataUpdated.put("isRespondent3SolicitorPresent", null != hearingData.getRespondentSolicitor3() ? Yes : No);
            caseDataUpdated.put("isRespondent4SolicitorPresent", null != hearingData.getRespondentSolicitor4() ? Yes : No);
            caseDataUpdated.put("isRespondent5SolicitorPresent", null != hearingData.getRespondentSolicitor5() ? Yes : No);
        }
    }
}

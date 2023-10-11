package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getApplicantSolicitorNameList;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getFL401SolicitorName;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getPartyNameList;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getRespondentSolicitorNameList;

@Slf4j
public class ManageOrdersUtils {

    private static final String[] HEARING_ORDER_IDS_NEED_SINGLE_HEARING =
        {"noticeOfProceedingsParties","noticeOfProceedingsNonParties","noticeOfProceedings"};

    public static List<String> getHearingScreenValidations(List<Element<HearingData>> ordersHearingDetails,
                                                           CreateSelectOrderOptionsEnum selectedOrderType,
                                                           boolean isSolicitorOrdersHearings) {
        log.info("### Create select order options {}", selectedOrderType);
        List<String> errorList = new ArrayList<>();
        //For C6, C6a & FL402 - restrict to only one hearing, throw error if no hearing or more than one hearing.
        singleHearingValidations(ordersHearingDetails, errorList, selectedOrderType, isSolicitorOrdersHearings);

        //hearingType is mandatory for all except dateConfirmedInHearingsTab
        hearingTypeAndEstimatedTimingsValidations(ordersHearingDetails, errorList);

        return errorList;
    }

    private static void singleHearingValidations(List<Element<HearingData>> ordersHearingDetails,
                                                 List<String> errorList,
                                                 CreateSelectOrderOptionsEnum selectedOrderType,
                                                 boolean isSolicitorOrdersHearings) {
        if (Arrays.stream(HEARING_ORDER_IDS_NEED_SINGLE_HEARING).anyMatch(
            orderId -> orderId.equalsIgnoreCase(String.valueOf(selectedOrderType)))) {
            if (isSolicitorOrdersHearings) {
                if (isEmpty(ordersHearingDetails)) {
                    errorList.add("Please provide at least one hearing details");
                } else if (ObjectUtils.isEmpty(ordersHearingDetails.get(0).getValue().getHearingTypes())
                    || ObjectUtils.isEmpty(ordersHearingDetails.get(0).getValue().getHearingTypes().getValue())) {
                    errorList.add("HearingType cannot be empty, please select a hearingType");
                }
            } else {
                if (isEmpty(ordersHearingDetails)
                    || ObjectUtils.isEmpty(ordersHearingDetails.get(0).getValue().getHearingDateConfirmOptionEnum())) {
                    errorList.add("Please provide at least one hearing details");
                }
            }
            if (isNotEmpty(ordersHearingDetails) && ordersHearingDetails.size() > 1) {
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

    public static void addHearingScreenFieldShowParams(HearingData hearingData,
                                                       Map<String, Object> caseDataUpdated,
                                                       CaseData caseData) {
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
        } else {
            List<String> applicantNames  = getPartyNameList(caseData.getApplicants());
            List<String> respondentNames = getPartyNameList(caseData.getRespondents());
            List<String> applicantSolicitorNames = getApplicantSolicitorNameList(caseData.getApplicants());
            List<String> respondentSolicitorNames = getRespondentSolicitorNameList(caseData.getRespondents());
            int numberOfApplicant = applicantNames.size();
            int numberOfRespondents = respondentNames.size();
            int numberOfApplicantSolicitors = applicantSolicitorNames.size();
            int numberOfRespondentSolicitors  = respondentSolicitorNames.size();
            //default to CAFCASS England if CaseManagementLocation is null
            boolean isCafcassCymru = null == caseData.getCaseManagementLocation()
                || YesOrNo.No.equals(CaseUtils.cafcassFlag(caseData.getCaseManagementLocation().getRegion()));
            boolean isFL401Case = FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication());
            String applicantSolicitor = getFL401SolicitorName(caseData.getApplicantsFL401());
            String respondentSolicitor = getFL401SolicitorName(caseData.getRespondentsFL401());

            //Cafcass or Cafacass Cymru
            caseDataUpdated.put("isCafcassCymru", isCafcassCymru ? YesOrNo.Yes : YesOrNo.No);

            //FL401
            caseDataUpdated.put("isFL401ApplicantPresent", isFL401Case ? Yes : No);
            caseDataUpdated.put("isFL401ApplicantSolicitorPresent", null != applicantSolicitor ? Yes : No);
            caseDataUpdated.put("isFL401RespondentPresent", isFL401Case ? Yes : No);
            caseDataUpdated.put("isFL401RespondentSolicitorPresent", null != respondentSolicitor ? Yes : No);

            //C100
            caseDataUpdated.put("isApplicant1Present", numberOfApplicant > 0 ? Yes : No);
            caseDataUpdated.put("isApplicant2Present", numberOfApplicant > 1 ? Yes : No);
            caseDataUpdated.put("isApplicant3Present", numberOfApplicant > 2 ? Yes : No);
            caseDataUpdated.put("isApplicant4Present", numberOfApplicant > 3 ? Yes : No);
            caseDataUpdated.put("isApplicant5Present", numberOfApplicant > 4 ? Yes : No);
            caseDataUpdated.put("isApplicant1SolicitorPresent", numberOfApplicantSolicitors > 0 ? Yes : No);
            caseDataUpdated.put("isApplicant2SolicitorPresent", numberOfApplicantSolicitors > 1 ? Yes : No);
            caseDataUpdated.put("isApplicant3SolicitorPresent", numberOfApplicantSolicitors > 2 ? Yes : No);
            caseDataUpdated.put("isApplicant4SolicitorPresent", numberOfApplicantSolicitors > 3 ? Yes : No);
            caseDataUpdated.put("isApplicant5SolicitorPresent", numberOfApplicantSolicitors > 4 ? Yes : No);
            caseDataUpdated.put("isRespondent1Present", numberOfRespondents > 0 ? Yes : No);
            caseDataUpdated.put("isRespondent2Present", numberOfRespondents > 1 ? Yes : No);
            caseDataUpdated.put("isRespondent3Present", numberOfRespondents > 2 ? Yes : No);
            caseDataUpdated.put("isRespondent4Present", numberOfRespondents > 3 ? Yes : No);
            caseDataUpdated.put("isRespondent5Present", numberOfRespondents > 4 ? Yes : No);
            caseDataUpdated.put("isRespondent1SolicitorPresent", numberOfRespondentSolicitors > 0 ? Yes : No);
            caseDataUpdated.put("isRespondent2SolicitorPresent", numberOfRespondentSolicitors > 1 ? Yes : No);
            caseDataUpdated.put("isRespondent3SolicitorPresent", numberOfRespondentSolicitors > 2 ? Yes : No);
            caseDataUpdated.put("isRespondent4SolicitorPresent", numberOfRespondentSolicitors > 3 ? Yes : No);
            caseDataUpdated.put("isRespondent5SolicitorPresent", numberOfRespondentSolicitors > 4 ? Yes : No);
        }
    }
}

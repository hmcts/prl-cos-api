package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.ApplicationStatus;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.PaymentStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ADD_HWF_CASE_NOTE_SHORT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_ADDTIONAL_APPLICATION_BUNDLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_WA_TASK_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS_AM_PM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HWF_APP_LIST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_THE_CASE_IN_DRAFT_STATE;
import static uk.gov.hmcts.reform.prl.enums.State.SUBMITTED_PAID;
import static uk.gov.hmcts.reform.prl.mapper.citizen.awp.CitizenAwpMapper.getAwpTaskName;
import static uk.gov.hmcts.reform.prl.services.citizen.CitizenCaseUpdateService.CASE_STATUS;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.DATE_TIME_OF_SUBMISSION_FORMAT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"java:S3776","java:S6204","java:S112","java:S4144", "java:S5665","java:S1172","java:S6541"})
public class HelpWithFeesService {

    public static final String APPLICATION_UPDATED = "# Application updated";
    public static final String HWF_APPLICATION_DYNAMIC_DATA_LABEL = "hwfApplicationDynamicData";
    public static final String CONFIRMATION_BODY = """
        \n
        You’ve updated the applicant’s help with fees record. You can now process the family application.
        \n
        If the applicant needs to make a payment. You or someone else at the court
        needs to contact the applicant or their legal representative, to arrange a payment.
        """;
    public static final String HWF_APPLICATION_DYNAMIC_DATA = """
       Application: %s \n
       Help with fees reference number: %s \n
       Applicant: %s \n
       Application submitted date: %s \n
        """;
    public static final String URGENT_HELP_WITH_FEES = "Urgent help with fees";

    private final ObjectMapper objectMapper;

    private final AddCaseNoteService addCaseNoteService;
    private final UserService userService;

    protected static final String[] HWF_TEMP_FIELDS = {HWF_APP_LIST,ADD_HWF_CASE_NOTE_SHORT,HWF_APPLICATION_DYNAMIC_DATA_LABEL};


    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted() {
        return ok(SubmittedCallbackResponse.builder()
                      .confirmationHeader(APPLICATION_UPDATED)
                      .confirmationBody(CONFIRMATION_BODY).build());
    }

    public Map<String, Object> setCaseStatus(CallbackRequest callbackRequest, String authorisation) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        if (callbackRequest.getCaseDetails().getState().equalsIgnoreCase((State.SUBMITTED_NOT_PAID.getValue()))) {
            caseDataUpdated.put(CASE_STATUS, CaseStatus.builder()
                .state(SUBMITTED_PAID.getLabel())
                .build());
            caseDataUpdated.put(IS_THE_CASE_IN_DRAFT_STATE, YesOrNo.Yes.getDisplayedValue());
        } else {
            Element<AdditionalApplicationsBundle> chosenAdditionalApplication = getChosenAdditionalApplication(caseData);
            List<Element<AdditionalApplicationsBundle>> additionalApplications
                = null != caseData.getAdditionalApplicationsBundle() ? caseData.getAdditionalApplicationsBundle()
                : new ArrayList<>();

            if (ObjectUtils.isNotEmpty(chosenAdditionalApplication) && ObjectUtils.isNotEmpty(chosenAdditionalApplication.getValue())) {
                AdditionalApplicationsBundle additionalApplicationsBundle = chosenAdditionalApplication.getValue()
                    .toBuilder()
                    .payment(chosenAdditionalApplication.getValue().getPayment().toBuilder()
                                 .status(PaymentStatus.PAID.getDisplayedValue())
                                 .build()
                    )
                    .c2DocumentBundle(ObjectUtils.isNotEmpty(chosenAdditionalApplication.getValue().getC2DocumentBundle())
                                          ? chosenAdditionalApplication.getValue().getC2DocumentBundle().toBuilder()
                        .applicationStatus(ApplicationStatus.SUBMITTED.getDisplayedValue())
                        .build() : chosenAdditionalApplication.getValue().getC2DocumentBundle())
                    .otherApplicationsBundle(ObjectUtils.isNotEmpty(chosenAdditionalApplication
                                                                        .getValue().getOtherApplicationsBundle())
                                                 ? chosenAdditionalApplication.getValue()
                        .getOtherApplicationsBundle().toBuilder()
                        .applicationStatus(ApplicationStatus.SUBMITTED.getDisplayedValue())
                        .build() : chosenAdditionalApplication.getValue().getOtherApplicationsBundle())
                    .build();
                additionalApplications.set(
                    caseData.getAdditionalApplicationsBundle().indexOf(
                        chosenAdditionalApplication),
                    element(
                        chosenAdditionalApplication.getId(),
                        additionalApplicationsBundle
                    )
                );
                caseDataUpdated.put(AWP_ADDTIONAL_APPLICATION_BUNDLE, additionalApplications);
                caseDataUpdated.put(IS_THE_CASE_IN_DRAFT_STATE, YesOrNo.No.getDisplayedValue());
                //WA fields
                caseDataUpdated.put(AWP_WA_TASK_NAME, getAwpTaskName(additionalApplicationsBundle));
            }
        }
        if (ObjectUtils.isNotEmpty(caseData.getProcessUrgentHelpWithFees())
            && null != caseData.getProcessUrgentHelpWithFees().getAddHwfCaseNoteShort()
            && StringUtils.isNotEmpty(caseData.getProcessUrgentHelpWithFees().getAddHwfCaseNoteShort().trim())) {
            CaseNoteDetails currentCaseNoteDetails = addCaseNoteService.getCurrentCaseNoteDetails(
                URGENT_HELP_WITH_FEES,
                caseData.getProcessUrgentHelpWithFees().getAddHwfCaseNoteShort().trim(),
                userService.getUserDetails(authorisation)
            );
            caseDataUpdated.put(
                CASE_NOTES,
                addCaseNoteService.getCaseNoteDetails(caseData, currentCaseNoteDetails)
            );
        }
        cleanup(caseDataUpdated);
        return caseDataUpdated;
    }

    private static void cleanup(Map<String, Object> caseDataUpdated) {
        for (String field : HWF_TEMP_FIELDS) {
            caseDataUpdated.remove(field);
        }
    }

    public Map<String, Object> handleAboutToStart(CaseDetails caseDetails) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        if (null != caseData) {
            if (State.SUBMITTED_NOT_PAID.getValue().equalsIgnoreCase(caseDetails.getState()) && YesOrNo.Yes.equals(caseData.getHelpWithFees())) {
                String dynamicElement = String.format("Child arrangements application C100 - %s",
                                                      CommonUtils.formatLocalDateTime(caseData.getCaseSubmittedTimeStamp(),
                                                                                      DATE_TIME_OF_SUBMISSION_FORMAT));
                caseDataUpdated.put(HWF_APP_LIST, DynamicList.builder().listItems(List.of(DynamicListElement.builder()
                                                                                      .code(dynamicElement)
                                                                                      .label(dynamicElement).build())).build());
            } else {
                List<Element<AdditionalApplicationsBundle>> additionalApplications
                    = null != caseData.getAdditionalApplicationsBundle() ? caseData.getAdditionalApplicationsBundle()
                    : new ArrayList<>();
                List<DynamicListElement> additionalApplicationsWithHwf = new ArrayList<>();

                additionalApplications.forEach(additionalApplication -> {
                    if (ObjectUtils.isNotEmpty(additionalApplication.getValue().getPayment())
                        && StringUtils.isNotEmpty(additionalApplication.getValue().getPayment().getHwfReferenceNumber())
                        && PaymentStatus.HWF.getDisplayedValue().equals(additionalApplication.getValue().getPayment().getStatus())) {
                        additionalApplicationsWithHwf
                            .add(DynamicListElement
                                .builder()
                                .code(additionalApplication.getId())
                                .label(getApplicationWithinProceedingsType(additionalApplication))
                                .build());
                    }
                });

                if (!additionalApplicationsWithHwf.isEmpty()) {
                    DynamicList dynamicList = DynamicList.builder()
                        .value(DynamicListElement.EMPTY)
                        .listItems(additionalApplicationsWithHwf)
                        .build();
                    caseDataUpdated.put(HWF_APP_LIST, dynamicList);
                }
            }
        }
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        return caseDataUpdated;
    }

    private String getApplicationWithinProceedingsType(Element<AdditionalApplicationsBundle> additionalApplication) {
        String applicationWithinProceedingsType = null;
        String time;
        if (ObjectUtils.isNotEmpty(additionalApplication.getValue().getC2DocumentBundle())) {
            time = additionalApplication.getValue().getC2DocumentBundle().getUploadedDateTime();
            applicationWithinProceedingsType = AdditionalApplicationTypeEnum.c2Order.getDisplayedValue() + " - " + time;
        } else if (ObjectUtils.isNotEmpty(additionalApplication.getValue().getOtherApplicationsBundle())
            && ObjectUtils.isNotEmpty(additionalApplication.getValue().getOtherApplicationsBundle().getApplicationType())) {
            time = additionalApplication.getValue().getOtherApplicationsBundle().getUploadedDateTime();
            applicationWithinProceedingsType = additionalApplication.getValue()
                .getOtherApplicationsBundle().getApplicationType().getDisplayedValue() + " - " + time;
        }

        return applicationWithinProceedingsType;
    }

    public Map<String, Object> populateHwfDynamicData(CaseDetails caseDetails) {
        log.info("inside populateHwfDynamicData");
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Map<String, Object> caseDataUpdated = caseDetails.getData();
        log.info("case state :" + caseDetails.getState());
        if (State.SUBMITTED_NOT_PAID.getValue().equalsIgnoreCase(caseDetails.getState())) {
            log.info("populate data for C100 application");
            caseDataUpdated.put(HWF_APPLICATION_DYNAMIC_DATA_LABEL,
                                String.format(
                                    HWF_APPLICATION_DYNAMIC_DATA,
                                    String.format("%s, %s", caseData.getApplicantCaseName(), caseData.getId()),
                                    caseData.getHelpWithFeesNumber(),
                                    caseData.getApplicants().get(0).getValue().getLabelForDynamicList(),
                                    CommonUtils.formatLocalDateTime(
                                        caseData.getCaseSubmittedTimeStamp(),
                                        DD_MMM_YYYY_HH_MM_SS
                                    )
                                )
            );
        } else {
            log.info("populate data for additional application");
            Element<AdditionalApplicationsBundle> chosenAdditionalApplication = getChosenAdditionalApplication(caseData);

            if (null != chosenAdditionalApplication && null != chosenAdditionalApplication.getValue()) {
                if (ObjectUtils.isNotEmpty(chosenAdditionalApplication.getValue().getC2DocumentBundle())) {
                    caseDataUpdated.put(HWF_APPLICATION_DYNAMIC_DATA_LABEL, String.format(
                        HWF_APPLICATION_DYNAMIC_DATA,
                        AdditionalApplicationTypeEnum.c2Order.getDisplayedValue(),
                        chosenAdditionalApplication.getValue().getPayment().getHwfReferenceNumber(),
                        chosenAdditionalApplication.getValue().getAuthor(),
                        getAwpUploadedDateTime(chosenAdditionalApplication.getValue().getC2DocumentBundle().getUploadedDateTime())
                    ));
                } else {
                    caseDataUpdated.put(HWF_APPLICATION_DYNAMIC_DATA_LABEL, String.format(
                        HWF_APPLICATION_DYNAMIC_DATA,
                        chosenAdditionalApplication.getValue().getOtherApplicationsBundle().getApplicationType().getDisplayedValue(),
                        chosenAdditionalApplication.getValue().getPayment().getHwfReferenceNumber(),
                        chosenAdditionalApplication.getValue().getAuthor(),
                        getAwpUploadedDateTime(chosenAdditionalApplication.getValue().getOtherApplicationsBundle().getUploadedDateTime())
                    ));
                }
            }
        }
        log.info("HWF_APPLICATION_DYNAMIC_DATA_LABEL => " + caseDataUpdated.get(HWF_APPLICATION_DYNAMIC_DATA_LABEL));
        return caseDataUpdated;
    }

    private String getAwpUploadedDateTime(String dateTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(
            dateTime, DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS_AM_PM, Locale.ENGLISH));
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS, Locale.ENGLISH);
        return localDateTime.format(dateTimeFormat);
    }

    private Element<AdditionalApplicationsBundle> getChosenAdditionalApplication(CaseData caseData) {
        AtomicReference<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new AtomicReference<>();

        if (ObjectUtils.isNotEmpty(caseData.getProcessUrgentHelpWithFees())
            && ObjectUtils.isNotEmpty(caseData.getProcessUrgentHelpWithFees().getHwfAppList())) {
            DynamicList listOfAdditionalApplications = caseData.getProcessUrgentHelpWithFees().getHwfAppList();

            if (ObjectUtils.isNotEmpty(listOfAdditionalApplications)) {
                List<Element<AdditionalApplicationsBundle>> additionalApplications
                    = null != caseData.getAdditionalApplicationsBundle() ? caseData.getAdditionalApplicationsBundle()
                    : new ArrayList<>();

                additionalApplications.forEach(additionalApplicationsBundleElement -> {
                    if (null != additionalApplicationsBundleElement.getId()
                        && additionalApplicationsBundleElement.getId().equals(listOfAdditionalApplications.getValueCodeAsUuid())) {
                        additionalApplicationsBundle.set(additionalApplicationsBundleElement);
                    }
                });
            }
        }

        return additionalApplicationsBundle.get();
    }

    public List<String> checkForManagerApproval(CaseDetails caseDetails) {
        List<String> errorList = new ArrayList<>();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (ObjectUtils.isNotEmpty(caseData.getProcessUrgentHelpWithFees())
            && YesOrNo.Yes.equals(caseData.getProcessUrgentHelpWithFees().getOutstandingBalance())
            && YesOrNo.No.equals(caseData.getProcessUrgentHelpWithFees().getManagerAgreedApplicationBeforePayment())
        ) {
            errorList.add("In order to proceed, a manager must agree to process the application");
        }
        return errorList;
    }
}

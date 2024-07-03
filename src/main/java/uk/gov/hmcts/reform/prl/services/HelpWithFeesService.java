package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TEST_UUID;
import static uk.gov.hmcts.reform.prl.enums.State.SUBMITTED_PAID;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.formateLocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"java:S3776","java:S6204","java:S112","java:S4144", "java:S5665","java:S1172","java:S6541"})
public class HelpWithFeesService {
    public static final String POST = "post";
    public static final String COURT = "Court";
    public static final String ENG = "eng";
    public static final String WEL = "wel";
    public static final String AUTHORIZATION = "authorization";

    public static final String APPLICATION_UPDATED = "# Application updated";
    public static final String HWF_APPLICATION_DYNAMIC_DATA_LABEL = "hwfApplicationDynamicData";
    public static final String CONFIRMATION_BODY = """
        \n
        You’ve updated the applicant’s help with fees record. You can now process the family application.
        \n
        If the applicant needs to make a payment. You or someone else at the court
        needs to contact the applicant or their legal representative. to arrange a payment.
        """;
    public static final String HWF_APPLICATION_DYNAMIC_DATA = """
       Application: %s \n
       Help with fees reference number: %s \n
       Applicant: %s \n
       Application submitted date: %s \n
        """;

    private final ObjectMapper objectMapper;

    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted() {
        return ok(SubmittedCallbackResponse.builder()
                      .confirmationHeader(APPLICATION_UPDATED)
                      .confirmationBody(CONFIRMATION_BODY).build());
    }

    public Map<String, Object> setCaseStatus(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("caseStatus", CaseStatus.builder()
            .state(SUBMITTED_PAID.getLabel())
            .build());
        return caseDataUpdated;
    }

    public Map<String, Object> handleAboutToStart(CaseDetails caseDetails) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        if (null != caseData) {
            if (caseDetails.getState().equalsIgnoreCase(State.SUBMITTED_NOT_PAID.getValue())) {
                String dynamicElement = String.format("Child arrangements application C100 - %s",
                    formateLocalDateTime(caseData.getCaseSubmittedTimeStamp()));
                caseDataUpdated.put(HWF_APPLICATION_DYNAMIC_DATA_LABEL, String.format(HWF_APPLICATION_DYNAMIC_DATA,
                    String.format("%s %s", caseData.getApplicantCaseName(), caseData.getId()),
                    caseData.getHelpWithFeesNumber(),
                    caseData.getApplicants().get(0).getValue().getLabelForDynamicList(),
                    caseData.getCaseSubmittedTimeStamp()));
                caseDataUpdated.put("hwfAppList", DynamicList.builder().listItems(List.of(DynamicListElement.builder()
                    .code(UUID.fromString(TEST_UUID))
                    .label(dynamicElement).build())).build());
                caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
            } else {
                List<Element<AdditionalApplicationsBundle>> additionalApplications
                    = null != caseData.getAdditionalApplicationsBundle() ? caseData.getAdditionalApplicationsBundle()
                    : new ArrayList<>();
                List<DynamicListElement> additionalApplicationsWithHwf = new ArrayList<>();

                additionalApplications.forEach(additionalApplication -> {
                    if (null != additionalApplication.getValue().getPayment()
                        && null != additionalApplication.getValue().getPayment().getHwfReferenceNumber()) {
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
                    caseDataUpdated.put("hwfAppList", dynamicList);
                }
            }
        }

        return caseDataUpdated;
    }

    private String getApplicationWithinProceedingsType(Element<AdditionalApplicationsBundle> additionalApplication) {
        String applicationWithinProceedingsType = null;

        if (null != additionalApplication.getValue().getC2DocumentBundle()) {
            String time = additionalApplication.getValue().getC2DocumentBundle().getUploadedDateTime();
            applicationWithinProceedingsType = additionalApplication.getValue().getC2DocumentBundle().getType().getDisplayedValue() + " - " + time;
        } else if (null != additionalApplication.getValue().getOtherApplicationsBundle()
            && null != additionalApplication.getValue().getOtherApplicationsBundle().getApplicationType()) {
            String time = additionalApplication.getValue().getOtherApplicationsBundle().getUploadedDateTime();
            applicationWithinProceedingsType = additionalApplication.getValue()
                .getOtherApplicationsBundle().getApplicationType().getDisplayedValue() + " - " + time;
        }

        return applicationWithinProceedingsType;
    }

    public Map<String, Object> populateHwfDynamicData(CaseDetails caseDetails) {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Map<String, Object> caseDataUpdated = new HashMap<>();

        if (null != caseData.getFm5ReminderNotificationDetails()
            && null != caseData.getFm5ReminderNotificationDetails().getProcessUrgentHelpWithFees()
            && null != caseData.getFm5ReminderNotificationDetails().getProcessUrgentHelpWithFees().getHwfAppList()) {
            DynamicList listOfAdditionalApplications = caseData.getFm5ReminderNotificationDetails().getProcessUrgentHelpWithFees().getHwfAppList();
            AdditionalApplicationsBundle chosenAdditionalApplication = getChosenAdditionalApplication(listOfAdditionalApplications, caseData);
            log.info("additionalApplication is {}", chosenAdditionalApplication);

            if (null != chosenAdditionalApplication) {
                if (null != chosenAdditionalApplication.getC2DocumentBundle()) {
                    caseDataUpdated.put(HWF_APPLICATION_DYNAMIC_DATA_LABEL, String.format(HWF_APPLICATION_DYNAMIC_DATA,
                        chosenAdditionalApplication.getC2DocumentBundle().getType().getDisplayedValue(),
                        chosenAdditionalApplication.getPayment().getHwfReferenceNumber(),
                        chosenAdditionalApplication.getAuthor(),
                        chosenAdditionalApplication.getC2DocumentBundle().getUploadedDateTime()));
                } else {
                    caseDataUpdated.put(HWF_APPLICATION_DYNAMIC_DATA_LABEL, String.format(HWF_APPLICATION_DYNAMIC_DATA,
                        chosenAdditionalApplication.getOtherApplicationsBundle().getApplicationType().getDisplayedValue(),
                        chosenAdditionalApplication.getPayment().getHwfReferenceNumber(),
                        chosenAdditionalApplication.getAuthor(),
                        chosenAdditionalApplication.getOtherApplicationsBundle().getUploadedDateTime()));
                }
                log.info("caseDataUpdated is {}", caseDataUpdated);
            }
        }

        return caseDataUpdated;
    }

    private AdditionalApplicationsBundle getChosenAdditionalApplication(DynamicList listOfAdditionalApplications, CaseData caseData) {
        AtomicReference<AdditionalApplicationsBundle> additionalApplicationsBundle = new AtomicReference<>();
        if (null != listOfAdditionalApplications) {
            List<Element<AdditionalApplicationsBundle>> additionalApplications
                = null != caseData.getAdditionalApplicationsBundle() ? caseData.getAdditionalApplicationsBundle()
                : new ArrayList<>();
            log.info("additionalApplication is {}", additionalApplications);
            additionalApplications.forEach(additionalApplicationsBundleElement -> {
                if (null != additionalApplicationsBundleElement.getId()
                    && additionalApplicationsBundleElement.getId().equals(listOfAdditionalApplications.getValueCodeAsUuid())) {
                    log.info("match found");
                    additionalApplicationsBundle.set(additionalApplicationsBundleElement.getValue());
                }
            });
        }

        return additionalApplicationsBundle.get();
    }
}

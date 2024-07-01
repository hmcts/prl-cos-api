package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TEST_UUID;
import static uk.gov.hmcts.reform.prl.enums.State.SUBMITTED_PAID;

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

    public Map<String, Object> setCaseStatus(CaseDetails caseDetails) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("caseStatus", CaseStatus.builder()
            .state(SUBMITTED_PAID.getLabel())
            .build());
        return caseDataUpdated;
    }

    public Map<String, Object> handleAboutToStart(CaseDetails caseDetails) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        if (null != caseData) {
            if (caseDetails.getState().equalsIgnoreCase(State.SUBMITTED_NOT_PAID.getLabel())) {
                log.info("Checking for c100 applications");
                String dynamicElement = String.format("Child arrangements application C100 - %s",
                    CommonUtils.formateLocalDateTime(caseData.getCaseSubmittedTimeStamp()));
                caseDataUpdated.put("hwfApplicationDynamicData", String.format(HWF_APPLICATION_DYNAMIC_DATA,
                    String.format("%s %s", caseData.getApplicantCaseName(), caseData.getId()),
                    caseData.getHelpWithFeesNumber(),
                    caseData.getApplicants().get(0).getValue().getLabelForDynamicList(),
                    caseData.getCaseSubmittedTimeStamp()));
                caseDataUpdated.put("hwfAppList", DynamicList.builder().listItems(List.of(DynamicListElement.builder()
                    .code(UUID.fromString(TEST_UUID))
                    .label(dynamicElement).build())).build());
                caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
            } else {
                log.info("No longer checking for c100 applications");
            }
        }

        return caseDataUpdated;
    }
}

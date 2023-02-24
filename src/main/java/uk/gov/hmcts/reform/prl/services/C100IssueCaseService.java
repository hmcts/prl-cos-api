package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class C100IssueCaseService {

    private final AllTabServiceImpl allTabsService;
    private final DocumentGenService documentGenService;
    private final SendgridService sendgridService;
    private final C100JsonMapper c100JsonMapper;
    private final CaseWorkerEmailService caseWorkerEmailService;
    private final LocationRefDataService locationRefDataService;

    private final ObjectMapper objectMapper;

    public Map<String, Object> issueAndSendToLocalCourt(String authorisation, CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        if (YesOrNo.No.equals(caseData.getConsentOrder())) {
            requireNonNull(caseData);
            sendgridService.sendEmail(c100JsonMapper.map(caseData));
        }
        caseData = caseData.toBuilder().issueDate(LocalDate.now()).build();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        if (null != caseData.getCourtList() && null != caseData.getCourtList().getValue()) {
            String[] idEmail = caseData.getCourtList().getValue().getCode().split(":");
            String baseLocationId = Arrays.stream(idEmail).toArray()[0].toString();
            Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId(
                baseLocationId,
                authorisation
            );
            String caseTypeOfApplication = CaseUtils.getCaseTypeOfApplication(caseData);
            CaseUtils.getCourtDetails(courtVenue, caseDataUpdated, baseLocationId);
            CaseUtils.getCourtEmail(caseDataUpdated, idEmail, caseTypeOfApplication);
            caseData.setCourtName(caseDataUpdated.get("courtName").toString());
        }
        caseData.setIssueDate();
        // Generate All Docs and set to casedataupdated.
        caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));

        // Refreshing the page in the same event. Hence no external event call needed.
        // Getting the tab fields and add it to the casedetails..
        Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);
        caseDataUpdated.put("issueDate", LocalDate.now());
        caseDataUpdated.putAll(allTabsFields);
        try {
            caseWorkerEmailService.sendEmailToCourtAdmin(callbackRequest.getCaseDetails().toBuilder().data(caseDataUpdated).build());
        } catch (Exception ex) {
            log.error("Email notification could not be sent", ex);
        }
        return caseDataUpdated;
    }


}

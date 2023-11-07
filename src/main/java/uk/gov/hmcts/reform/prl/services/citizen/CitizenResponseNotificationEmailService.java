package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.ApplicantSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenResponseNotificationEmailService {
    private final EmailService emailService;
    @Value("${xui.url}")
    private String manageCaseUrl;
    @Value("${citizen.url}")
    private String dashboardUrl;
    private final ObjectMapper objectMapper;

    public void sendC100ApplicantSolicitorNotification(CaseDetails caseDetails) {

        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (!State.CASE_WITHDRAWN.equals(caseData.getState())) {
            Map<String, String> applicantSolicitors = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toMap(
                    PartyDetails::getSolicitorEmail,
                    i -> i.getRepresentativeFirstName() + " " + i.getRepresentativeLastName()
                ));

            for (Map.Entry<String, String> appSols : applicantSolicitors.entrySet()) {
                emailService.send(
                    appSols.getKey(),
                    EmailTemplateNames.CA_APPLICANT_SOLICITOR_RES_NOTIFICATION,
                    buildApplicantSolicitorEmail(caseDetails, appSols.getValue()),
                    LanguagePreference.english
                );
            }
        }
    }

    private EmailTemplateVars buildApplicantSolicitorEmail(CaseDetails caseDetails, String solicitorName) {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        return ApplicantSolicitorEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .issueDate(caseData.getIssueDate())
            .build();
    }


}

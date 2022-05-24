package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.ApplicantSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.RespondentEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.RespondentSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;
import uk.gov.service.notify.NotificationClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceOfApplicationEmailService {

    @Autowired
    private EmailService emailService;

    @Value("${xui.url}")
    private String manageCaseUrl;

    public void sendEmailC100(CaseDetails caseDetails) throws Exception {
        log.info("Sending the server Parties emails for C100 Application for caseId {}", caseDetails.getId());

        CaseData caseData = emailService.getCaseData(caseDetails);
        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        for (PartyDetails applicant : applicants) {
            String solicitorName = applicant.getRepresentativeFirstName() + " " + applicant.getRepresentativeLastName();
            emailService.send(
                applicant.getSolicitorEmail(),
                EmailTemplateNames.APPLICANT_SOLICITOR,
                buildApplicantSolicitorEmail(caseDetails,solicitorName),
                LanguagePreference.english
            );
        }

        List<PartyDetails> respondents = caseData
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        for (PartyDetails respondent : respondents) {
            if (YesNoDontKnow.yes.equals(respondent.getDoTheyHaveLegalRepresentation())) {
                String solicitorName = respondent.getRepresentativeFirstName() + " " + respondent.getRepresentativeLastName();
                emailService.send(
                    respondent.getSolicitorEmail(),
                    EmailTemplateNames.RESPONDENT_SOLICITOR,
                    buildRespondentSolicitorEmail(caseDetails, solicitorName),
                    LanguagePreference.english
                );
            }
        }
    }

    public void sendEmailFL401(CaseDetails caseDetails) throws Exception {
        log.info("Sending the server Parties emails for C100 Application for caseId {}", caseDetails.getId());

        CaseData caseData = emailService.getCaseData(caseDetails);
        PartyDetails applicant = caseData.getApplicantsFL401();
        PartyDetails respondent = caseData.getRespondentsFL401();

        String solicitorName = applicant.getRepresentativeFirstName() + " " + applicant.getRepresentativeLastName();
        emailService.send(
            applicant.getSolicitorEmail(),
            EmailTemplateNames.APPLICANT_SOLICITOR,
            buildApplicantSolicitorEmail(caseDetails,solicitorName),
            LanguagePreference.english
        );

        if (YesNoDontKnow.yes.equals(respondent.getDoTheyHaveLegalRepresentation())) {
            String respondentSolicitorName = respondent.getRepresentativeFirstName() + " " + respondent.getRepresentativeLastName();
            emailService.send(
                respondent.getSolicitorEmail(),
                EmailTemplateNames.RESPONDENT_SOLICITOR,
                buildRespondentSolicitorEmail(caseDetails, respondentSolicitorName),
                LanguagePreference.english
            );
        }
    }

    private EmailTemplateVars buildApplicantSolicitorEmail(CaseDetails caseDetails, String solicitorName) throws Exception {

        CaseData caseData = emailService.getCaseData(caseDetails);
        Map<String,Object> privacy = new HashMap<>();
        privacy.put("file",NotificationClient.prepareUpload(ResourceLoader.loadResource("Privacy_Notice.pdf")).get("file"));
        return ApplicantSolicitorEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .privacyNoticeLink(privacy)
            .issueDate(caseData.getIssueDate())
            .build();
    }

    private EmailTemplateVars buildRespondentSolicitorEmail(CaseDetails caseDetails, String solicitorName) throws Exception {

        CaseData caseData = emailService.getCaseData(caseDetails);
        return RespondentSolicitorEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .issueDate(caseData.getIssueDate())
            .build();
    }

    private EmailTemplateVars buildRespondentEmail(CaseDetails caseDetails, String solicitorName) throws Exception {

        CaseData caseData = emailService.getCaseData(caseDetails);

        return RespondentEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .respondentName("")
            .createLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .applicantNames("")
            .accessCode("")
            .build();
    }
}

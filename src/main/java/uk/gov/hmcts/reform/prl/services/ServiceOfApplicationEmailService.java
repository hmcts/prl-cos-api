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
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.LocalAuthorityEmail;
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
        log.info("Sending the serve Parties emails for C100 Application for caseId {}", caseDetails.getId());

        CaseData caseData = emailService.getCaseData(caseDetails);
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
                EmailTemplateNames.APPLICANT_SOLICITOR_CA,
                buildApplicantSolicitorEmail(caseDetails, appSols.getValue()),
                LanguagePreference.english
            );
        }
        List<Map<String,List<String>>> respondentSolicitors = caseData
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .filter(i -> YesNoDontKnow.yes.equals(i.getDoTheyHaveLegalRepresentation()))
            .map(i -> {
                Map<String, List<String>> temp = new HashMap<>();
                temp.put(i.getSolicitorEmail(),List.of(i.getRepresentativeFirstName() + " " + i.getRepresentativeLastName(),
                                                              i.getFirstName() + " " + i.getLastName()));
                return temp;
            })
            .collect(Collectors.toList());

        for (Map<String,List<String>> resSols : respondentSolicitors) {
            String solicitorEmail = resSols.keySet().toArray()[0].toString();
            emailService.send(
                solicitorEmail,
                EmailTemplateNames.RESPONDENT_SOLICITOR,
                buildRespondentSolicitorEmail(caseDetails, resSols.get(solicitorEmail).get(0),
                                              resSols.get(solicitorEmail).get(1)),
                LanguagePreference.english
            );

        }

        //
        if (caseData.getConfirmRecipients() != null) {
            for (Element element : caseData.getConfirmRecipients().getOtherEmailAddressList()) {
                String email = element.getValue().toString();
                emailService.send(
                    email,
                    EmailTemplateNames.LOCAL_AUTHORITY,
                    buildLocalAuthorityEmail(caseDetails),
                    LanguagePreference.english
                );
            }
        }
    }

    public void sendEmailFL401(CaseDetails caseDetails) throws Exception {
        log.info("Sending the server Parties emails for FL401 Application for caseId {}", caseDetails.getId());

        CaseData caseData = emailService.getCaseData(caseDetails);
        PartyDetails applicant = caseData.getApplicantsFL401();
        PartyDetails respondent = caseData.getRespondentsFL401();

        String solicitorName = applicant.getRepresentativeFirstName() + " " + applicant.getRepresentativeLastName();
        emailService.send(
            applicant.getSolicitorEmail(),
            EmailTemplateNames.APPLICANT_SOLICITOR_DA,
            buildApplicantSolicitorEmail(caseDetails, solicitorName),
            LanguagePreference.english
        );

        if (YesNoDontKnow.yes.equals(respondent.getDoTheyHaveLegalRepresentation())) {
            String respondentSolicitorName = respondent.getRepresentativeFirstName() + " "
                + respondent.getRepresentativeLastName();
            emailService.send(
                respondent.getSolicitorEmail(),
                EmailTemplateNames.RESPONDENT_SOLICITOR,
                buildRespondentSolicitorEmail(caseDetails, respondentSolicitorName,
                                              respondent.getFirstName() + " "
                                                  + respondent.getLastName()),
                LanguagePreference.english
            );
        }
    }

    private EmailTemplateVars buildApplicantSolicitorEmail(CaseDetails caseDetails, String solicitorName)
        throws Exception {

        CaseData caseData = emailService.getCaseData(caseDetails);
        Map<String, Object> privacy = new HashMap<>();
        privacy.put("file",
                    NotificationClient.prepareUpload(ResourceLoader.loadResource("Privacy_Notice.pdf"))
                        .get("file"));
        return ApplicantSolicitorEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .privacyNoticeLink(privacy)
            .issueDate(caseData.getIssueDate())
            .build();
    }

    private EmailTemplateVars buildRespondentSolicitorEmail(CaseDetails caseDetails, String solicitorName,
                                                            String respondentName) throws Exception {

        CaseData caseData = emailService.getCaseData(caseDetails);
        Map<String, Object> privacy = new HashMap<>();
        privacy.put("file",
                    NotificationClient.prepareUpload(ResourceLoader.loadResource("Privacy_Notice.pdf"))
                        .get("file"));
        return RespondentSolicitorEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .privacyNoticeLink(privacy)
            .respondentName(respondentName)
            .issueDate(caseData.getIssueDate())
            .respondentName(respondentName)
            .build();
    }

    private EmailTemplateVars buildLocalAuthorityEmail(CaseDetails caseDetails) throws Exception {

        CaseData caseData = emailService.getCaseData(caseDetails);
        return LocalAuthorityEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .issueDate(caseData.getIssueDate())
            .build();
    }
}

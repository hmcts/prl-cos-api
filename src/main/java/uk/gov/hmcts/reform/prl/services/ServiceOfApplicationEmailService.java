package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenCaseSubmissionEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.ApplicantSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.CafcassEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.RespondentSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;
import uk.gov.service.notify.NotificationClient;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_CAN_VIEW_ONLINE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_DASHBOARD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceOfApplicationEmailService {

    private final EmailService emailService;

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${citizen.url}")
    private String citizenUrl;

    private final SendgridService sendgridService;

    public EmailNotificationDetails sendEmailNotificationToApplicantSolicitor(String authorization, CaseData caseData,
                                                                              PartyDetails partyDetails, EmailTemplateNames templateName,
                                                                              List<Document> docs,String servedParty) throws Exception {
        emailService.sendSoa(
            partyDetails.getSolicitorEmail(),
            templateName,
            buildApplicantSolicitorEmail(caseData, partyDetails.getRepresentativeFirstName()
                + " " + partyDetails.getRepresentativeLastName()),
            LanguagePreference.getPreferenceLanguage(caseData)
        );
        return sendgridService.sendEmailWithAttachments(authorization,
                                                        getEmailProps(partyDetails.getRepresentativeFullName(), caseData.getApplicantCaseName(),
                                                                      String.valueOf(caseData.getId())),
                                                        partyDetails.getSolicitorEmail(), docs, servedParty);
    }

    public EmailNotificationDetails sendEmailNotificationToFirstApplicantSolicitor(String authorization, CaseData caseData,
                                                                                   PartyDetails partyDetails, EmailTemplateNames templateName,
                                                                                   List<Document> docs,String servedParty) throws Exception {
        emailService.sendSoa(
            partyDetails.getSolicitorEmail(),
            templateName,
            buildApplicantSolicitorEmail(caseData, partyDetails.getRepresentativeFirstName()
                + " " + partyDetails.getRepresentativeLastName()),
            LanguagePreference.getPreferenceLanguage(caseData)
        );
        Map<String, String> temp = new HashMap<>();
        temp.put("specialNote", "Yes");
        temp.putAll(getEmailProps(partyDetails.getRepresentativeFullName(), caseData.getApplicantCaseName(), String.valueOf(caseData.getId())));
        return sendgridService.sendEmailWithAttachments(authorization,
                                                        temp,
                                                        partyDetails.getSolicitorEmail(), docs, servedParty
        );
    }

    private Map<String, String> getEmailProps(String name, String applicantCaseName, String caseId) {
        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", applicantCaseName);
        combinedMap.put("caseNumber", caseId);
        combinedMap.put("solicitorName", name);
        combinedMap.putAll(getCommonEmailProps());
        return combinedMap;
    }

    public EmailNotificationDetails sendEmailNotificationToRespondentSolicitor(String authorization, CaseData caseData,
                                                                               PartyDetails partyDetails, EmailTemplateNames templateName,
                                                                               List<Document> docs, String servedParty) throws Exception {
        emailService.sendSoa(
            partyDetails.getSolicitorEmail(),
            EmailTemplateNames.RESPONDENT_SOLICITOR,
            buildRespondentSolicitorEmail(caseData, partyDetails.getRepresentativeFirstName() + " "
                                              + partyDetails.getRepresentativeLastName(),
                                          partyDetails.getFirstName() + " "
                                              + partyDetails.getLastName()
            ),
            LanguagePreference.english
        );
        return sendgridService.sendEmailWithAttachments(authorization,
                                                        getEmailProps(
                                                            partyDetails.getRepresentativeFullName(),
                                                            caseData.getApplicantCaseName(),
                                                            String.valueOf(caseData.getId())
                                                        ),
                                                        partyDetails.getSolicitorEmail(),
                                                        docs,
                                                        servedParty
        );
    }

    public EmailNotificationDetails sendEmailNotificationToCafcass(CaseData caseData, String email, String servedParty) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);
        emailService.sendSoa(
            email,
            EmailTemplateNames.CAFCASS_APPLICATION_SERVED,
            buildCafcassEmail(caseData),
            LanguagePreference.english
        );
        return EmailNotificationDetails.builder()
            .emailAddress(email)
            .servedParty(servedParty)
            .docs(Collections.emptyList())
            .attachedDocs(CAFCASS_CAN_VIEW_ONLINE)
            .timeStamp(currentDate).build();
    }


    public Map<String, String> getCommonEmailProps() {
        Map<String, String> emailProps = new HashMap<>();
        emailProps.put("subject", "Case documents for : ");
        emailProps.put("content", "Case details");
        emailProps.put("attachmentType", "pdf");
        emailProps.put("disposition", "attachment");
        return emailProps;
    }

    private EmailTemplateVars buildApplicantSolicitorEmail(CaseData caseData, String solicitorName)
        throws Exception {

        Map<String, Object> privacy = new HashMap<>();
        privacy.put(
            "file",
            NotificationClient.prepareUpload(ResourceLoader.loadResource("Privacy_Notice.pdf"))
                .get("file")
        );
        return ApplicantSolicitorEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseData.getId())
            .privacyNoticeLink(privacy)
            .issueDate(caseData.getIssueDate())
            .build();
    }

    private EmailTemplateVars buildRespondentSolicitorEmail(CaseData caseData, String solicitorName,
                                                            String respondentName) throws Exception {

        Map<String, Object> privacy = new HashMap<>();
        privacy.put(
            "file",
            NotificationClient.prepareUpload(ResourceLoader.loadResource("Privacy_Notice.pdf"))
                .get("file")
        );
        return RespondentSolicitorEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseData.getId())
            .privacyNoticeLink(privacy)
            .respondentName(respondentName)
            .issueDate(caseData.getIssueDate())
            .respondentName(respondentName)
            .build();
    }

    private EmailTemplateVars buildCafcassEmail(CaseData caseData) {

        return CafcassEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseLink(manageCaseUrl + URL_STRING + caseData.getId())
            .build();
    }

    public void sendEmailToC100Applicants(CaseData caseData) {

        Map<String, String> applicantEmails = caseData.getApplicants().stream()
            .map(Element::getValue)
            .filter(applicant -> !CaseUtils.hasLegalRepresentation(applicant)
                && Yes.equals(applicant.getCanYouProvideEmailAddress()))
            .collect(Collectors.toMap(
                PartyDetails::getEmail,
                party -> party.getFirstName() + " " + party.getLastName(),
                (x, y) -> x
            ));

        if (!applicantEmails.isEmpty()) {
            applicantEmails.forEach(
                (key, value) ->
                    emailService.sendSoa(
                        key,
                        EmailTemplateNames.CA_APPLICANT_SERVICE_APPLICATION,
                        buildApplicantEmailVars(caseData, value),
                        LanguagePreference.getPreferenceLanguage(caseData)
                    ));
        }
    }

    private EmailTemplateVars buildApplicantEmailVars(CaseData caseData, String applicantName) {
        return CitizenCaseSubmissionEmail.builder()
            .caseNumber(String.valueOf(caseData.getId()))
            .applicantName(applicantName)
            .caseName(caseData.getApplicantCaseName())
            .caseLink(citizenUrl + CITIZEN_DASHBOARD)
            .build();

    }

    public EmailNotificationDetails sendEmailNotificationToApplicant(String authorization, CaseData caseData,
                                                                              PartyDetails partyDetails,
                                                                              List<Document> docs,String servedParty) throws Exception {
        return sendgridService.sendEmailWithAttachments(authorization,
                                                        getEmailProps(partyDetails.getFirstName() + " "
                                                                          + partyDetails.getLastName(),
                                                                      caseData.getApplicantCaseName(),
                                                                      String.valueOf(caseData.getId())),
                                                        partyDetails.getEmail(), docs, servedParty);
    }
}

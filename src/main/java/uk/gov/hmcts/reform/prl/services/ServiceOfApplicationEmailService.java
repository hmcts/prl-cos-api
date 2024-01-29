package uk.gov.hmcts.reform.prl.services;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.io.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.ApplicantSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.CafcassEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.RespondentSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;
import uk.gov.service.notify.NotificationClient;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_CAN_VIEW_ONLINE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
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
                                                        EmailUtils.getEmailProps(null, false, partyDetails.getRepresentativeFullName(),
                                                                                 null, caseData.getApplicantCaseName(),
                                                                      String.valueOf(caseData.getId())),
                                                        partyDetails.getSolicitorEmail(), docs, servedParty);
    }

    public EmailNotificationDetails sendEmailNotificationToSolicitor(String authorization, CaseData caseData,
                                                                                   PartyDetails partyDetails, EmailTemplateNames templateName,
                                                                                   List<Document> docs,String servedParty) throws Exception {
        EmailTemplateVars templateVars;
        LanguagePreference languagePreference = LanguagePreference.english;
        Map<String, String> temp = new HashMap<>();
        if (PrlAppsConstants.SERVED_PARTY_RESPONDENT_SOLICITOR.equalsIgnoreCase(servedParty)) {
            templateVars = buildRespondentSolicitorEmail(caseData, partyDetails.getRepresentativeFirstName() + " "
                                              + partyDetails.getRepresentativeLastName(),
                                          partyDetails.getFirstName() + " "
                                              + partyDetails.getLastName()
            );
        } else {
            templateVars = buildApplicantSolicitorEmail(caseData, partyDetails.getRepresentativeFirstName()
                + " " + partyDetails.getRepresentativeLastName());
            languagePreference = LanguagePreference.getPreferenceLanguage(caseData);
            temp.put("specialNote", "Yes");
        }
        log.info("Runtime.getRuntime().totalMemory() {}", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()));
        log.info("Runtime.getRuntime().maxMemory() {}", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().maxMemory()));
        log.info("Runtime.getRuntime().freeMemory() {}", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().freeMemory()));
        emailService.sendSoa(
            partyDetails.getSolicitorEmail(),
            templateName,
            templateVars,
            languagePreference
        );
        temp.putAll(EmailUtils.getEmailProps(null, false, partyDetails.getRepresentativeFullName(),
                                             null, caseData.getApplicantCaseName(), String.valueOf(caseData.getId())));
        return sendgridService.sendEmailWithAttachments(authorization,
                                                        temp,
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

    public EmailNotificationDetails sendEmailNotificationToApplicant(String authorization, CaseData caseData,
                                                                      PartyDetails partyDetails,
                                                                      List<Document> docs,String servedParty) throws IOException {
        return sendgridService.sendEmailWithAttachments(authorization,
                                                        EmailUtils.getEmailProps(null, false,
                                                                                 partyDetails.getFirstName() + " "
                                                                          + partyDetails.getLastName(),null,
                                                                      caseData.getApplicantCaseName(),
                                                                      String.valueOf(caseData.getId())),
                                                        partyDetails.getEmail(), docs, servedParty);
    }

    public EmailNotificationDetails sendEmailNotificationToLocalAuthority(String authorization, CaseData caseData,
                                                                          String email,
                                                                          List<Document> docs,String servedParty) throws IOException {
        return sendgridService.sendEmailWithAttachments(authorization,
                                                        EmailUtils.getEmailProps(null,false,
                                                                                 PrlAppsConstants.SERVED_PARTY_LOCAL_AUTHORITY,null,
                                                                      caseData.getApplicantCaseName(),
                                                                      String.valueOf(caseData.getId())),
                                                        email, docs, servedParty);
    }

    public EmailNotificationDetails sendEmailUsingTemplateWithAttachments(String authorization,
                                                      String email,
                                                      List<Document> docs,
                                                      SendgridEmailTemplateNames template,
                                                      Map<String, Object> dynamicData,
                                                                          String servedParty) {
        try {
            sendgridService.sendEmailUsingTemplateWithAttachments(
                template,
                authorization,
                SendgridEmailConfig.builder()
                    .toEmailAddress(email)
                    .dynamicTemplateData(dynamicData)
                    .listOfAttachments(docs).languagePreference(LanguagePreference.english).build()
            );
            return EmailNotificationDetails.builder()
                .emailAddress(email)
                .servedParty(servedParty)
                .docs(wrapElements(docs))
                .attachedDocs(String.join(",", docs.stream().map(Document::getDocumentFileName).toList()))
                .timeStamp(DateTimeFormatter
                               .ofPattern("dd MMM yyyy HH:mm:ss")
                               .format(ZonedDateTime.now(ZoneId.of("Europe/London")))).build();
        } catch (IOException e) {
            log.error("there is a failure in sending email for email {} with exception {}", email,e.getMessage());
        }
        return null;
    }
}

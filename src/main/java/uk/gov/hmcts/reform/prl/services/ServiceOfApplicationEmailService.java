package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenCaseSubmissionEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.ApplicantSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.CafcassEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.LocalAuthorityEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.RespondentSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;
import uk.gov.service.notify.NotificationClient;

import java.io.IOException;
import java.time.LocalDateTime;
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
    @Autowired
    private LaunchDarklyClient launchDarklyClient;

    @Autowired
    private EmailService emailService;

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${citizen.url}")
    private String citizenUrl;

    @Autowired
    private final ObjectMapper objectMapper;
    private final SendgridService sendgridService;
    private final C100JsonMapper c100JsonMapper;

    public void sendEmailC100(CaseDetails caseDetails) throws Exception {
        log.info("Sending the serve Parties emails for C100 Application for caseId {}", caseDetails.getId());

        CaseData caseData = emailService.getCaseData(caseDetails);
        Map<String, String> applicantSolicitors = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toMap(
                PartyDetails::getSolicitorEmail,
                i -> i.getRepresentativeFirstName() + " " + i.getRepresentativeLastName(),
                (x, y) -> x
            ));

        for (Map.Entry<String, String> appSols : applicantSolicitors.entrySet()) {

            emailService.sendSoa(
                appSols.getKey(),
                EmailTemplateNames.APPLICANT_SOLICITOR_CA,
                buildApplicantSolicitorEmail(caseData, appSols.getValue()),
                LanguagePreference.getPreferenceLanguage(caseData)
            );
        }
        if (launchDarklyClient.isFeatureEnabled("send-res-email-notification")) {
            List<Map<String, List<String>>> respondentSolicitors = caseData
                .getRespondents()
                .stream()
                .map(Element::getValue)
                .filter(i -> YesNoDontKnow.yes.equals(i.getDoTheyHaveLegalRepresentation()))
                .map(i -> {
                    Map<String, List<String>> temp = new HashMap<>();
                    temp.put(i.getSolicitorEmail(), List.of(
                        i.getRepresentativeFirstName() + " " + i.getRepresentativeLastName(),
                        i.getFirstName() + " " + i.getLastName()
                    ));
                    return temp;
                })
                .collect(Collectors.toList());

            for (Map<String, List<String>> resSols : respondentSolicitors) {
                String solicitorEmail = resSols.keySet().toArray()[0].toString();
                emailService.sendSoa(
                    solicitorEmail,
                    EmailTemplateNames.RESPONDENT_SOLICITOR,
                    buildRespondentSolicitorEmail(caseData, resSols.get(solicitorEmail).get(0),
                                                  resSols.get(solicitorEmail).get(1)
                    ),
                    LanguagePreference.english
                );

            }

            sendEmailToLocalAuthority(caseData);

        }
        sendEmailToCafcass(caseData);
    }

    public void sendEmailToLocalAuthority(CaseData caseData) throws IOException {
        List<Element<EmailNotificationDetails>> emailNotifyCollectionList;
        log.info("*** About to send ***");
        if (caseData.getConfirmRecipients() != null && caseData.getConfirmRecipients().getOtherEmailAddressList() != null) {
            for (Element<String> element : caseData.getConfirmRecipients().getOtherEmailAddressList()) {
                String email = element.getValue();
                emailService.sendSoa(
                    email,
                    EmailTemplateNames.LOCAL_AUTHORITY,
                    buildLocalAuthorityEmail(caseData),
                    LanguagePreference.english
                );

                log.info("Email notification for SoA sent successfully to LA for caseId {}", caseData.getId());
            }
        }
    }

    public void sendEmailFL401(CaseDetails caseDetails) throws Exception {
        log.info("Sending the server Parties emails for FL401 Application for caseId {}", caseDetails.getId());

        CaseData caseData = emailService.getCaseData(caseDetails);
        PartyDetails applicant = caseData.getApplicantsFL401();
        PartyDetails respondent = caseData.getRespondentsFL401();

        String solicitorName = applicant.getRepresentativeFirstName() + " " + applicant.getRepresentativeLastName();
        emailService.sendSoa(
            applicant.getSolicitorEmail(),
            EmailTemplateNames.APPLICANT_SOLICITOR_DA,
            buildApplicantSolicitorEmail(caseData, solicitorName),
            LanguagePreference.english
        );

        if (YesNoDontKnow.yes.equals(respondent.getDoTheyHaveLegalRepresentation())
            && launchDarklyClient.isFeatureEnabled("send-res-email-notification")) {
            String respondentSolicitorName = respondent.getRepresentativeFirstName() + " "
                + respondent.getRepresentativeLastName();
            emailService.sendSoa(
                respondent.getSolicitorEmail(),
                EmailTemplateNames.RESPONDENT_SOLICITOR,
                buildRespondentSolicitorEmail(caseData, respondentSolicitorName,
                                              respondent.getFirstName() + " "
                                                  + respondent.getLastName()
                ),
                LanguagePreference.english
            );
        }

        sendEmailToLocalAuthority(caseData);
    }

    private void sendEmailToCafcass(CaseData caseData) {
        if (caseData.getConfirmRecipients() != null
            && caseData.getConfirmRecipients().getCafcassEmailOptionChecked() != null
            && !caseData.getConfirmRecipients().getCafcassEmailOptionChecked()
            .isEmpty()
            && caseData.getConfirmRecipients().getCafcassEmailOptionChecked().get(0) != null) {

            caseData.getConfirmRecipients().getCafcassEmailAddressList().stream().forEach(
                emailAddressElement -> emailService.sendSoa(
                    emailAddressElement.getValue(),
                    EmailTemplateNames.CAFCASS_APPLICATION_SERVED,
                    buildCafcassEmail(
                        caseData),
                    LanguagePreference.english
                )
            );

        }
    }

    public EmailNotificationDetails sendEmailNotificationToApplicantSolicitor(String authorization, CaseData caseData,
                                                                              PartyDetails partyDetails, EmailTemplateNames templateName,
                                                                              List<Document> docs,String servedParty) throws Exception {
        log.info("*** Applicant sol email id *** " + partyDetails.getSolicitorEmail());
        log.info("****Sending email using gov notify*****");
        emailService.sendSoa(
            partyDetails.getSolicitorEmail(),
            templateName,
            buildApplicantSolicitorEmail(caseData, partyDetails.getRepresentativeFirstName()
                + " " + partyDetails.getRepresentativeLastName()),
            LanguagePreference.getPreferenceLanguage(caseData)
        );
        log.info("****Sending email using send grid*****");
        return sendgridService.sendEmailWithAttachments(String.valueOf(caseData.getId()), authorization,
                                                        getEmailProps(partyDetails, caseData.getApplicantCaseName(),
                                                                      String.valueOf(caseData.getId())),
                                                        partyDetails.getSolicitorEmail(), docs, servedParty);
    }

    public EmailNotificationDetails sendEmailNotificationToFirstApplicantSolicitor(String authorization, CaseData caseData,
                                                                                   PartyDetails partyDetails, EmailTemplateNames templateName,
                                                                                   List<Document> docs,String servedParty) throws Exception {
        log.info("*** Applicant sol email id *** " + partyDetails.getSolicitorEmail());
        log.info("****Sending email using gov notify*****");
        emailService.sendSoa(
            partyDetails.getSolicitorEmail(),
            templateName,
            buildApplicantSolicitorEmail(caseData, partyDetails.getRepresentativeFirstName()
                + " " + partyDetails.getRepresentativeLastName()),
            LanguagePreference.getPreferenceLanguage(caseData)
        );
        Map<String, String> temp = new HashMap<>();
        temp.put("specialNote", "Yes");
        temp.putAll(getEmailProps(partyDetails, caseData.getApplicantCaseName(), String.valueOf(caseData.getId())));
        log.info("****Sending email using send grid*****");
        return sendgridService.sendEmailWithAttachments(String.valueOf(caseData.getId()), authorization,
                                                        temp,
                                                        partyDetails.getSolicitorEmail(), docs, servedParty
        );
    }

    private Map<String, String> getEmailProps(PartyDetails partyDetails, String applicantCaseName, String caseId) {
        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", applicantCaseName);
        combinedMap.put("caseNumber", caseId);
        combinedMap.put("solicitorName", partyDetails.getRepresentativeFullName());
        combinedMap.putAll(getCommonEmailProps());
        return combinedMap;
    }

    public EmailNotificationDetails sendEmailNotificationToRespondentSolicitor(String authorization, CaseData caseData,
                                                                               PartyDetails partyDetails, EmailTemplateNames templateName,
                                                                               List<Document> docs, String servedParty) throws Exception {
        log.info("***Respondent sol email id *** " + partyDetails.getSolicitorEmail());
        log.info("****Sending email using gov notify*****");
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
        log.info("****Sending email using send grid*****");
        return sendgridService.sendEmailWithAttachments(String.valueOf(caseData.getId()), authorization, getCommonEmailProps(),
                                                        partyDetails.getSolicitorEmail(), docs, servedParty
        );
    }

    public EmailNotificationDetails sendEmailNotificationToCafcass(CaseData caseData, String email, String servedParty) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM YYYY HH:mm:ss");
        LocalDateTime datetime = LocalDateTime.now();
        String currentDate = datetime.format(formatter);
        emailService.sendSoa(
            email,
            EmailTemplateNames.CAFCASS_APPLICATION_SERVED,
            buildCafcassEmail(caseData),
            LanguagePreference.english
        );
        log.info("*** Do not call sendgrid for cafcass***");
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

    private EmailTemplateVars buildLocalAuthorityEmail(CaseData caseData) {

        return LocalAuthorityEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseLink(manageCaseUrl + URL_STRING + caseData.getId())
            .issueDate(caseData.getIssueDate())
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
}

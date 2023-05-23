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
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.LocalAuthorityEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.RespondentSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;
import uk.gov.service.notify.NotificationClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
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

            emailService.send(
                appSols.getKey(),
                EmailTemplateNames.APPLICANT_SOLICITOR_CA,
                buildApplicantSolicitorEmail(caseDetails, appSols.getValue()),
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
                emailService.send(
                    solicitorEmail,
                    EmailTemplateNames.RESPONDENT_SOLICITOR,
                    buildRespondentSolicitorEmail(caseDetails, resSols.get(solicitorEmail).get(0),
                                                  resSols.get(solicitorEmail).get(1)
                    ),
                    LanguagePreference.english
                );

            }

            sendEmailToLocalAuthority(caseDetails, caseData);
        }
    }

    public void sendEmailNotificationToApplicantSolicitor(String authorization, CaseDetails caseDetails, PartyDetails partyDetails,
                                                          EmailTemplateNames templateName, List<Document> docs) throws Exception {
        log.info("*** About to send email ***");
        log.info("*** email id ***" + partyDetails.getSolicitorEmail());
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<Element<EmailNotificationDetails>> emailNotifyCollectionList = new ArrayList<>();
        //CaseData caseData = emailService.getCaseData(caseDetails);
        log.info("*** document list ***" + docs);

        emailService.send(
            partyDetails.getSolicitorEmail(),
            templateName,
            buildApplicantSolicitorEmail(caseDetails, partyDetails.getRepresentativeFirstName()
                + " " + partyDetails.getRepresentativeLastName()),
            LanguagePreference.getPreferenceLanguage(caseData)
        );

        if (caseData.getEmailNotificationDetails() != null) {
            log.info("*** EmailNotificationDetails object available in case data ***" + caseData.getEmailNotificationDetails());
            caseData.getEmailNotificationDetails().forEach(emailNotifyCollectionList::add);
        } else {
            log.info("*** EmailNotificationDetails object empty in case data ***");
            emailNotifyCollectionList = new ArrayList<>();
        }
        log.info("*** About to call sendgrid ***");
        requireNonNull(caseData);
        emailNotifyCollectionList
            .add(sendgridService.sendEmailWithAttachments(authorization,
                                                          getCommonEmailProps(), partyDetails.getSolicitorEmail(), docs));
        log.info(
            "Email notification for SoA sent successfully to applicant solicitor for caseId {}",
            caseDetails.getId());
        caseData.setEmailNotificationDetails(emailNotifyCollectionList);
        log.info("*** Email Notification details set in case data ***" + caseData.getEmailNotificationDetails());

    }

    public void sendEmailNotificationToRespondentSolicitor(String authorization, CaseDetails caseDetails, PartyDetails partyDetails,
                                                           EmailTemplateNames templateName, List<Document> docs) throws Exception {
        log.info("*** About to send ***");
        log.info("*** email id ***" + partyDetails.getSolicitorEmail());
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        //CaseData caseData1 = emailService.getCaseData(caseDetails);
        List<Element<EmailNotificationDetails>> emailNotifyCollectionList;
        String respondentSolicitorName = partyDetails.getRepresentativeFirstName() + " "
            + partyDetails.getRepresentativeLastName();
        log.info("*** document list ***" + docs);

        emailService.send(
            partyDetails.getSolicitorEmail(),
            EmailTemplateNames.RESPONDENT_SOLICITOR,
            buildRespondentSolicitorEmail(caseDetails, respondentSolicitorName,
                                          partyDetails.getFirstName() + " "
                                              + partyDetails.getLastName()
            ),
            LanguagePreference.english
        );

        if (caseData.getEmailNotificationDetails() != null) {
            emailNotifyCollectionList = caseData.getEmailNotificationDetails();
        } else {
            emailNotifyCollectionList = new ArrayList<>();
        }
        log.info("*** About to call sendgrid ***");
        requireNonNull(caseData);
        emailNotifyCollectionList
            .add(sendgridService.sendEmailWithAttachments(authorization, getCommonEmailProps(),
                                                          partyDetails.getSolicitorEmail(), docs));

        log.info(
            "Email notification for SoA sent successfully to respondent solicitor for caseId {}",
            caseDetails.getId());
        caseData.setEmailNotificationDetails(emailNotifyCollectionList);
        log.info("*** Email Notification details set in case data ***" + caseData.getEmailNotificationDetails());

    }

    public void sendEmailNotificationToOtherEmails(String authorization, CaseDetails caseDetails,
                                                   CaseData caseData, List<Document> docs) throws Exception {
        List<Element<EmailNotificationDetails>> emailNotifyCollectionList;
        if (caseData.getServiceOfApplication() != null && caseData.getServiceOfApplication().getSoaOtherEmailAddressList() != null) {
            for (Element<String> element : caseData.getServiceOfApplication().getSoaOtherEmailAddressList()) {
                log.info("**SERVING OTHER EMAILS**");
                String email = element.getValue();
                emailService.send(
                    email,
                    EmailTemplateNames.LOCAL_AUTHORITY,
                    buildLocalAuthorityEmail(caseDetails),
                    LanguagePreference.english
                );

                if (caseData.getEmailNotificationDetails() != null) {
                    emailNotifyCollectionList = caseData.getEmailNotificationDetails();
                } else {
                    emailNotifyCollectionList = new ArrayList<>();
                }

                log.info("*** About to call sendgrid ***");
                requireNonNull(caseData);
                emailNotifyCollectionList
                    .add(sendgridService.sendEmailWithAttachments(authorization,
                                                                  getCommonEmailProps(), email, docs));

                log.info("Email notification for SoA sent successfully to Other Emails included for caseId {}", caseDetails.getId());
                caseData.setEmailNotificationDetails(emailNotifyCollectionList);
                log.info("*** Email Notification details set in case data ***" + caseData.getEmailNotificationDetails());
            }
        }


    }

    public void sendEmailNotificationToCafcass(String authorization, CaseDetails caseDetails,
                                                   CaseData caseData, List<Document> docs) throws Exception {
        List<Element<EmailNotificationDetails>> emailNotifyCollectionList;
        if (caseData.getServiceOfApplication() != null && caseData.getServiceOfApplication().getSoaCafcassEmailAddressList() != null) {
            for (Element<String> element : caseData.getServiceOfApplication().getSoaCafcassEmailAddressList()) {
                log.info("**SERVING EMAIL TO CAFCASS**");
                String email = element.getValue();
                emailService.send(
                    email,
                    EmailTemplateNames.CAFCASS_OTHER,
                    buildLocalAuthorityEmail(caseDetails),
                    LanguagePreference.english
                );

                if (caseData.getEmailNotificationDetails() != null) {
                    emailNotifyCollectionList = caseData.getEmailNotificationDetails();
                } else {
                    emailNotifyCollectionList = new ArrayList<>();
                }

                log.info("*** About to call sendgrid ***");
                requireNonNull(caseData);
                emailNotifyCollectionList
                    .add(sendgridService.sendEmailWithAttachments(authorization,
                                                                  getCommonEmailProps(), email, docs));

                log.info("Email notification for SoA sent successfully to Cafcass for caseId {}", caseDetails.getId());
                caseData.setEmailNotificationDetails(emailNotifyCollectionList);
                log.info("*** Email Notification details set in case data ***" + caseData.getEmailNotificationDetails());
            }
        }


    }

    public void sendEmailToLocalAuthority(CaseDetails caseDetails, CaseData caseData) throws IOException {
        List<Element<EmailNotificationDetails>> emailNotifyCollectionList;
        log.info("*** About to send ***");
        if (caseData.getConfirmRecipients() != null && caseData.getConfirmRecipients().getOtherEmailAddressList() != null) {
            for (Element<String> element : caseData.getConfirmRecipients().getOtherEmailAddressList()) {
                String email = element.getValue();
                emailService.send(
                    email,
                    EmailTemplateNames.LOCAL_AUTHORITY,
                    buildLocalAuthorityEmail(caseDetails),
                    LanguagePreference.english
                );

                log.info("Email notification for SoA sent successfully to LA for caseId {}", caseDetails.getId());
            }
        }
    }

    public Map<String, String> getCommonEmailProps() {
        Map<String, String> emailProps = new HashMap<>();
        emailProps.put("subject", "casedocuments");
        emailProps.put("content", "casedetails");
        emailProps.put("attachmentType", "pdf");
        emailProps.put("disposition", "attachment");

        return emailProps;
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

        if (YesNoDontKnow.yes.equals(respondent.getDoTheyHaveLegalRepresentation())
            && launchDarklyClient.isFeatureEnabled("send-res-email-notification")) {
            String respondentSolicitorName = respondent.getRepresentativeFirstName() + " "
                + respondent.getRepresentativeLastName();
            emailService.send(
                respondent.getSolicitorEmail(),
                EmailTemplateNames.RESPONDENT_SOLICITOR,
                buildRespondentSolicitorEmail(caseDetails, respondentSolicitorName,
                                              respondent.getFirstName() + " "
                                                  + respondent.getLastName()
                ),
                LanguagePreference.english
            );
        }

        sendEmailToLocalAuthority(caseDetails, caseData);
    }

    private EmailTemplateVars buildApplicantSolicitorEmail(CaseDetails caseDetails, String solicitorName)
        throws Exception {

        CaseData caseData = emailService.getCaseData(caseDetails);
        Map<String, Object> privacy = new HashMap<>();
        privacy.put(
            "file",
            NotificationClient.prepareUpload(ResourceLoader.loadResource("Privacy_Notice.pdf"))
                .get("file")
        );
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
        privacy.put(
            "file",
            NotificationClient.prepareUpload(ResourceLoader.loadResource("Privacy_Notice.pdf"))
                .get("file")
        );
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

    private EmailTemplateVars buildLocalAuthorityEmail(CaseDetails caseDetails) {

        CaseData caseData = emailService.getCaseData(caseDetails);
        return LocalAuthorityEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
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
                    emailService.send(
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

package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmailVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.CafcassEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_CAN_VIEW_ONLINE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;
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

    private final ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));

    public EmailNotificationDetails sendEmailNotificationToCafcass(CaseData caseData, String email, String servedParty) {
        sendGovNotifyEmail(
            LanguagePreference.english,
            email,
            EmailTemplateNames.CAFCASS_APPLICATION_SERVED,
            buildCafcassEmail(caseData)
        );
        return EmailNotificationDetails.builder()
            .emailAddress(email)
            .servedParty(servedParty)
            .docs(Collections.emptyList())
            .attachedDocs(CAFCASS_CAN_VIEW_ONLINE)
            .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime)).build();
    }

    private EmailTemplateVars buildCafcassEmail(CaseData caseData) {

        return CafcassEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseLink(manageCaseUrl + URL_STRING + caseData.getId())
            .build();
    }

    public EmailNotificationDetails sendEmailUsingTemplateWithAttachments(String authorization,
                                                      String email,
                                                      List<Document> docs,
                                                      SendgridEmailTemplateNames template,
                                                      Map<String, Object> dynamicData,
                                                                          String servedParty) {
        try {
            boolean emailSentSuccessfully = sendgridService.sendEmailUsingTemplateWithAttachments(
                template,
                authorization,
                SendgridEmailConfig.builder()
                    .toEmailAddress(email)
                    .dynamicTemplateData(dynamicData)
                    .listOfAttachments(docs).languagePreference(LanguagePreference.english).build()
            );
            if (emailSentSuccessfully) {
                return EmailNotificationDetails.builder()
                    .emailAddress(email)
                    .servedParty(servedParty)
                    .docs(wrapElements(docs))
                    .attachedDocs(String.join(",", docs.stream().map(Document::getDocumentFileName).toList()))
                    .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime))
                    .build();
            }
        } catch (IOException e) {
            log.error("there is a failure in sending email for email {} with exception {}", email,e.getMessage(), e);
        }
        log.error("there is a failure in sending email for party {}", servedParty);
        return null;
    }

    public EmailNotificationDetails sendEmailNotificationToLocalAuthority(String authorization, CaseData caseData,
                                                                          String email,
                                                                          List<Document> docs,String servedParty) throws IOException {
        Map<String, Object> combinedMap = new HashMap<>();
        combinedMap.put("caseName", caseData.getApplicantCaseName());
        combinedMap.put("caseReference", String.valueOf(caseData.getId()));
        combinedMap.put("localAuthorityName", servedParty);
        combinedMap.putAll(EmailUtils.getCommonEmailProps());

        try {
            boolean emailSentSuccessfully = sendgridService.sendEmailUsingTemplateWithAttachments(
                SendgridEmailTemplateNames.SOA_CA_LOCAL_AUTHORITY,
                authorization,
                SendgridEmailConfig.builder().toEmailAddress(
                    email).dynamicTemplateData(
                    combinedMap).listOfAttachments(
                    docs).languagePreference(LanguagePreference.english).build()
            );
            if (emailSentSuccessfully) {
                return EmailNotificationDetails.builder()
                    .emailAddress(email)
                    .servedParty(servedParty)
                    .docs(wrapElements(docs))
                    .attachedDocs(String.join(",", docs.stream().map(Document::getDocumentFileName).toList()))
                    .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime)).build();
            }
        } catch (IOException e) {
            log.error("there is a failure in sending email to Local Authority {} with exception {}",
                      email, e.getMessage(), e
            );
        }
        return null;
    }

    public EmailTemplateVars buildCitizenEmailVars(CaseData caseData,
                                                   PartyDetails party, String c1aExists) {
        return CitizenEmailVars.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseLink(citizenUrl)
            .applicantName(party.getLabelForDynamicList())
            .doesC1aExist(c1aExists)
            .build();
    }

    public void sendGovNotifyEmail(LanguagePreference languagePreference,
                                   String email,
                                   EmailTemplateNames template,
                                   EmailTemplateVars emailTemplateVars) {
        //send gov notify email
        emailService.sendSoa(email,
                             template,
                             emailTemplateVars,
                             languagePreference);
    }
}

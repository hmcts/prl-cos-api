package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.FmPendingParty;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationDetails;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationType;
import uk.gov.hmcts.reform.prl.models.dto.notification.PartyType;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmailVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LTR_ENG_C100_FM5;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ENG_STATIC_DOCS_PATH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_ENGLISH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.DASH_BOARD_LINK;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.hasDashboardAccess;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Fm5NotificationService {

    public static final String BLANK_FM5_FILE = "FM5_Blank.pdf";

    private final ServiceOfApplicationEmailService serviceOfApplicationEmailService;
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;
    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;
    private final BulkPrintService bulkPrintService;
    private final DocumentLanguageService documentLanguageService;
    private final DgsService dgsService;

    @Value("${xui.url}")
    private String manageCaseUrl;
    @Value("${citizen.url}")
    private String citizenUrl;

    public List<Element<NotificationDetails>> sendFm5ReminderNotifications(CaseData caseData,
                                                                           FmPendingParty fmPendingParty) {
        long startTime = System.currentTimeMillis();
        List<Element<NotificationDetails>> fm5ReminderNotifications = new ArrayList<>();
        if ((fmPendingParty.equals(FmPendingParty.BOTH))) {
            //send reminders to both applicants & respondents
            caseData.getApplicants()
                .forEach(party ->
                             fm5ReminderNotifications.add(sendFm5ReminderNotification(
                                 caseData,
                                 party,
                                 true
                             ))
                );
            caseData.getRespondents()
                .forEach(party ->
                             fm5ReminderNotifications.add(sendFm5ReminderNotification(
                                 caseData,
                                 party,
                                 false
                             ))
                );
        } else if (fmPendingParty.equals(FmPendingParty.APPLICANT)) {
            caseData.getApplicants()
                .forEach(party ->
                             fm5ReminderNotifications.add(sendFm5ReminderNotification(
                                 caseData,
                                 party,
                                 true
                             ))
                );
        } else if (fmPendingParty.equals(FmPendingParty.RESPONDENT)) {
            caseData.getRespondents()
                .forEach(party ->
                             fm5ReminderNotifications.add(sendFm5ReminderNotification(
                                 caseData,
                                 party,
                                 false
                             ))
                );
        }
        log.info(
            "*** Time taken to send fm5 reminders for case {} - {}s ***", caseData.getId(),
            TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
        );
        return fm5ReminderNotifications;
    }

    private Element<NotificationDetails> sendFm5ReminderNotification(CaseData caseData,
                                                                     Element<PartyDetails> party,
                                                                     boolean isApplicant) {
        String authorization = systemUserService.getSysUserToken();
        //if represented then send reminder to solicitor
        if (YesNoDontKnow.yes.equals(party.getValue().getDoTheyHaveLegalRepresentation())
            && isNotEmpty(party.getValue().getSolicitorEmail())) {
            return sendFm5ReminderToSolicitor(authorization, caseData, party, isApplicant);
        } else {
            //Not represented, remind citizen LiP
            return sendFm5ReminderToCitizen(authorization, caseData, party, isApplicant);
        }
    }

    private Element<NotificationDetails> sendFm5ReminderToSolicitor(String authorization,
                                                                    CaseData caseData,
                                                                    Element<PartyDetails> party,
                                                                    boolean isApplicantSolicitor) {
        log.info("Send FM5 reminder to solicitor for party {}", party.getId());
        Map<String, Object> dynamicData = getEmailDynamicData(
            caseData,
            party.getValue(),
            false
        );

        serviceOfApplicationEmailService
            .sendEmailUsingTemplateWithAttachments(
                authorization,
                party.getValue().getSolicitorEmail(),
                getBlankFm5Form(authorization),
                SendgridEmailTemplateNames.FM5_REMINDER_APPLICANT_RESPONDENT_SOLICITOR,
                dynamicData,
                SOLICITOR
        );

        return getNotificationDetails(party.getId(),
                                      isApplicantSolicitor ? PartyType.APPLICANT_SOLICITOR
                                          : PartyType.RESPONDENT_SOLICITOR,
                                      NotificationType.SENDGRID_EMAIL,
                                      null,
                                      null
        );
    }

    private Element<NotificationDetails> sendFm5ReminderToCitizen(String authorization,
                                                                  CaseData caseData,
                                                                  Element<PartyDetails> party,
                                                                  boolean isApplicant) {
        log.info("Contact pref is {} for party {}", party.getValue().getContactPreferences(), party.getId());
        if (YesOrNo.Yes.equals(party.getValue().getCanYouProvideEmailAddress())
            && isNotEmpty(party.getValue().getEmail())) {
            return sendFm5ReminderToLipViaEmail(authorization, caseData, party, isApplicant);
        } else {
            return sendFm5ReminderToLipViaPost(authorization, caseData, party, isApplicant);
        }
    }

    private Element<NotificationDetails> sendFm5ReminderToLipViaEmail(String authorization,
                                                                      CaseData caseData,
                                                                      Element<PartyDetails> party,
                                                                      boolean isApplicant) {
        //if party has access to dashboard then send gov notify email else send grid
        if (hasDashboardAccess(party)) {
            //Send a gov notify email
            serviceOfApplicationEmailService.sendGovNotifyEmail(
                LanguagePreference.getPreferenceLanguage(caseData),
                party.getValue().getEmail(),
                EmailTemplateNames.FM5_REMINDER_APPLICANT_RESPONDENT,
                buildCitizenEmailVars(
                    caseData,
                    party.getValue()
                )
            );

            return getNotificationDetails(party.getId(),
                                          isApplicant ? PartyType.APPLICANT : PartyType.RESPONDENT,
                                          NotificationType.GOV_NOTIFY_EMAIL,
                                          null,
                                          null
            );
        } else {
            Map<String, Object> dynamicData = getEmailDynamicData(caseData,
                                                                  party.getValue(),
                                                                  true);
            serviceOfApplicationEmailService
                .sendEmailUsingTemplateWithAttachments(
                    authorization,
                    party.getValue().getEmail(),
                    getBlankFm5Form(authorization),
                    SendgridEmailTemplateNames.FM5_REMINDER_APPLICANT_RESPONDENT,
                    dynamicData,
                    SOA_CITIZEN
            );

            return getNotificationDetails(party.getId(),
                                          isApplicant ? PartyType.APPLICANT : PartyType.RESPONDENT,
                                          NotificationType.SENDGRID_EMAIL,
                                          null,
                                          null
            );
        }
    }

    private EmailTemplateVars buildCitizenEmailVars(CaseData caseData,
                                                    PartyDetails party) {
        return CitizenEmailVars.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .partyName(party.getLabelForDynamicList())
            .caseLink(citizenUrl)
            .build();
    }

    private Element<NotificationDetails> sendFm5ReminderToLipViaPost(String authorization,
                                                                     CaseData caseData,
                                                                     Element<PartyDetails> party,
                                                                     boolean isApplicant) {

        if (isNotEmpty(party.getValue().getAddress())
            && isNotEmpty(party.getValue().getAddress().getAddressLine1())) {
            //generate cover sheets & add to documents
            List<Document> documents = new ArrayList<>(generateCoverSheets(authorization, caseData, party.getValue()));
            //generate LTR-FM5 letter & add to documents
            documents.add(generateFm5CoverLetter(authorization, caseData, party));
            //get blank fm5 form & add to documents
            documents.addAll(getBlankFm5Form(authorization));

            UUID bulkPrintId = bulkPrintService.send(
                String.valueOf(caseData.getId()),
                systemUserService.getSysUserToken(),
                "FM5Reminder",
                documents,
                party.getValue().getLabelForDynamicList()
            );
            log.info("FM5 reminder -> Sent Blank FM5 form with cover sheet to LiP {} via bulk print id {}", party.getId(), bulkPrintId);
            return getNotificationDetails(party.getId(),
                                          isApplicant ? PartyType.APPLICANT : PartyType.RESPONDENT,
                                          NotificationType.BULK_PRINT,
                                          bulkPrintId,
                                          null
            );
        } else {
            log.info(
                "Couldn't post letters to party address, as address is null/empty for {}", party.getId());
            return getNotificationDetails(party.getId(),
                                          isApplicant ? PartyType.APPLICANT : PartyType.RESPONDENT,
                                          NotificationType.BULK_PRINT,
                                          null,
                                          "Couldn't send FM5 reminder via post as address is not present"
            );
        }
    }

    private Element<NotificationDetails> getNotificationDetails(UUID partyId,
                                                                PartyType partyType,
                                                                NotificationType notificationType,
                                                                UUID bulkPrintId,
                                                                String remarks) {
        return element(NotificationDetails.builder()
                           .partyId(String.valueOf(partyId))
                           .partyType(partyType)
                           .notificationType(notificationType)
                           .bulkPrintId(String.valueOf(bulkPrintId))
                           .sentDateTime(LocalDateTime.now())
                           .remarks(remarks)
                           .build()
        );
    }

    private Document generateFm5CoverLetter(String authorisation,
                                            CaseData caseData,
                                            Element<PartyDetails> party) {

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", caseData.getId());
        dataMap.put("serviceUrl", citizenUrl);
        dataMap.put("address", party.getValue().getAddress());
        dataMap.put(NAME, party.getValue().getLabelForDynamicList());
        dataMap.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dataMap.put("dashboardAccess", hasDashboardAccess(party) ? YesOrNo.Yes : YesOrNo.No);

        log.info("*** Generating FM5 reminder cover letter ***");
        try {
            GeneratedDocumentInfo fm5CoverLetter = dgsService.generateDocument(
                authorisation,
                String.valueOf(caseData.getId()),
                PRL_LTR_ENG_C100_FM5,
                dataMap
            );
            return Document.builder()
                .documentUrl(fm5CoverLetter.getUrl())
                .documentFileName(fm5CoverLetter.getDocName())
                .documentBinaryUrl(fm5CoverLetter.getBinaryUrl())
                .documentCreatedOn(new Date())
                .build();
        } catch (Exception e) {
            log.error("generate FM5 cover letter failed for {} ",caseData.getId(), e);
        }
        return null;
    }

    private List<Document> getBlankFm5Form(String authorisation) {
        UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
            authorisation,
            authTokenGenerator.generate(),
            PrlAppsConstants.CASE_TYPE,
            PrlAppsConstants.JURISDICTION,
            List.of(
                new InMemoryMultipartFile(
                    SOA_MULTIPART_FILE,
                    BLANK_FM5_FILE,
                    APPLICATION_PDF_VALUE,
                    DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + BLANK_FM5_FILE)
                )
            )
        );
        if (null != uploadResponse) {
            return uploadResponse.getDocuments().stream()
                .map(DocumentUtils::toPrlDocument)
                .toList();
        }
        return Collections.emptyList();
    }

    private List<Document> generateCoverSheets(String authorisation,
                                     CaseData caseData,
                                     PartyDetails party) {
        List<Document> coverSheets = null;
        try {
            coverSheets = serviceOfApplicationPostService.getCoverSheets(
                caseData,
                authorisation,
                party.getAddress(),
                party.getLabelForDynamicList(),
                DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT
            );
        } catch (Exception e) {
            log.error("Error occurred in generating cover sheets", e);
        }
        log.info("Cover sheets generated {}", coverSheets);
        return coverSheets;
    }

    private Map<String, Object> getEmailDynamicData(CaseData caseData,
                                                    PartyDetails party,
                                                    boolean isCitizen) {
        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        dynamicData.put(NAME, isCitizen ? party.getLabelForDynamicList() : party.getRepresentativeFullName());
        dynamicData.put(DASH_BOARD_LINK, isCitizen ? citizenUrl
            : manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        dynamicData.put(IS_ENGLISH, documentLanguage.isGenEng());
        dynamicData.put(IS_WELSH, documentLanguage.isGenWelsh());

        return dynamicData;
    }

}

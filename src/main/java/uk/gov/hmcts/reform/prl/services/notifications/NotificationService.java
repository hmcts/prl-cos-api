package uk.gov.hmcts.reform.prl.services.notifications;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmailVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.BulkPrintService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationPostService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_C100_AP13;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_C100_AP14;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_C100_AP15;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BULK_SCAN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURTNAV;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_ENGLISH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_PROFESSIONAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_CAFCASS_CYMRU;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.C1A_NOTIFICATION_APPLICANT;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.C1A_RESPONSE_NOTIFICATION_APPLICANT;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.C7_NOTIFICATION_APPLICANT;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.RESPONDENT_ALLEGATIONS_OF_HARM_CAFCASS;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.RESPONDENT_RESPONDED_ALLEGATIONS_OF_HARM_CAFCASS;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.RESPONDENT_RESPONDED_CAFCASS;
import static uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames.C1A_NOTIFICATION_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames.C1A_RESPONSE_NOTIFICATION_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.DASH_BOARD_LINK;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.hasDashboardAccess;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {

    private static final String LETTER_TYPE = "responsePack";
    public static final String RESPONDENT = "The respondent";
    public static final String RESPONDENT_WELSH = "Mae’r atebydd";
    public static final String RESPONDENT_NAME = "respondentName";
    public static final String RESPONDENT_NAME_PRESENT = "respNamePresent";
    public static final String RESPONDENT_NAME_PREFIX = "Mae";
    public static final String ID = "id";
    public static final String APPLICANT_ADDRESS = "applicantAddress";
    public static final String APPLICANT_NAME = "applicantName";
    public static final String DATE = "date";
    public static final String DAT_FORMAT = "dd MMM yyyy";

    private final SystemUserService systemUserService;
    private final ServiceOfApplicationService serviceOfApplicationService;
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;
    private final DocumentLanguageService documentLanguageService;
    private final EmailService emailService;
    private final SendgridService sendgridService;
    private final BulkPrintService bulkPrintService;

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${citizen.url}")
    private String citizenDashboardUrl;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Async
    public void sendNotificationsAsync(CaseData caseData,
                                       QuarantineLegalDoc quarantineLegalDoc,
                                       String userRole) {
        scheduler.schedule(() -> sendNotifications(caseData,
                                                   quarantineLegalDoc,
                                                   userRole), 500, TimeUnit.MILLISECONDS);
    }

    public void sendNotifications(CaseData caseData,
                                  QuarantineLegalDoc quarantineLegalDoc,
                                  String userRole) {
        log.info("*** Send notifications, uploader role {}", userRole);
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            String respondentName = getNameOfRespondent(quarantineLegalDoc, userRole);
            String cafcassCymruEmail = getCafcassCymruEmail(caseData);
            Document responseDocument = getQuarantineDocumentForUploader(quarantineLegalDoc.getUploaderRole(), quarantineLegalDoc);

            if (RESPONDENT_APPLICATION.equalsIgnoreCase(quarantineLegalDoc.getCategoryId())) {
                log.info("*** Sending respondent C7 response documents to applicants ***");
                //C7 response
                sendNotificationToApplicantsLipOrSolicitor(
                    caseData,
                    C7_NOTIFICATION_APPLICANT,
                    SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT,
                    PRL_LET_ENG_C100_AP13,
                    C7_NOTIFICATION_APPLICANT_SOLICITOR,
                    respondentName,
                    responseDocument
                );

                sendNotificationToCafCass(caseData,
                                          RESPONDENT_RESPONDED_CAFCASS,
                                          respondentName,
                                          cafcassCymruEmail);

            } else if (RESPONDENT_C1A_APPLICATION.equalsIgnoreCase(quarantineLegalDoc.getCategoryId())) {
                log.info("*** Sending respondent C1A documents to applicants/solicitor ***");
                //C1A
                sendNotificationToApplicantsLipOrSolicitor(
                    caseData,
                    C1A_NOTIFICATION_APPLICANT,
                    SendgridEmailTemplateNames.C1A_NOTIFICATION_APPLICANT,
                    PRL_LET_ENG_C100_AP14,
                    C1A_NOTIFICATION_APPLICANT_SOLICITOR,
                    respondentName,
                    responseDocument
                );

                sendNotificationToCafCass(caseData,
                                          RESPONDENT_ALLEGATIONS_OF_HARM_CAFCASS,
                                          respondentName,
                                          cafcassCymruEmail);

            } else if (RESPONDENT_C1A_RESPONSE.equalsIgnoreCase(quarantineLegalDoc.getCategoryId())) {
                log.info("*** Sending respondent response to C1A documents to applicants/solicitor ***");
                //C1A response
                sendNotificationToApplicantsLipOrSolicitor(
                    caseData,
                    C1A_RESPONSE_NOTIFICATION_APPLICANT,
                    SendgridEmailTemplateNames.C1A_RESPONSE_NOTIFICATION_APPLICANT,
                    PRL_LET_ENG_C100_AP15,
                    C1A_RESPONSE_NOTIFICATION_APPLICANT_SOLICITOR,
                    respondentName,
                    responseDocument
                );

                sendNotificationToCafCass(caseData,
                                          RESPONDENT_RESPONDED_ALLEGATIONS_OF_HARM_CAFCASS,
                                          respondentName,
                                          cafcassCymruEmail);
            }
        }
    }

    private void sendNotificationToCafCass(CaseData caseData,
                                           EmailTemplateNames emailTemplate,
                                           String respondentName,
                                           String cafcassCymruEmail) {
        if (null != cafcassCymruEmail) {
            String dashboardUrl = manageCaseUrl + "/" + caseData.getId() + "#Case%20documents";
            emailService.send(
                cafcassCymruEmail,
                emailTemplate,
                buildEmailData(caseData, null, respondentName, dashboardUrl),
                LanguagePreference.getPreferenceLanguage(caseData)
            );
            log.info(
                "Response documents are sent to Cafcass cymru via gov notify email for the case {}",
                caseData.getId()
            );
        }
    }

    private EmailTemplateVars buildEmailData(CaseData caseData,
                                             String applicantName,
                                             String respondentName,
                                             String link) {
        return CitizenEmailVars.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantName)
            .respondentName(null != respondentName ? respondentName : RESPONDENT)
            .respondentNameWel(null != respondentName ? getRespondentNameWelsh(respondentName) : RESPONDENT_WELSH)
            .caseLink(link)
            .build();
    }

    private String getRespondentNameWelsh(String respondentName) {
        return String.format("%s %s", RESPONDENT_NAME_PREFIX, respondentName);
    }

    private void sendNotificationToApplicantsLipOrSolicitor(CaseData caseData,
                                              EmailTemplateNames partyGovNotifyTemplate,
                                              SendgridEmailTemplateNames partySendgridTemplate,
                                              String coverLetterTemplateHint,
                                              SendgridEmailTemplateNames solicitorSendgridTemplate,
                                              String respondentName,
                                              Document responseDocument) {
        caseData.getApplicants().forEach(partyDataEle -> {
            PartyDetails partyData = partyDataEle.getValue();
            Map<String, Object> dynamicData = getEmailDynamicData(caseData, partyData, respondentName);
            if (CommonUtils.isNotEmpty(partyData.getSolicitorEmail())) {
                sendEmailViaSendGrid(
                    systemUserService.getSysUserToken(),
                    responseDocument,
                    dynamicData,
                    partyData.getSolicitorEmail(),
                    solicitorSendgridTemplate
                );
                log.info(
                    "Response documents are sent to solicitor via email for applicant {}, in the case {}",
                    partyDataEle.getId(),
                    caseData.getId()
                );
            } else {
                if (CommonUtils.isNotEmpty(partyData.getEmail())
                    && ContactPreferences.email.equals(partyData.getContactPreferences())) {
                    if (hasDashboardAccess(element(partyData))) {
                        sendEmailToParty(
                            caseData,
                            partyData,
                            respondentName,
                            partyGovNotifyTemplate
                        );
                        log.info(
                            "Response documents are sent to applicant {} via gov notify email, in the case {}",
                            partyDataEle.getId(),
                            caseData.getId()
                        );
                    } else {
                        sendEmailViaSendGrid(
                            systemUserService.getSysUserToken(),
                            responseDocument,
                            dynamicData,
                            partyData.getEmail(),
                            partySendgridTemplate
                        );
                        log.info(
                            "Response documents are sent to applicant {} via email, in the case {}",
                            partyDataEle.getId(),
                            caseData.getId()
                        );
                    }
                } else {
                    //Bulk print
                    generateAndSendPostNotification(
                        caseData,
                        partyDataEle,
                        respondentName,
                        responseDocument,
                        coverLetterTemplateHint
                    );
                }
            }
        });
    }

    private Map<String, Object> getEmailDynamicData(CaseData caseData,
                                                    PartyDetails applicant,
                                                    String respondentName) {
        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        dynamicData.put(APPLICANT_NAME, applicant.getLabelForDynamicList());
        dynamicData.put("solicitorName", applicant.getRepresentativeFullName());
        dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        dynamicData.put(IS_ENGLISH, documentLanguage.isGenEng());
        dynamicData.put(IS_WELSH, documentLanguage.isGenWelsh());
        dynamicData.put(RESPONDENT_NAME,  null != respondentName ? respondentName : RESPONDENT);
        dynamicData.put(RESPONDENT_NAME_PRESENT, null != respondentName);
        return dynamicData;
    }

    private void sendEmailViaSendGrid(String authorisation,
                                      Document responseDocument,
                                      Map<String, Object> dynamicDataForEmail,
                                      String emailAddress,
                                      SendgridEmailTemplateNames sendgridEmailTemplateName) {
        try {
            sendgridService.sendEmailUsingTemplateWithAttachments(
                sendgridEmailTemplateName,
                authorisation,
                SendgridEmailConfig.builder()
                    .toEmailAddress(emailAddress)
                    .dynamicTemplateData(dynamicDataForEmail)
                    .listOfAttachments(List.of(responseDocument))
                    .languagePreference(LanguagePreference.english)
                    .build()
            );
        } catch (IOException e) {
            log.error("There is a failure in sending email to {} with exception {}", emailAddress, e.getMessage());
            throw (new RuntimeException(e));
        }
    }

    private void sendEmailToParty(CaseData caseData,
                                  PartyDetails partyData,
                                  String respondentName,
                                  EmailTemplateNames emailTemplate) {
        EmailTemplateVars emailData = buildEmailData(caseData,
                                                     partyData.getLabelForDynamicList(),
                                                     respondentName,
                                                     citizenDashboardUrl);
        emailService.send(
            partyData.getEmail(),
            emailTemplate,
            emailData,
            LanguagePreference.getPreferenceLanguage(caseData)
        );
    }

    private void generateAndSendPostNotification(CaseData caseData,
                                                 Element<PartyDetails> applicant,
                                                 String respondentName,
                                                 Document responseDocument,
                                                 String coverLetterTemplateHint) {
        if (ObjectUtils.isNotEmpty(applicant.getValue())
            && ObjectUtils.isNotEmpty(applicant.getValue().getAddress())
            && ObjectUtils.isNotEmpty(applicant.getValue().getAddress().getAddressLine1())) {
            try {
                //generate cover sheet
                String authorisation = systemUserService.getSysUserToken();
                List<Document> responseDocuments = serviceOfApplicationPostService.getCoverSheets(
                    caseData,
                    authorisation,
                    applicant.getValue().getAddress(),
                    applicant.getValue().getLabelForDynamicList(),
                    DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT
                );

                //cover letters
                Map<String, Object> dataMap = fetchApplicantResponseDataMap(caseData,
                                                                            applicant.getValue().getAddress(),
                                                                            applicant.getValue().getLabelForDynamicList(),
                                                                            respondentName);
                DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
                responseDocuments.addAll(serviceOfApplicationService.fetchCoverLetter(authorisation, coverLetterTemplateHint,
                                                 dataMap, documentLanguage.isGenEng(), documentLanguage.isGenWelsh()));
                //response document
                responseDocuments.add(responseDocument);

                // Add coversheet and send it to bulk print
                UUID bulkPrintId = bulkPrintService.send(
                    String.valueOf(caseData.getId()),
                    authorisation,
                    LETTER_TYPE,
                    responseDocuments,
                    applicant.getValue().getLabelForDynamicList()
                );
                log.info(
                    "Response documents are sent to applicant {} in the case {} - via post {}",
                    applicant.getId(),
                    caseData.getId(),
                    bulkPrintId
                );
            } catch (Exception e) {
                log.error("Failed to send response documents to applicant {} in the case {}", applicant.getId(), caseData.getId());
                throw (new RuntimeException(e));
            }
        } else {
            log.warn("Couldn't post response documents - address is null/empty for applicant {} in the case {}", applicant.getId(), caseData.getId());
        }
    }

    private Map<String, Object> fetchApplicantResponseDataMap(CaseData caseData,
                                                              Address address,
                                                              String applicantName,
                                                              String respondentName) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(APPLICANT_NAME, null != applicantName ? applicantName : " ");
        dataMap.put(APPLICANT_ADDRESS, address);
        dataMap.put(ID, String.valueOf(caseData.getId()));
        dataMap.put(RESPONDENT_NAME,  null != respondentName ? respondentName : RESPONDENT);
        dataMap.put(RESPONDENT_NAME_PRESENT, null != respondentName);
        dataMap.put(DATE, LocalDate.now().format(DateTimeFormatter.ofPattern(DAT_FORMAT)));
        return dataMap;
    }

    private  String getNameOfRespondent(QuarantineLegalDoc quarantineLegalDoc,
                                        String userRole) {
        if (SOLICITOR.equalsIgnoreCase(userRole)) {
            return quarantineLegalDoc.getSolicitorRepresentedPartyName();
        } else if (CITIZEN.equalsIgnoreCase(userRole)) {
            return quarantineLegalDoc.getUploadedBy();
        }
        return null;
    }

    private String getCafcassCymruEmail(CaseData caseData) {
        //get Cafcass cymru email from SOA packs
        String cafcassCymruEmail = null;
        if (CollectionUtils.isNotEmpty(caseData.getFinalServedApplicationDetailsList())) {
            for (Element<ServedApplicationDetails> soaPack : caseData.getFinalServedApplicationDetailsList()) {
                if (CollectionUtils.isNotEmpty(soaPack.getValue().getEmailNotificationDetails())) {
                    cafcassCymruEmail = checkAndFetchCafcassCymruEmail(cafcassCymruEmail, soaPack);
                }
                if (null != cafcassCymruEmail) {
                    break;
                }
            }
        }
        return cafcassCymruEmail;
    }

    private String checkAndFetchCafcassCymruEmail(String cafcassCymruEmail, Element<ServedApplicationDetails> soaPack) {
        for (Element<EmailNotificationDetails> soaEmail : soaPack.getValue().getEmailNotificationDetails()) {
            if (SERVED_PARTY_CAFCASS_CYMRU.equalsIgnoreCase(soaEmail.getValue().getServedParty())) {
                cafcassCymruEmail = soaEmail.getValue().getEmailAddress();
                break;
            }
        }
        return cafcassCymruEmail;
    }

    private Document getQuarantineDocumentForUploader(String uploaderRole,
                                                      QuarantineLegalDoc quarantineLegalDoc) {
        return switch (uploaderRole) {
            case SOLICITOR, LEGAL_PROFESSIONAL -> quarantineLegalDoc.getDocument();
            case CAFCASS -> quarantineLegalDoc.getCafcassQuarantineDocument();
            case COURT_STAFF -> quarantineLegalDoc.getCourtStaffQuarantineDocument();
            case BULK_SCAN -> quarantineLegalDoc.getUrl();
            case CITIZEN -> quarantineLegalDoc.getCitizenQuarantineDocument();
            case COURTNAV -> quarantineLegalDoc.getCourtNavQuarantineDocument();
            default -> null;
        };
    }
}

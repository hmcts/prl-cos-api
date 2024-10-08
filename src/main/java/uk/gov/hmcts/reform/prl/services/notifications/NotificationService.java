package uk.gov.hmcts.reform.prl.services.notifications;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.reform.prl.models.dto.notification.DocumentsNotification;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationDetails;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationType;
import uk.gov.hmcts.reform.prl.models.dto.notification.PartyType;
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
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.config.templates.Templates.AP13_HINT;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.AP14_HINT;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.AP15_HINT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.C1A_NOTIFICATION_APPLICANT;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.C1A_RESPONSE_NOTIFICATION_APPLICANT;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.C7_NOTIFICATION_APPLICANT;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.RESPONDENT_ALLEGATIONS_OF_HARM_CAFCASS;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.RESPONDENT_RESPONDED_ALLEGATIONS_OF_HARM_CAFCASS;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.RESPONDENT_RESPONDED_CAFCASS;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.DASH_BOARD_LINK;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.hasDashboardAccess;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {

    public static final String SOLICITOR = "solicitor";
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
    public static final String PARTY = "party";

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


    public void sendNotifications(CaseData caseData,
                                  QuarantineLegalDoc quarantineLegalDoc,
                                  String userRole, Map<String, Object> caseDataMap) {
        log.info("*** Send notifications, uploader role {}", userRole);
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            String respondentName = getNameOfRespondent(quarantineLegalDoc, userRole);
            String cafcassCymruEmail = getCafcassCymruEmail(caseData);
            Document responseDocument = getQuarantineDocumentForUploader(quarantineLegalDoc.getUploaderRole(), quarantineLegalDoc);
            Map<String, SendgridEmailTemplateNames> sendGridTemplateMap = new HashMap<>();
            if (RESPONDENT_APPLICATION.equalsIgnoreCase(quarantineLegalDoc.getCategoryId())) {
                log.info("*** Sending respondent C7 response documents to applicants ***");
                sendGridTemplateMap.put(PARTY, SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT);
                sendGridTemplateMap.put(SOLICITOR, SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT_SOLICITOR);
                //C7 response
                sendNotificationToApplicantsLipOrSolicitor(
                    caseData,
                    C7_NOTIFICATION_APPLICANT,
                    sendGridTemplateMap,
                    AP13_HINT,
                    respondentName,
                    responseDocument,
                    caseDataMap
                );

                sendNotificationToCafCass(caseData,
                                          RESPONDENT_RESPONDED_CAFCASS,
                                          respondentName,
                                          cafcassCymruEmail);

            } else if (RESPONDENT_C1A_APPLICATION.equalsIgnoreCase(quarantineLegalDoc.getCategoryId())) {
                log.info("*** Sending respondent C1A documents to applicants/solicitor ***");
                sendGridTemplateMap.put(PARTY, SendgridEmailTemplateNames.C1A_NOTIFICATION_APPLICANT);
                sendGridTemplateMap.put(SOLICITOR, SendgridEmailTemplateNames.C1A_NOTIFICATION_APPLICANT_SOLICITOR);
                //C1A
                sendNotificationToApplicantsLipOrSolicitor(
                    caseData,
                    C1A_NOTIFICATION_APPLICANT,
                    sendGridTemplateMap,
                    AP14_HINT,
                    respondentName,
                    responseDocument,
                    caseDataMap
                );

                sendNotificationToCafCass(caseData,
                                          RESPONDENT_ALLEGATIONS_OF_HARM_CAFCASS,
                                          respondentName,
                                          cafcassCymruEmail);

            } else if (RESPONDENT_C1A_RESPONSE.equalsIgnoreCase(quarantineLegalDoc.getCategoryId())) {
                log.info("*** Sending respondent response to C1A documents to applicants/solicitor ***");
                sendGridTemplateMap.put(PARTY, SendgridEmailTemplateNames.C1A_RESPONSE_NOTIFICATION_APPLICANT);
                sendGridTemplateMap.put(SOLICITOR, SendgridEmailTemplateNames.C1A_RESPONSE_NOTIFICATION_APPLICANT_SOLICITOR);
                //C1A response
                sendNotificationToApplicantsLipOrSolicitor(
                    caseData,
                    C1A_RESPONSE_NOTIFICATION_APPLICANT,
                    sendGridTemplateMap,
                    AP15_HINT,
                    respondentName,
                    responseDocument,
                    caseDataMap
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
                                                            Map<String, SendgridEmailTemplateNames> sendGridTemplateMap,
                                                            String coverLetterTemplateHint,
                                                            String respondentName,
                                                            Document responseDocument, Map<String, Object> caseDataMap) {
        caseData.getApplicants().forEach(partyDataEle -> {
            PartyDetails partyData = partyDataEle.getValue();
            Map<String, Object> dynamicData = getEmailDynamicData(caseData, partyData, respondentName);
            if (CommonUtils.isNotEmpty(partyData.getSolicitorEmail())) {
                sendEmailViaSendGrid(
                    systemUserService.getSysUserToken(),
                    responseDocument,
                    dynamicData,
                    partyData.getSolicitorEmail(),
                    sendGridTemplateMap.get(SOLICITOR)
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
                            sendGridTemplateMap.get(PARTY)

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
                        coverLetterTemplateHint,
                        caseDataMap
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
        dynamicData.put(PrlAppsConstants.IS_ENGLISH, documentLanguage.isGenEng());
        dynamicData.put(PrlAppsConstants.IS_WELSH, documentLanguage.isGenWelsh());
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
            log.error("There is a failure in sending email to {} with exception {}", emailAddress, e.getMessage(), e);
            throw new RuntimeException(e);
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
                                                 String coverLetterTemplateHint, Map<String, Object> caseDataMap) {
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
                    PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT
                );

                //cover letters
                Map<String, Object> dataMap = fetchApplicantResponseDataMap(caseData,
                                                                            applicant.getValue().getAddress(),
                                                                            applicant.getValue().getLabelForDynamicList(),
                                                                            respondentName);
                List<Document> coverLetters = serviceOfApplicationService
                    .getCoverLetters(
                        authorisation,
                        caseData,
                        coverLetterTemplateHint,
                        dataMap
                    );
                responseDocuments.addAll(coverLetters);
                //response document
                responseDocuments.add(responseDocument);
                log.info("response docs {}", responseDocuments);
                // Add coversheet and send it to bulk print
                UUID bulkPrintId = bulkPrintService.send(
                    String.valueOf(caseData.getId()),
                    authorisation,
                    LETTER_TYPE,
                    responseDocuments,
                    applicant.getValue().getLabelForDynamicList()
                );
                List<Element<DocumentsNotification>> documentsNotifications = CaseUtils.getExistingAccessCodeNotifications(caseData);
                documentsNotifications.add(element(DocumentsNotification.builder()
                                                       .notification(NotificationDetails.builder()
                                                                         .bulkPrintId(String.valueOf(bulkPrintId))
                                                                         .notificationType(NotificationType.BULK_PRINT)
                                                                         .partyId(String.valueOf(applicant.getId()))
                                                                         .partyType(PartyType.APPLICANT)
                                                                         .sentDateTime(LocalDateTime.now(ZoneId.of(
                                                                             PrlAppsConstants.LONDON_TIME_ZONE)))
                                                                         .build())
                                                       .documents(ElementUtils.wrapElements(coverLetters))
                                                       .build()));
                caseDataMap.put("accessCodeNotifications", documentsNotifications);
                log.info(
                    "Response documents are sent to applicant {} in the case {} - via post {}",
                    applicant.getId(),
                    caseData.getId(),
                    bulkPrintId
                );
            } catch (Exception e) {
                log.error("Failed to send response documents to applicant {} in the case {}", applicant.getId(), caseData.getId(), e);
                throw new RuntimeException(e);
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
        } else if (PrlAppsConstants.CITIZEN.equalsIgnoreCase(userRole)) {
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
                    for (Element<EmailNotificationDetails> soaEmail : soaPack.getValue().getEmailNotificationDetails()) {
                        if (PrlAppsConstants.SERVED_PARTY_CAFCASS_CYMRU.equalsIgnoreCase(soaEmail.getValue().getServedParty())) {
                            cafcassCymruEmail = soaEmail.getValue().getEmailAddress();
                            break;
                        }
                    }
                }
                if (null != cafcassCymruEmail) {
                    break;
                }
            }
        }
        return cafcassCymruEmail;
    }

    private Document getQuarantineDocumentForUploader(String uploaderRole,
                                                      QuarantineLegalDoc quarantineLegalDoc) {
        return switch (uploaderRole) {
            case SOLICITOR -> quarantineLegalDoc.getDocument();
            case PrlAppsConstants.CAFCASS -> quarantineLegalDoc.getCafcassQuarantineDocument();
            case PrlAppsConstants.COURT_STAFF -> quarantineLegalDoc.getCourtStaffQuarantineDocument();
            case PrlAppsConstants.BULK_SCAN -> quarantineLegalDoc.getUrl();
            case PrlAppsConstants.CITIZEN -> quarantineLegalDoc.getCitizenQuarantineDocument();
            case PrlAppsConstants.COURTNAV -> quarantineLegalDoc.getCourtNavQuarantineDocument();
            default -> null;
        };
    }
}
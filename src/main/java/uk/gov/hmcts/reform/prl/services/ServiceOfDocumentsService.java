package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoNotApplicable;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.ServiceOfDocumentsCheckEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.SodCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.SodSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.DocumentsDynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofdocuments.SodPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmailVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_CAN_VIEW_ONLINE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_ENGLISH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MISSING_ADDRESS_WARNING_TEXT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_OTHER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PARTIES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PEOPLE_PRESENT_IN_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_RECIPIENT_OPTIONS;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.AUTHORIZATION;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.COURT;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.DASH_BOARD_LINK;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.DISPLAY_LEGAL_REP_OPTION;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ServiceOfDocumentsService {

    private final ObjectMapper objectMapper;
    private final SendAndReplyService sendAndReplyService;
    private final ServiceOfApplicationService serviceOfApplicationService;
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;
    private final DynamicMultiSelectListService dynamicMultiSelectListService;
    private final UserService userService;
    private final AllTabServiceImpl allTabService;
    private final DocumentLanguageService documentLanguageService;
    private final EmailService emailService;
    private final SendgridService sendgridService;

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${citizen.url}")
    private String citizenDashboardUrl;

    public Map<String, Object> handleAboutToStart(String authorisation,
                                                  CallbackRequest callbackRequest) {

        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        caseDataMap.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        caseDataMap.put(DISPLAY_LEGAL_REP_OPTION, CaseUtils.isCitizenCase(caseData) ? "No" : "Yes");
        caseDataMap.put(
            MISSING_ADDRESS_WARNING_TEXT,
            serviceOfApplicationService.checkIfPostalAddressMissedForRespondentAndOtherParties(caseData)
        );
        caseDataMap.put("sodDocumentsList", List.of(element(DocumentsDynamicList.builder()
                                                                .documentsList(sendAndReplyService.getCategoriesAndDocuments(
                                                                    authorisation,
                                                                    String.valueOf(caseData.getId())
                                                                )).build())));
        List<DynamicMultiselectListElement> otherPeopleList = dynamicMultiSelectListService.getOtherPeopleMultiSelectList(
            caseData);
        caseDataMap.put(SOA_OTHER_PARTIES, DynamicMultiSelectList.builder().listItems(otherPeopleList).build());
        caseDataMap.put(
            SOA_OTHER_PEOPLE_PRESENT_IN_CASE,
            CollectionUtils.isNotEmpty(otherPeopleList) ? YesOrNo.Yes : YesOrNo.No
        );
        caseDataMap.put(SOA_RECIPIENT_OPTIONS, serviceOfApplicationService.getCombinedRecipients(caseData));
        //ADD DATA CHECK TO HANDLE REPLACING EXISTING SOD DOCUMENTS PENDING MANAGER CHECK

        return caseDataMap;
    }

    public Map<String, Object> handleAboutToSubmit(String authorisation,
                                                   CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        SodPack unServedPack = SodPack.builder()
            .documents(wrapElements(getDocumentsTobeServed(authorisation, caseData)))
            .submittedBy(userService.getUserDetails(authorisation).getFullName())
            .submittedDateTime(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE)).toLocalDateTime())
            .build();
        if (!YesNoNotApplicable.NotApplicable.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
            //Personal service
            if (YesNoNotApplicable.Yes.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
                String servedBy = CaseUtils.isCitizenCase(caseData)
                    ? caseData.getServiceOfDocuments().getSodCitizenServingRespondentsOptions().getDisplayedValue()
                    : caseData.getServiceOfDocuments().getSodSolicitorServingRespondentsOptions().getDisplayedValue();
                unServedPack = unServedPack.toBuilder()
                    .servedBy(servedBy)
                    .isPersonalService(YesOrNo.Yes)
                    .build();
            } else if (YesNoNotApplicable.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
                //Non-personal service
                unServedPack = unServedPack.toBuilder()
                    .applicantIds(getSelectedPartyIds(
                        caseData,
                        caseData.getApplicants(),
                        caseData.getApplicantsFL401()
                    ))
                    .respondentIds(getSelectedPartyIds(
                        caseData,
                        caseData.getRespondents(),
                        caseData.getRespondentsFL401()
                    ))
                    .servedBy(COURT)
                    .isPersonalService(YesOrNo.No)
                    .build();
            }
        }
        //other persons
        if (null != caseData.getServiceOfApplication().getSoaOtherParties()) {
            unServedPack = unServedPack.toBuilder()
                .otherPersonIds(wrapElements(caseData.getServiceOfApplication().getSoaOtherParties().getValue()
                                                 .stream()
                                                 .map(DynamicMultiselectListElement::getCode)
                                                 .toList()))
                .build();
        }
        //additional recipients
        if (CollectionUtils.isNotEmpty(caseData.getServiceOfDocuments().getSodAdditionalRecipients())) {
            unServedPack = unServedPack.toBuilder()
                .additionalRecipients(caseData.getServiceOfDocuments().getSodAdditionalRecipientsList())
                .build();
        }
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        //Add un-served documents to caseData
        caseDataMap.put("sodUnServedPack", unServedPack);

        return caseDataMap;
    }

    private List<Document> getDocumentsTobeServed(String authorisation,
                                                  CaseData caseData) {
        List<Document> documents = new ArrayList<>();
        //get user uploaded documents
        List<Document> uploadedDocuments = getUploadedDocuments(caseData);
        if (CollectionUtils.isNotEmpty(uploadedDocuments)) {
            documents.addAll(uploadedDocuments);
        }
        //get selected documents
        List<Document> cfvDocuments = getSelectedDocuments(authorisation, caseData);
        if (CollectionUtils.isNotEmpty(cfvDocuments)) {
            documents.addAll(cfvDocuments);
        }
        return documents;
    }

    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted(String authorisation,
                                                                     CallbackRequest callbackRequest) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartAllTabsUpdate(String.valueOf(
            callbackRequest.getCaseDetails().getId()));
        Map<String, Object> caseDataMap = startAllTabsUpdateDataContent.caseDataMap();
        CaseData caseData = startAllTabsUpdateDataContent.caseData();
        SodPack unServedPack = caseData.getServiceOfDocuments().getSodUnServedPack();

        //Send notifications if no check needed
        if (null != unServedPack
            && ServiceOfDocumentsCheckEnum.noCheck.equals(caseData.getServiceOfDocuments().getSodDocumentsCheckOptions())) {
            List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
            List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
            if (!YesNoNotApplicable.NotApplicable.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
                if (YesOrNo.Yes.equals(unServedPack.getIsPersonalService())) {
                    //personal service
                    handlePersonalServiceOfDocuments(
                        authorisation,
                        caseData,
                        unServedPack,
                        emailNotificationDetails,
                        bulkPrintDetails
                    );
                } else {
                    //non-personal service
                    handleNonPersonalServiceOfDocuments(
                        authorisation,
                        caseData,
                        unServedPack,
                        emailNotificationDetails,
                        bulkPrintDetails
                    );
                }
            }
            //serve other persons
            serveDocumentsToOtherPerson(authorisation, caseData, unServedPack, bulkPrintDetails);

            //serve additional recipients

            //Reset unserved packs
            caseDataMap.put("sodUnServedPack", null);
            //Update served documents
            caseDataMap.put("servedDocumentsDetailsList",
                            getUpdatedServedDocumentsDetailsList(caseData, unServedPack, emailNotificationDetails, bulkPrintDetails));
        }

        //Clean up the fields
        cleanUpSelections(caseDataMap);

        allTabService.submitAllTabsUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            String.valueOf(caseData.getId()),
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataMap
        );
        return ok(SubmittedCallbackResponse.builder().build());
    }

    private Object getUpdatedServedDocumentsDetailsList(CaseData caseData,
                                                        SodPack unServedPack,
                                                        List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                        List<Element<BulkPrintDetails>> bulkPrintDetails) {
        List<Element<ServedApplicationDetails>> servedDocumentsDetailsList = new ArrayList<>();

        servedDocumentsDetailsList.add(element(ServedApplicationDetails.builder()
                                                   .emailNotificationDetails(emailNotificationDetails)
                                                   .bulkPrintDetails(bulkPrintDetails)
                                                   .modeOfService(CaseUtils.getModeOfService(emailNotificationDetails, bulkPrintDetails))
                                                   .whoIsResponsible(unServedPack.getServedBy())
                                                   .servedBy(unServedPack.getSubmittedBy())
                                                   .servedAt(CaseUtils.getCurrentDate())
                                                   .build()));

        if (CollectionUtils.isNotEmpty(caseData.getServiceOfDocuments().getServedDocumentsDetailsList())) {
            servedDocumentsDetailsList.addAll(caseData.getServiceOfDocuments().getServedDocumentsDetailsList());
        }

        return servedDocumentsDetailsList;
    }

    private void handlePersonalServiceOfDocuments(String authorisation,
                                                  CaseData caseData,
                                                  SodPack unServedPack,
                                                  List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                  List<Element<BulkPrintDetails>> bulkPrintDetails) {
        if (SodCitizenServingRespondentsEnum.unrepresentedApplicant
            .getDisplayedValue().equals(unServedPack.getServedBy())) {
            //unrepresented applicant lip
            handlePersonalServiceUnRepApplicant(authorisation, caseData, unServedPack.getDocuments(), emailNotificationDetails, bulkPrintDetails);
        } else if (SodSolicitorServingRespondentsEnum.applicantLegalRepresentative
            .getDisplayedValue().equals(unServedPack.getServedBy())) {
            //applicant solicitor
        }
    }

    private void handlePersonalServiceUnRepApplicant(String authorisation,
                                                     CaseData caseData,
                                                     List<Element<Document>> documents,
                                                     List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                     List<Element<BulkPrintDetails>> bulkPrintDetails) {
        //C100
        if (C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            caseData.getApplicants()
                .forEach(applicant -> {
                    if (ContactPreferences.email.equals(applicant.getValue().getContactPreferences())) {
                        Map<String, String> params = new HashMap<>();
                        params.put(AUTHORIZATION, authorisation);
                        params.put("servedParty", SERVED_PARTY_APPLICANT);
                        sendEmailToParty(caseData, applicant, documents,
                                         EmailTemplateNames.SOD_PERSONAL_SERVICE_APPLICANT_LIP,
                                         SendgridEmailTemplateNames.SOD_PERSONAL_SERVICE_APPLICANT_LIP,
                                         emailNotificationDetails,
                                         params
                        );
                    } else {
                        sendPostToParty(
                            authorisation,
                            caseData,
                            applicant,
                            SERVED_PARTY_APPLICANT,
                            documents,
                            bulkPrintDetails
                        );
                    }
                });
        } else {
            if (ContactPreferences.email.equals(caseData.getApplicantsFL401().getContactPreferences())) {
                Map<String, String> params = new HashMap<>();
                params.put(AUTHORIZATION, authorisation);
                params.put("servedParty", SERVED_PARTY_APPLICANT);
                sendEmailToParty(caseData,
                                 element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401()),
                                 documents,
                                 EmailTemplateNames.SOD_PERSONAL_SERVICE_APPLICANT_LIP,
                                 SendgridEmailTemplateNames.SOD_PERSONAL_SERVICE_APPLICANT_LIP,
                                 emailNotificationDetails,
                                 params
                );
            } else {
                sendPostToParty(
                    authorisation,
                    caseData,
                    element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401()),
                    SERVED_PARTY_APPLICANT,
                    documents,
                    bulkPrintDetails
                );
            }
        }
    }

    private void sendEmailToParty(CaseData caseData,
                                  Element<PartyDetails> party,
                                  List<Element<Document>> documents,
                                  EmailTemplateNames govNotifyTemplate,
                                  SendgridEmailTemplateNames sendgridTemplate,
                                  List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                  Map<String, String> params) {
        String authorisation = params.get(AUTHORIZATION);
        String servedParty = params.get("servedParty");
        EmailNotificationDetails emailNotification;
        if (CaseUtils.isCitizenAccessEnabled(party.getValue())) {
            log.debug("Party has access to dashboard -> send gov notify email for {}", party.getId());
            emailNotification = sendGovNotifyEmail(caseData, documents, party, servedParty, govNotifyTemplate);
        } else {
            log.debug("Party does not access to dashboard -> send documents via sendgrid email for {}", party.getId());
            emailNotification = sendSendgridEmail(authorisation, caseData, documents, party, servedParty, sendgridTemplate);
        }

        if (emailNotification != null) {
            emailNotificationDetails.add(element(emailNotification.toBuilder()
                                                     .partyIds(String.valueOf(party.getId()))
                                                     .build()));
        }
    }

    private void sendPostToParty(String authorisation,
                                 CaseData caseData,
                                 Element<PartyDetails> party,
                                 String servedParty,
                                 List<Element<Document>> documents,
                                 List<Element<BulkPrintDetails>> bulkPrintDetails) {
        try {
            if ((isNotEmpty(party.getValue())
                && isNotEmpty(party.getValue().getAddress()))
                && isNotEmpty(party.getValue().getAddress().getAddressLine1())) {
                List<Document> docs = new ArrayList<>(serviceOfApplicationPostService
                                                          .getCoverSheets(caseData, authorisation,
                                                                          party.getValue().getAddress(),
                                                                          party.getValue().getLabelForDynamicList(),
                                                                          DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT
                                                          ));

                docs.addAll(unwrapElements(documents));
                bulkPrintDetails.add(element(serviceOfApplicationPostService.sendPostNotificationToParty(
                    caseData,
                    authorisation,
                    party,
                    docs,
                    servedParty
                )));
            } else {
                log.error(
                    "Couldn't post the documents to party address, as address is null/empty for {}",
                    party.getId()
                );
            }
        } catch (Exception e) {
            log.error("error while generating coversheet {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private EmailNotificationDetails sendGovNotifyEmail(CaseData caseData,
                                                        List<Element<Document>> documents,
                                                        Element<PartyDetails> party,
                                                        String servedParty,
                                                        EmailTemplateNames govNotifyTemplate) {
        sendEmail(caseData, party.getValue(), SERVED_PARTY_APPLICANT.equals(servedParty), govNotifyTemplate);

        //Create email notification with packs
        return EmailNotificationDetails.builder()
            .emailAddress(party.getValue().getEmail())
            .servedParty(servedParty)
            .docs(documents)
            .attachedDocs(CITIZEN_CAN_VIEW_ONLINE)
            .timeStamp(CaseUtils.getCurrentDate())
            .build();
    }

    private void sendEmail(CaseData caseData, PartyDetails party, boolean isApplicant, EmailTemplateNames govNotifyTemplate) {
        EmailTemplateVars emailData = buildEmailData(caseData, party, isApplicant);
        emailService.send(
            party.getEmail(),
            govNotifyTemplate,
            emailData,
            LanguagePreference.getPreferenceLanguage(caseData)
        );
    }

    private EmailNotificationDetails sendEmail(String authorisation,
                                               List<Element<Document>> documents,
                                               Map<String, Object> dynamicDataForEmail,
                                               String emailAddress,
                                               String servedParty,
                                               SendgridEmailTemplateNames sendgridEmailTemplateName) {
        try {
            boolean emailSent = sendgridService.sendEmailUsingTemplateWithAttachments(
                sendgridEmailTemplateName,
                authorisation,
                SendgridEmailConfig.builder()
                    .toEmailAddress(emailAddress)
                    .dynamicTemplateData(dynamicDataForEmail)
                    .listOfAttachments(unwrapElements(documents))
                    .languagePreference(LanguagePreference.english)
                    .build()
            );

            if (emailSent) {
                return EmailNotificationDetails.builder()
                    .emailAddress(emailAddress)
                    .servedParty(servedParty)
                    .docs(documents)
                    .attachedDocs(String.join(
                        ",",
                        documents.stream().map(Element::getValue).map(Document::getDocumentFileName).toList()
                    ))
                    .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                                   .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
                    .build();
            }
        } catch (IOException e) {
            log.error("There is a failure in sending email to {} with exception {}", emailAddress, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return null;
    }

    private EmailTemplateVars buildEmailData(CaseData caseData,
                                             PartyDetails party,
                                             boolean isApplicant) {
        return CitizenEmailVars.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .partyName(party.getLabelForDynamicList())
            .applicantName(isApplicant ? party.getLabelForDynamicList() : null)
            .respondentName(!isApplicant ? party.getLabelForDynamicList() : null)
            .caseLink(citizenDashboardUrl)
            .build();
    }

    private Map<String, Object> getEmailDynamicData(CaseData caseData,
                                                    PartyDetails party) {
        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        dynamicData.put("partyName", party.getLabelForDynamicList());
        dynamicData.put("solicitorName", party.getRepresentativeFullName());
        dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        dynamicData.put(IS_ENGLISH, documentLanguage.isGenEng());
        dynamicData.put(IS_WELSH, documentLanguage.isGenWelsh());
        return dynamicData;
    }

    private EmailNotificationDetails sendSendgridEmail(String authorisation,
                                                       CaseData caseData,
                                                       List<Element<Document>> documents,
                                                       Element<PartyDetails> party,
                                                       String servedParty,
                                                       SendgridEmailTemplateNames sendgridTemplate) {
        Map<String, Object> dynamicData = getEmailDynamicData(caseData, party.getValue());

        return sendEmail(authorisation, documents, dynamicData, party.getValue().getEmail(), servedParty, sendgridTemplate);
    }

    private void handleNonPersonalServiceOfDocuments(String authorisation,
                                                     CaseData caseData,
                                                     SodPack unServedPack,
                                                     List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                     List<Element<BulkPrintDetails>> bulkPrintDetails) {
        //applicants
        if (CollectionUtils.isNotEmpty(unServedPack.getApplicantIds())) {
            handleNonPersonalServiceToParty(
                authorisation,
                caseData,
                unServedPack.getApplicantIds(),
                true,
                unServedPack.getDocuments(),
                emailNotificationDetails,
                bulkPrintDetails
            );
        }
        //respondents
        if (CollectionUtils.isNotEmpty(unServedPack.getRespondentIds())) {
            handleNonPersonalServiceToParty(
                authorisation,
                caseData,
                unServedPack.getRespondentIds(),
                false,
                unServedPack.getDocuments(),
                emailNotificationDetails,
                bulkPrintDetails
            );
        }
    }

    private void handleNonPersonalServiceToParty(String authorisation,
                                                 CaseData caseData,
                                                 List<Element<String>> partyIds,
                                                 boolean isApplicant,
                                                 List<Element<Document>> documents,
                                                 List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                 List<Element<BulkPrintDetails>> bulkPrintDetails) {
        partyIds.stream()
            .map(Element::getValue)
            .forEach(partyId -> {
                Element<PartyDetails> party = getPartyDetailsById(caseData, partyId, isApplicant);
                if (null != party) {
                    if (ContactPreferences.email.equals(party.getValue().getContactPreferences())) {
                        Map<String, String> params = new HashMap<>();
                        params.put(AUTHORIZATION, authorisation);
                        params.put("servedParty", isApplicant ? SERVED_PARTY_APPLICANT : SERVED_PARTY_RESPONDENT);
                        sendEmailToParty(caseData, party, documents,
                                         EmailTemplateNames.SOD_NON_PERSONAL_SERVICE_APPLICANT_RESPONDENT_LIP,
                                         SendgridEmailTemplateNames.SOD_NON_PERSONAL_SERVICE_APPLICANT_RESPONDENT_LIP,
                                         emailNotificationDetails,
                                         params
                        );
                    } else {
                        sendPostToParty(
                            authorisation,
                            caseData,
                            party,
                            isApplicant ? SERVED_PARTY_APPLICANT : SERVED_PARTY_RESPONDENT,
                            documents,
                            bulkPrintDetails
                        );
                    }
                }
            });
    }

    private Element<PartyDetails> getPartyDetailsById(CaseData caseData,
                                                      String partyId,
                                                      boolean isApplicant) {
        if (isApplicant) {
            if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())
                && caseData.getApplicantsFL401().getPartyId().toString().equals(partyId)) {
                return element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401());
            } else {
                return caseData.getApplicants().stream()
                    .filter(applicant -> applicant.getId().toString().equals(partyId))
                    .findFirst()
                    .orElse(null);
            }
        } else {
            if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())
                && caseData.getRespondentsFL401().getPartyId().toString().equals(partyId)) {
                return element(caseData.getRespondentsFL401().getPartyId(), caseData.getRespondentsFL401());
            } else {
                return caseData.getRespondents().stream()
                    .filter(respondent -> respondent.getId().toString().equals(partyId))
                    .findFirst()
                    .orElse(null);
            }
        }
    }

    private void serveDocumentsToOtherPerson(String authorisation,
                                             CaseData caseData,
                                             SodPack unServedPack,
                                             List<Element<BulkPrintDetails>> bulkPrintDetails) {
        //send post notifications to other person if selected
        if (CollectionUtils.isNotEmpty(unServedPack.getOtherPersonIds())) {
            unServedPack.getOtherPersonIds().stream()
                .map(Element::getValue)
                .forEach(id -> {
                    PartyDetails otherPerson = CaseUtils.getOtherPerson(id, caseData);
                    if (null != otherPerson) {
                        sendPostToParty(
                            authorisation,
                            caseData,
                            element(UUID.fromString(id), otherPerson),
                            SERVED_PARTY_OTHER,
                            unServedPack.getDocuments(),
                            bulkPrintDetails
                        );
                    }
                });
        }
    }

    private List<Document> getSelectedDocuments(String authorisation,
                                                CaseData caseData) {
        if (null != caseData.getServiceOfDocuments()
            && CollectionUtils.isNotEmpty(caseData.getServiceOfDocuments().getSodDocumentsList())) {
            return caseData.getServiceOfDocuments().getSodDocumentsList().stream()
                .map(Element::getValue)
                .map(docsdynamicList -> sendAndReplyService.getSelectedDocument(
                    authorisation, docsdynamicList.getDocumentsList()))
                .toList();
        }
        return Collections.emptyList();
    }

    private List<Document> getUploadedDocuments(CaseData caseData) {
        return nullSafeCollection(caseData.getServiceOfDocuments()
                                      .getSodAdditionalDocumentsList()).stream()
            .map(Element::getValue)
            .toList();
    }

    private List<Element<String>> getSelectedPartyIds(CaseData caseData,
                                                      List<Element<PartyDetails>> parties,
                                                      PartyDetails fl401Party) {
        return wrapElements(CaseUtils.getSelectedPartyIds(
            caseData.getCaseTypeOfApplication(),
            parties,
            fl401Party,
            caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
        ));
    }

    public void cleanUpSelections(Map<String, Object> caseDataMap) {
        List<String> sodFields = new ArrayList<>(List.of(
            "sodDocumentsList",
            "sodAdditionalDocumentsList",
            "sodAdditionalRecipients",
            "sodAdditionalRecipientsList",
            "sodDocumentsCheckOptions",
            "soaServeToRespondentOptions",
            "sodSolicitorServingRespondentsOptions",
            "sodCitizenServingRespondentsOptions",
            "soaRecipientsOptions",
            "soaOtherPeoplePresentInCaseFlag",
            "soaOtherParties",
            "missingAddressWarningText",
            "displayLegalRepOption"
        ));

        for (String field : sodFields) {
            if (caseDataMap.containsKey(field)) {
                caseDataMap.put(field, null);
            }
        }
    }
}

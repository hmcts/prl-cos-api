package uk.gov.hmcts.reform.prl.services.serviceofdocuments;

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
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.AdditionalRecipients;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.ServiceOfDocumentsCheckEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.SodCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.SodSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.DocumentsDynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofdocuments.SodPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmailVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.BulkPrintService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationEmailService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationPostService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DISPLAY_LEGAL_REP_OPTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_ENGLISH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MISSING_ADDRESS_WARNING_TEXT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_OTHER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_OTHER_ORGANISATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PARTIES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PEOPLE_PRESENT_IN_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_RECIPIENT_OPTIONS;
import static uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames.SOD_ADDITIONAL_RECIPIENTS;
import static uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames.SOD_APPLICANT_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.AUTHORIZATION;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.COURT;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.DASH_BOARD_LINK;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ServiceOfDocumentsService {

    public static final String GOV_NOTIFY_TEMPLATE = "govNotifyTemplate";
    public static final String SEND_GRID_TEMPLATE = "sendGridTemplate";
    public static final String SERVED_PARTY = "servedParty";
    private static final String LETTER_TYPE = "Documents";
    private final ObjectMapper objectMapper;
    private final SendAndReplyService sendAndReplyService;
    private final ServiceOfApplicationService serviceOfApplicationService;
    private final ServiceOfApplicationEmailService serviceOfApplicationEmailService;
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;
    private final DynamicMultiSelectListService dynamicMultiSelectListService;
    private final UserService userService;
    private final AllTabServiceImpl allTabService;
    private final DocumentLanguageService documentLanguageService;
    private final EmailService emailService;
    private final BulkPrintService bulkPrintService;

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
            .servedBy(COURT)
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
        if (CollectionUtils.isNotEmpty(caseData.getServiceOfDocuments().getSodAdditionalRecipients())
            && caseData.getServiceOfDocuments().getSodAdditionalRecipients().contains(AdditionalRecipients.additionalRecipients)
            && CollectionUtils.isNotEmpty(caseData.getServiceOfDocuments().getSodAdditionalRecipientsList())) {
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
                handleServiceOfDocuments(
                    authorisation,
                    caseData,
                    unServedPack,
                    emailNotificationDetails,
                    bulkPrintDetails
                );
            }
            //serve other persons
            serveDocumentsToOtherPerson(authorisation, caseData, unServedPack, bulkPrintDetails);

            //serve additional recipients
            serveDocumentsToAdditionalRecipients(authorisation, caseData, unServedPack, emailNotificationDetails, bulkPrintDetails);

            //Reset unserved packs
            caseDataMap.put("sodUnServedPack", null);
            //Update served documents
            caseDataMap.put(
                "servedDocumentsDetailsList",
                getUpdatedServedDocumentsDetailsList(caseData, unServedPack, emailNotificationDetails, bulkPrintDetails)
            );
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
                                                   .servedAt(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                                                                 .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
                                                   .build()));

        if (CollectionUtils.isNotEmpty(caseData.getServiceOfDocuments().getServedDocumentsDetailsList())) {
            servedDocumentsDetailsList.addAll(caseData.getServiceOfDocuments().getServedDocumentsDetailsList());
        }

        return servedDocumentsDetailsList;
    }

    private void handleServiceOfDocuments(String authorisation,
                                          CaseData caseData,
                                          SodPack unServedPack,
                                          List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                          List<Element<BulkPrintDetails>> bulkPrintDetails) {
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

    private void handlePersonalServiceOfDocuments(String authorisation,
                                                  CaseData caseData,
                                                  SodPack unServedPack,
                                                  List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                  List<Element<BulkPrintDetails>> bulkPrintDetails) {
        if (SodCitizenServingRespondentsEnum.unrepresentedApplicant
            .getDisplayedValue().equals(unServedPack.getServedBy())) {
            //unrepresented applicant lip
            handlePersonalServiceUnRepApplicant(
                authorisation,
                caseData,
                unServedPack.getDocuments(),
                emailNotificationDetails,
                bulkPrintDetails
            );
        } else if (SodSolicitorServingRespondentsEnum.applicantLegalRepresentative
            .getDisplayedValue().equals(unServedPack.getServedBy())) {
            //applicant solicitor
            handlePersonalServiceApplicantSolicitor(
                authorisation,
                caseData,
                unServedPack.getDocuments(),
                emailNotificationDetails
            );
        }
    }

    private void handlePersonalServiceUnRepApplicant(String authorisation,
                                                     CaseData caseData,
                                                     List<Element<Document>> documents,
                                                     List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                     List<Element<BulkPrintDetails>> bulkPrintDetails) {
        Map<String, Object> params = new HashMap<>();
        params.put(AUTHORIZATION, authorisation);
        params.put(GOV_NOTIFY_TEMPLATE, EmailTemplateNames.SOD_PERSONAL_SERVICE_APPLICANT_LIP);
        params.put(SEND_GRID_TEMPLATE, SendgridEmailTemplateNames.SOD_PERSONAL_SERVICE_APPLICANT_LIP);
        if (C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            //C100
            caseData.getApplicants()
                .forEach(applicant -> handlePersonalNonPersonalServiceOfDocuments(
                    caseData,
                    applicant,
                    true,
                    documents,
                    emailNotificationDetails,
                    bulkPrintDetails,
                    params
                ));
        } else {
            //FL401
            handlePersonalNonPersonalServiceOfDocuments(
                caseData,
                element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401()),
                true,
                documents,
                emailNotificationDetails,
                bulkPrintDetails,
                params
            );
        }
    }

    private void handlePersonalNonPersonalServiceOfDocuments(CaseData caseData,
                                                             Element<PartyDetails> party,
                                                             boolean isApplicant,
                                                             List<Element<Document>> documents,
                                                             List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                             List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                             Map<String, Object> inputParams) {
        Map<String, String> params = new HashMap<>();
        params.put(AUTHORIZATION, (String) inputParams.get(AUTHORIZATION));
        if (isNotEmpty(party.getValue().getSolicitorEmail())) {
            params.put(
                SERVED_PARTY,
                isApplicant ? SERVED_PARTY_APPLICANT_SOLICITOR : SERVED_PARTY_RESPONDENT_SOLICITOR
            );
            sendEmailToSolicitor(
                caseData,
                party,
                documents,
                SOD_APPLICANT_RESPONDENT_SOLICITOR,
                emailNotificationDetails,
                params
            );
        } else if (isNotEmpty(party.getValue().getEmail())
            && ContactPreferences.email.equals(party.getValue().getContactPreferences())) {
            params.put(SERVED_PARTY, isApplicant ? SERVED_PARTY_APPLICANT : SERVED_PARTY_RESPONDENT);
            sendEmailToParty(
                caseData,
                party,
                documents,
                (EmailTemplateNames) inputParams.get(GOV_NOTIFY_TEMPLATE),
                (SendgridEmailTemplateNames) inputParams.get(SEND_GRID_TEMPLATE),
                emailNotificationDetails,
                params
            );
        } else {
            sendPostToParty(
                (String) inputParams.get(AUTHORIZATION),
                caseData,
                party,
                isApplicant ? SERVED_PARTY_APPLICANT : SERVED_PARTY_RESPONDENT,
                documents,
                bulkPrintDetails
            );
        }
    }

    private void handlePersonalServiceApplicantSolicitor(String authorisation,
                                                         CaseData caseData,
                                                         List<Element<Document>> documents,
                                                         List<Element<EmailNotificationDetails>> emailNotificationDetails) {
        Map<String, String> params = new HashMap<>();
        params.put(AUTHORIZATION, authorisation);
        params.put(SERVED_PARTY, SERVED_PARTY_APPLICANT_SOLICITOR);
        Element<PartyDetails> party = C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))
            ? caseData.getApplicants().get(0) : element(
            caseData.getApplicantsFL401().getPartyId(),
            caseData.getApplicantsFL401()
        );
        //serve documents only to main applicant solicitor
        sendEmailToSolicitor(
            caseData,
            party,
            documents,
            SOD_APPLICANT_RESPONDENT_SOLICITOR,
            emailNotificationDetails,
            params
        );
    }

    private void sendEmailToSolicitor(CaseData caseData,
                                      Element<PartyDetails> party,
                                      List<Element<Document>> documents,
                                      SendgridEmailTemplateNames sendgridTemplate,
                                      List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                      Map<String, String> params) {
        String authorisation = params.get(AUTHORIZATION);
        String servedParty = params.get(SERVED_PARTY);
        if (isNotEmpty(party.getValue().getSolicitorEmail())) {
            EmailNotificationDetails emailNotification = sendSendgridEmail(
                authorisation,
                caseData,
                documents,
                party,
                servedParty,
                party.getValue().getSolicitorEmail(),
                sendgridTemplate
            );
            if (emailNotification != null) {
                emailNotificationDetails.add(element(emailNotification.toBuilder()
                                                         .partyIds(String.valueOf(party.getId()))
                                                         .build()));
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
        String servedParty = params.get(SERVED_PARTY);
        EmailNotificationDetails emailNotification;
        if (CaseUtils.hasDashboardAccess(party)) {
            log.debug("Party has access to dashboard -> send gov notify email for {}", party.getId());
            emailNotification = sendGovNotifyEmail(caseData, documents, party, servedParty, govNotifyTemplate);
        } else {
            log.debug("Party does not access to dashboard -> send documents via sendgrid email for {}", party.getId());
            emailNotification = sendSendgridEmail(
                authorisation,
                caseData,
                documents,
                party,
                servedParty,
                party.getValue().getEmail(),
                sendgridTemplate
            );
        }

        if (emailNotification != null) {
            emailNotificationDetails.add(element(emailNotification.toBuilder()
                                                     .partyIds(String.valueOf(party.getId()))
                                                     .build()));
        }
    }

    private void sendEmailToAdditionalRecipients(String authorisation,
                                                 CaseData caseData,
                                                 SodPack unServedPack,
                                                 List<EmailInformation> emailList,
                                                 List<Element<EmailNotificationDetails>> emailNotificationDetails) {
        emailList.forEach(emailInfo -> {
            Map<String, Object> dynamicData = getEmailDynamicData(caseData, null, emailInfo.getEmailName());

            emailNotificationDetails.add(
                element(serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(
                    authorisation,
                    emailInfo.getEmailAddress(),
                    unwrapElements(unServedPack.getDocuments()),
                    SOD_ADDITIONAL_RECIPIENTS,
                    dynamicData,
                    SERVED_PARTY_OTHER_ORGANISATION
                )));
        });
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

                BulkPrintDetails bulkPrintNotif = sendPostViaBulkPrint(
                    authorisation,
                    caseData,
                    docs,
                    party.getValue().getLabelForDynamicList(),
                    party.getValue().getAddress(),
                    servedParty
                );
                if (null != bulkPrintNotif) {
                    bulkPrintDetails.add(element(bulkPrintNotif.toBuilder()
                                                     .partyIds(String.valueOf(party.getId()))
                                                     .build()));
                }
            } else {
                log.error(
                    "Couldn't post the documents to party address, as address is null/empty for {}",
                    party.getId()
                );
            }
        } catch (Exception e) {
            log.error("error while generating coversheet {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private void sendPostToAdditionalRecipients(String authorisation,
                                                CaseData caseData,
                                                SodPack unServedPack,
                                                List<PostalInformation> postList,
                                                List<Element<BulkPrintDetails>> bulkPrintDetails) {
        postList.forEach(postInfo -> {
            if ((isNotEmpty(postInfo)
                && isNotEmpty(postInfo.getPostalAddress()))
                && isNotEmpty(postInfo.getPostalAddress().getAddressLine1())) {
                List<Document> documents = null;
                try {
                    documents = new ArrayList<>(serviceOfApplicationPostService
                                                    .getCoverSheets(
                                                        caseData,
                                                        authorisation,
                                                        postInfo.getPostalAddress(),
                                                        postInfo.getPostalName(),
                                                        DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT
                                                    ));
                    documents.addAll(unwrapElements(unServedPack.getDocuments()));

                    bulkPrintDetails.add(element(sendPostViaBulkPrint(
                        authorisation,
                        caseData,
                        documents,
                        postInfo.getPostalName(),
                        postInfo.getPostalAddress(),
                        SERVED_PARTY_OTHER_ORGANISATION
                    )));
                } catch (Exception e) {
                    log.error("error while generating coversheet {}", e.getMessage(), e);
                    throw new RuntimeException(e.getMessage());
                }
            } else {
                log.error(
                    "Couldn't post the documents to additional recipients address, as address is null/empty for {}",
                    postInfo.getPostalName()
                );
            }
        });
    }

    private BulkPrintDetails sendPostViaBulkPrint(String authorisation,
                                                  CaseData caseData,
                                                  List<Document> documents,
                                                  String name,
                                                  Address address,
                                                  String servedParty) {
        UUID bulkPrintId = bulkPrintService.send(
            String.valueOf(caseData.getId()),
            authorisation,
            LETTER_TYPE,
            documents,
            name
        );

        return BulkPrintDetails.builder()
            .bulkPrintId(String.valueOf(bulkPrintId))
            .servedParty(servedParty)
            .printedDocs(String.join(",", documents.stream().map(Document::getDocumentFileName).toList()))
            .recipientsName(name)
            .printDocs(documents.stream().map(ElementUtils::element).toList())
            .postalAddress(address)
            .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                           .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
            .build();
    }

    private EmailNotificationDetails sendGovNotifyEmail(CaseData caseData,
                                                        List<Element<Document>> documents,
                                                        Element<PartyDetails> party,
                                                        String servedParty,
                                                        EmailTemplateNames govNotifyTemplate) {
        sendEmail(caseData, party.getValue(), SERVED_PARTY_APPLICANT.equals(servedParty), govNotifyTemplate);

        //Create email notification with documents
        return EmailNotificationDetails.builder()
            .emailAddress(party.getValue().getEmail())
            .servedParty(servedParty)
            .docs(documents)
            .attachedDocs(CITIZEN_CAN_VIEW_ONLINE)
            .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                           .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
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
                                                    PartyDetails party,
                                                    String addlRecipientName) {
        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        if (null != party) {
            dynamicData.put("partyName", party.getLabelForDynamicList());
            dynamicData.put("solicitorName", party.getRepresentativeFullName());
        }
        if (null != addlRecipientName) {
            dynamicData.put("name", addlRecipientName);
        }
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
                                                       String email,
                                                       SendgridEmailTemplateNames sendgridTemplate) {
        Map<String, Object> dynamicData = getEmailDynamicData(caseData, party.getValue(), null);

        return serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(
            authorisation,
            email,
            unwrapElements(documents),
            sendgridTemplate,
            dynamicData,
            servedParty
        );
    }

    private void handleNonPersonalServiceOfDocuments(String authorisation,
                                                     CaseData caseData,
                                                     SodPack unServedPack,
                                                     List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                     List<Element<BulkPrintDetails>> bulkPrintDetails) {
        //applicants
        if (CollectionUtils.isNotEmpty(unServedPack.getApplicantIds())) {
            handleNonPersonalServiceToPartyOrSolicitor(
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
            handleNonPersonalServiceToPartyOrSolicitor(
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

    private void handleNonPersonalServiceToPartyOrSolicitor(String authorisation,
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
                    Map<String, Object> params = new HashMap<>();
                    params.put(AUTHORIZATION, authorisation);
                    params.put(
                        GOV_NOTIFY_TEMPLATE,
                        EmailTemplateNames.SOD_NON_PERSONAL_SERVICE_APPLICANT_RESPONDENT_LIP
                    );
                    params.put(
                        SEND_GRID_TEMPLATE,
                        SendgridEmailTemplateNames.SOD_NON_PERSONAL_SERVICE_APPLICANT_RESPONDENT_LIP
                    );
                    handlePersonalNonPersonalServiceOfDocuments(
                        caseData,
                        party,
                        isApplicant,
                        documents,
                        emailNotificationDetails,
                        bulkPrintDetails,
                        params
                    );
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

    private void serveDocumentsToAdditionalRecipients(String authorisation,
                                                      CaseData caseData,
                                                      SodPack unServedPack,
                                                      List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                      List<Element<BulkPrintDetails>> bulkPrintDetails) {
        List<EmailInformation> emailList = new ArrayList<>();
        List<PostalInformation> postList = new ArrayList<>();
        //get email and postal information
        unServedPack.getAdditionalRecipients().stream()
            .map(Element::getValue)
            .forEach(addRecipient -> {
                if (DeliveryByEnum.email.equals(addRecipient.getServeByPostOrEmail())) {
                    emailList.add(addRecipient.getEmailInformation());
                } else if (DeliveryByEnum.post.equals(addRecipient.getServeByPostOrEmail())) {
                    postList.add(addRecipient.getPostalInformation());
                }
            });

        if (CollectionUtils.isNotEmpty(emailList)) {
            sendEmailToAdditionalRecipients(authorisation, caseData, unServedPack, emailList, emailNotificationDetails);
        }
        if (CollectionUtils.isNotEmpty(postList)) {
            sendPostToAdditionalRecipients(authorisation, caseData, unServedPack, postList, bulkPrintDetails);
        }
    }

    private List<Document> getSelectedDocuments(String authorisation,
                                                CaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getServiceOfDocuments().getSodDocumentsList())) {
            return caseData.getServiceOfDocuments().getSodDocumentsList().stream()
                .map(Element::getValue)
                .map(docsDynamicList -> sendAndReplyService.getSelectedDocument(
                    authorisation, docsDynamicList.getDocumentsList()))
                .toList();
        }
        return Collections.emptyList();
    }

    private List<Document> getUploadedDocuments(CaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getServiceOfDocuments().getSodAdditionalDocumentsList())) {
            return caseData.getServiceOfDocuments().getSodAdditionalDocumentsList()
                .stream()
                .map(Element::getValue)
                .toList();
        }
        return Collections.emptyList();
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

    public List<String> validateDocuments(CallbackRequest callbackRequest) {
        List<String> errors = new ArrayList<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        if (CollectionUtils.isEmpty(caseData.getServiceOfDocuments().getSodAdditionalDocumentsList())
            && emptyCfvSelectedDocuments(caseData)) {
            errors.add("Please select a document or upload a document to serve");
        }
        return errors;
    }

    private boolean emptyCfvSelectedDocuments(CaseData caseData) {
        return CollectionUtils.isEmpty(caseData.getServiceOfDocuments().getSodDocumentsList())
            || null == caseData.getServiceOfDocuments().getSodDocumentsList().get(0)
            || null == caseData.getServiceOfDocuments().getSodDocumentsList().get(0).getValue();
    }
}

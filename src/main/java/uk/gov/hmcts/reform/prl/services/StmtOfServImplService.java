package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SosUploadedByEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.StatementOfServiceWhatWasServed;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notification.DocumentsNotification;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationDetails;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationType;
import uk.gov.hmcts.reform.prl.models.dto.notification.PartyType;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.IncrementalInteger;
import uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.RE7_HINT;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.RE8_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALL_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C9_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMMA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FL415_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOS_COMPLETED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOS_PENDING;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PERSONAL_SERVICE_SERVED_BY_BAILIFF;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PERSONAL_SERVICE_SERVED_BY_CA;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PRL_COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StmtOfServImplService {
    public static final String RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_POST = "Respondent has been served personally by Court,"
        + " hence no bulk print id is generated";
    public static final String RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_EMAIL = "Respondent has been served personally by Court through email";
    private static final String BY_POST = "By post";
    public static final String UN_SERVED_RESPONDENT_PACK = "unServedRespondentPack";
    public static final String UNSERVED_CITIZEN_RESPONDENT_PACK = "unservedCitizenRespondentPack";
    public static final String STMT_OF_SERVICE_FOR_APPLICATION = "stmtOfServiceForApplication";
    public static final String ACCESS_CODE_NOTIFICATIONS = "accessCodeNotifications";
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final ServiceOfApplicationService serviceOfApplicationService;
    private final AllTabServiceImpl allTabService;
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;
    private final LaunchDarklyClient launchDarklyClient;
    private final ManageDocumentsService manageDocumentsService;

    public Map<String, Object> retrieveRespondentsList(CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(
            caseDetails.getData(),
            CaseData.class
        );

        Map<String, Object> caseDataUpdated = caseDetails.getData();
        List<Element<StmtOfServiceAddRecipient>> stmtOfServiceAddRecipient = new ArrayList<>();
        stmtOfServiceAddRecipient.add(element(StmtOfServiceAddRecipient.builder()
                                                  .respondentDynamicList(DynamicList.builder()
                                                                             .listItems(getRespondentsList(caseData))
                                                                             .value(DynamicListElement.builder()
                                                                                        .code(UUID.randomUUID())
                                                                                        .label(ALL_RESPONDENTS).build())
                                                                             .build())
                                                  .build()));

        log.info("Statement of service dynamic list value:: {}", stmtOfServiceAddRecipient);
        caseDataUpdated.put("stmtOfServiceAddRecipient", stmtOfServiceAddRecipient);
        return caseDataUpdated;
    }

    public Map<String, Object> handleSosAboutToSubmit(CaseDetails caseDetails, String authorisation) {
        Map<String, Object> caseDataUpdateMap = caseDetails.getData();
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        log.info("*** Statement of service, about-to-submit callback *** {}",
                 caseData.getStatementOfService().getStmtOfServiceWhatWasServed());
        log.info("SOS request {}", caseData.getStatementOfService().getStmtOfServiceAddRecipient());
        if (StatementOfServiceWhatWasServed.statementOfServiceApplicationPack
            .equals(caseData.getStatementOfService().getStmtOfServiceWhatWasServed())) {
            //Application packs
            handleSosForApplicationPacks(authorisation, caseData, caseDataUpdateMap);
        } else if (StatementOfServiceWhatWasServed.statementOfServiceOrder
            .equals(caseData.getStatementOfService().getStmtOfServiceWhatWasServed())) {
            //Orders
            handleSosForOrders(authorisation, caseData, caseDataUpdateMap);
        }

        caseDataUpdateMap.put("stmtOfServiceAddRecipient", null);
        caseDataUpdateMap.put("stmtOfServiceWhatWasServed", null);
        return caseDataUpdateMap;
    }

    private void handleSosForApplicationPacks(String authorisation,
                                              CaseData caseData,
                                              Map<String, Object> caseDataMap) {
        log.info("SOS for Application packs");
        List<Element<StmtOfServiceAddRecipient>> sosRecipients = new ArrayList<>();
        Map<String,List<Element<Document>>> coverLettersMap = new HashMap<>();
        //Get all sos recipients
        caseData.getStatementOfService().getStmtOfServiceAddRecipient()
            .stream()
            .map(Element::getValue)
            .forEach(sosRecipient -> {
                sosRecipient = getUpdatedSosRecipient(authorisation, caseData, sosRecipient, null);

                //PRL-5979 - Send cover letter with access code to respondents
                caseDataMap.put(
                    ACCESS_CODE_NOTIFICATIONS,
                    sendAccessCodesToRespondentsByCourtLegalRep(authorisation, caseData, sosRecipient, coverLettersMap)
                );

                sosRecipients.add(element(sosRecipient.toBuilder()
                                              .respondentDynamicList(null) //clear dynamic list after sending access code info
                                              .build()));
            });
        //Add all existing sos recipients & update into case data
        if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForApplication())) {
            sosRecipients.addAll(caseData.getStatementOfService().getStmtOfServiceForApplication());
        }
        caseDataMap.put(STMT_OF_SERVICE_FOR_APPLICATION, sosRecipients);

        //Update final served application packs
        if (isNotEmpty(caseData.getServiceOfApplication())
            && isNotEmpty(caseData.getServiceOfApplication().getUnServedRespondentPack())
            && CollectionUtils.isNotEmpty(caseData.getServiceOfApplication().getUnServedRespondentPack().getPackDocument())) {
            List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList = new ArrayList<>();
            //CHECK WITH ORDER FOR EXISTING
            if (CollectionUtils.isNotEmpty(caseData.getFinalServedApplicationDetailsList())) {
                finalServedApplicationDetailsList = caseData.getFinalServedApplicationDetailsList();
            }
            //update served application details for respondents
            finalServedApplicationDetailsList.add(element(checkAndServeRespondentPacksPersonalService(
                caseData,
                authorisation,
                coverLettersMap
            )));
            caseDataMap.put("finalServedApplicationDetailsList", finalServedApplicationDetailsList);
            caseDataMap.put(UN_SERVED_RESPONDENT_PACK, null);
        }
    }

    private void handleSosForOrders(String authorisation,
                                    CaseData caseData,
                                    Map<String, Object> caseDataMap) {
        log.info("SOS for Orders");
        List<Element<StmtOfServiceAddRecipient>> sosRecipients = new ArrayList<>();
        List<Element<OrderDetails>> orderCollection = caseData.getOrderCollection();

        //Get all sos recipients
        caseData.getStatementOfService().getStmtOfServiceAddRecipient()
            .stream()
            .map(Element::getValue)
            .forEach(sosRecipient -> {
                sosRecipient = getUpdatedSosRecipient(authorisation, caseData, sosRecipient, orderCollection);

                sosRecipients.add(element(sosRecipient.toBuilder()
                                              .respondentDynamicList(null) //clear dynamic list
                                              .build()));
            });

        //Add all existing sos recipients & update into case data
        if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForOrder())) {
            sosRecipients.addAll(caseData.getStatementOfService().getStmtOfServiceForOrder());
        }
        caseDataMap.put("stmtOfServiceForOrder", sosRecipients);
        //PRL-6122
        caseDataMap.put(ORDER_COLLECTION, orderCollection);
    }

    private StmtOfServiceAddRecipient getUpdatedSosRecipient(String authorisation,
                                                             CaseData caseData,
                                                             StmtOfServiceAddRecipient recipient,
                                                             List<Element<OrderDetails>> orderCollection) {
        log.info("Inside *getUpdatedSosRecipient*, sosRecipient {}", recipient);
        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            if (ALL_RESPONDENTS.equals(recipient.getRespondentDynamicList().getValue().getLabel())) {
                List<String> respondentNamesList = CaseUtils.getPartyNameList(caseData.getRespondents());
                String allRespondentNames = String.join(", ", respondentNamesList).concat(" (All respondents)");
                recipient = getRecipient(
                    authorisation,
                    recipient,
                    CaseUtils.getPartyIdListAsString(caseData.getRespondents()),
                    allRespondentNames
                );

                //PRL-6122 served parties in order collection if sos for orders
                updateOrdersServedParties(caseData, orderCollection,
                                          ManageOrdersUtils.getServedParties(caseData.getRespondents()));

            } else {
                recipient = getRecipient(authorisation,
                                         recipient,
                                         recipient.getRespondentDynamicList().getValue().getCode(),
                                         recipient.getRespondentDynamicList().getValue().getLabel()
                );

                StmtOfServiceAddRecipient finalRecipient = recipient;
                List<Element<PartyDetails>> partiesServed = caseData.getRespondents().stream()
                    .filter(respondent -> respondent.getId().toString().equals(
                            finalRecipient.getRespondentDynamicList().getValue().getCode()))
                    .toList();
                //PRL-6122
                updateOrdersServedParties(caseData, orderCollection, ManageOrdersUtils.getServedParties(partiesServed));
            }
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            recipient = getRecipient(authorisation,
                                     recipient,
                                     recipient.getRespondentDynamicList().getValue().getCode(),
                                     recipient.getRespondentDynamicList().getValue().getLabel()
            );
            //PRL-6122
            updateOrdersServedParties(caseData, orderCollection,
                                      List.of(ManageOrdersUtils.getServedParty(caseData.getRespondentsFL401())));
        }
        return recipient;
    }

    private void updateOrdersServedParties(CaseData caseData,
                                           List<Element<OrderDetails>> orderCollection,
                                           List<Element<ServedParties>> servedParties) {
        log.info("Inside *updateOrdersServedParties*, servedParties {}", servedParties);
        if (CollectionUtils.isNotEmpty(orderCollection)) {
            //PRL-6122 served parties in order collection
            nullSafeCollection(orderCollection)
                .stream()
                .filter(order -> SOS_PENDING.equals(order.getValue().getSosStatus()))
                .forEach(order -> {
                    log.info("Order is pending for SOS {}", order.getId());
                    List<Element<ServedParties>> updatedServedParties = new ArrayList<>(order.getValue().getServeOrderDetails().getServedParties());
                    updatedServedParties.addAll(servedParties);

                    OrderDetails updatedOrder = order.getValue().toBuilder()
                        .serveOrderDetails(order.getValue().getServeOrderDetails().toBuilder()
                                               .servedParties(updatedServedParties)
                                               .build())
                        .build();
                    //Update order sos status
                    //FL401 - COMPLETED always as there is only one respondent
                    //C100 - COMPLETED if all respondents are served, determined by checking respondents with served parties
                    updatedOrder = areAllRespondentsServed(updatedOrder, caseData)
                        ? updatedOrder.toBuilder().sosStatus(SOS_COMPLETED).build()
                        : updatedOrder;

                    orderCollection.set(orderCollection.indexOf(order), element(order.getId(), updatedOrder));
                    log.info("Order SOS done, SOS status is updated to {}", updatedOrder.getSosStatus());
                });
        }
    }

    private boolean areAllRespondentsServed(OrderDetails updatedOrder,
                                            CaseData caseData) {
        return FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            || caseData.getRespondents().stream()
            .map(respondent -> respondent.getId().toString())
            .allMatch(updatedOrder.getServeOrderDetails().getServedParties().stream()
                          .map(Element::getValue)
                          .map(ServedParties::getPartyId)
                          .collect(Collectors.toSet())::contains);
    }

    private StmtOfServiceAddRecipient getRecipient(String authorisation,
                                                   StmtOfServiceAddRecipient recipient,
                                                   String selectedPartyId,
                                                   String selectedPartyName) {
        return recipient.toBuilder()
            .selectedPartyId(selectedPartyId)
            .selectedPartyName(selectedPartyName)
            .stmtOfServiceDocument(recipient.getStmtOfServiceDocument())
            .servedDateTimeOption(recipient.getServedDateTimeOption())
            .uploadedBy(getSosUploadedBy(authorisation))
            .build();
    }

    private SosUploadedByEnum getSosUploadedBy(String authorisation) {
        List<String> loggedInUserRoles = manageDocumentsService.getLoggedInUserType(authorisation);
        if (CollectionUtils.isNotEmpty(loggedInUserRoles)) {
            if (loggedInUserRoles.contains(SOLICITOR_ROLE)) {
                return SosUploadedByEnum.APPLICANT_SOLICITOR;
            } else if (loggedInUserRoles.contains(CITIZEN_ROLE)) {
                return SosUploadedByEnum.APPLICANT_LIP;
            } else {
                return SosUploadedByEnum.COURT_STAFF;
            }
        }
        return null;
    }

    private List<DynamicListElement> getRespondentsList(CaseData caseData) {
        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        List<DynamicListElement> respondentListItems = new ArrayList<>();
        IncrementalInteger i = new IncrementalInteger(1);
        if (respondents != null) {
            respondents.forEach(respondent -> respondentListItems.add(DynamicListElement.builder().code(respondent.getId().toString())
                              .label(respondent.getValue().getFirstName() + " "
                                         + respondent.getValue().getLastName()
                                         + " (Respondent " + i.getAndIncrement() + ")").build()));
            respondentListItems.add(DynamicListElement.builder().code(ALL_RESPONDENTS).label(ALL_RESPONDENTS).build());
        } else if (caseData.getRespondentsFL401() != null) {
            String name = caseData.getRespondentsFL401().getFirstName() + " "
                + caseData.getRespondentsFL401().getLastName()
                + " (Respondent)";

            respondentListItems.add(DynamicListElement.builder().code(name).label(name).build());
        }

        return respondentListItems;
    }

    public ServedApplicationDetails checkAndServeRespondentPacksPersonalService(CaseData caseData, String authorization, Map<String,
        List<Element<Document>>> coverLettersMap) {
        SoaPack unServedRespondentPack = caseData.getServiceOfApplication().getUnServedRespondentPack();
        String whoIsResponsible = SoaCitizenServingRespondentsEnum.courtAdmin
            .toString().equalsIgnoreCase(unServedRespondentPack.getPersonalServiceBy())
            ? PERSONAL_SERVICE_SERVED_BY_CA : PERSONAL_SERVICE_SERVED_BY_BAILIFF;
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        String caseTypeOfApplication = CaseUtils.getCaseTypeOfApplication(caseData);
        if (FL401_CASE_TYPE.equalsIgnoreCase(caseTypeOfApplication)) {
            unServedRespondentPack = unServedRespondentPack.toBuilder()
                .packDocument(unServedRespondentPack.getPackDocument()
                                  .stream()
                                  .filter(d -> !d.getValue().getDocumentFileName().equalsIgnoreCase(
                                      SOA_FL415_FILENAME)).toList())
                .build();
            List<Element<Document>> docs = unServedRespondentPack.getPackDocument();
            String partyId = String.valueOf(caseData.getRespondentsFL401().getPartyId());
            docs.addAll(coverLettersMap.get(partyId));
            if (SoaSolicitorServingRespondentsEnum.courtAdmin.toString().equalsIgnoreCase(unServedRespondentPack.getPersonalServiceBy())) {
                emailNotificationDetails.add(element(getEmailNotificationDetailsForaParty(docs, partyId)));
            } else if (SoaSolicitorServingRespondentsEnum.courtBailiff.toString()
                .equalsIgnoreCase(unServedRespondentPack.getPersonalServiceBy())) {
                bulkPrintDetails.add(element(getBulkPrintDetailsForaParty(docs, partyId)));
            }
        } else {
            unServedRespondentPack = unServedRespondentPack.toBuilder()
                .packDocument(unServedRespondentPack.getPackDocument()
                                  .stream()
                                  .filter(d -> !C9_DOCUMENT_FILENAME.equalsIgnoreCase(d.getValue().getDocumentFileName()))
                                  .toList())
                .build();
            List<Element<Document>> docs = unServedRespondentPack.getPackDocument();
            for (int i = 0; i < caseData.getRespondents().size(); i++) {
                String partyId = String.valueOf(caseData.getRespondents().get(i).getId());
                docs.addAll(coverLettersMap.get(partyId));
                if (SoaSolicitorServingRespondentsEnum.courtAdmin.toString().equalsIgnoreCase(unServedRespondentPack.getPersonalServiceBy())) {
                    emailNotificationDetails.add(element(getEmailNotificationDetailsForaParty(docs, partyId)));
                } else if (SoaSolicitorServingRespondentsEnum.courtBailiff.toString()
                    .equalsIgnoreCase(unServedRespondentPack.getPersonalServiceBy())) {
                    bulkPrintDetails.add(element(getBulkPrintDetailsForaParty(docs, partyId)));
                }
            }
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        String formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        return ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .servedAt(formatter)
            .modeOfService(CaseUtils.getModeOfService(emailNotificationDetails, bulkPrintDetails))
            .whoIsResponsible(whoIsResponsible)
            .bulkPrintDetails(bulkPrintDetails).build();
    }

    private EmailNotificationDetails getEmailNotificationDetailsForaParty(List<Element<Document>> docs, String partyId) {
        return EmailNotificationDetails.builder()
            .emailAddress(RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_EMAIL)
            .servedParty(PRL_COURT_ADMIN)
            .docs(docs)
            .attachedDocs(String.join(",", docs.stream()
                .map(Element::getValue)
                .map(Document::getDocumentFileName).toList()))
            .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                           .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
            .partyIds(partyId)
            .build();
    }

    private BulkPrintDetails getBulkPrintDetailsForaParty(List<Element<Document>> docs, String partyId) {
        return BulkPrintDetails.builder()
            .servedParty(PRL_COURT_ADMIN)
            .bulkPrintId(RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_POST)
            .printedDocs(String.join(",", docs.stream()
                .map(Element::getValue)
                .map(Document::getDocumentFileName).toList()))
            .printDocs(docs)
            .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                           .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
            .partyIds(partyId)
            .build();
    }

    private String getPartyIds(String caseTypeOfApplication,
                               List<Element<PartyDetails>> parties,
                               PartyDetails fl401Party) {
        if (FL401_CASE_TYPE.equalsIgnoreCase(caseTypeOfApplication)) {
            return String.valueOf(fl401Party.getPartyId());
        }
        return String.join(COMMA,
                           CaseUtils.getPartyIdList(parties).stream()
                               .map(Element::getValue)
                               .toList());
    }

    public void saveCitizenSos(String caseId, String eventId,String authorisation, CitizenSos sosObject) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = allTabService.getStartUpdateForSpecificEvent(caseId, eventId);
        Map<String, Object> updatedCaseDataMap = startAllTabsUpdateDataContent.caseDataMap();
        CaseData updatedCaseData = startAllTabsUpdateDataContent.caseData();
        if (null != updatedCaseDataMap.get(UNSERVED_CITIZEN_RESPONDENT_PACK)
            && CollectionUtils.isNotEmpty(updatedCaseData.getServiceOfApplication().getUnservedCitizenRespondentPack().getPackDocument())) {
            List<Element<StmtOfServiceAddRecipient>> stmtOfServiceforApplication = new ArrayList<>();
            updateStatementOfServiceCollection(sosObject, updatedCaseData, stmtOfServiceforApplication);
            updatedCaseDataMap.put(STMT_OF_SERVICE_FOR_APPLICATION, stmtOfServiceforApplication);
            if (YesOrNo.No.equals(sosObject.getIsOrder())) {
                updateFinalListOfServedApplications(
                    authorisation,
                    startAllTabsUpdateDataContent.authorisation(),
                    updatedCaseData,
                    sosObject.getPartiesServed(),
                    updatedCaseDataMap
                );
                updatedCaseDataMap.put(UNSERVED_CITIZEN_RESPONDENT_PACK,
                                       updateUnserVedCitizenRespondentPack(sosObject.getPartiesServed(), updatedCaseData.getServiceOfApplication()
                                           .getUnservedCitizenRespondentPack()));
            }
        }
        allTabService.submitAllTabsUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            updatedCaseDataMap
        );
    }

    private SoaPack updateUnserVedCitizenRespondentPack(List<String> partiesServed, SoaPack unservedCitizenRespondentPack) {
        List<String> partyIds = new ArrayList<>(unservedCitizenRespondentPack.getPartyIds().stream().map(Element::getValue).toList());
        partyIds.removeAll(partiesServed);
        return CollectionUtils.isNotEmpty(partyIds) ? unservedCitizenRespondentPack : null;
    }

    private void updateFinalListOfServedApplications(String authorisation, String authorization,
                                                     CaseData updatedCaseData, List<String> partiesList, Map<String, Object> updatedCaseDataMap) {
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
        List<Element<Document>> packDocs = updatedCaseData.getServiceOfApplication().getUnservedCitizenRespondentPack().getPackDocument();
        List<String> partiesServed = new ArrayList<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(updatedCaseData))) {
            updatedCaseData.getRespondents().forEach(respondent -> {
                if (partiesList.contains(String.valueOf(respondent.getId()))) {
                    partiesServed.add(respondent.getValue().getLabelForDynamicList());
                }
            });
        } else {
            if (partiesList.contains(String.valueOf(updatedCaseData.getRespondentsFL401().getPartyId()))) {
                partiesServed.add(updatedCaseData.getRespondentsFL401().getLabelForDynamicList());
            }
        }
        log.info("pack docs {}", packDocs);
        if (updatedCaseData.getFinalServedApplicationDetailsList() != null) {
            finalServedApplicationDetailsList = updatedCaseData.getFinalServedApplicationDetailsList();
        } else {
            log.info("*** finalServedApplicationDetailsList is empty in case data ***");
            finalServedApplicationDetailsList = new ArrayList<>();
        }
        finalServedApplicationDetailsList.add(element(ServedApplicationDetails.builder()
                                                          .servedBy(userService.getUserDetails(authorisation).getFullName())
                                                          .servedAt(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                                                                        .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
                                                          .modeOfService(BY_POST)
                                                          .whoIsResponsible("Applicant Lip")
                                                          .bulkPrintDetails(List.of(element(BulkPrintDetails.builder()
                                                                                .bulkPrintId("Application personally served by "
                                                                                                 + userService.getUserDetails(authorisation)
                                                                                    .getFullName())
                                                                                .servedParty(String.join(",", partiesServed))
                                                                                .printedDocs(String.join(",", unwrapElements(packDocs).stream()
                                                                                    .filter(Objects::nonNull)
                                                                                    .map(Document::getDocumentFileName)
                                                                                    .toList()))
                                                                                .recipientsName(String.join(",", partiesServed))
                                                                                .printDocs(unwrapElements(packDocs).stream()
                                                                                               .map(ElementUtils::element)
                                                                                               .toList())
                                                                                .partyIds(String.join(",", partiesList))
                                                                                .build()))).build()));
        updatedCaseDataMap.put("finalServedApplicationDetailsList", finalServedApplicationDetailsList);
        //PRL-5979 - Send cover letter with access code to respondents
        updatedCaseDataMap.put(
            ACCESS_CODE_NOTIFICATIONS,
            sendAccessCodesToRespondentsByLip(authorization, updatedCaseData, partiesList)
        );
    }

    private void updateStatementOfServiceCollection(CitizenSos sosObject, CaseData updatedCaseData,
                                                    List<Element<StmtOfServiceAddRecipient>> stmtOfServiceforApplication) {
        List<String> partiesList = sosObject.getPartiesServed();
        List<String> partiesServed = new ArrayList<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(updatedCaseData))) {
            updatedCaseData.getRespondents().forEach(respondent -> {
                if (partiesList.contains(String.valueOf(respondent.getId()))) {
                    partiesServed.add(respondent.getValue().getLabelForDynamicList());
                }
            });
        } else {
            if (partiesList.contains(String.valueOf(updatedCaseData.getRespondentsFL401().getPartyId()))) {
                partiesServed.add(updatedCaseData.getRespondentsFL401().getLabelForDynamicList());
            }
        }
        stmtOfServiceforApplication.add(element(StmtOfServiceAddRecipient.builder()
                                                  .citizenPartiesServedDate(sosObject.getPartiesServedDate())
                                                  .citizenPartiesServedList(String.join(",", partiesServed))
                                                    .stmtOfServiceDocument(Document.builder()
                                                                               .documentFileName(sosObject.getCitizenSosDocs()
                                                                                                     .getDocumentFileName())
                                                                               .documentUrl(sosObject.getCitizenSosDocs()
                                                                                                .getDocumentUrl())
                                                                               .documentHash(sosObject.getCitizenSosDocs()
                                                                                                 .getDocumentHash())
                                                                               .documentBinaryUrl(sosObject.getCitizenSosDocs()
                                                                                                      .getDocumentBinaryUrl())
                                                                               .build())
                                                    .selectedPartyId(String.join(",", partiesList))
                                                  .build()));

        log.info("Statement of service list :: {}", stmtOfServiceforApplication);
        if (ObjectUtils.isNotEmpty(updatedCaseData.getStatementOfService())
                && CollectionUtils.isNotEmpty(updatedCaseData.getStatementOfService().getStmtOfServiceForApplication())) {
            stmtOfServiceforApplication.addAll(updatedCaseData.getStatementOfService().getStmtOfServiceForApplication());
        }
    }


    private List<Element<DocumentsNotification>> sendAccessCodesToRespondentsByCourtLegalRep(String authorization,
                                                                                             CaseData caseData,
                                                                                             StmtOfServiceAddRecipient recipient,
                                                                                             Map<String, List<Element<Document>>> coverLettersMap) {
        List<Element<DocumentsNotification>> documentsNotifications = getExistingAccessCodeNotifications(caseData);
        //PRL-5979 - Send cover letter with access code to respondent only if LD flag is enabled
        if (launchDarklyClient.isFeatureEnabled(ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER)) {
            if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
                if (ALL_RESPONDENTS.equals(recipient.getRespondentDynamicList().getValue().getLabel())) {
                    //send to all respondents
                    documentsNotifications.addAll(caseData.getRespondents().stream()
                                                      .map(respondent -> {
                                                          Element<DocumentsNotification> documentsNotificationElement = sendAccessCodeCoverLetter(
                                                              authorization,
                                                              caseData,
                                                              respondent,
                                                              true
                                                          );
                                                          if (null != documentsNotificationElement) {
                                                              coverLettersMap.put(String.valueOf(respondent.getId()),
                                                                                  documentsNotificationElement.getValue().getDocuments());
                                                          }
                                                          return documentsNotificationElement;
                                                      }).toList());
                } else {
                    //send to selected respondent
                    documentsNotifications.addAll(caseData.getRespondents().stream()
                                                      .filter(respondent -> respondent.getId().toString().equals(
                                                          recipient.getRespondentDynamicList().getValue().getCode()))
                                                      .map(respondent -> {
                                                          Element<DocumentsNotification> documentsNotificationElement = sendAccessCodeCoverLetter(
                                                              authorization,
                                                              caseData,
                                                              respondent,
                                                              true
                                                          );
                                                          if (null != documentsNotificationElement) {

                                                              coverLettersMap.put(
                                                                  String.valueOf(respondent.getId()),
                                                                  documentsNotificationElement.getValue().getDocuments()
                                                              );
                                                          }
                                                          return documentsNotificationElement;
                                                      }).toList());
                }
            } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                //send to respondent
                documentsNotifications.add(sendAccessCodeCoverLetter(
                    authorization,
                    caseData,
                    element(caseData.getRespondentsFL401().getPartyId(), caseData.getRespondentsFL401()),
                    false
                ));
            }
        } else {
            documentsNotifications.add(getNoAccessCodeDocumentsNotification());
        }

        return documentsNotifications.stream().filter(Objects::nonNull).toList();
    }

    private List<Element<DocumentsNotification>> sendAccessCodesToRespondentsByLip(String authorization,
                                                                                   CaseData caseData,
                                                                                   List<String> partiesList) {
        List<Element<DocumentsNotification>> documentsNotifications = getExistingAccessCodeNotifications(caseData);
        //PRL-5979 - Send cover letter with access code to respondent only if LD flag is enabled
        if (launchDarklyClient.isFeatureEnabled(ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER)) {
            if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
                documentsNotifications.addAll(caseData.getRespondents().stream()
                                                  .filter(respondent -> partiesList.contains(String.valueOf(respondent.getId())))
                                                  .map(respondent ->
                                                           sendAccessCodeCoverLetter(
                                                               authorization,
                                                               caseData,
                                                               respondent,
                                                               true
                                                           )).toList());

            } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                //send to respondent
                documentsNotifications.add(sendAccessCodeCoverLetter(
                    authorization,
                    caseData,
                    element(caseData.getRespondentsFL401().getPartyId(), caseData.getRespondentsFL401()),
                    false
                ));
            }
        } else {
            documentsNotifications.add(getNoAccessCodeDocumentsNotification());
        }
        return documentsNotifications.stream().filter(Objects::nonNull).toList();
    }

    private List<Element<DocumentsNotification>> getExistingAccessCodeNotifications(CaseData caseData) {
        return null != caseData.getDocumentsNotifications()
            && CollectionUtils.isNotEmpty(caseData.getDocumentsNotifications()
                                              .getAccessCodeNotifications())
            ? caseData.getDocumentsNotifications().getAccessCodeNotifications()
            : new ArrayList<>();
    }

    private Element<DocumentsNotification> sendAccessCodeCoverLetter(String authorization,
                                                                     CaseData caseData,
                                                                     Element<PartyDetails> respondent,
                                                                     boolean isC100Case) {
        if (!CaseUtils.hasDashboardAccess(respondent)
            && !CaseUtils.hasLegalRepresentation(respondent.getValue())) {
            List<Document> documents = null;
            try {
                //cover sheets
                documents = new ArrayList<>(serviceOfApplicationPostService
                                                .getCoverSheets(caseData, authorization,
                                                                respondent.getValue().getAddress(),
                                                                respondent.getValue().getLabelForDynamicList(),
                                                                DOCUMENT_COVER_SHEET_HINT
                                                ));

                //cover letters
                List<Document> coverLetters = serviceOfApplicationService.getCoverLetters(
                    authorization,
                    caseData,
                    respondent,
                    isC100Case ? RE7_HINT : RE8_HINT,
                    true
                );

                documents.addAll(coverLetters);

                //post letters via bulk print
                BulkPrintDetails bulkPrintDetails = serviceOfApplicationPostService.sendPostNotificationToParty(
                    caseData,
                    authorization,
                    respondent,
                    documents,
                    respondent.getValue().getLabelForDynamicList()
                );

                return element(DocumentsNotification.builder()
                                   .notification(NotificationDetails.builder()
                                                     .bulkPrintId(bulkPrintDetails.getBulkPrintId())
                                                     .notificationType(NotificationType.BULK_PRINT)
                                                     .partyId(String.valueOf(respondent.getId()))
                                                     .partyType(PartyType.RESPONDENT)
                                                     .sentDateTime(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
                                                     .build())
                                   .documents(ElementUtils.wrapElements(coverLetters))
                                   .build());
            } catch (Exception e) {
                log.error(
                    "SOS: Exception occurred in sending access code cover letter to respondent {} ",
                    respondent.getId(),
                    e
                );
                return null;
            }
        } else {
            log.warn(
                "Respondent {} is either represented or has got dashboard access, no need to send access code",
                respondent.getId()
            );
            return element(DocumentsNotification.builder()
                               .notification(NotificationDetails.builder()
                                                 .partyId(String.valueOf(respondent.getId()))
                                                 .partyType(PartyType.RESPONDENT)
                                                 .sentDateTime(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
                                                 .remarks(
                                                     "Respondent is either represented or has got dashboard access, no need to send access code")
                                                 .build())
                               .build());
        }
    }

    private Element<DocumentsNotification> getNoAccessCodeDocumentsNotification() {
        log.warn(
            "LD flag {} is not enabled to send access code in cover letter for citizen",
            ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER
        );
        return element(DocumentsNotification.builder()
                           .notification(NotificationDetails.builder()
                                             .partyType(PartyType.RESPONDENT)
                                             .sentDateTime(LocalDateTime.now(ZoneId.of(
                                                 LONDON_TIME_ZONE)))
                                             .remarks(
                                                 "Citizen journey is not enabled to send access code for respondents")
                                             .build())
                           .build());
    }

}

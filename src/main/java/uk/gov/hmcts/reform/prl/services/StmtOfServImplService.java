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
import uk.gov.hmcts.reform.prl.config.templates.Templates;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.StatementOfServiceWhatWasServed;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.IncrementalInteger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALL_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C9_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMMA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FL415_FILENAME;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PERSONAL_SERVICE_SERVED_BY_BAILIFF;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PERSONAL_SERVICE_SERVED_BY_CA;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PRL_COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
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
    public static final String STMT_OF_SERVICE_FOR_APPLICATION = "stmtOfServiceForApplication";
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final ServiceOfApplicationService serviceOfApplicationService;
    private final AllTabServiceImpl allTabService;

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
                                                                                        .label("All respondents").build())
                                                                             .build())
                                                  .build()));

        log.info("Statement of service dynamic list value:: {}", stmtOfServiceAddRecipient);
        caseDataUpdated.put("stmtOfServiceAddRecipient", stmtOfServiceAddRecipient);
        return caseDataUpdated;
    }

    public Map<String, Object> retrieveAllRespondentNames(CaseDetails caseDetails, String authorisation) {
        Map<String, Object> caseDataUpdateMap = caseDetails.getData();
        CaseData caseData = objectMapper.convertValue(
            caseDetails.getData(),
            CaseData.class
        );

        List<Element<StmtOfServiceAddRecipient>> addRecipientElementList = caseData.getStatementOfService().getStmtOfServiceAddRecipient();
        List<Element<StmtOfServiceAddRecipient>> elementList = new ArrayList<>();
        List<StmtOfServiceAddRecipient> recipients = addRecipientElementList
            .stream()
            .map(Element::getValue)
            .toList();

        for (StmtOfServiceAddRecipient recipient : recipients) {
            if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
                if (ALL_RESPONDENTS.equals(recipient.getRespondentDynamicList().getValue().getLabel())) {
                    List<String> respondentNamesList = caseData
                        .getRespondents()
                        .stream()
                        .map(Element::getValue)
                        .map(PartyDetails::getLabelForDynamicList)
                        .toList();
                    String allRespondentNames = String.join(", ", respondentNamesList).concat(" (All respondents)");
                    recipient = recipient.toBuilder()
                        .respondentDynamicList(null)
                        .selectedPartyId("00000000-0000-0000-0000-000000000000")
                        .selectedPartyName(allRespondentNames)
                        .stmtOfServiceDocument(recipient.getStmtOfServiceDocument())
                        .servedDateTimeOption(recipient.getServedDateTimeOption())
                        .build();
                } else {
                    recipient = recipient.toBuilder()
                        .respondentDynamicList(null)
                        .selectedPartyId(recipient.getRespondentDynamicList().getValue().getCode())
                        .selectedPartyName(recipient.getRespondentDynamicList().getValue().getLabel())
                        .stmtOfServiceDocument(recipient.getStmtOfServiceDocument())
                        .servedDateTimeOption(recipient.getServedDateTimeOption())
                        .build();
                }

            } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                recipient = recipient.toBuilder()
                    .respondentDynamicList(null)
                    .selectedPartyId(recipient.getRespondentDynamicList().getValue().getCode())
                    .selectedPartyName(recipient.getRespondentDynamicList().getValue().getLabel())
                    .stmtOfServiceDocument(recipient.getStmtOfServiceDocument())
                    .servedDateTimeOption(recipient.getServedDateTimeOption())
                    .build();
            }
            if (isNotEmpty(caseData.getServiceOfApplication())
                && isNotEmpty(caseData.getServiceOfApplication().getUnServedRespondentPack())
                && CollectionUtils.isNotEmpty(caseData.getServiceOfApplication().getUnServedRespondentPack().getPackDocument())
                && StatementOfServiceWhatWasServed.statementOfServiceApplicationPack
                .equals(caseData.getStatementOfService()
                            .getStmtOfServiceWhatWasServed())
            ) {
                caseData = cleanupAndServeRespondentPacksPersonalService(caseData, authorisation);
                caseDataUpdateMap.put(
                    "finalServedApplicationDetailsList",
                    caseData.getFinalServedApplicationDetailsList()
                );
                caseDataUpdateMap.put("unServedRespondentPack", null);
            }
            elementList.add(element(recipient));
        }

        caseDataUpdateMap.put(
            STMT_OF_SERVICE_FOR_APPLICATION,
            appendStatementOfServiceToSoaTab(
                caseData,
                elementList
            )
        );
        caseDataUpdateMap.put(
            "stmtOfServiceForOrder",
            appendStatementOfServiceToOrdersTab(
                caseData,
                elementList
            )
        );
        caseDataUpdateMap.put("stmtOfServiceAddRecipient", null);
        caseDataUpdateMap.put("stmtOfServiceWhatWasServed", null);
        return caseDataUpdateMap;
    }

    private List<Element<StmtOfServiceAddRecipient>> appendStatementOfServiceToSoaTab(
        CaseData caseData,
        List<Element<StmtOfServiceAddRecipient>> statementOfServiceListFromCurrentEvent) {

        if (StatementOfServiceWhatWasServed.statementOfServiceApplicationPack
            .equals(caseData.getStatementOfService()
                        .getStmtOfServiceWhatWasServed())) {
            if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForApplication())) {
                statementOfServiceListFromCurrentEvent.addAll(caseData.getStatementOfService().getStmtOfServiceForApplication());
            }
            return statementOfServiceListFromCurrentEvent;
        } else {
            if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForApplication())) {
                return caseData.getStatementOfService().getStmtOfServiceForApplication();
            }
        }
        return Collections.emptyList();
    }

    private List<Element<StmtOfServiceAddRecipient>> appendStatementOfServiceToOrdersTab(
        CaseData caseData,
        List<Element<StmtOfServiceAddRecipient>> statementOfServiceListFromCurrentEvent) {

        if (StatementOfServiceWhatWasServed.statementOfServiceOrder
            .equals(caseData.getStatementOfService()
                        .getStmtOfServiceWhatWasServed())) {
            if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForOrder())) {
                statementOfServiceListFromCurrentEvent.addAll(caseData.getStatementOfService().getStmtOfServiceForOrder());
            }
            return statementOfServiceListFromCurrentEvent;
        } else {
            if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForOrder())) {
                return caseData.getStatementOfService().getStmtOfServiceForOrder();
            }
        }

        return Collections.emptyList();
    }

    private CaseData cleanupAndServeRespondentPacksPersonalService(CaseData caseData, String authorisation) {
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseData.getFinalServedApplicationDetailsList())) {
            finalServedApplicationDetailsList = caseData.getFinalServedApplicationDetailsList();
        }
        finalServedApplicationDetailsList.add(element(checkAndServeRespondentPacksPersonalService(
            caseData,
            authorisation
        )));
        return caseData.toBuilder()
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList)
            .serviceOfApplication(caseData.getServiceOfApplication().toBuilder().unServedRespondentPack(null).build())
            .build();
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

    public ServedApplicationDetails checkAndServeRespondentPacksPersonalService(CaseData caseData, String authorization) {
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
        } else {
            unServedRespondentPack = unServedRespondentPack.toBuilder()
                .packDocument(unServedRespondentPack.getPackDocument()
                                  .stream()
                                  .filter(d -> !C9_DOCUMENT_FILENAME.equalsIgnoreCase(d.getValue().getDocumentFileName()))
                                  .toList())
                .build();
        }
        if (SoaSolicitorServingRespondentsEnum.courtAdmin.toString().equalsIgnoreCase(unServedRespondentPack.getPersonalServiceBy())) {
            emailNotificationDetails.add(element(EmailNotificationDetails.builder()
                                                     .emailAddress(RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_EMAIL)
                                                     .servedParty(PRL_COURT_ADMIN)
                                                     .docs(unServedRespondentPack.getPackDocument())
                                                     .attachedDocs(String.join(",", unServedRespondentPack
                                                         .getPackDocument().stream()
                                                         .map(Element::getValue)
                                                         .map(Document::getDocumentFileName).toList()))
                                                     .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                                                                    .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
                                                     .partyIds(getPartyIds(caseTypeOfApplication,
                                                                           caseData.getRespondents(),
                                                                           caseData.getRespondentsFL401()))
                                                     .build()));
        } else if (SoaSolicitorServingRespondentsEnum.courtBailiff.toString()
            .equalsIgnoreCase(unServedRespondentPack.getPersonalServiceBy())) {
            bulkPrintDetails.add(element(BulkPrintDetails.builder()
                                             .servedParty(PRL_COURT_ADMIN)
                                             .bulkPrintId(RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_POST)
                                             .printedDocs(String.join(",", unServedRespondentPack
                                                 .getPackDocument().stream()
                                                 .map(Element::getValue)
                                                 .map(Document::getDocumentFileName).toList()))
                                             .printDocs(unServedRespondentPack.getPackDocument())
                                             .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                                                            .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
                                             .partyIds(getPartyIds(caseTypeOfApplication,
                                                                   caseData.getRespondents(),
                                                                   caseData.getRespondentsFL401()))
                                             .build()));
        } else if (SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString()
            .equalsIgnoreCase(unServedRespondentPack.getPersonalServiceBy())) {
            List<Element<Document>> packDocs = new ArrayList<>();
            caseData.getRespondents().forEach(respondent -> {
                if (!CaseUtils.hasLegalRepresentation(respondent.getValue())) {
                    packDocs.add(element(serviceOfApplicationService.generateCoverLetterBasedOnCaseAccess(authorization, caseData, respondent,
                                                                                     Templates.PRL_LET_ENG_RE5
                    )));
                }
            });
            packDocs.addAll(unServedRespondentPack.getPackDocument());
            bulkPrintDetails.add(element(BulkPrintDetails.builder()
                                             .servedParty("Applicant Lip")
                                             .bulkPrintId("Respondent will be served personally by Applicant LIP")
                                             .printedDocs(String.join(",", packDocs.stream()
                                                 .map(Element::getValue)
                                                 .map(Document::getDocumentFileName).toList()))
                                             .printDocs(packDocs)
                                             .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                                                            .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
                                             .partyIds(getPartyIds(caseTypeOfApplication,
                                                                   caseData.getRespondents(),
                                                                   caseData.getRespondentsFL401()))
                                             .build()));
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
        log.info("Unserved respondent pack {}", updatedCaseDataMap.get(UN_SERVED_RESPONDENT_PACK));
        if (null != updatedCaseDataMap.get(UN_SERVED_RESPONDENT_PACK)
            && CollectionUtils.isNotEmpty(updatedCaseData.getServiceOfApplication().getUnServedRespondentPack().getPackDocument())) {
            List<Element<StmtOfServiceAddRecipient>> stmtOfServiceforApplication = new ArrayList<>();
            updateStatementOfServiceCollection(sosObject, updatedCaseData, stmtOfServiceforApplication);
            updatedCaseDataMap.put(STMT_OF_SERVICE_FOR_APPLICATION, stmtOfServiceforApplication);
            if (YesOrNo.No.equals(sosObject.getIsOrder())) {
                List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
                List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
                finalServedApplicationDetailsList = updateFinalListOfServedApplications(
                    authorisation,
                    startAllTabsUpdateDataContent.authorisation(),
                    updatedCaseData,
                    bulkPrintDetails
                );
                updatedCaseDataMap.put("finalServedApplicationDetailsList", finalServedApplicationDetailsList);
                updatedCaseDataMap.put(UN_SERVED_RESPONDENT_PACK, null);
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

    private List<Element<ServedApplicationDetails>> updateFinalListOfServedApplications(String authorisation, String authorization,
                                                                                        CaseData updatedCaseData,
                                                                                        List<Element<BulkPrintDetails>> bulkPrintDetails) {
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
        List<Element<Document>> packDocs;
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(updatedCaseData))) {
            packDocs = updatedCaseData.getServiceOfApplication().getUnServedRespondentPack().getPackDocument()
                .stream()
                .filter(d -> !C9_DOCUMENT_FILENAME.equalsIgnoreCase(d.getValue().getDocumentFileName()))
                .toList();
            updatedCaseData.getRespondents().forEach(respondent -> {
                if (!CaseUtils.hasLegalRepresentation(respondent.getValue())) {
                    Document coverLetter = getCoverLetter(authorization, updatedCaseData, respondent,
                                                          updatedCaseData.getApplicants().get(0).getValue().getLabelForDynamicList());

                    serviceOfApplicationService.sendPostWithAccessCodeLetterToParty(
                        updatedCaseData,
                        authorization,
                        new ArrayList<>(),
                        bulkPrintDetails,
                        respondent,
                        coverLetter,
                        respondent.getValue().getLabelForDynamicList());
                }
            });
        } else {
            packDocs = updatedCaseData.getServiceOfApplication().getUnServedRespondentPack().getPackDocument()
                .stream()
                .filter(d -> !SOA_FL415_FILENAME.equalsIgnoreCase(d.getValue().getDocumentFileName()))
                .toList();
            if (!CaseUtils.hasLegalRepresentation(updatedCaseData.getRespondentsFL401())) {
                Document coverLetter = getCoverLetter(authorization, updatedCaseData,
                                                      element(updatedCaseData.getRespondentsFL401().getPartyId(),
                                                                                              updatedCaseData.getRespondentsFL401()),
                                                      updatedCaseData.getApplicantsFL401().getLabelForDynamicList());

                serviceOfApplicationService.sendPostWithAccessCodeLetterToParty(
                    updatedCaseData,
                    authorization,
                    new ArrayList<>(),
                    bulkPrintDetails,
                    element(updatedCaseData.getRespondentsFL401().getPartyId(),
                            updatedCaseData.getRespondentsFL401()),
                    coverLetter,
                    updatedCaseData.getRespondentsFL401().getLabelForDynamicList());
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
                                                          .bulkPrintDetails(bulkPrintDetails).build()));
        return finalServedApplicationDetailsList;
    }

    private Document getCoverLetter(String authorization, CaseData updatedCaseData, Element<PartyDetails> respondent, String applicantName) {
        CaseInvite caseInvite = serviceOfApplicationService.getCaseInvite(respondent.getId(), updatedCaseData.getCaseInvites());
        Map<String, Object> dataMap = serviceOfApplicationService.populateAccessCodeMap(
            updatedCaseData,
            respondent, caseInvite);
        dataMap.put("applicantName", applicantName);
        return serviceOfApplicationService.fetchCoverLetter(authorization, Templates.PRL_LET_ENG_RE7, dataMap);
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat targetFormat = new SimpleDateFormat("dd MMM yyyy");
        try {
            stmtOfServiceforApplication.add(element(StmtOfServiceAddRecipient.builder()
                                                      .citizenPartiesServedDate(targetFormat.format(format.parse(sosObject
                                                                                                                     .getPartiesServedDate())))
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
                                                        .selectedPartyName(String.join(",", partiesServed))
                                                      .build()));
        } catch (ParseException e) {
            log.error("Error while building Sos Object {}", e.getMessage());
        }

        log.info("Statement of service list :: {}", stmtOfServiceforApplication);
        if (ObjectUtils.isNotEmpty(updatedCaseData.getStatementOfService())
                && CollectionUtils.isNotEmpty(updatedCaseData.getStatementOfService().getStmtOfServiceForApplication())) {
            stmtOfServiceforApplication.addAll(updatedCaseData.getStatementOfService().getStmtOfServiceForApplication());
        }
    }
}

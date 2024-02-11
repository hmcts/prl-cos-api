package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.StatementOfServiceWhatWasServed;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StatementOfService;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.IncrementalInteger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALL_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.COURT;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PRL_COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StmtOfServImplService {
    public static final String RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_POST = "Respondent has been served personally by Court,"
        + " hence no bulk print id is generated";
    public static final String RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_EMAIL = "Respondent has been served personally by Court through email";
    private final ObjectMapper objectMapper;
    private final UserService userService;

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

    public CaseData retrieveAllRespondentNames(CaseDetails caseDetails, String authorisation) {
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
                    List<PartyDetails> respondents = caseData
                        .getRespondents()
                        .stream()
                        .map(Element::getValue)
                        .toList();
                    List<String> respondentNamesList = respondents.stream()
                        .map(element -> element.getFirstName() + " " + element.getLastName())
                        .toList();
                    String allRespondentNames = String.join(", ", respondentNamesList);
                    recipient = recipient.toBuilder()
                        .respondentDynamicList(DynamicList.builder()
                                                   .listItems(recipient.getRespondentDynamicList().getListItems())
                                                   .value(DynamicListElement.builder()
                                                              .code(UUID.randomUUID())
                                                              .label(allRespondentNames)
                                                              .build()).build())
                        .stmtOfServiceDocument(recipient.getStmtOfServiceDocument())
                        .servedDateTimeOption(recipient.getServedDateTimeOption())
                        .build();
                } else {
                    recipient = recipient.toBuilder()
                        .respondentDynamicList(DynamicList.builder()
                                                   .listItems(recipient.getRespondentDynamicList().getListItems())
                                                   .value(DynamicListElement.builder()
                                                              .code(recipient.getRespondentDynamicList().getValue().getCode())
                                                              .label(recipient.getRespondentDynamicList().getValue().getLabel())
                                                              .build()).build())
                        .stmtOfServiceDocument(recipient.getStmtOfServiceDocument())
                        .servedDateTimeOption(recipient.getServedDateTimeOption())
                        .build();
                }

            } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                recipient = recipient.toBuilder()
                    .respondentDynamicList(DynamicList.builder()
                                               .listItems(recipient.getRespondentDynamicList().getListItems())
                                               .value(DynamicListElement.builder()
                                                          .label(caseData.getRespondentsFL401().getFirstName()
                                                                     + " " + caseData.getRespondentsFL401().getLastName())
                                                          .build()).build())
                    .stmtOfServiceDocument(recipient.getStmtOfServiceDocument())
                    .servedDateTimeOption(recipient.getServedDateTimeOption())
                    .build();
            }
            if (isNotEmpty(caseData.getServiceOfApplication())
                && isNotEmpty(caseData.getServiceOfApplication().getUnServedRespondentPack())) {
                caseData = cleanupRespondentPacksCaOrBailiffPersonalService(caseData, authorisation);
            }
            elementList.add(element(recipient));
        }

        caseData = caseData.toBuilder()
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(appendStatementOfServiceToSoaTab(
                                        caseData,
                                        elementList
                                    ))
                                    .stmtOfServiceForOrder(appendStatementOfServiceToOrdersTab(
                                        caseData,
                                        elementList
                                    ))
                                    .stmtOfServiceAddRecipient(null)
                                    .build())
            .build();

        return caseData;
    }

    private List<Element<StmtOfServiceAddRecipient>> appendStatementOfServiceToSoaTab(
        CaseData caseData,
        List<Element<StmtOfServiceAddRecipient>> statementOfServiceListFromCurrentEvent) {

        if (caseData.getStatementOfService()
            .getStmtOfServiceWhatWasServed().equals(
                StatementOfServiceWhatWasServed.statementOfServiceApplicationPack)) {
            if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForApplication())) {
                statementOfServiceListFromCurrentEvent.addAll(caseData.getStatementOfService().getStmtOfServiceForApplication());
            }
            return statementOfServiceListFromCurrentEvent;
        } else {
            if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForApplication())) {
                return caseData.getStatementOfService().getStmtOfServiceForApplication();
            }
        }
        return null;
    }

    private List<Element<StmtOfServiceAddRecipient>> appendStatementOfServiceToOrdersTab(
        CaseData caseData,
        List<Element<StmtOfServiceAddRecipient>> statementOfServiceListFromCurrentEvent) {

        if (caseData.getStatementOfService()
            .getStmtOfServiceWhatWasServed().equals(
                StatementOfServiceWhatWasServed.statementOfServiceOrder)) {
            if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForOrder())) {
                statementOfServiceListFromCurrentEvent.addAll(caseData.getStatementOfService().getStmtOfServiceForOrder());
            }
            return statementOfServiceListFromCurrentEvent;
        } else {
            if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForOrder())) {
                return caseData.getStatementOfService().getStmtOfServiceForOrder();
            }
        }

        return null;
    }

    private CaseData cleanupRespondentPacksCaOrBailiffPersonalService(CaseData caseData, String authorisation) {
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList = new ArrayList<>();
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseData.getFinalServedApplicationDetailsList())) {
            finalServedApplicationDetailsList = caseData.getFinalServedApplicationDetailsList();
        }
        String email;
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            email = caseData.getRespondents().stream().map(Element::getValue)
                .map(PartyDetails::getEmail)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(","));

        } else {
            email = caseData.getRespondentsFL401().getEmail();
        }
        finalServedApplicationDetailsList.add(element(checkAndServeRespondentPacksCaOrBailiffPersonalService(
            emailNotificationDetails,
            bulkPrintDetails,
            caseData.getServiceOfApplication().getUnServedRespondentPack(),
            authorisation,
            email
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

    public ServedApplicationDetails checkAndServeRespondentPacksCaOrBailiffPersonalService(
        List<Element<EmailNotificationDetails>> emailNotificationDetails,
                           List<Element<BulkPrintDetails>> bulkPrintDetails,
                           SoaPack unServedRespondentPack,
                           String authorization, String email) {
        if (SoaSolicitorServingRespondentsEnum.courtAdmin.toString().equalsIgnoreCase(unServedRespondentPack.getPersonalServiceBy())) {
            emailNotificationDetails.add(element(EmailNotificationDetails.builder()
                                                     .emailAddress(RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_EMAIL)
                                                     .servedParty(PRL_COURT_ADMIN)
                                                     .docs(unServedRespondentPack.getPackDocument())
                                                     .attachedDocs(String.join(",", unServedRespondentPack
                                                         .getPackDocument().stream()
                                                         .map(Element::getValue)
                                                         .map(Document::getDocumentFileName).toList()))
                                                     .timeStamp(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")
                                                                    .format(ZonedDateTime.now(ZoneId.of("Europe/London"))))
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
                                             .timeStamp(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")
                                                            .format(ZonedDateTime.now(ZoneId.of("Europe/London"))))
                                             .build()));
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        String formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        return ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .servedAt(formatter)
            .modeOfService(CaseUtils.getModeOfService(emailNotificationDetails, bulkPrintDetails))
            .whoIsResponsible(COURT)
            .bulkPrintDetails(bulkPrintDetails).build();
    }
}

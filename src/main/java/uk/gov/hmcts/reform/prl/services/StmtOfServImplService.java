package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.utils.IncrementalInteger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALL_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StmtOfServImplService {
    private final ObjectMapper objectMapper;

    private final ServiceOfApplicationService serviceOfApplicationService;

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

        List<Element<StmtOfServiceAddRecipient>> addRecipientElementList = caseData.getStmtOfServiceAddRecipient();
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
                                               .value(DynamicListElement.builder()
                                                          .label(caseData.getRespondentsFL401().getFirstName()
                                                                     + " " + caseData.getRespondentsFL401().getLastName())
                                                          .build()).build())
                    .stmtOfServiceDocument(recipient.getStmtOfServiceDocument())
                    .servedDateTimeOption(recipient.getServedDateTimeOption())
                    .build();
                if (isNotEmpty(caseData.getServiceOfApplication())
                    && isNotEmpty(caseData.getServiceOfApplication().getUnServedRespondentPack())) {
                    caseData = cleanupDaRespondentPacksCaOrBailiffPersonalService(caseData, authorisation);
                }
            }
            elementList.add(element(recipient));
        }
        caseData = caseData.toBuilder()
            .stmtOfServiceAddRecipient(elementList)
            .build();

        return caseData;
    }

    private CaseData cleanupDaRespondentPacksCaOrBailiffPersonalService(CaseData caseData, String authorisation) {
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList = new ArrayList<>();
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        if (caseData.getFinalServedApplicationDetailsList() != null) {
            finalServedApplicationDetailsList = caseData.getFinalServedApplicationDetailsList();
        }
        finalServedApplicationDetailsList.add(element(serviceOfApplicationService.checkAndServeRespondentPacksCaOrBailiffPersonalService(
            caseData,
            emailNotificationDetails,
            bulkPrintDetails,
            caseData.getServiceOfApplication().getUnServedRespondentPack(),
            authorisation,
            caseData.getRespondentsFL401().getSolicitorEmail()
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


}

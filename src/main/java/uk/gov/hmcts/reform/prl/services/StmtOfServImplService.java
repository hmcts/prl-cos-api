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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.utils.IncrementalInteger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALL_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
public class StmtOfServImplService {

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> retrieveRespondentsList(CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(
            caseDetails.getData(),
            CaseData.class
        );

        Map<String, Object> caseDataUpdated = caseDetails.getData();
        List<Element<StmtOfServiceAddRecipient>> stmtOfServiceAddRecipient = new ArrayList();
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

    public CaseData retrieveAllRespondentNames(CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(
            caseDetails.getData(),
            CaseData.class
        );

        List<Element<StmtOfServiceAddRecipient>> addRecipientElementList = caseData.getStmtOfServiceAddRecipient();
        List<Element<StmtOfServiceAddRecipient>> elementList = new ArrayList<>();
        List<StmtOfServiceAddRecipient> recipients = addRecipientElementList
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        for (StmtOfServiceAddRecipient recipient : recipients) {
            if (ALL_RESPONDENTS.equals(recipient.getRespondentDynamicList().getValue().getLabel())
                && C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
                List<PartyDetails> respondents = caseData
                    .getRespondents()
                    .stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                List<String> respondentNamesList = respondents.stream()
                    .map(element -> element.getFirstName() + " " + element.getLastName())
                    .collect(Collectors.toList());
                String allRespondentNames = String.join(", ", respondentNamesList);
                recipient = recipient.toBuilder()
                    .respondentDynamicList(DynamicList.builder()
                                               .value(DynamicListElement.builder()
                                                          .label(allRespondentNames)
                                                          .build()).build())
                    .stmtOfServiceDocument(recipient.getStmtOfServiceDocument())
                    .servedDateTimeOption(recipient.getServedDateTimeOption())
                    .build();

            } else if (ALL_RESPONDENTS.equals(recipient.getRespondentDynamicList().getValue().getLabel())
                && FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                recipient = recipient.toBuilder()
                    .respondentDynamicList(DynamicList.builder()
                                               .value(DynamicListElement.builder()
                                                          .label(caseData.getRespondentsFL401().getFirstName()
                                                                     + " " + caseData.getRespondentsFL401().getLastName())
                                                          .build()).build())
                    .stmtOfServiceDocument(recipient.getStmtOfServiceDocument())
                    .servedDateTimeOption(recipient.getServedDateTimeOption())
                    .build();
            }
            elementList.add(element(recipient));
        }
        log.info("Statement of service dynamic list value:: {}", elementList);

        caseData = caseData.toBuilder()
            .stmtOfServiceAddRecipient(elementList)
            .build();

        return caseData;
    }

    private List<DynamicListElement> getRespondentsList(CaseData caseData) {
        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        List<DynamicListElement> respondentListItems = new ArrayList<>();
        IncrementalInteger i = new IncrementalInteger(1);
        if (respondents != null) {
            respondents.forEach(respondent -> {
                respondentListItems.add(DynamicListElement.builder().code(respondent.getId().toString())
                                  .label(respondent.getValue().getFirstName() + " "
                                             + respondent.getValue().getLastName()
                                             + " (Respondent " + i.getAndIncrement() + ")").build());

            });
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

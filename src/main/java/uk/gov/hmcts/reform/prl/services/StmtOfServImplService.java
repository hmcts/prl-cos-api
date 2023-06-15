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

@Service
@Slf4j
@RequiredArgsConstructor
public class StmtOfServImplService {

    @Autowired
    private ObjectMapper objectMapper;

    public CaseData retrieveRespondentsList(CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(
            caseDetails.getData(),
            CaseData.class
        );

        caseData = caseData.toBuilder()
            .stmtOfServiceAddRecipient(StmtOfServiceAddRecipient.builder()
                                    .respondentDynamicList(DynamicList.builder()
                                                               .listItems(getRespondentsList(caseData))
                                                               .value(DynamicListElement.builder()
                                                                          .label("All respondents")
                                                                          .build())
                                    .build()).build())
            .build();
        return caseData;

    }

    private DynamicListElement getAllRespondents(CaseData caseData) {

        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        final DynamicListElement[] allRespondents = new DynamicListElement[1];
        IncrementalInteger i = new IncrementalInteger(1);
        if (respondents != null) {
            respondents.forEach(respondent -> {
                allRespondents[0] = DynamicListElement.builder().code(respondent.getId().toString())
                                            .label(respondent.getValue().getFirstName() + " "
                                                       + respondent.getValue().getLastName()
                                                       + " (All respondents)").build();
            });
        } else if (caseData.getRespondentsFL401() != null) {
            String name = caseData.getRespondentsFL401().getFirstName() + " "
                + caseData.getRespondentsFL401().getLastName()
                + " (Respondent)";

            allRespondents[0] = DynamicListElement.builder().code(name).label(name).build();
        }

        return allRespondents[0];
    }

    public List<DynamicListElement> getRespondentsList(CaseData caseData) {
        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        List<DynamicListElement> respondentListItems = new ArrayList<>();
        IncrementalInteger i = new IncrementalInteger(1);
        if (respondents != null) {
            respondents.forEach(respondent -> {
                respondentListItems.add(DynamicListElement.builder().code(respondent.getId().toString())
                                  .label(respondent.getValue().getFirstName() + " "
                                             + respondent.getValue().getLastName()
                                             + " (Respondent " + i.getAndIncrement() + ")").build());
                respondentListItems.add(DynamicListElement.builder().code(respondent.getId().toString())
                                  .label(respondent.getValue().getFirstName() + " "
                                             + respondent.getValue().getLastName()
                                             + " (All respondents)").build());
            });
        } else if (caseData.getRespondentsFL401() != null) {
            String name = caseData.getRespondentsFL401().getFirstName() + " "
                + caseData.getRespondentsFL401().getLastName()
                + " (Respondent)";

            respondentListItems.add(DynamicListElement.builder().code(name).label(name).build());
        }

        return respondentListItems;
    }
}

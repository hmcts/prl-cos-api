package uk.gov.hmcts.reform.prl.services.barrister;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BarristerAllocationService {
    ObjectMapper objectMapper;

    public String getPartyListAsJson(Map<String, Object> caseData) throws JsonProcessingException {
        objectMapper.writer().withDefaultPrettyPrinter();
        return objectMapper.writeValueAsString(getPartyList(caseData));
    }

    private DynamicList getPartyList(Map<String, Object> caseData) {
        List<Element<PartyDetails>> applicantsPartyDetailsList = objectMapper.convertValue(
            caseData.get("applicants"),
            List.class
        );
        List<Element<PartyDetails>> respondentsPartyDetailsList = objectMapper.convertValue(
            caseData.get("respondents"),
            List.class
        );

        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.addAll(getPartyDynamicListElements(applicantsPartyDetailsList));
        listItems.addAll(getPartyDynamicListElements(respondentsPartyDetailsList));
        return  DynamicList.builder().value(null).listItems(listItems).build();
    }

    private static List<DynamicListElement> getPartyDynamicListElements(List<Element<PartyDetails>> partyDetailsList) {
        List<DynamicListElement> applicantsItemsList = new ArrayList<>();
        for (Element element : partyDetailsList) {
            PartyDetails partyDetails = (PartyDetails) element.getValue();
            StringBuilder sb = new StringBuilder();
            sb.append(partyDetails.getFirstName() + " " + partyDetails.getLastName());
            sb.append(" (Applicant) ");
            if (YesNoDontKnow.yes.equals(partyDetails.getDoTheyHaveLegalRepresentation())) {
                sb.append(partyDetails.getRepresentativeFullName() + " ");
                sb.append(partyDetails.getSolicitorOrg().getOrganisationName() + " ");
            }
            DynamicListElement applicantDynamicItem = DynamicListElement.builder()
                .code(partyDetails.getPartyId()).label(sb.toString()).build();
            applicantsItemsList.add(applicantDynamicItem);
        }
        return applicantsItemsList;
    }
}

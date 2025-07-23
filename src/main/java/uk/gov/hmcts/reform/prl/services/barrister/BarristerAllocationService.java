package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BarristerAllocationService {
    public static final String APPLICANT = "Applicant";
    public static final String RESPONDENT = "Respondent";

    public AllocatedBarrister getAllocatedBarrister(CaseData caseData) {
        return AllocatedBarrister.builder().partyList(getSolicitorPartyDynamicList(caseData))
            .build();
    }

    private DynamicList getSolicitorPartyDynamicList(CaseData caseData) {
        List<DynamicListElement> listItems = new ArrayList<>();
        List<Element<PartyDetails>> applicants = caseData.getApplicants();
        if (applicants != null) {
            listItems.addAll(getPartyDynamicListElements(applicants, true));
        }

        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        if (respondents != null) {
            listItems.addAll(getPartyDynamicListElements(respondents, false));
        }

        return  DynamicList.builder().value(null).listItems(listItems).build();
    }

    private static List<DynamicListElement> getPartyDynamicListElements(List<Element<PartyDetails>> partyDetailsList, boolean applicantOrRespondent) {
        List<DynamicListElement> applicantsItemsList = new ArrayList<>();
        for (Element element : partyDetailsList) {
            PartyDetails partyDetails = (PartyDetails) element.getValue();
            if (yes.equals(partyDetails.getDoTheyHaveLegalRepresentation())) {
                String label = String.format("%s %s (%s) %s %s", partyDetails.getFirstName(),
                                             partyDetails.getLastName(),
                                             applicantOrRespondent ? APPLICANT : RESPONDENT,
                                             partyDetails.getRepresentativeFullName(),
                                             partyDetails.getSolicitorOrg().getOrganisationName()
                );

                DynamicListElement applicantDynamicItem = DynamicListElement.builder()
                    .code(partyDetails.getPartyId()).label(label).build();
                applicantsItemsList.add(applicantDynamicItem);
            }
        }
        return applicantsItemsList;
    }

}

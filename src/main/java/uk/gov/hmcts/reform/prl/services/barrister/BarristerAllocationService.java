package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BarristerAllocationService {
    public static final String APPLICANT = "Applicant";
    public static final String RESPONDENT = "Respondent";

    public AllocatedBarrister getAllocatedBarrister(CaseData caseData, UserDetails userDetails) {
        return AllocatedBarrister.builder()
            .partyList(getSolicitorPartyDynamicList(caseData, userDetails))
            .barristerName(null)
            .barristerEmail(null)
            .barristerOrg(Organisation.builder().build())
            .roleItem(null)
            .build();
    }

    private DynamicList getSolicitorPartyDynamicList(CaseData caseData, UserDetails userDetails) {
        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            return getSolicitorPartyDynamicListFL401(caseData, userDetails);
        } else if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getSolicitorPartyDynamicListC100(caseData, userDetails);
        } else {
            throw new RuntimeException("Invalid case type detected for case " + caseData.getId());
        }
    }

    private DynamicList getSolicitorPartyDynamicListC100(CaseData caseData, UserDetails userDetails) {
        List<DynamicListElement> listItems = new ArrayList<>();
        List<Element<PartyDetails>> applicants = caseData.getApplicants();
        if (applicants != null) {
            listItems.addAll(getRelatedPeopleC100(userDetails, applicants, true));
        }

        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        if (respondents != null) {
            listItems.addAll(getRelatedPeopleC100(userDetails, respondents, false));
        }

        return  DynamicList.builder().value(null).listItems(listItems).build();
    }

    private DynamicList getSolicitorPartyDynamicListFL401(CaseData caseData, UserDetails userDetails) {
        List<DynamicListElement> listItems = new ArrayList<>();
        PartyDetails applicant = caseData.getApplicantsFL401();
        if (applicant != null) {
            DynamicListElement dynamicListElement = getRelatedPeopleFL401(userDetails, applicant, true);
            if (dynamicListElement != null) {
                listItems.add(dynamicListElement);
            }
        }

        PartyDetails respondent = caseData.getRespondentsFL401();
        if (respondent != null) {
            DynamicListElement dynamicListElement = getRelatedPeopleFL401(userDetails, respondent, false);
            if (dynamicListElement != null) {
                listItems.add(dynamicListElement);
            }
        }

        return  DynamicList.builder().value(null).listItems(listItems).build();
    }

    private List<DynamicListElement> getRelatedPeopleC100(UserDetails userDetails, List<Element<PartyDetails>> people, Boolean isApplicant) {
        if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
            String solicitorEmail = userDetails.getEmail();
            List<Element<PartyDetails>> relatedPeople = new ArrayList<>();

            for (Element<PartyDetails> person : people) {
                if (person.getValue().getSolicitorEmail().equals(solicitorEmail)) {
                    relatedPeople.add(person);
                }
            }
            return getPartyDynamicListElements(relatedPeople, isApplicant);
        } else {
            return getPartyDynamicListElements(people, isApplicant);
        }
    }

    private DynamicListElement getRelatedPeopleFL401(UserDetails userDetails, PartyDetails person, Boolean isApplicant) {
        if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
            String solicitorEmail = userDetails.getEmail();
            PartyDetails relatedPerson = null;

            if (person.getSolicitorEmail().equals(solicitorEmail)) {
                relatedPerson = person;
            }

            return getPartyDynamicListElement(isApplicant, relatedPerson);
        } else {
            return getPartyDynamicListElement(isApplicant, person);
        }
    }

    private List<DynamicListElement> getPartyDynamicListElements(List<Element<PartyDetails>> partyDetailsList,
                                                                 boolean applicantOrRespondent) {
        List<DynamicListElement> itemsList = new ArrayList<>();
        for (Element<PartyDetails> partyDetailsElement : partyDetailsList) {
            DynamicListElement dynamicListElement = getPartyDynamicListElement(applicantOrRespondent, partyDetailsElement.getValue());
            if (dynamicListElement != null) {
                itemsList.add(dynamicListElement);
            }
        }
        return itemsList;
    }

    private DynamicListElement getPartyDynamicListElement(boolean applicantOrRespondent, PartyDetails partyDetails) {
        if (isPartyApplicableToAdd(partyDetails, applicantOrRespondent)) {
            String label = String.format("%s %s (%s), %s, %s", partyDetails.getFirstName(),
                                         partyDetails.getLastName(),
                                         applicantOrRespondent ? APPLICANT : RESPONDENT,
                                         partyDetails.getRepresentativeFullName(),
                                         partyDetails.getSolicitorOrg().getOrganisationName()
            );

            DynamicListElement applicantDynamicItem = DynamicListElement.builder()
                .code(partyDetails.getPartyId()).label(label).build();
            return applicantDynamicItem;
        }
        return null;
    }

    private static boolean isPartyApplicableToAdd(PartyDetails partyDetails, boolean applicantOrRespondent) {
        return (applicantOrRespondent && partyDetails.getSolicitorPartyId() != null)
            || (!applicantOrRespondent && yes.equals(partyDetails.getDoTheyHaveLegalRepresentation()));
    }

}

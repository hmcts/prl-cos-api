package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.OrganisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BarristerAllocationService {
    public static final String APPLICANT = "Applicant";
    public static final String RESPONDENT = "Respondent";
    private final OrganisationService organisationService;

    public AllocatedBarrister getAllocatedBarrister(CaseData caseData, UserDetails userDetails, String authorisation) {
        return AllocatedBarrister.builder()
            .partyList(getSolicitorPartyDynamicList(caseData, userDetails, authorisation))
            .barristerName(null)
            .barristerEmail(null)
            .barristerOrg(Organisation.builder().build())
            .roleItem(null)
            .build();
    }

    private DynamicList getSolicitorPartyDynamicList(CaseData caseData, UserDetails userDetails, String authorisation) {
        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            return getSolicitorPartyDynamicListFL401(caseData, userDetails, authorisation);
        } else if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getSolicitorPartyDynamicListC100(caseData, userDetails, authorisation);
        } else {
            throw new RuntimeException("Invalid case type detected for case " + caseData.getId());
        }
    }

    private DynamicList getSolicitorPartyDynamicListC100(CaseData caseData, UserDetails userDetails, String authorisation) {
        List<DynamicListElement> listItems = new ArrayList<>();
        List<Element<PartyDetails>> applicants = caseData.getApplicants();
        if (applicants != null) {
            log.info("*** Applicants found, getting related people for {}", applicants);
            listItems.addAll(getRelatedPeopleC100(userDetails, applicants, true, authorisation));
        }

        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        if (respondents != null) {
            log.info("*** Respondents found, getting related people for {}", respondents);
            listItems.addAll(getRelatedPeopleC100(userDetails, respondents, false, authorisation));
        }

        return  DynamicList.builder().value(null).listItems(listItems).build();
    }

    private DynamicList getSolicitorPartyDynamicListFL401(CaseData caseData, UserDetails userDetails, String authorisation) {
        List<DynamicListElement> listItems = new ArrayList<>();
        PartyDetails applicant = caseData.getApplicantsFL401();
        if (applicant != null) {
            log.info("*** Applicant found, getting related people for {}", applicant);
            DynamicListElement dynamicListElement = getRelatedPeopleFL401(userDetails, applicant, true, authorisation);
            if (dynamicListElement != null) {
                listItems.add(dynamicListElement);
            }
        }

        PartyDetails respondent = caseData.getRespondentsFL401();
        if (respondent != null) {
            log.info("*** Respondent found, getting related people for {}", respondent);
            DynamicListElement dynamicListElement = getRelatedPeopleFL401(userDetails, respondent, false, authorisation);
            if (dynamicListElement != null) {
                listItems.add(dynamicListElement);
            }
        }

        return  DynamicList.builder().value(null).listItems(listItems).build();
    }

    private List<DynamicListElement> getRelatedPeopleC100(UserDetails userDetails, List<Element<PartyDetails>> people,
                                                          Boolean isApplicant, String usersAuthorisation) {
        if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
            Optional<Organisations> usersOrganisation = organisationService.findUserOrganisation(usersAuthorisation);
            List<Element<PartyDetails>> relatedPeople = new ArrayList<>();

            for (Element<PartyDetails> person : people) {
                if (isSameOrganisation(person.getValue(), usersOrganisation)) {
                    relatedPeople.add(person);
                }
            }
            log.info("*** Found these related people {}", relatedPeople);
            return getPartyDynamicListElements(relatedPeople, isApplicant);
        } else {
            log.info("*** Returning all people for caseworker");
            return getPartyDynamicListElements(people, isApplicant);
        }
    }

    private DynamicListElement getRelatedPeopleFL401(UserDetails userDetails, PartyDetails person, Boolean isApplicant, String usersAuthorisation) {
        if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
            Optional<Organisations> usersOrganisation = organisationService.findUserOrganisation(usersAuthorisation);
            PartyDetails relatedPerson = null;

            if (isSameOrganisation(person, usersOrganisation)) {
                relatedPerson = person;
            }
            log.info("*** Found this related person {}", person);
            return getPartyDynamicListElement(isApplicant, relatedPerson);
        } else {
            log.info("*** Returning all for caseworker");
            return getPartyDynamicListElement(isApplicant, person);
        }
    }

    private boolean isSameOrganisation(PartyDetails person, Optional<Organisations> usersOrganisation) {
        return usersOrganisation.isPresent()
            && usersOrganisation.get().getOrganisationIdentifier().equals(person.getSolicitorOrg().getOrganisationID());
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

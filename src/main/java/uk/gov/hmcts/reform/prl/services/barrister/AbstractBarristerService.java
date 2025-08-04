package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.OrganisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Slf4j
public abstract class AbstractBarristerService {
    protected static final String APPLICANT = "Applicant";
    protected static final String RESPONDENT = "Respondent";
    protected final OrganisationService organisationService;

    protected AbstractBarristerService(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    protected DynamicListElement getPartyDynamicListElement(boolean applicantOrRespondent, Element<PartyDetails> partyDetailsElement) {
        if (isPartyApplicable(applicantOrRespondent, partyDetailsElement.getValue())) {
            String label = getLabelForAction(applicantOrRespondent, partyDetailsElement.getValue());

            DynamicListElement applicantDynamicItem = DynamicListElement.builder()
                .code(getCodeForAction(partyDetailsElement))
                .label(label).build();
            return applicantDynamicItem;
        } else {
            return null;
        }
    }

    protected DynamicList getSolicitorPartyDynamicListC100(CaseData caseData, UserDetails userDetails, String authorisation) {
        List<DynamicListElement> listItems = new ArrayList<>();
        List<Element<PartyDetails>> applicants = caseData.getApplicants();
        if (applicants != null) {
            listItems.addAll(getRelatedPeopleC100(userDetails, applicants, true, authorisation));
        }

        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        if (respondents != null) {
            listItems.addAll(getRelatedPeopleC100(userDetails, respondents, false, authorisation));
        }
        log.info("Returning these dynamic list items (c100): {}", listItems);
        return  DynamicList.builder().value(null).listItems(listItems).build();
    }

    private List<DynamicListElement> getRelatedPeopleC100(UserDetails userDetails, List<Element<PartyDetails>> people,
                                                          Boolean isApplicant, String usersAuthorisation) {
        if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
            Optional<Organisations> usersOrganisation = organisationService.findUserOrganisation(usersAuthorisation);
            log.info("User organisation (c100): {}", usersOrganisation);
            List<Element<PartyDetails>> relatedPeople = new ArrayList<>();

            for (Element<PartyDetails> person : people) {
                log.info("Party ID for person (c100): {}", person.getValue().getPartyId());
                if (isSameOrganisation(person.getValue(), usersOrganisation)) {
                    relatedPeople.add(person);
                }
            }
            log.info("Related people found: {}", relatedPeople);
            return getPartyDynamicListElements(relatedPeople, isApplicant);
        } else {
            return getPartyDynamicListElements(people, isApplicant);
        }
    }

    protected DynamicList getSolicitorPartyDynamicListFL401(CaseData caseData, UserDetails userDetails, String authorisation) {
        List<DynamicListElement> listItems = new ArrayList<>();
        PartyDetails applicant = caseData.getApplicantsFL401();
        checkAndAddPartyToListFL401(listItems, applicant, true, userDetails, authorisation);

        PartyDetails respondent = caseData.getRespondentsFL401();
        checkAndAddPartyToListFL401(listItems, respondent, false, userDetails, authorisation);

        return  DynamicList.builder().value(null).listItems(listItems).build();
    }

    private DynamicListElement getRelatedPeopleFL401(UserDetails userDetails, PartyDetails person, Boolean isApplicant, String usersAuthorisation) {
        if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
            Optional<Organisations> usersOrganisation = organisationService.findUserOrganisation(usersAuthorisation);
            log.info("User organisation: {}", usersOrganisation);
            log.info("Party ID for person: {}", person.getPartyId());

            PartyDetails relatedPerson = null;

            if (isSameOrganisation(person, usersOrganisation)) {
                relatedPerson = person;
            }
            log.info("Related person found: {}", relatedPerson);
            //because the partyId on the PartyDetails is not actually being filled!
            if (relatedPerson != null) {
                Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder()
                    .id(relatedPerson.getPartyId())
                    .value(relatedPerson)
                    .build();
                return getPartyDynamicListElement(isApplicant, partyDetailsElement);
            }
            return null;
        } else {
            Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder()
                .id(person.getPartyId())
                .value(person)
                .build();
            return getPartyDynamicListElement(isApplicant, partyDetailsElement);
        }
    }

    private void checkAndAddPartyToListFL401(List<DynamicListElement> listToAddTo, PartyDetails party, boolean appOrResp,
                                        UserDetails userDetails, String authorisation) {
        if (party != null) {
            DynamicListElement dynamicListElement = getRelatedPeopleFL401(userDetails, party, appOrResp, authorisation);
            if (dynamicListElement != null) {
                listToAddTo.add(dynamicListElement);
            }
        }
    }

    private boolean isSameOrganisation(PartyDetails person, Optional<Organisations> usersOrganisation) {
        log.info("Party solicitor organisation ID: {}", person.getSolicitorOrg().getOrganisationID());

        return usersOrganisation.isPresent()
            && usersOrganisation.get().getOrganisationIdentifier().equals(person.getSolicitorOrg().getOrganisationID());
    }

    protected List<DynamicListElement> getPartyDynamicListElements(List<Element<PartyDetails>> partyDetailsList,
                                                                 boolean applicantOrRespondent) {
        List<DynamicListElement> itemsList = new ArrayList<>();
        for (Element<PartyDetails> partyDetailsElement : partyDetailsList) {
            DynamicListElement dynamicListElement = getPartyDynamicListElement(applicantOrRespondent, partyDetailsElement);
            if (dynamicListElement != null) {
                itemsList.add(dynamicListElement);
            }
        }
        return itemsList;
    }

    protected DynamicList getSolicitorPartyDynamicList(CaseData caseData, UserDetails userDetails, String authorisation) {
        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            return getSolicitorPartyDynamicListFL401(caseData, userDetails, authorisation);
        } else if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getSolicitorPartyDynamicListC100(caseData, userDetails, authorisation);
        } else {
            throw new RuntimeException("Invalid case type detected for case " + caseData.getId());
        }
    }

    protected boolean hasBarrister(PartyDetails partyDetails) {
        return  (partyDetails.getBarrister() != null && partyDetails.getBarrister().getBarristerId() != null);
    }

    protected abstract boolean isPartyApplicable(boolean applicantOrRespondent, PartyDetails partyDetails);

    protected abstract String getLabelForAction(boolean applicantOrRespondent, PartyDetails partyDetails);

    protected abstract String getCodeForAction(Element<PartyDetails> partyDetailsElement);
}

package uk.gov.hmcts.reform.prl.services.barrister;

import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

public abstract class AbstractBarristerService {
    protected static final String APPLICANT = "Applicant";
    protected static final String RESPONDENT = "Respondent";
    private final UserService userService;
    private final OrganisationService organisationService;

    protected AbstractBarristerService(UserService userService, OrganisationService organisationService) {
        this.userService = userService;
        this.organisationService = organisationService;
    }

    protected DynamicList getPartiesToList(CaseData caseData, String authorisation) {
        return getPartiesToListForC100OrFL401(caseData, populateBarristerFilter(caseData, authorisation));
    }

    protected boolean hasBarrister(PartyDetails partyDetails) {
        return (partyDetails.getBarrister() != null && partyDetails.getBarrister().getBarristerId() != null);
    }

    private BarristerFilter populateBarristerFilter(CaseData caseData, String authorisation) {
        return BarristerFilter.builder()
            .userOrgIdentifier(getUserOrgId(authorisation))
            .caseworkerOrSolicitor(!isSolicitor(authorisation))
            .caseTypeC100OrFL401(isC100CaseType(caseData))
            .build();

    }

    private String getUserOrgId(String usersAuthorisation) {

        Optional<Organisations> usersOrganisation = organisationService.findUserOrganisation(usersAuthorisation);
        if (usersOrganisation.isPresent()) {
            return usersOrganisation.get().getOrganisationIdentifier();
        }
        return null;
    }

    private boolean isSolicitor(String authorisation) {
        List<String> roles = userService.getUserDetails(authorisation).getRoles();
        return (roles.contains(Roles.SOLICITOR.getValue()));
    }

    private DynamicList getPartiesToListForC100OrFL401(CaseData caseData, BarristerFilter barristerFilter) {
        if (barristerFilter.isCaseTypeC100OrFL401()) {
            return getPartiesToListForC100(caseData, barristerFilter);
        } else {
            return getPartiesToListForFL401(caseData, barristerFilter);
        }
    }


    private DynamicList getPartiesToListForC100(CaseData caseData, BarristerFilter barristerFilter) {
        List<DynamicListElement> listItems = new ArrayList<>();
        List<Element<PartyDetails>> applicants = caseData.getApplicants();
        if (applicants != null) {
            listItems.addAll(getPartyDynamicListElements(applicants, true, barristerFilter));
        }

        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        if (respondents != null) {
            listItems.addAll(getPartyDynamicListElements(respondents, false, barristerFilter));
        }

        return DynamicList.builder().value(null).listItems(listItems).build();
    }

    private DynamicList getPartiesToListForFL401(CaseData caseData, BarristerFilter barristerFilter) {
        List<DynamicListElement> listItems = new ArrayList<>();
        PartyDetails applicant = caseData.getApplicantsFL401();
        checkAndAddPartyToListFL401(true, listItems, applicant, barristerFilter);

        PartyDetails respondent = caseData.getRespondentsFL401();
        checkAndAddPartyToListFL401(false, listItems, respondent, barristerFilter);

        return DynamicList.builder().value(null).listItems(listItems).build();
    }

    private void checkAndAddPartyToListFL401(boolean applicantOrRespondent, List<DynamicListElement> listToAddTo,
                                             PartyDetails party, BarristerFilter barristerFilter) {
        if (party != null) {
            //because the partyId on the PartyDetails is not actually being filled!
            Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder()
                .id(party.getPartyId())
                .value(party)
                .build();
            DynamicListElement dynamicListElement = getPartyDynamicListElement(
                applicantOrRespondent,
                barristerFilter, partyDetailsElement
            );
            if (dynamicListElement != null) {
                listToAddTo.add(dynamicListElement);
            }
        }
    }

    private List<DynamicListElement> getPartyDynamicListElements(List<Element<PartyDetails>> partyDetailsList,
                                                                 boolean applicantOrRespondent, BarristerFilter barristerFilter) {
        List<DynamicListElement> itemsList = new ArrayList<>();
        for (Element<PartyDetails> partyDetailsElement : partyDetailsList) {
            DynamicListElement dynamicListElement = getPartyDynamicListElement(
                applicantOrRespondent,
                barristerFilter,
                partyDetailsElement
            );
            if (dynamicListElement != null) {
                itemsList.add(dynamicListElement);
            }
        }
        return itemsList;
    }

    private DynamicListElement getPartyDynamicListElement(boolean applicantOrRespondent,
                                                          BarristerFilter barristerFilter,
                                                          Element<PartyDetails> partyDetailsElement) {
        if (isPartyApplicableForFiltering(applicantOrRespondent, barristerFilter, partyDetailsElement.getValue())) {
            String label = getLabelForAction(applicantOrRespondent, barristerFilter, partyDetailsElement.getValue());

            DynamicListElement applicantDynamicItem = DynamicListElement.builder()
                .code(getCodeForAction(partyDetailsElement))
                .label(label).build();
            return applicantDynamicItem;
        } else {
            return null;
        }
    }

    private boolean isC100CaseType(CaseData caseData) {
        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            return false;
        } else if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return true;
        } else {
            throw new RuntimeException("Invalid case type detected for case " + caseData.getId());
        }
    }

    protected abstract boolean isPartyApplicableForFiltering(boolean applicantOrRespondent, BarristerFilter barristerFilter,
                                                             PartyDetails partyDetails);

    protected abstract String getLabelForAction(boolean applicantOrRespondent, BarristerFilter barristerFilter, PartyDetails partyDetails);

    protected abstract String getCodeForAction(Element<PartyDetails> partyDetailsElement);
}

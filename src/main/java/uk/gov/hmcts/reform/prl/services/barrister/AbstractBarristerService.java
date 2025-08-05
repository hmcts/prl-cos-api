package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
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
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.applicant;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.respondent;

@Slf4j
public abstract class AbstractBarristerService {
    protected final OrganisationService organisationService;

    protected AbstractBarristerService(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    protected DynamicListElement getPartyDynamicListElement(boolean isApplicant, Element<PartyDetails> partyDetailsElement) {
        if (isPartyApplicable(isApplicant, partyDetailsElement.getValue())) {
            String label = getLabelForAction(isApplicant, partyDetailsElement.getValue());

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
            listItems.addAll(getPartiesWithSameOrganisation(userDetails, applicants, applicant, authorisation));
        }

        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        if (respondents != null) {
            listItems.addAll(getPartiesWithSameOrganisation(userDetails, respondents, respondent, authorisation));
        }
        log.info("Returning these dynamic list items (c100): {}", listItems);
        return  DynamicList.builder().value(null).listItems(listItems).build();
    }

    private List<DynamicListElement> getPartiesWithSameOrganisation(UserDetails userDetails, List<Element<PartyDetails>> partyDetailsList,
                                                          PartyEnum partyEnum, String usersAuthorisation) {
        boolean isApplicant = partyEnum == applicant;
        if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
            Optional<Organisations> usersOrganisation = organisationService.findUserOrganisation(usersAuthorisation);
            log.info("User organisation (c100): {}", usersOrganisation);
            List<Element<PartyDetails>> partyDetailsListWithSameOrg = new ArrayList<>();

            for (Element<PartyDetails> partyDetailsElement : partyDetailsList) {
                log.info("Party ID for partyDetailsElement (c100): {}", partyDetailsElement.getValue().getPartyId());
                if (isSameOrganisation(partyDetailsElement.getValue(), usersOrganisation)) {
                    partyDetailsListWithSameOrg.add(partyDetailsElement);
                }
            }
            log.info("Parties with the same org: {}", partyDetailsListWithSameOrg);
            return getPartyDynamicListElements(partyDetailsListWithSameOrg, isApplicant);
        } else {
            return getPartyDynamicListElements(partyDetailsList, isApplicant);
        }
    }

    protected DynamicList getSolicitorPartyDynamicListFL401(CaseData caseData, UserDetails userDetails, String authorisation) {
        List<DynamicListElement> listItems = new ArrayList<>();
        PartyDetails applicantPartyDetails = caseData.getApplicantsFL401();
        checkAndAddPartyToListFL401(listItems, applicantPartyDetails, applicant, userDetails, authorisation);

        PartyDetails respondentPartyDetails = caseData.getRespondentsFL401();
        checkAndAddPartyToListFL401(listItems, respondentPartyDetails, respondent, userDetails, authorisation);

        return  DynamicList.builder().value(null).listItems(listItems).build();
    }

    private DynamicListElement getPartiesWithSameOrganisationFL401(UserDetails userDetails, PartyDetails partyDetails,
                                                                      Boolean isApplicant, String usersAuthorisation) {
        if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
            Optional<Organisations> usersOrganisation = organisationService.findUserOrganisation(usersAuthorisation);
            log.info("User organisation: {}", usersOrganisation);
            log.info("Party ID for party details: {}", partyDetails.getPartyId());

            PartyDetails partyDetailsWithSameOrg = null;

            if (isSameOrganisation(partyDetails, usersOrganisation)) {
                partyDetailsWithSameOrg = partyDetails;
            }
            log.info("Party Details with the same org: {}", partyDetailsWithSameOrg);
            if (partyDetailsWithSameOrg != null) {
                return getPartyDynamicListElement(isApplicant, buildPartyDetailsElement(partyDetailsWithSameOrg));
            } else {
                return null;
            }
        } else {
            return getPartyDynamicListElement(isApplicant, buildPartyDetailsElement(partyDetails));
        }
    }

    private Element<PartyDetails> buildPartyDetailsElement(PartyDetails partyDetails) {
        return Element.<PartyDetails>builder()
            .id(partyDetails.getPartyId())
            .value(partyDetails)
            .build();
    }

    private void checkAndAddPartyToListFL401(List<DynamicListElement> listToAddTo, PartyDetails partyDetails, PartyEnum partyEnum,
                                        UserDetails userDetails, String authorisation) {
        boolean isApplicant = partyEnum == applicant;
        if (partyDetails != null) {
            DynamicListElement dynamicListElement = getPartiesWithSameOrganisationFL401(userDetails, partyDetails, isApplicant, authorisation);
            if (dynamicListElement != null) {
                listToAddTo.add(dynamicListElement);
            }
        }
    }

    private boolean isSameOrganisation(PartyDetails partyDetails, Optional<Organisations> usersOrganisation) {
        return usersOrganisation.isPresent() && partyDetails.getSolicitorOrg() != null
            && usersOrganisation.get().getOrganisationIdentifier().equals(partyDetails.getSolicitorOrg().getOrganisationID());
    }

    protected List<DynamicListElement> getPartyDynamicListElements(List<Element<PartyDetails>> partyDetailsList,
                                                                 boolean isApplicant) {
        List<DynamicListElement> itemsList = new ArrayList<>();
        for (Element<PartyDetails> partyDetailsElement : partyDetailsList) {
            DynamicListElement dynamicListElement = getPartyDynamicListElement(isApplicant, partyDetailsElement);
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

    protected abstract boolean isPartyApplicable(boolean isApplicant, PartyDetails partyDetails);

    protected abstract String getLabelForAction(boolean isApplicant, PartyDetails partyDetails);

    protected abstract String getCodeForAction(Element<PartyDetails> partyDetailsElement);
}

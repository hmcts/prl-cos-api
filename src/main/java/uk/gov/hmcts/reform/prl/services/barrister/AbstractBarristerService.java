package uk.gov.hmcts.reform.prl.services.barrister;

import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.barrister.TypeOfBarristerEventEnum;
import uk.gov.hmcts.reform.prl.events.BarristerChangeEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.applicant;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.respondent;

public abstract class AbstractBarristerService {
    protected static final String APPLICANT = "Applicant";
    protected static final String RESPONDENT = "Respondent";
    private final UserService userService;
    private final OrganisationService organisationService;
    private final EventService eventPublisher;

    protected AbstractBarristerService(UserService userService,
                                       OrganisationService organisationService,
                                       EventService eventPublisher) {
        this.userService = userService;
        this.organisationService = organisationService;
        this.eventPublisher = eventPublisher;
    }

    protected DynamicList getPartiesToList(CaseData caseData, String authorisation) {
        return getPartiesToListForC100OrFL401(caseData, populateBarristerFilter(caseData, authorisation));
    }

    protected boolean hasBarrister(PartyDetails partyDetails) {
        return (partyDetails.getBarrister() != null && partyDetails.getBarrister().getBarristerId() != null);
    }

    protected boolean partyHasSolicitorOrg(PartyDetails partyDetails) {
        return (partyDetails.getSolicitorOrg() != null && partyDetails.getSolicitorOrg().getOrganisationID() != null
            && partyDetails.getSolicitorOrg().getOrganisationName() != null);
    }

    private BarristerFilter populateBarristerFilter(CaseData caseData, String authorisation) {
        boolean isSolicitor = isSolicitor(authorisation);
        return BarristerFilter.builder()
            .userOrgIdentifier(isSolicitor ? getUserOrgId(authorisation) : null)
            .caseworkerOrSolicitor(!isSolicitor)
            .caseTypeC100OrFL401(isC100CaseType(caseData))
            .build();

    }

    protected BarristerChangeEvent prepareAndPublishBarristerChangeEvent(AllocatedBarrister allocatedBarrister,
                                                              CaseData newCaseData,
                                                              TypeOfBarristerEventEnum typeOfEvent) {
        if (allocatedBarrister != null) {
            BarristerChangeEvent barristerChangeEvent = BarristerChangeEvent.builder()
                .caseData(newCaseData)
                .allocatedBarrister(allocatedBarrister)
                .typeOfEvent(typeOfEvent)
                .build();
            eventPublisher.publishEvent(barristerChangeEvent);
        }
        return null;
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
            listItems.addAll(getPartyDynamicListElements(applicants, applicant, barristerFilter));
        }

        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        if (respondents != null) {
            listItems.addAll(getPartyDynamicListElements(respondents, respondent, barristerFilter));
        }

        return DynamicList.builder().value(null).listItems(listItems).build();
    }

    private DynamicList getPartiesToListForFL401(CaseData caseData, BarristerFilter barristerFilter) {
        List<DynamicListElement> listItems = new ArrayList<>();
        PartyDetails applicantPartyDetails = caseData.getApplicantsFL401();
        checkAndAddPartyToListFL401(applicant, listItems, applicantPartyDetails, barristerFilter);

        PartyDetails respondentPartyDetails = caseData.getRespondentsFL401();
        checkAndAddPartyToListFL401(respondent, listItems, respondentPartyDetails, barristerFilter);

        return DynamicList.builder().value(null).listItems(listItems).build();
    }

    private void checkAndAddPartyToListFL401(PartyEnum partyEnum, List<DynamicListElement> listToAddTo,
                                             PartyDetails party, BarristerFilter barristerFilter) {
        if (party != null) {
            boolean isApplicant = partyEnum == applicant;
            //because the partyId on the PartyDetails is not actually being filled!
            Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder()
                .id(party.getPartyId())
                .value(party)
                .build();
            DynamicListElement dynamicListElement = getPartyDynamicListElement(
                isApplicant,
                barristerFilter, partyDetailsElement
            );
            if (dynamicListElement != null) {
                listToAddTo.add(dynamicListElement);
            }
        }
    }

    private List<DynamicListElement> getPartyDynamicListElements(List<Element<PartyDetails>> partyDetailsList,
                                                                 PartyEnum partyEnum, BarristerFilter barristerFilter) {
        boolean isApplicant = partyEnum == applicant;
        List<DynamicListElement> itemsList = new ArrayList<>();
        for (Element<PartyDetails> partyDetailsElement : partyDetailsList) {
            DynamicListElement dynamicListElement = getPartyDynamicListElement(
                isApplicant,
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

    public abstract void notifyBarrister(AllocatedBarrister allocatedBarrister, CaseData caseData);
}

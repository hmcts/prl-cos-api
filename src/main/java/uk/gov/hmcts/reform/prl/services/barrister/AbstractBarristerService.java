package uk.gov.hmcts.reform.prl.services.barrister;

import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

public abstract class AbstractBarristerService {
    protected static final String APPLICANT = "Applicant";
    protected static final String RESPONDENT = "Respondent";

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

    protected DynamicList getSolicitorPartyDynamicListC100(CaseData caseData) {
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

    protected DynamicList getSolicitorPartyDynamicListFL401(CaseData caseData) {
        List<DynamicListElement> listItems = new ArrayList<>();
        PartyDetails applicant = caseData.getApplicantsFL401();
        checkAndAddPartyToListFL401(listItems, applicant, true);

        PartyDetails respondent = caseData.getRespondentsFL401();
        checkAndAddPartyToListFL401(listItems, respondent, false);

        return  DynamicList.builder().value(null).listItems(listItems).build();
    }

    private void checkAndAddPartyToListFL401(List<DynamicListElement> listToAddTo, PartyDetails party, boolean appOrResp) {
        if (party != null) {
            //because the partyId on the PartyDetails is not actually being filled!
            Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder()
                .id(party.getPartyId())
                .value(party)
                .build();
            DynamicListElement dynamicListElement = getPartyDynamicListElement(appOrResp, partyDetailsElement);
            if (dynamicListElement != null) {
                listToAddTo.add(dynamicListElement);
            }
        }
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

    protected DynamicList getSolicitorPartyDynamicList(CaseData caseData) {
        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            return getSolicitorPartyDynamicListFL401(caseData);
        } else if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getSolicitorPartyDynamicListC100(caseData);
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

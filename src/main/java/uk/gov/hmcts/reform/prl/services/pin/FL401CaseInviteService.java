package uk.gov.hmcts.reform.prl.services.pin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
public class FL401CaseInviteService implements CaseInviteService {

    @Autowired
    CaseInviteEmailService caseInviteEmailService;

    @Autowired
    private LaunchDarklyClient launchDarklyClient;


    private CaseInvite generateCaseInvite(PartyDetails partyDetails) {
        //no party id required as fl401 cases have only a single respondent
        return new CaseInvite().generateAccessCode(partyDetails.getEmail(), null);
    }

    private void sendCaseInvite(CaseInvite caseInvite, PartyDetails partyDetails, CaseData caseData) {
        caseInviteEmailService.sendCaseInviteEmail(caseInvite, partyDetails, caseData);
    }

    @Override
    public CaseData generateAndSendRespondentCaseInvite(CaseData caseData) {
        PartyDetails respondent = caseData.getRespondentsFL401();
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites() != null ? caseData.getCaseInvites() : new ArrayList<>();

        if (!respondentHasLegalRepresentation(respondent) && Yes.equals(respondent.getCanYouProvideEmailAddress())) {
            CaseInvite caseInvite = generateCaseInvite(respondent);
            caseInvites.add(element(caseInvite));
            sendCaseInvite(caseInvite, respondent, caseData);
        }

        if (launchDarklyClient.isFeatureEnabled("generate-da-citizen-applicant-pin")) {
            PartyDetails applicant = caseData.getApplicantsFL401();
            CaseInvite caseInvite = generateCaseInvite(applicant);
            caseInvites.add(element(caseInvite));
            sendCaseInvite(caseInvite, applicant, caseData);
        }
        return caseData.toBuilder().caseInvites(caseInvites).build();
    }

    public boolean respondentHasLegalRepresentation(PartyDetails partyDetails) {
        return yes.equals(partyDetails.getDoTheyHaveLegalRepresentation());
    }
}

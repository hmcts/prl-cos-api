package uk.gov.hmcts.reform.prl.services.pin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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


    private CaseInvite generateRespondentCaseInvite(PartyDetails partyDetails) {
        //no party id required as fl401 cases have only a single respondent
        return new CaseInvite().generateAccessCode(partyDetails.getEmail(), null);
    }

    private void sendCaseInvite(CaseInvite caseInvite, PartyDetails partyDetails, CaseData caseData) {
        caseInviteEmailService.sendCaseInviteEmail(caseInvite, partyDetails, caseData);
    }

    @Override
    public CaseData generateAndSendRespondentCaseInvite(CaseData caseData) {
        PartyDetails respondent = caseData.getRespondentsFL401();
        List<Element<CaseInvite>> caseInvites = caseData.getRespondentCaseInvites() != null ? caseData.getRespondentCaseInvites() : new ArrayList<>();

        if (!respondentHasLegalRepresentation(respondent) && Yes.equals(respondent.getCanYouProvideEmailAddress())) {
            CaseInvite caseInvite = generateRespondentCaseInvite(respondent);
            caseInvites.add(element(caseInvite));
            sendCaseInvite(caseInvite, respondent, caseData);
        }
        return caseData.toBuilder().respondentCaseInvites(caseInvites).build();
    }

    public boolean respondentHasLegalRepresentation(PartyDetails partyDetails) {
        return yes.equals(partyDetails.getDoTheyHaveLegalRepresentation());
    }
}

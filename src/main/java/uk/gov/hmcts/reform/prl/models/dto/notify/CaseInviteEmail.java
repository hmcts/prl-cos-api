package uk.gov.hmcts.reform.prl.models.dto.notify;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

@Getter
public class CaseInviteEmail extends EmailTemplateVars {

    private final String accessCode;
    private final String respondentFullName;

    @Builder
    public CaseInviteEmail(CaseInvite caseInvite, String caseReference, PartyDetails party) {
        super(caseReference);
        this.accessCode = caseInvite.getAccessCode();
        this.respondentFullName = String.format("%s %s", party.getFirstName(), party.getLastName());
    }
}

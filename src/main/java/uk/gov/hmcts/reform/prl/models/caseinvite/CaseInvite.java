package uk.gov.hmcts.reform.prl.models.caseinvite;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.utils.AccessCodeGenerator;

import java.util.UUID;


@Data
public class CaseInvite {

    private UUID partyId;
    private String caseInviteEmail;
    private String accessCode;
    private String invitedUserId;

    @Builder()
    public CaseInvite() {}

    public CaseInvite(String caseInviteEmail, String accessCode, String invitedUserId, UUID partyId) {
        this.accessCode = accessCode;
        this.caseInviteEmail = caseInviteEmail;
        this.invitedUserId = invitedUserId;
        this.partyId = partyId;
    }

    public CaseInvite generateAccessCode(String caseInviteEmail, UUID partyId) {

        return new CaseInvite(caseInviteEmail, AccessCodeGenerator.generateAccessCode(), invitedUserId, partyId);
    }
}

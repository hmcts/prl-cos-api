package uk.gov.hmcts.reform.prl.models.caseinvite;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.utils.AccessCodeGenerator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


@Data
public class CaseInvite {

    private UUID partyId;
    private String caseInviteEmail;
    private String accessCode;
    private String invitedUserId;
    private String hasLinked;
    private LocalDate expiryDate;


    @Builder()
    public CaseInvite() {

    }

    public CaseInvite(String caseInviteEmail, String accessCode, String invitedUserId, UUID partyId) {
        this.accessCode = accessCode;
        this.caseInviteEmail = caseInviteEmail;
        this.invitedUserId = invitedUserId;
        this.partyId = partyId;
        this.expiryDate = LocalDate.now().plus(2, ChronoUnit.WEEKS);
    }

    public CaseInvite generateAccessCode(String caseInviteEmail, UUID partyId) {

        return new CaseInvite(caseInviteEmail, AccessCodeGenerator.generateAccessCode(), invitedUserId, partyId);
    }
}

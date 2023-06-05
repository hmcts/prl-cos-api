package uk.gov.hmcts.reform.prl.models.caseinvite;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.utils.AccessCodeGenerator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class CaseInvite {

    private UUID partyId;
    private String caseInviteEmail;
    private String accessCode;
    private String invitedUserId;
    private String hasLinked;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    private YesOrNo isApplicant;


    @Builder()
    public CaseInvite() {

    }

    public CaseInvite(String caseInviteEmail, String accessCode, String invitedUserId, UUID partyId, YesOrNo isApplicant) {
        this.accessCode = accessCode;
        this.caseInviteEmail = caseInviteEmail;
        this.invitedUserId = invitedUserId;
        this.partyId = partyId;
        this.expiryDate = LocalDate.now().plus(2, ChronoUnit.WEEKS);
        this.isApplicant = isApplicant;
    }

    public CaseInvite generateAccessCode(String caseInviteEmail, UUID partyId, YesOrNo isApplicant) {

        return new CaseInvite(caseInviteEmail, AccessCodeGenerator.generateAccessCode(), invitedUserId, partyId, isApplicant);
    }
}

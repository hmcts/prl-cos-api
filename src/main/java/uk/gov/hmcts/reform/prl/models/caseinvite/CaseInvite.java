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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;


@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class CaseInvite {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Text)
    private UUID partyId;
    //caseInviteEmail is not used correctly and it has to get updated whenever the access code is sent
    @CCD(label = " ", searchable = false)
    private String caseInviteEmail;
    @CCD(label = " ", searchable = false)
    private String accessCode;
    @CCD(label = " ", searchable = false)
    private String invitedUserId;
    @CCD(label = " ", searchable = false)
    private String hasLinked;

    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
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

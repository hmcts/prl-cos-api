package uk.gov.hmcts.reform.prl.models.caseflags;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AllPartyFlags {
    private PartyFlags caApplicant1Flags;
    private PartyFlags caApplicant2Flags;
    private PartyFlags caApplicant3Flags;
    private PartyFlags caApplicant4Flags;
    private PartyFlags caApplicant5Flags;
    private PartyFlags caApplicantSolicitor1Flags;
    private PartyFlags caApplicantSolicitor2Flags;
    private PartyFlags caApplicantSolicitor3Flags;
    private PartyFlags caApplicantSolicitor4Flags;
    private PartyFlags caApplicantSolicitor5Flags;
    private PartyFlags caRespondent1Flags;
    private PartyFlags caRespondent2Flags;
    private PartyFlags caRespondent3Flags;
    private PartyFlags caRespondent4Flags;
    private PartyFlags caRespondent5Flags;
    private PartyFlags caRespondentSolicitor1Flags;
    private PartyFlags caRespondentSolicitor2Flags;
    private PartyFlags caRespondentSolicitor3Flags;
    private PartyFlags caRespondentSolicitor4Flags;
    private PartyFlags caRespondentSolicitor5Flags;
    private PartyFlags caOtherParty1Flags;
    private PartyFlags caOtherParty2Flags;
    private PartyFlags caOtherParty3Flags;
    private PartyFlags caOtherParty4Flags;
    private PartyFlags caOtherParty5Flags;
    private PartyFlags daApplicantFlags;
    private PartyFlags daApplicantSolicitorFlags;
    private PartyFlags daRespondentFlags;
    private PartyFlags daRespondentSolicitorFlags;
}

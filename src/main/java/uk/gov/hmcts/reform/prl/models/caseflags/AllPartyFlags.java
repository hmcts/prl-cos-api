package uk.gov.hmcts.reform.prl.models.caseflags;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AllPartyFlags {
    private PartyFlags caApplicant1;
    private PartyFlags caApplicant2;
    private PartyFlags caApplicant3;
    private PartyFlags caApplicant4;
    private PartyFlags caApplicant5;
    private PartyFlags caApplicantSolicitor1;
    private PartyFlags caApplicantSolicitor2;
    private PartyFlags caApplicantSolicitor3;
    private PartyFlags caApplicantSolicitor4;
    private PartyFlags caApplicantSolicitor5;
    private PartyFlags caRespondent1;
    private PartyFlags caRespondent2;
    private PartyFlags caRespondent3;
    private PartyFlags caRespondent4;
    private PartyFlags caRespondent5;
    private PartyFlags caRespondentSolicitor1;
    private PartyFlags caRespondentSolicitor2;
    private PartyFlags caRespondentSolicitor3;
    private PartyFlags caRespondentSolicitor4;
    private PartyFlags caRespondentSolicitor5;
    private PartyFlags caOtherParty1;
    private PartyFlags caOtherParty2;
    private PartyFlags caOtherParty3;
    private PartyFlags caOtherParty4;
    private PartyFlags caOtherParty5;
    private PartyFlags caOtherParty6;
    private PartyFlags caOtherParty7;
    private PartyFlags caOtherParty8;
    private PartyFlags caOtherParty9;
    private PartyFlags caOtherParty10;
    private PartyFlags daApplicant;
    private PartyFlags daApplicantSolicitor1;
    private PartyFlags daRespondent;
    private PartyFlags caRespondentSolicitor;
}

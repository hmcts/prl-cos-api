package uk.gov.hmcts.reform.prl.models.caseflags;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllPartyFlags {
    private Flags caApplicant1ExternalFlags;
    private Flags caApplicant2ExternalFlags;
    private Flags caApplicant3ExternalFlags;
    private Flags caApplicant4ExternalFlags;
    private Flags caApplicant5ExternalFlags;
    private Flags caApplicantSolicitor1ExternalFlags;
    private Flags caApplicantSolicitor2ExternalFlags;
    private Flags caApplicantSolicitor3ExternalFlags;
    private Flags caApplicantSolicitor4ExternalFlags;
    private Flags caApplicantSolicitor5ExternalFlags;
    private Flags caRespondent1ExternalFlags;
    private Flags caRespondent2ExternalFlags;
    private Flags caRespondent3ExternalFlags;
    private Flags caRespondent4ExternalFlags;
    private Flags caRespondent5ExternalFlags;
    private Flags caRespondentSolicitor1ExternalFlags;
    private Flags caRespondentSolicitor2ExternalFlags;
    private Flags caRespondentSolicitor3ExternalFlags;
    private Flags caRespondentSolicitor4ExternalFlags;
    private Flags caRespondentSolicitor5ExternalFlags;
    private Flags caOtherParty1ExternalFlags;
    private Flags caOtherParty2ExternalFlags;
    private Flags caOtherParty3ExternalFlags;
    private Flags caOtherParty4ExternalFlags;
    private Flags caOtherParty5ExternalFlags;
    private Flags daApplicantExternalFlags;
    private Flags daApplicantSolicitorExternalFlags;
    private Flags daRespondentExternalFlags;
    private Flags daRespondentSolicitorExternalFlags;
    private Flags caApplicant1InternalFlags;
    private Flags caApplicant2InternalFlags;
    private Flags caApplicant3InternalFlags;
    private Flags caApplicant4InternalFlags;
    private Flags caApplicant5InternalFlags;
    private Flags caApplicantSolicitor1InternalFlags;
    private Flags caApplicantSolicitor2InternalFlags;
    private Flags caApplicantSolicitor3InternalFlags;
    private Flags caApplicantSolicitor4InternalFlags;
    private Flags caApplicantSolicitor5InternalFlags;
    private Flags caRespondent1InternalFlags;
    private Flags caRespondent2InternalFlags;
    private Flags caRespondent3InternalFlags;
    private Flags caRespondent4InternalFlags;
    private Flags caRespondent5InternalFlags;
    private Flags caRespondentSolicitor1InternalFlags;
    private Flags caRespondentSolicitor2InternalFlags;
    private Flags caRespondentSolicitor3InternalFlags;
    private Flags caRespondentSolicitor4InternalFlags;
    private Flags caRespondentSolicitor5InternalFlags;
    private Flags caOtherParty1InternalFlags;
    private Flags caOtherParty2InternalFlags;
    private Flags caOtherParty3InternalFlags;
    private Flags caOtherParty4InternalFlags;
    private Flags caOtherParty5InternalFlags;
    private Flags daApplicantInternalFlags;
    private Flags daApplicantSolicitorInternalFlags;
    private Flags daRespondentInternalFlags;
    private Flags daRespondentSolicitorInternalFlags;
}

package uk.gov.hmcts.reform.prl.models.caseflags;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FLAG_INITIAL_STATUS;

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

    public boolean isExternalReviewPendingForCaseType(CaseData caseData) {
        String caseTypeOfApplication = CaseUtils.getCaseTypeOfApplication(caseData);
        if(C100_CASE_TYPE.equals(caseTypeOfApplication)) {
            return isRequestStatusPresent(getCaExternalFlags());
        } else if (FL401_CASE_TYPE.equals(caseTypeOfApplication)) {
            return isRequestStatusPresent(getDaExternalFlags());
        }
        throw new IllegalArgumentException("Invalid case type: "+ caseTypeOfApplication);
    }

    private boolean isRequestStatusPresent(List<Flags> flags) {
        return flags.stream()
            .filter(Objects::nonNull)
            .map(Flags::getDetails)
            .flatMap(Collection::stream)
            .map(Element::getValue)
            .anyMatch(flagDetail -> FLAG_INITIAL_STATUS.equals(flagDetail.getStatus()));
    }

    private List<Flags> getCaExternalFlags() {
        return List.of(caApplicant1ExternalFlags,
                        caApplicant2ExternalFlags,
                        caApplicant3ExternalFlags,
                        caApplicant4ExternalFlags,
                        caApplicant5ExternalFlags,
                        caApplicantSolicitor1ExternalFlags,
                        caApplicantSolicitor2ExternalFlags,
                        caApplicantSolicitor3ExternalFlags,
                        caApplicantSolicitor4ExternalFlags,
                        caApplicantSolicitor5ExternalFlags,
                        caRespondent1ExternalFlags,
                        caRespondent2ExternalFlags,
                        caRespondent3ExternalFlags,
                        caRespondent4ExternalFlags,
                        caRespondent5ExternalFlags,
                        caRespondentSolicitor1ExternalFlags,
                        caRespondentSolicitor2ExternalFlags,
                        caRespondentSolicitor3ExternalFlags,
                        caRespondentSolicitor4ExternalFlags,
                        caRespondentSolicitor5ExternalFlags,
                        caOtherParty1ExternalFlags,
                        caOtherParty2ExternalFlags,
                        caOtherParty3ExternalFlags,
                        caOtherParty4ExternalFlags,
                        caOtherParty5ExternalFlags);
    }

    private List<Flags> getDaExternalFlags() {
            return List.of(daApplicantExternalFlags,
            daApplicantSolicitorExternalFlags,
            daRespondentExternalFlags,
            daRespondentSolicitorExternalFlags);
    }
}

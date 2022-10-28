package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class C100RebuildSafetyConcernsElements {

    @JsonProperty("c1A_haveSafetyConcerns")
    private YesOrNo haveSafetyConcerns;
    @JsonProperty("c1A_safetyConernAbout")
    private String[] safetyConernAbout;
    @JsonProperty("c1A_concernAboutChild")
    private String[] concernAboutChild;
    @JsonProperty("c1A_safteyConcerns")
    private SafetyConcerns safetyConcerns;
    @JsonProperty("c1A_abductionReasonOutsideUk")
    private String abductionReasonOutsideUk;
    @JsonProperty("c1A_childsCurrentLocation")
    private String childsCurrentLocation;
    @JsonProperty("c1A_passportOffice")
    private YesOrNo passportOffice;
    @JsonProperty("c1A_childrenMoreThanOnePassport")
    private YesOrNo childrenMoreThanOnePassport;
    @JsonProperty("c1A_possessionChildrenPassport")
    private String[] possessionChildrenPassport;
    @JsonProperty("c1A_provideOtherDetails")
    private String provideOtherDetails;
    @JsonProperty("c1A_abductionPassportOfficeNotified")
    private YesOrNo abductionPassportOfficeNotified;
    @JsonProperty("c1A_childAbductedBefore")
    private YesOrNo childAbductedBefore;
    @JsonProperty("c1A_previousAbductionsShortDesc")
    private String previousAbductionsShortDesc;
    @JsonProperty("c1A_policeOrInvestigatorInvolved")
    private YesOrNo policeOrInvestigatorInvolved;
    @JsonProperty("c1A_policeOrInvestigatorOtherDetails")
    private String policeOrInvestigatorOtherDetails;
    @JsonProperty("c1A_concernAboutApplicant")
    private String[] concernAboutApplicant;
    @JsonProperty("c1A_otherConcernsDrugs")
    private YesOrNo otherConcernsDrugs;
    @JsonProperty("c1A_otherConcernsDrugsDetails")
    private String otherConcernsDrugsDetails;
    @JsonProperty("c1A_childSafetyConcerns")
    private YesOrNo childSafetyConcerns;
    @JsonProperty("c1A_childSafetyConcernsDetails")
    private String childSafetyConcernsDetails;
    @JsonProperty("c1A_keepingSafeStatement")
    private String keepingSafeStatement;
    @JsonProperty("c1A_supervisionAgreementDetails")
    private YesOrNo supervisionAgreementDetails;
    @JsonProperty("c1A_agreementOtherWaysDetails")
    private YesOrNo agreementOtherWaysDetails;
}
package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SafetyConcerns {

    @JsonProperty("c1A_haveSafetyConcerns")
    private YesOrNo haveSafetyConcerns;
    @JsonProperty("c1A_safetyConernAbout")
    private String[] WhoConcernAbout;
    @JsonProperty("c1A_concernAboutChild")
    private String[] c1AConcernAboutChild;
   // c1A_concernAboutApplicant

    @JsonProperty("c1A_otherConcernsDrugs")
    private YesOrNo c1AOtherConcernsDrugs;

    @JsonProperty("c1A_otherConcernsDrugsDetails")
    private String c1AOtherConcernsDrugsDetails;

    @JsonProperty("c1A_childSafetyConcerns")
    private YesOrNo c1AChildSafetyConcerns;

    @JsonProperty("c1A_childSafetyConcernsDetails")
    private String c1AChildSafetyConcernsDetails;
    @JsonProperty("c1A_abductionReasonOutsideUk")
    private String c1AAbductionReasonOutsideUk;

    @JsonProperty("c1A_childsCurrentLocation")
    private String c1AChildsCurrentLocation;

    @JsonProperty("c1A_passportOffice")
    private YesOrNo c1APassportOffice;

    @JsonProperty("c1A_childrenMoreThanOnePassport")
    private YesOrNo c1AChildrenMoreThanOnePassport;

    @JsonProperty("c1A_possessionChildrenPassport")
    private String[] c1APossessionChildrenPassport;

    @JsonProperty("c1A_provideOtherDetails")
    private String c1AProvideOtherDetails;
    @JsonProperty("c1A_abductionPassportOfficeNotified")
    private YesOrNo c1AAbductionPassportOfficeNotified;
    @JsonProperty("c1A_childAbductedBefore")
    private YesOrNo c1AChildAbductedBefore;
    @JsonProperty( "c1A_previousAbductionsShortDesc")
    private String c1APreviousAbductionsShortDesc;
    @JsonProperty("c1A_policeOrInvestigatorInvolved")
    private YesOrNo c1APoliceOrInvestigatorInvolved;
    @JsonProperty("c1A_policeOrInvestigatorOtherDetails")
    private String c1APoliceOrInvestigatorOtherDetails;
    @JsonProperty("c1A_concernAboutApplicant")
    private String[] c1AConcernAboutApplicant;
//    @JsonProperty( "c1A_otherConcernsDrugs")
//    private boolean c1AOtherConcernsDrugs;
//    @JsonProperty("c1A_otherConcernsDrugsDetails")
//    private String c1AOtherConcernsDrugsDetails;
//    @JsonProperty( "c1A_childSafetyConcerns")
//    private boolean c1AChildSafetyConcerns;
//    @JsonProperty( "c1A_childSafetyConcernsDetails")
//    private String c1AChildSafetyConcernsDetails;
    @JsonProperty( "c1A_keepingSafeStatement")
    private String c1AKeepingSafeStatement;
    @JsonProperty( "c1A_supervisionAgreementDetails")
    private String c1ASupervisionAgreementDetails;
    @JsonProperty( "c1A_agreementOtherWaysDetails")
    private YesOrNo c1AAgreementOtherWaysDetails;

    @JsonProperty("applicant")
    private List<ApplicantSafteConcernDTO> applicant;
    @JsonProperty("child")
    private List<ChildSafetyConcernsDto> child;

}

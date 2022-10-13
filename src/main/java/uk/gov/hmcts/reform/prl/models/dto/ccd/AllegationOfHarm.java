package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllegationOfHarm {

    /**
     * Allegations of harm.
     */

    private final YesOrNo allegationsOfHarmYesNo;
    private final YesOrNo allegationsOfHarmDomesticAbuseYesNo;
    @JsonProperty("physicalAbuseVictim")
    private final List<ApplicantOrChildren> physicalAbuseVictim;
    @JsonProperty("emotionalAbuseVictim")
    private final List<ApplicantOrChildren> emotionalAbuseVictim;
    @JsonProperty("psychologicalAbuseVictim")
    private final List<ApplicantOrChildren> psychologicalAbuseVictim;
    @JsonProperty("sexualAbuseVictim")
    private final List<ApplicantOrChildren> sexualAbuseVictim;
    @JsonProperty("financialAbuseVictim")
    private final List<ApplicantOrChildren> financialAbuseVictim;
    private final YesOrNo allegationsOfHarmChildAbductionYesNo;
    private final String childAbductionReasons;
    private final YesOrNo previousAbductionThreats;
    private final String previousAbductionThreatsDetails;
    private final String childrenLocationNow;
    private final YesOrNo abductionPassportOfficeNotified;
    private final YesOrNo abductionChildHasPassport;
    private final AbductionChildPassportPossessionEnum abductionChildPassportPosession;
    private final String abductionChildPassportPosessionOtherDetail;
    private final YesOrNo abductionPreviousPoliceInvolvement;
    private final String abductionPreviousPoliceInvolvementDetails;
    private final YesOrNo allegationsOfHarmChildAbuseYesNo;
    private final YesOrNo allegationsOfHarmSubstanceAbuseYesNo;
    private final YesOrNo allegationsOfHarmOtherConcernsYesNo;
    @JsonProperty("behaviours")
    private final List<Element<Behaviours>> behaviours;
    private final YesOrNo ordersNonMolestation;
    private final YesOrNo ordersOccupation;
    private final YesOrNo ordersForcedMarriageProtection;
    private final YesOrNo ordersRestraining;
    private final YesOrNo ordersOtherInjunctive;
    private final YesOrNo ordersUndertakingInPlace;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersNonMolestationDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersNonMolestationEndDate;
    private final YesOrNo ordersNonMolestationCurrent;
    private final String ordersNonMolestationCourtName;
    private final Document ordersNonMolestationDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersOccupationDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersOccupationEndDate;
    private final YesOrNo ordersOccupationCurrent;
    private final String ordersOccupationCourtName;
    private final Document ordersOccupationDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersForcedMarriageProtectionDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersForcedMarriageProtectionEndDate;
    private final YesOrNo ordersForcedMarriageProtectionCurrent;
    private final String ordersForcedMarriageProtectionCourtName;
    private final Document ordersForcedMarriageProtectionDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersRestrainingDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersRestrainingEndDate;
    private final YesOrNo ordersRestrainingCurrent;
    private final String ordersRestrainingCourtName;
    private final Document ordersRestrainingDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersOtherInjunctiveDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersOtherInjunctiveEndDate;
    private final YesOrNo ordersOtherInjunctiveCurrent;
    private final String ordersOtherInjunctiveCourtName;
    private final Document ordersOtherInjunctiveDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersUndertakingInPlaceDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersUndertakingInPlaceEndDate;
    private final YesOrNo ordersUndertakingInPlaceCurrent;
    private final String ordersUndertakingInPlaceCourtName;
    private final Document ordersUndertakingInPlaceDocument;
    private final YesOrNo allegationsOfHarmOtherConcerns;
    private final String allegationsOfHarmOtherConcernsDetails;
    private final String allegationsOfHarmOtherConcernsCourtActions;
    private final YesOrNo agreeChildUnsupervisedTime;
    private final YesOrNo agreeChildSupervisedTime;
    private final YesOrNo agreeChildOtherContact;

}

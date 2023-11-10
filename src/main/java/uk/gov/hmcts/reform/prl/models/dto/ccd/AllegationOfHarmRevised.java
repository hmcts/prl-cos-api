package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllegationOfHarmRevised {

    private final YesOrNo newAllegationsOfHarmYesNo;
    private final YesOrNo newAllegationsOfHarmDomesticAbuseYesNo;
    private final YesOrNo newAllegationsOfHarmChildAbductionYesNo;
    private final YesOrNo newAllegationsOfHarmChildAbuseYesNo;
    private final String newChildAbductionReasons;
    private final YesOrNo newPreviousAbductionThreats;
    private final String newPreviousAbductionThreatsDetails;
    private final String newChildrenLocationNow;
    private final YesOrNo newAbductionPassportOfficeNotified;
    private final YesOrNo newAbductionChildHasPassport;

    @JsonProperty("childPassportDetails")
    private ChildPassportDetails childPassportDetails;

    private final YesOrNo newAbductionPreviousPoliceInvolvement;
    private final String newAbductionPreviousPoliceInvolvementDetails;
    private final YesOrNo allegationsOfHarmChildAbuseYesNo;
    private final YesOrNo newAllegationsOfHarmSubstanceAbuseYesNo;
    private final String newAllegationsOfHarmSubstanceAbuseDetails;

    private final YesOrNo newOrdersNonMolestation;
    private final YesOrNo newOrdersOccupation;
    private final YesOrNo newOrdersForcedMarriageProtection;
    private final YesOrNo newOrdersRestraining;
    private final YesOrNo newOrdersOtherInjunctive;
    private final YesOrNo newOrdersUndertakingInPlace;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersNonMolestationDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersNonMolestationEndDate;
    private final YesOrNo newOrdersNonMolestationCurrent;
    private final String newOrdersNonMolestationCourtName;
    private final Document newOrdersNonMolestationDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersOccupationDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersOccupationEndDate;
    private final YesOrNo newOrdersOccupationCurrent;
    private final String newOrdersOccupationCourtName;
    private final Document newOrdersOccupationDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersForcedMarriageProtectionDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersForcedMarriageProtectionEndDate;
    private final YesOrNo newOrdersForcedMarriageProtectionCurrent;
    private final String newOrdersForcedMarriageProtectionCourtName;
    private final Document newOrdersForcedMarriageProtectionDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersRestrainingDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersRestrainingEndDate;
    private final String newOrdersNonMolestationCaseNumber;
    private final String newOrdersOccupationCaseNumber;
    private final String newOrdersForcedMarriageProtectionCaseNumber;
    private final String newOrdersRestrainingCaseNumber;
    private final String newOrdersOtherInjunctiveCaseNumber;
    private final String newOrdersUndertakingInPlaceCaseNumber;
    private final YesOrNo newOrdersRestrainingCurrent;
    private final String newOrdersRestrainingCourtName;
    private final Document newOrdersRestrainingDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersOtherInjunctiveDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersOtherInjunctiveEndDate;
    private final YesOrNo newOrdersOtherInjunctiveCurrent;
    private final String newOrdersOtherInjunctiveCourtName;
    private final Document newOrdersOtherInjunctiveDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersUndertakingInPlaceDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersUndertakingInPlaceEndDate;
    private final YesOrNo newOrdersUndertakingInPlaceCurrent;
    private final String newOrdersUndertakingInPlaceCourtName;
    private final Document newOrdersUndertakingInPlaceDocument;
    private final YesOrNo newAllegationsOfHarmOtherConcerns;
    private final String newAllegationsOfHarmOtherConcernsDetails;
    private final String newAllegationsOfHarmOtherConcernsCourtActions;
    private final YesOrNo newAgreeChildUnsupervisedTime;
    private final YesOrNo newAgreeChildSupervisedTime;
    private final YesOrNo newAgreeChildOtherContact;

    // for the new text box added under Drugs,alcolhol,substance abuse Radio Button , Other safety or welfare concerns Radio button
    private final String allegationsOfHarmOtherConcernsDetails;

    @JsonProperty("domesticBehaviours")
    private final List<Element<DomesticAbuseBehaviours>> domesticBehaviours;

    private final List<ChildAbuseEnum> childAbuses;

    @JsonProperty("childPhysicalAbuse")
    private final ChildAbuse childPhysicalAbuse;

    @JsonProperty("childPsychologicalAbuse")
    private final ChildAbuse childPsychologicalAbuse;

    @JsonProperty("childFinancialAbuse")
    private final ChildAbuse childFinancialAbuse;

    @JsonProperty("childSexualAbuse")
    private final ChildAbuse childSexualAbuse;

    @JsonProperty("childEmotionalAbuse")
    private final ChildAbuse childEmotionalAbuse;

    @JsonIgnore
    @JsonProperty("childAbuseBehavioursDocmosis")
    private final  List<Element<ChildAbuseBehaviour>> childAbuseBehavioursDocmosis;

    @JsonProperty("allChildrenAreRiskPhysicalAbuse")
    private YesOrNo allChildrenAreRiskPhysicalAbuse;

    @JsonProperty("allChildrenAreRiskPsychologicalAbuse")
    private YesOrNo allChildrenAreRiskPsychologicalAbuse;

    @JsonProperty("allChildrenAreRiskSexualAbuse")
    private YesOrNo allChildrenAreRiskSexualAbuse;

    @JsonProperty("allChildrenAreRiskEmotionalAbuse")
    private YesOrNo allChildrenAreRiskEmotionalAbuse;

    @JsonProperty("allChildrenAreRiskFinancialAbuse")
    private YesOrNo allChildrenAreRiskFinancialAbuse;

    @JsonProperty("whichChildrenAreRiskPhysicalAbuse")
    private DynamicMultiSelectList whichChildrenAreRiskPhysicalAbuse;

    @JsonProperty("whichChildrenAreRiskPsychologicalAbuse")
    private DynamicMultiSelectList whichChildrenAreRiskPsychologicalAbuse;

    @JsonProperty("whichChildrenAreRiskSexualAbuse")
    private DynamicMultiSelectList whichChildrenAreRiskSexualAbuse;

    @JsonProperty("whichChildrenAreRiskEmotionalAbuse")
    private DynamicMultiSelectList whichChildrenAreRiskEmotionalAbuse;

    @JsonProperty("whichChildrenAreRiskFinancialAbuse")
    private DynamicMultiSelectList whichChildrenAreRiskFinancialAbuse;



}

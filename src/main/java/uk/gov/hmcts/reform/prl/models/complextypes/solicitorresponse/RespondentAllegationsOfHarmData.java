package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.RespChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.RespDomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespChildAbuseBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespChildPassportDetails;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class RespondentAllegationsOfHarmData {

    private final YesOrNo respAohYesOrNo;

    private final YesOrNo respAohDomesticAbuseYesNo;
    private final YesOrNo respAohChildAbductionYesNo;
    private final YesOrNo respAohChildAbuseYesNo;
    private final String respChildAbductionReasons;
    private final YesOrNo respPreviousAbductionThreats;
    private final String respPreviousAbductionThreatsDetails;
    private final String respChildrenLocationNow;
    private final YesOrNo respAbductionPassportOfficeNotified;
    private final YesOrNo respAbductionChildHasPassport;

    @JsonProperty("respChildPassportDetails")
    private RespChildPassportDetails respChildPassportDetails;

    private final YesOrNo respAbductionPreviousPoliceInvolvement;
    private final String respAbductionPreviousPoliceInvolvementDetails;
    private final YesOrNo respAohSubstanceAbuseYesNo;
    private final String respAohSubstanceAbuseDetails;

    private final YesOrNo respOrdersNonMolestation;
    private final YesOrNo respOrdersOccupation;
    private final YesOrNo respOrdersForcedMarriageProtection;
    private final YesOrNo respOrdersRestraining;
    private final YesOrNo respOrdersOtherInjunctive;
    private final YesOrNo respOrdersUndertakingInPlace;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersNonMolestationDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersNonMolestationEndDate;
    private final YesOrNo respOrdersNonMolestationCurrent;
    private final String respOrdersNonMolestationCourtName;
    private final Document respOrdersNonMolestationDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersOccupationDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersOccupationEndDate;
    private final YesOrNo respOrdersOccupationCurrent;
    private final String respOrdersOccupationCourtName;
    private final Document respOrdersOccupationDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersForcedMarriageProtectionDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersForcedMarriageProtectionEndDate;
    private final YesOrNo respOrdersForcedMarriageProtectionCurrent;
    private final String respOrdersForcedMarriageProtectionCourtName;
    private final Document respOrdersForcedMarriageProtectionDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersRestrainingDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersRestrainingEndDate;
    private final String respOrdersNonMolestationCaseNumber;
    private final String respOrdersOccupationCaseNumber;
    private final String respOrdersForcedMarriageProtectionCaseNumber;
    private final String respOrdersRestrainingCaseNumber;
    private final String respOrdersOtherInjunctiveCaseNumber;
    private final String respOrdersUndertakingInPlaceCaseNumber;
    private final YesOrNo respOrdersRestrainingCurrent;
    private final String respOrdersRestrainingCourtName;
    private final Document respOrdersRestrainingDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersOtherInjunctiveDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersOtherInjunctiveEndDate;
    private final YesOrNo respOrdersOtherInjunctiveCurrent;
    private final String respOrdersOtherInjunctiveCourtName;
    private final Document respOrdersOtherInjunctiveDocument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersUndertakingInPlaceDateIssued;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersUndertakingInPlaceEndDate;
    private final YesOrNo respOrdersUndertakingInPlaceCurrent;
    private final String respOrdersUndertakingInPlaceCourtName;
    private final Document respOrdersUndertakingInPlaceDocument;
    private final YesOrNo respAohOtherConcerns;
    private final String respAohOtherConcernsDetails;
    private final String respAohOtherConcernsCourtActions;
    private final YesOrNo respAgreeChildUnsupervisedTime;
    private final YesOrNo respAgreeChildSupervisedTime;
    private final YesOrNo respAgreeChildOtherContact;

    @JsonProperty("respDomesticBehaviours")
    private final List<Element<RespDomesticAbuseBehaviours>> respDomesticBehaviours;

    private final List<ChildAbuseEnum> respChildAbuses;

    @JsonProperty("respChildPhysicalAbuse")
    private final RespChildAbuse respChildPhysicalAbuse;

    @JsonProperty("respChildPsychologicalAbuse")
    private final RespChildAbuse respChildPsychologicalAbuse;

    @JsonProperty("respChildFinancialAbuse")
    private final RespChildAbuse respChildFinancialAbuse;

    @JsonProperty("respChildSexualAbuse")
    private final RespChildAbuse respChildSexualAbuse;

    @JsonProperty("respChildEmotionalAbuse")
    private final RespChildAbuse respChildEmotionalAbuse;

    @JsonIgnore
    @JsonProperty("respChildAbuseBehavioursDocmosis")
    private final  List<Element<RespChildAbuseBehaviour>> respChildAbuseBehavioursDocmosis;

    @JsonProperty("respAllChildrenAreRiskPhysicalAbuse")
    private YesOrNo respAllChildrenAreRiskPhysicalAbuse;

    @JsonProperty("respAllChildrenAreRiskPsychologicalAbuse")
    private YesOrNo respAllChildrenAreRiskPsychologicalAbuse;

    @JsonProperty("respAllChildrenAreRiskSexualAbuse")
    private YesOrNo respAllChildrenAreRiskSexualAbuse;

    @JsonProperty("respAllChildrenAreRiskEmotionalAbuse")
    private YesOrNo respAllChildrenAreRiskEmotionalAbuse;

    @JsonProperty("respAllChildrenAreRiskFinancialAbuse")
    private YesOrNo respAllChildrenAreRiskFinancialAbuse;

    @JsonProperty("respWhichChildrenAreRiskPhysicalAbuse")
    private DynamicMultiSelectList respWhichChildrenAreRiskPhysicalAbuse;

    @JsonProperty("respWhichChildrenAreRiskPsychologicalAbuse")
    private DynamicMultiSelectList respWhichChildrenAreRiskPsychologicalAbuse;

    @JsonProperty("respWhichChildrenAreRiskSexualAbuse")
    private DynamicMultiSelectList respWhichChildrenAreRiskSexualAbuse;

    @JsonProperty("respWhichChildrenAreRiskEmotionalAbuse")
    private DynamicMultiSelectList respWhichChildrenAreRiskEmotionalAbuse;

    @JsonProperty("respWhichChildrenAreRiskFinancialAbuse")
    private DynamicMultiSelectList respWhichChildrenAreRiskFinancialAbuse;
}

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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class RespondentAllegationsOfHarmData {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respAohYesOrNo;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respAohDomesticAbuseYesNo;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respAohChildAbductionYesNo;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respAohChildAbuseYesNo;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String respChildAbductionReasons;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respPreviousAbductionThreats;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String respPreviousAbductionThreatsDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String respChildrenLocationNow;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respAbductionPassportOfficeNotified;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respAbductionChildHasPassport;

    @CCD(label = " ", searchable = false)
    @JsonProperty("respChildPassportDetails")
    private RespChildPassportDetails respChildPassportDetails;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respAbductionPreviousPoliceInvolvement;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String respAbductionPreviousPoliceInvolvementDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respAohSubstanceAbuseYesNo;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String respAohSubstanceAbuseDetails;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respOrdersNonMolestation;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respOrdersOccupation;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respOrdersForcedMarriageProtection;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respOrdersRestraining;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respOrdersOtherInjunctive;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respOrdersUndertakingInPlace;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersNonMolestationDateIssued;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersNonMolestationEndDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respOrdersNonMolestationCurrent;
    @CCD(label = " ", searchable = false)
    private final String respOrdersNonMolestationCourtName;
    @CCD(label = " ", searchable = false)
    private final Document respOrdersNonMolestationDocument;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersOccupationDateIssued;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersOccupationEndDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respOrdersOccupationCurrent;
    @CCD(label = " ", searchable = false)
    private final String respOrdersOccupationCourtName;
    @CCD(label = " ", searchable = false)
    private final Document respOrdersOccupationDocument;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersForcedMarriageProtectionDateIssued;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersForcedMarriageProtectionEndDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respOrdersForcedMarriageProtectionCurrent;
    @CCD(label = " ", searchable = false)
    private final String respOrdersForcedMarriageProtectionCourtName;
    @CCD(label = " ", searchable = false)
    private final Document respOrdersForcedMarriageProtectionDocument;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersRestrainingDateIssued;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersRestrainingEndDate;
    @CCD(label = " ", searchable = false)
    private final String respOrdersNonMolestationCaseNumber;
    @CCD(label = " ", searchable = false)
    private final String respOrdersOccupationCaseNumber;
    @CCD(label = " ", searchable = false)
    private final String respOrdersForcedMarriageProtectionCaseNumber;
    @CCD(label = " ", searchable = false)
    private final String respOrdersRestrainingCaseNumber;
    @CCD(label = " ", searchable = false)
    private final String respOrdersOtherInjunctiveCaseNumber;
    @CCD(label = " ", searchable = false)
    private final String respOrdersUndertakingInPlaceCaseNumber;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respOrdersRestrainingCurrent;
    @CCD(label = " ", searchable = false)
    private final String respOrdersRestrainingCourtName;
    @CCD(label = " ", searchable = false)
    private final Document respOrdersRestrainingDocument;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersOtherInjunctiveDateIssued;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersOtherInjunctiveEndDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respOrdersOtherInjunctiveCurrent;
    @CCD(label = " ", searchable = false)
    private final String respOrdersOtherInjunctiveCourtName;
    @CCD(label = " ", searchable = false)
    private final Document respOrdersOtherInjunctiveDocument;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersUndertakingInPlaceDateIssued;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respOrdersUndertakingInPlaceEndDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respOrdersUndertakingInPlaceCurrent;
    @CCD(label = " ", searchable = false)
    private final String respOrdersUndertakingInPlaceCourtName;
    @CCD(label = " ", searchable = false)
    private final Document respOrdersUndertakingInPlaceDocument;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respAohOtherConcerns;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String respAohOtherConcernsDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String respAohOtherConcernsCourtActions;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respAgreeChildUnsupervisedTime;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respAgreeChildSupervisedTime;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respAgreeChildOtherContact;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespDomesticBehaviours"
    )
    @JsonProperty("respDomesticBehaviours")
    private final List<Element<RespDomesticAbuseBehaviours>> respDomesticBehaviours;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ChildTypeOfAbuse"
    )
    private final List<ChildAbuseEnum> respChildAbuses;

    @CCD(label = " ", searchable = false)
    @JsonProperty("respChildPhysicalAbuse")
    private final RespChildAbuse respChildPhysicalAbuse;

    @CCD(label = " ", searchable = false)
    @JsonProperty("respChildPsychologicalAbuse")
    private final RespChildAbuse respChildPsychologicalAbuse;

    @CCD(label = " ", searchable = false)
    @JsonProperty("respChildFinancialAbuse")
    private final RespChildAbuse respChildFinancialAbuse;

    @CCD(label = " ", searchable = false)
    @JsonProperty("respChildSexualAbuse")
    private final RespChildAbuse respChildSexualAbuse;

    @CCD(label = " ", searchable = false)
    @JsonProperty("respChildEmotionalAbuse")
    private final RespChildAbuse respChildEmotionalAbuse;

    @JsonIgnore
    @JsonProperty("respChildAbuseBehavioursDocmosis")
    private final  List<Element<RespChildAbuseBehaviour>> respChildAbuseBehavioursDocmosis;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("respAllChildrenAreRiskPhysicalAbuse")
    private YesOrNo respAllChildrenAreRiskPhysicalAbuse;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("respAllChildrenAreRiskPsychologicalAbuse")
    private YesOrNo respAllChildrenAreRiskPsychologicalAbuse;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("respAllChildrenAreRiskSexualAbuse")
    private YesOrNo respAllChildrenAreRiskSexualAbuse;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("respAllChildrenAreRiskEmotionalAbuse")
    private YesOrNo respAllChildrenAreRiskEmotionalAbuse;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("respAllChildrenAreRiskFinancialAbuse")
    private YesOrNo respAllChildrenAreRiskFinancialAbuse;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicMultiSelectList)
    @JsonProperty("respWhichChildrenAreRiskPhysicalAbuse")
    private DynamicMultiSelectList respWhichChildrenAreRiskPhysicalAbuse;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicMultiSelectList)
    @JsonProperty("respWhichChildrenAreRiskPsychologicalAbuse")
    private DynamicMultiSelectList respWhichChildrenAreRiskPsychologicalAbuse;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicMultiSelectList)
    @JsonProperty("respWhichChildrenAreRiskSexualAbuse")
    private DynamicMultiSelectList respWhichChildrenAreRiskSexualAbuse;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicMultiSelectList)
    @JsonProperty("respWhichChildrenAreRiskEmotionalAbuse")
    private DynamicMultiSelectList respWhichChildrenAreRiskEmotionalAbuse;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicMultiSelectList)
    @JsonProperty("respWhichChildrenAreRiskFinancialAbuse")
    private DynamicMultiSelectList respWhichChildrenAreRiskFinancialAbuse;
}

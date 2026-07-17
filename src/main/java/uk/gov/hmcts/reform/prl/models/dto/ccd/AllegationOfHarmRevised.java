package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCruCaseworkerPrivatelawSystemupdateCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllegationOfHarmRevised {

    @CCD(
            label = "*Are there allegations of harm?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
    )
    private final YesOrNo newAllegationsOfHarmYesNo;
    @CCD(
            label = "Any form of domestic abuse towards the applicant",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class, CaseworkerPrivatelawLaCruCaseworkerPrivatelawSystemupdateCuAccess.class}
    )
    private final YesOrNo newAllegationsOfHarmDomesticAbuseYesNo;
    @CCD(
            label = "Child abduction",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class, CaseworkerPrivatelawLaCruCaseworkerPrivatelawSystemupdateCuAccess.class}
    )
    private final YesOrNo newAllegationsOfHarmChildAbductionYesNo;
    @CCD(
            label = "Child abuse towards the children in this application",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class, CaseworkerPrivatelawLaCruCaseworkerPrivatelawSystemupdateCuAccess.class}
    )
    private final YesOrNo newAllegationsOfHarmChildAbuseYesNo;
    @CCD(
            label = "Why do you believe the child(ren) may be abducted?",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final String newChildAbductionReasons;
    @CCD(
            label = "Have there been any previous threats, attempts to abduct or actual\nabduction of the child(ren)?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newPreviousAbductionThreats;
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final String newPreviousAbductionThreatsDetails;
    @CCD(
            label = "Where is/are the child(ren) now?",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final String newChildrenLocationNow;
    @CCD(
            label = "Has the passport office been notifed?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newAbductionPassportOfficeNotified;
    @CCD(
            label = "Do any of the children have a passport?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newAbductionChildHasPassport;

    @CCD(label = " ", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonProperty("childPassportDetails")
    private ChildPassportDetails childPassportDetails;

    @CCD(
            label = "Were the police or any other organisation/agency involved in any previous\nincident of attempted abduction or abduction?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newAbductionPreviousPoliceInvolvement;
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final String newAbductionPreviousPoliceInvolvementDetails;
    @CCD(ignore = true)
    private final YesOrNo allegationsOfHarmChildAbuseYesNo;
    @CCD(
            label = "Drugs, alcohol or substance abuse?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newAllegationsOfHarmSubstanceAbuseYesNo;
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final String newAllegationsOfHarmSubstanceAbuseDetails;

    @CCD(
            label = "Non-molestation order",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newOrdersNonMolestation;
    @CCD(
            label = "Occupation order",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newOrdersOccupation;
    @CCD(
            label = "Forced marriage protection order",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newOrdersForcedMarriageProtection;
    @CCD(
            label = "Restraining order",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newOrdersRestraining;
    @CCD(
            label = "Other injunctive order",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newOrdersOtherInjunctive;
    @CCD(
            label = "Undertaking in place of order",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newOrdersUndertakingInPlace;
    @CCD(label = "Date issued", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersNonMolestationDateIssued;
    @CCD(label = "End date", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersNonMolestationEndDate;
    @CCD(
            label = "Is the order current?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newOrdersNonMolestationCurrent;
    @CCD(label = "Name of court", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    private final String newOrdersNonMolestationCourtName;
    @CCD(
            label = "Upload relevant order(s)",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private final Document newOrdersNonMolestationDocument;
    @CCD(label = "Date issued", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersOccupationDateIssued;
    @CCD(label = "End date", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersOccupationEndDate;
    @CCD(
            label = "Is the order current?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newOrdersOccupationCurrent;
    @CCD(label = "Name of court", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    private final String newOrdersOccupationCourtName;
    @CCD(
            label = "Upload relevant order(s)",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private final Document newOrdersOccupationDocument;
    @CCD(label = "Date issued", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersForcedMarriageProtectionDateIssued;
    @CCD(label = "End date", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersForcedMarriageProtectionEndDate;
    @CCD(
            label = "Is the order current?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newOrdersForcedMarriageProtectionCurrent;
    @CCD(label = "Name of court", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    private final String newOrdersForcedMarriageProtectionCourtName;
    @CCD(
            label = "Upload relevant order(s)",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private final Document newOrdersForcedMarriageProtectionDocument;
    @CCD(label = "Date issued", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersRestrainingDateIssued;
    @CCD(label = "End date", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersRestrainingEndDate;
    @CCD(label = "Case number", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    private final String newOrdersNonMolestationCaseNumber;
    @CCD(label = "Case number", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    private final String newOrdersOccupationCaseNumber;
    @CCD(label = "Case number", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    private final String newOrdersForcedMarriageProtectionCaseNumber;
    @CCD(label = "Case number", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    private final String newOrdersRestrainingCaseNumber;
    @CCD(label = "Case number", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    private final String newOrdersOtherInjunctiveCaseNumber;
    @CCD(label = "Case number", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    private final String newOrdersUndertakingInPlaceCaseNumber;
    @CCD(
            label = "Is the order current?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newOrdersRestrainingCurrent;
    @CCD(label = "Name of court", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    private final String newOrdersRestrainingCourtName;
    @CCD(
            label = "Upload relevant order(s)",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private final Document newOrdersRestrainingDocument;
    @CCD(label = "Date issued", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersOtherInjunctiveDateIssued;
    @CCD(label = "End date", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersOtherInjunctiveEndDate;
    @CCD(
            label = "Is the order current?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newOrdersOtherInjunctiveCurrent;
    @CCD(label = "Name of court", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    private final String newOrdersOtherInjunctiveCourtName;
    @CCD(
            label = "Upload relevant order(s)",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private final Document newOrdersOtherInjunctiveDocument;
    @CCD(label = "Date issued", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersUndertakingInPlaceDateIssued;
    @CCD(label = "End date", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate newOrdersUndertakingInPlaceEndDate;
    @CCD(
            label = "Is the order current?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newOrdersUndertakingInPlaceCurrent;
    @CCD(label = "Name of court", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    private final String newOrdersUndertakingInPlaceCourtName;
    @CCD(
            label = "Upload relevant order(s)",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private final Document newOrdersUndertakingInPlaceDocument;
    @CCD(
            label = "Other safety or welfare concerns?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newAllegationsOfHarmOtherConcerns;
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final String newAllegationsOfHarmOtherConcernsDetails;
    @CCD(
            label = "What steps or orders does the applicant want the court to take or make to\nprotect the safety of the child(ren) and/or themselves?",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final String newAllegationsOfHarmOtherConcernsCourtActions;
    @CCD(
            label = "Do you agree to the child(ren) spending unsupervised time with the other\nperson(s) in receipt of this form?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newAgreeChildUnsupervisedTime;
    @CCD(
            label = "Do you agree to the child(ren) spending supervised time with the other\nperson(s) in receipt of this form?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newAgreeChildSupervisedTime;
    @CCD(
            label = "Do you agree to the child having other forms of contact with the other\nperson in receipt of this form? (by telephone, text, email, social media)",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final YesOrNo newAgreeChildOtherContact;

    // for the new text box added under Drugs,alcolhol,substance abuse Radio Button , Other safety or welfare concerns Radio button
    @CCD(ignore = true)
    private final String allegationsOfHarmOtherConcernsDetails;

    @CCD(
            label = "Behaviour",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DomesticBehaviours",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    @JsonProperty("domesticBehaviours")
    private final List<Element<DomesticAbuseBehaviours>> domesticBehaviours;

    @CCD(
            label = "Type of abuse",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ChildTypeOfAbuse",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    private final List<ChildAbuseEnum> childAbuses;

    @CCD(label = " ", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonProperty("childPhysicalAbuse")
    private final ChildAbuse childPhysicalAbuse;

    @CCD(label = " ", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonProperty("childPsychologicalAbuse")
    private final ChildAbuse childPsychologicalAbuse;

    @CCD(label = " ", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonProperty("childFinancialAbuse")
    private final ChildAbuse childFinancialAbuse;

    @CCD(label = " ", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonProperty("childSexualAbuse")
    private final ChildAbuse childSexualAbuse;

    @CCD(label = " ", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonProperty("childEmotionalAbuse")
    private final ChildAbuse childEmotionalAbuse;

    @CCD(label = "Behaviour", access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class})
    @JsonProperty("childAbuseBehavioursDocmosis")
    private final  List<Element<ChildAbuseBehaviour>> childAbuseBehavioursDocmosis;

    @CCD(
            label = "Are all the children in the application at risk from this behaviour?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    @JsonProperty("allChildrenAreRiskPhysicalAbuse")
    private YesOrNo allChildrenAreRiskPhysicalAbuse;

    @CCD(
            label = "Are all the children in the application at risk from this behaviour?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    @JsonProperty("allChildrenAreRiskPsychologicalAbuse")
    private YesOrNo allChildrenAreRiskPsychologicalAbuse;

    @CCD(
            label = "Are all the children in the application at risk from this behaviour?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    @JsonProperty("allChildrenAreRiskSexualAbuse")
    private YesOrNo allChildrenAreRiskSexualAbuse;

    @CCD(
            label = "Are all the children in the application at risk from this behaviour?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    @JsonProperty("allChildrenAreRiskEmotionalAbuse")
    private YesOrNo allChildrenAreRiskEmotionalAbuse;

    @CCD(
            label = "Are all the children in the application at risk from this behaviour?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    @JsonProperty("allChildrenAreRiskFinancialAbuse")
    private YesOrNo allChildrenAreRiskFinancialAbuse;

    @CCD(
            label = "Which children are at risk?",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    @JsonProperty("whichChildrenAreRiskPhysicalAbuse")
    private DynamicMultiSelectList whichChildrenAreRiskPhysicalAbuse;

    @CCD(
            label = "Which children are at risk?",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    @JsonProperty("whichChildrenAreRiskPsychologicalAbuse")
    private DynamicMultiSelectList whichChildrenAreRiskPsychologicalAbuse;

    @CCD(
            label = "Which children are at risk?",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    @JsonProperty("whichChildrenAreRiskSexualAbuse")
    private DynamicMultiSelectList whichChildrenAreRiskSexualAbuse;

    @CCD(
            label = "Which children are at risk?",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    @JsonProperty("whichChildrenAreRiskEmotionalAbuse")
    private DynamicMultiSelectList whichChildrenAreRiskEmotionalAbuse;

    @CCD(
            label = "Which children are at risk?",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
    )
    @JsonProperty("whichChildrenAreRiskFinancialAbuse")
    private DynamicMultiSelectList whichChildrenAreRiskFinancialAbuse;



  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Types of harm")
  private String typesOfHarmRevised;
  // ==== end synthesised definition-only fields ====
}

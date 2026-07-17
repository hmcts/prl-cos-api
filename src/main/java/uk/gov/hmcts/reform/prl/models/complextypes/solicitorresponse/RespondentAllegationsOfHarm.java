package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class RespondentAllegationsOfHarm {

    @CCD(
            label = "*Any form of domestic abuse towards the respondent",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo respondentDomesticAbuse;
    @CCD(
            label = "*Child abuse towards the children in this application",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo respondentChildAbuse;
    @CCD(label = "*Child abduction", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isRespondentChildAbduction;
    @CCD(label = "*Drugs, alcohol or substance abuse", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respondentDrugOrAlcoholAbuse;
    @CCD(
            label = "*Give details",
            showCondition = "respondentDrugOrAlcoholAbuse=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String respondentDrugOrAlcoholAbuseDetails;
    @CCD(label = "*Other safety or welfare concerns", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respondentOtherSafetyConcerns;
    @CCD(
            label = "*Give details",
            showCondition = "respondentOtherSafetyConcerns=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String respondentOtherSafetyConcernsDetails;
    @CCD(label = "*Non-molestation order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respondentNonMolestationOrder;
    @CCD(label = "Date issued", showCondition = "respondentNonMolestationOrder=\"Yes\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentNonMolestationOrderIssueDate;
    @CCD(label = "End date", showCondition = "respondentNonMolestationOrder=\"Yes\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentNonMolestationOrderEndDate;
    @CCD(
            label = "Is the order current?",
            showCondition = "respondentNonMolestationOrder=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo respondentNonMolestationOrderIsCurrent;
    @CCD(label = "Name of court", showCondition = "respondentNonMolestationOrder=\"Yes\"", searchable = false)
    private final String respondentNonMolestationOrderCourt;
    @CCD(label = "Case number ", showCondition = "respondentNonMolestationOrder=\"Yes\"", searchable = false)
    private final String respondentNonMolestationOrderCaseNumber;
    @CCD(
            label = "Upload relevant order(s)",
            showCondition = "respondentNonMolestationOrder=\"Yes\"",
            searchable = false
    )
    private final Document respondentNonMolestationOrderDocument;
    @CCD(label = "*Occupation order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respondentOccupationOrder;
    @CCD(label = "Date issued", showCondition = "respondentOccupationOrder=\"Yes\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentOccupationOrderIssueDate;
    @CCD(label = "End date", showCondition = "respondentOccupationOrder=\"Yes\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentOccupationOrderEndDate;
    @CCD(
            label = "Is the order current?",
            showCondition = "respondentOccupationOrder=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo respondentOccupationOrderIsCurrent;
    @CCD(label = "Name of court", showCondition = "respondentOccupationOrder=\"Yes\"", searchable = false)
    private final String respondentOccupationOrderCourt;
    @CCD(label = "Case number ", showCondition = "respondentOccupationOrder=\"Yes\"", searchable = false)
    private final String respondentOccupationOrderCaseNumber;
    @CCD(label = "Upload relevant order(s)", showCondition = "respondentOccupationOrder=\"Yes\"", searchable = false)
    private final Document respondentOccupationOrderDocument;

    @CCD(label = "*Forced marriage protection order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respondentForcedMarriageOrder;
    @CCD(label = "Date issued", showCondition = "respondentForcedMarriageOrder=\"Yes\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentForcedMarriageIssueDate;
    @CCD(label = "End date", showCondition = "respondentForcedMarriageOrder=\"Yes\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentForcedMarriageEndDate;
    @CCD(
            label = "Is the order current?",
            showCondition = "respondentForcedMarriageOrder=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo respondentForcedMarriageIsCurrent;
    @CCD(label = "Name of court", showCondition = "respondentForcedMarriageOrder=\"Yes\"", searchable = false)
    private final String respondentForcedMarriageCourt;
    @CCD(label = "Case number ", showCondition = "respondentForcedMarriageOrder=\"Yes\"", searchable = false)
    private final String respondentForcedMarriageCaseNumber;
    @CCD(
            label = "Upload relevant order(s)",
            showCondition = "respondentForcedMarriageOrder=\"Yes\"",
            searchable = false
    )
    private final Document respondentForcedMarriageDocument;

    @CCD(label = "*Restraining order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respondentRestrainingOrder;
    @CCD(label = "Date issued", showCondition = "respondentRestrainingOrder=\"Yes\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentRestrainingIssueDate;
    @CCD(label = "End date", showCondition = "respondentRestrainingOrder=\"Yes\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentRestrainingEndDate;
    @CCD(
            label = "Is the order current?",
            showCondition = "respondentRestrainingOrder=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo respondentRestrainingIsCurrent;
    @CCD(label = "Name of court", showCondition = "respondentRestrainingOrder=\"Yes\"", searchable = false)
    private final String respondentRestrainingCourt;
    @CCD(label = "Case number ", showCondition = "respondentRestrainingOrder=\"Yes\"", searchable = false)
    private final String respondentRestrainingCaseNumber;
    @CCD(label = "Upload relevant order(s)", showCondition = "respondentRestrainingOrder=\"Yes\"", searchable = false)
    private final Document respondentRestrainingDocument;

    @CCD(label = "*Other injunctive order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respondentOtherInjunctiveOrder;
    @CCD(label = "Date issued", showCondition = "respondentOtherInjunctiveOrder=\"Yes\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentOtherInjunctiveIssueDate;
    @CCD(label = "End date", showCondition = "respondentOtherInjunctiveOrder=\"Yes\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentOtherInjunctiveEndDate;
    @CCD(
            label = "Is the order current?",
            showCondition = "respondentOtherInjunctiveOrder=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo respondentOtherInjunctiveIsCurrent;
    @CCD(label = "Name of court", showCondition = "respondentOtherInjunctiveOrder=\"Yes\"", searchable = false)
    private final String respondentOtherInjunctiveCourt;
    @CCD(label = "Case number ", showCondition = "respondentOtherInjunctiveOrder=\"Yes\"", searchable = false)
    private final String respondentOtherInjunctiveCaseNumber;
    @CCD(
            label = "Upload relevant order(s)",
            showCondition = "respondentOtherInjunctiveOrder=\"Yes\"",
            searchable = false
    )
    private final Document respondentOtherInjunctiveDocument;

    @CCD(label = "*Undertaking in place of order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respondentUndertakingOrder;
    @CCD(label = "Date issued", showCondition = "respondentUndertakingOrder=\"Yes\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentUndertakingIssueDate;
    @CCD(label = "End date", showCondition = "respondentUndertakingOrder=\"Yes\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentUndertakingEndDate;
    @CCD(
            label = "Is the order current?",
            showCondition = "respondentUndertakingOrder=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo respondentUndertakingIsCurrent;
    @CCD(label = "Name of court", showCondition = "respondentUndertakingOrder=\"Yes\"", searchable = false)
    private final String respondentUndertakingCourt;
    @CCD(label = "Case number ", showCondition = "respondentUndertakingOrder=\"Yes\"", searchable = false)
    private final String respondentUndertakingCaseNumber;
    @CCD(label = "Upload relevant order(s)", showCondition = "respondentUndertakingOrder=\"Yes\"", searchable = false)
    private final Document respondentUndertakingDocument;


  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Orders", searchable = false, typeOverride = FieldType.Label)
  private String respondentOrdersLabel;
  @CCD(
          label = "*Has the respondent had (or does the respondent currently have) any of these orders?",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String respondentHasOrdersLabel;
  // ==== end synthesised definition-only fields ====
}

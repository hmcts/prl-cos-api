package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.manageorders.DateOrderEndsTimeEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentNotToDoEnum1;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentNotToDoEnum2;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentNotToDoEnum3;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentNotToDoEnum4;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentNotToDoEnum5;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentNotToDoEnum6;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentNotToDoEnum7;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentNotToDoEnum8;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentNotToDoEnum9;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentNotToDoEnum10;
import uk.gov.hmcts.reform.prl.models.dto.ccd.OrderNoticeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CourtDeclaresEnum1;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CourtDeclaresEnum2;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CourtDeclaresEnum3;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CourtDeclaresEnum4;
import uk.gov.hmcts.reform.prl.models.dto.ccd.TheRespondentEnum1;
import uk.gov.hmcts.reform.prl.models.dto.ccd.TheRespondentEnum2;
import uk.gov.hmcts.reform.prl.models.dto.ccd.TheRespondentEnum3;
import uk.gov.hmcts.reform.prl.models.dto.ccd.TheRespondentEnum4;
import uk.gov.hmcts.reform.prl.models.dto.ccd.TheRespondentEnum5;
import uk.gov.hmcts.reform.prl.models.dto.ccd.TheRespondentEnum6;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FL400", generate = true)
@Builder(toBuilder = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FL404 {

    @CCD(label = "Court name", searchable = false)
    private final String fl404bCourtName;
    @CCD(
            label = "Court address",
            showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.AddressUK
    )
    private final Address fl404bCourtAddress;
    @CCD(label = "Case number", showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"", searchable = false)
    private final String fl404bCaseNumber;
    @CCD(label = "Applicant name", showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"", searchable = false)
    private final String fl404bApplicantName;
    @CCD(label = "Applicant reference", showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"", searchable = false)
    private final String fl404bApplicantReference;
    @CCD(label = "Respondent name", showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"", searchable = false)
    private final String fl404bRespondentName;
    @CCD(label = "Respondent reference", showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"", searchable = false)
    private final String fl404bRespondentReference;
    @CCD(label = "Respondent date of birth", showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate fl404bRespondentDob;
    @CCD(label = "Respondent address", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address fl404bRespondentAddress;
    @CCD(label = "Hearing outcome", searchable = false, typeOverride = FieldType.TextArea)
    private final String fl404bHearingOutcome;
    //private final String fl404bChangedCourtLocation;

    @CCD(
            label = "Paragraphs of the order to which the power of arrest applies",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String fl404bPowerOfArrestParagraph;
    @CCD(
            label = "Is there risk of significant harm to the applicant or children if ther power of arrest is not attached immediately?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final String fl404bRiskOfSignificantHarm;
    @CCD(label = "Date order made", searchable = false, typeOverride = FieldType.Date)
    private final String fl404bDateOrderMade;
    @CCD(label = "Date order ends", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final LocalDateTime fl404bDateOrderEnd;
    @CCD(label = "Time", searchable = false)
    private final String fl404bDateOrderEndTime;

    @CCD(label = "Does the order mention a property?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final String fl404bMentionedProperty;
    @CCD(label = "Add the address of the property", searchable = false, typeOverride = FieldType.TextArea)
    private final String fl404bAddressOfProperty;
    @CCD(
            label = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "respondentNotToDoEnum1",
            typeParameterClass = RespondentNotToDoEnum1.class
    )
    private final List<String> fl404bRespondentNotToThreat;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "respondentNotToDoEnum2",
            typeParameterClass = RespondentNotToDoEnum2.class
    )
    private final List<String> fl404bRespondentNotIntimidate;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "respondentNotToDoEnum3",
            typeParameterClass = RespondentNotToDoEnum3.class
    )
    private final List<String> fl404bRespondentNotToTelephone;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "respondentNotToDoEnum4",
            typeParameterClass = RespondentNotToDoEnum4.class
    )
    private final List<String> fl404bRespondentNotToDamageOrThreat;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "respondentNotToDoEnum5",
            typeParameterClass = RespondentNotToDoEnum5.class
    )
    private final List<String> fl404bRespondentNotToDamage;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "respondentNotToDoEnum6",
            typeParameterClass = RespondentNotToDoEnum6.class
    )
    private final List<String> fl404bRespondentNotToEnterProperty;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "respondentNotToDoEnum7",
            typeParameterClass = RespondentNotToDoEnum7.class
    )
    private final List<String> fl404bRespondentNotToThreatChild;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "respondentNotToDoEnum8",
            typeParameterClass = RespondentNotToDoEnum8.class
    )
    private final List<String> fl404bRespondentNotHarassOrIntimidate;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "respondentNotToDoEnum9",
            typeParameterClass = RespondentNotToDoEnum9.class
    )
    private final List<String> fl404bRespondentNotToTelephoneChild;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "respondentNotToDoEnum10",
            typeParameterClass = RespondentNotToDoEnum10.class
    )
    private final List<String> fl404bRespondentNotToEnterSchool;
    @CCD(label = "Add further details", searchable = false, typeOverride = FieldType.TextArea)
    private final String fl404bAddMoreDetailsPhoneChild;
    @CCD(label = "Add further details", searchable = false, typeOverride = FieldType.TextArea)
    private final String fl404bAddMoreDetailsTelephone;
    @CCD(label = "Add further details", searchable = false, typeOverride = FieldType.TextArea)
    private final String fl404bAddMoreDetailsProperty;
    @CCD(label = "Add school name", searchable = false)
    private final String fl404bAddSchool;
    @CCD(label = "Add further details", searchable = false, typeOverride = FieldType.TextArea)
    private final String fl404bAddMoreDetailsSchool;
    @CCD(label = "Court name", searchable = false)
    private final String fl404bCourtName1;
    @CCD(label = "Court address", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address fl404bOtherCourtAddress;
    @CCD(label = "Costs of this application", searchable = false, typeOverride = FieldType.TextArea)
    private final String fl404bCostOfApplication;
    @CCD(
            label = "Is this order made with or without notice?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "OrderNoticeEnum",
            typeParameterClass = OrderNoticeEnum.class
    )
    private final String fl404bIsNoticeGiven;
    @CCD(label = "Time estimate", searchable = false)
    private final String fl404bTimeEstimate;
    @CCD(label = "Address the order applies to", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address fl404bAddressAppliedFor;
    @CCD(
            label = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "courtDeclaresEnum1",
            typeParameterClass = CourtDeclaresEnum1.class
    )
    private final List<String> fl404bApplicantIsEntitledToOccupy;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "courtDeclaresEnum2",
            typeParameterClass = CourtDeclaresEnum2.class
    )
    private final List<String> fl404bApplicantHasHomeRight;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "courtDeclaresEnum3",
            typeParameterClass = CourtDeclaresEnum3.class
    )
    private final List<String> fl404bApplicantHasRightToEnter;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "courtDeclaresEnum4",
            typeParameterClass = CourtDeclaresEnum4.class
    )
    private final List<String> fl404bApplicantHasOtherInstruction;
    @CCD(label = "Add details about home rights", searchable = false, typeOverride = FieldType.TextArea)
    private final String fl404bApplicantHomeInstruction;
    @CCD(
            label = "Add another instruction relating to the applicant",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String fl404bApplicantOtherInstruction;
    @CCD(
            label = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "theRespondentEnum1",
            typeParameterClass = TheRespondentEnum1.class
    )
    private final List<String> fl404bApplicantAllowedToOccupy;
    @CCD(
            label = "Is a power of arrest attached to this paragraph?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final String fl404bIsPowerOfArrest1;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "theRespondentEnum2",
            typeParameterClass = TheRespondentEnum2.class
    )
    private final List<String> fl404bRespondentMustNotOccupyAddress;

    @CCD(
            label = "Is a power of arrest attached to this paragraph?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final String fl404bIsPowerOfArrest2;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "theRespondentEnum3",
            typeParameterClass = TheRespondentEnum3.class
    )
    private final List<String> fl404bRespondentShallLeaveAddress;
    @CCD(label = "Add when they shall leave", searchable = false, typeOverride = FieldType.TextArea)
    private final String fl404bWhenRespondentShallLeave;
    @CCD(
            label = "Is a power of arrest attached to this paragraph?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final String fl404bIsPowerOfArrest3;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "theRespondentEnum4",
            typeParameterClass = TheRespondentEnum4.class
    )
    private final List<String> fl404bRespondentMustNotEnterAddress;
    @CCD(label = "Add more details", searchable = false, typeOverride = FieldType.TextArea)
    private final String fl404bAddMoreDetails;
    @CCD(
            label = "Is a power of arrest attached to this paragraph?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final String fl404bIsPowerOfArrest4;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "theRespondentEnum5",
            typeParameterClass = TheRespondentEnum5.class
    )
    private final List<String> fl404bRespondentObstructOrHarass;
    @CCD(
            label = "Is a power of arrest attached to this paragraph?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final String fl404bIsPowerOfArrest5;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "theRespondentEnum6",
            typeParameterClass = TheRespondentEnum6.class
    )
    private final List<String> fl404bRespondentOtherInstructions;
    @CCD(
            label = "Add another instruction relating to the respondent",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String fl404bAddAnotherInstructions;
    @CCD(
            label = "Is a power of arrest attached to this paragraph?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final String fl404bIsPowerOfArrest6;
    @CCD(label = "between", searchable = false, typeOverride = FieldType.DateTime)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final String fl404bOccupationDate1;
    @CCD(label = "Time", searchable = false)
    private final String fl404bOccupationTime1;
    @CCD(label = "and", searchable = false, typeOverride = FieldType.DateTime)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final String fl404bOccupationDate2;
    @CCD(label = "Time", searchable = false)
    private final String fl404bOccupationTime2;
    @CCD(label = "Date and place of next hearing", searchable = false, typeOverride = FieldType.Date)
    private final String fl404bDateOfNextHearing;
    @CCD(label = "Time", searchable = false)
    private final String fl404bTimeOfNextHearing;

    //Draft order changes
    @CCD(label = " ", searchable = false)
    @JsonProperty("addDirections")
    private final List<Element<DirectionDetails>> addDirections;

    @CCD(label = "How long will the order be in force?", searchable = false)
    private DateOrderEndsTimeEnum orderEndDateAndTimeOptions;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime orderSpecifiedDateTime;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "# The respondent must not do the following:", searchable = false, typeOverride = FieldType.Label)
  private String fl404bRespondentLabel;
  @CCD(label = "**The court declares that the applicant**", searchable = false, typeOverride = FieldType.Label)
  private String fl404bApplicantLabel;
  @CCD(label = "**The respondent**", searchable = false, typeOverride = FieldType.Label)
  private String fl404bRespondentCheckListLabel;
  @CCD(label = "Date and place of next hearing", searchable = false)
  private java.time.LocalDateTime fl404bDateAndTimeOfNextHearing;
  // ==== end synthesised definition-only fields ====
}

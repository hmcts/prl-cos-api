package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.reform.prl.enums.HearingChannelsEnum;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.HearingPriorityTypeEnum;
import uk.gov.hmcts.reform.prl.enums.HearingSpecificDatesOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.HearingDateTimeOption;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingDataFromTabToDocmosis;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
public class HearingData {

    @CCD(label = "Hearing Type", typeOverride = FieldType.DynamicList)
    private DynamicList hearingTypes;

    @CCD(
            label = "Hearing Id",
            showCondition = "hearingId=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String hearingId;

    @CCD(label = "Select from hearing", typeOverride = FieldType.DynamicList)
    private DynamicList confirmedHearingDates;

    @CCD(label = "Select from hearing", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo displayConfirmedHearing = YesOrNo.No;

    @CCD(
            label = "hearing channels",
            showCondition = "hearingChannels=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.DynamicList
    )
    private DynamicList hearingChannels;

    @CCD(label = "Select a platform", typeOverride = FieldType.DynamicList)
    private DynamicList hearingVideoChannels;

    @CCD(label = "Select a platform", typeOverride = FieldType.DynamicList)
    private DynamicList hearingTelephoneChannels;

    @CCD(label = "Hearing location", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList courtList;

    @CCD(label = "Local authority", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList localAuthorityHearingChannel;

    @CCD(label = "Hearing listed with a linked case", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList hearingListedLinkedCases;

    @CCD(label = "${applicantSolicitor}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicantSolicitorHearingChannel;

    @CCD(label = "${respondentName}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList respondentHearingChannel;

    @CCD(label = "${respondentSolicitor}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList respondentSolicitorHearingChannel;

    @CCD(label = "Cafcass", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList cafcassHearingChannel;

    @CCD(label = "Cafcass Cymru", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList cafcassCymruHearingChannel;

    @CCD(label = "${applicantName}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicantHearingChannel;

    @CCD(label = "Has the hearing date been confirmed?", searchable = false)
    @JsonSerialize(using = CustomEnumSerializer.class)
    @JsonProperty("hearingDateConfirmOptionEnum")
    private HearingDateConfirmOptionEnum hearingDateConfirmOptionEnum;

    @CCD(label = "Additional hearing details", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("additionalHearingDetails")
    private String additionalHearingDetails;

    @CCD(
            label = "Insert joining instructions for remote hearing",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("instructionsForRemoteHearing")
    private String instructionsForRemoteHearing;

    @CCD(label = " ", searchable = false)
    @JsonProperty("hearingDateTimes")
    private List<Element<HearingDateTimeOption>> hearingDateTimes;

    @CCD(label = "Hours")
    @JsonProperty("hearingEstimatedHours")
    private final String hearingEstimatedHours;

    @CCD(label = "Minutes")
    @JsonProperty("hearingEstimatedMinutes")
    private final String hearingEstimatedMinutes;

    @CCD(label = "Days")
    @JsonProperty("hearingEstimatedDays")
    private final String hearingEstimatedDays;

    @CCD(
            label = "Will all parties attend the hearing in the same way?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("allPartiesAttendHearingSameWayYesOrNo")
    private final YesOrNo allPartiesAttendHearingSameWayYesOrNo;

    @CCD(label = "This hearing will be before", searchable = false)
    @JsonSerialize(using = CustomEnumSerializer.class)
    @JsonProperty("hearingAuthority")
    private DioBeforeAEnum hearingAuthority;

    @CCD(label = "How does the hearing need to take place?", searchable = false)
    @JsonSerialize(using = CustomEnumSerializer.class)
    @JsonProperty("hearingChannelsEnum")
    private HearingChannelsEnum hearingChannelsEnum;

    @CCD(label = "Hearing judge", typeOverride = FieldType.JudicialUser)
    @JsonProperty("hearingJudgeNameAndEmail")
    private final JudicialUser hearingJudgeNameAndEmail;

    @CCD(label = "hearingJudgePersonalCode", showCondition = "hearingJudgePersonalCode=\"DO_NOT_SHOW\"")
    @JsonProperty("hearingJudgePersonalCode")
    private String hearingJudgePersonalCode;

    @CCD(label = "Last name", showCondition = "hearingJudgeLastName=\"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("hearingJudgeLastName")
    private String hearingJudgeLastName;

    @CCD(label = "Email address", showCondition = "hearingJudgeEmailAddress=\"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("hearingJudgeEmailAddress")
    private String hearingJudgeEmailAddress;

    @CCD(label = "applicantName", showCondition = "applicantName=\"DO_NOT_SHOW\"", searchable = false)
    private String applicantName;
    @CCD(label = "applicant Solicitor", showCondition = "applicantSolicitor=\"DO_NOT_SHOW\"", searchable = false)
    private String applicantSolicitor;
    @CCD(label = "respondent Name", showCondition = "respondentName=\"DO_NOT_SHOW\"", searchable = false)
    private String respondentName;
    @CCD(label = "respondent Solicitor", showCondition = "respondentSolicitor=\"DO_NOT_SHOW\"", searchable = false)
    private String respondentSolicitor;

    @CCD(label = "Does the hearing need to take place on a specific date?", searchable = false)
    @JsonProperty("hearingSpecificDatesOptionsEnum")
    private HearingSpecificDatesOptionsEnum hearingSpecificDatesOptionsEnum;


    @CCD(label = "First date of the hearing", searchable = false)
    @JsonProperty("firstDateOfTheHearing")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate firstDateOfTheHearing;

    @CCD(label = "Hour")
    @JsonProperty("hearingMustTakePlaceAtHour")
    private String hearingMustTakePlaceAtHour;

    @CCD(label = "Minute")
    @JsonProperty("hearingMustTakePlaceAtMinute")
    private String hearingMustTakePlaceAtMinute;

    @CCD(label = "Earliest hearing date", searchable = false)
    @JsonProperty("earliestHearingDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate earliestHearingDate;

    @CCD(label = "Latest hearing date", searchable = false)
    @JsonProperty("latestHearingDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate latestHearingDate;

    @CCD(label = "What is the priority level of this hearing?", searchable = false)
    @JsonProperty("hearingPriorityTypeEnum")
    private HearingPriorityTypeEnum hearingPriorityTypeEnum;

    @CCD(label = "Custom details", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("customDetails")
    private  String customDetails;

    @CCD(label = "Is rendering required flag?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("isRenderingRequiredFlag")
    private YesOrNo isRenderingRequiredFlag;

    @CCD(label = " ", showCondition = "fillingFormRenderingInfo=\"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("fillingFormRenderingInfo")
    private String fillingFormRenderingInfo;

    @CCD(label = "(Applicant1)", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicantHearingChannel1;
    @CCD(label = "(Applicant2)", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicantHearingChannel2;
    @CCD(label = "(Applicant3)", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicantHearingChannel3;
    @CCD(label = "(Applicant4)", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicantHearingChannel4;
    @CCD(label = "(Applicant5)", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicantHearingChannel5;

    @CCD(label = "${applicantSolicitor1}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicantSolicitorHearingChannel1;
    @CCD(label = "${applicantSolicitor2}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicantSolicitorHearingChannel2;
    @CCD(label = "${applicantSolicitor3}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicantSolicitorHearingChannel3;
    @CCD(label = "${applicantSolicitor4}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicantSolicitorHearingChannel4;
    @CCD(label = "${applicantSolicitor5}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicantSolicitorHearingChannel5;

    @CCD(label = "${respondentName1}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList respondentHearingChannel1;
    @CCD(label = "${respondentName2}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList respondentHearingChannel2;
    @CCD(label = "${respondentName3}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList respondentHearingChannel3;
    @CCD(label = "${respondentName4}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList respondentHearingChannel4;
    @CCD(label = "${respondentName5}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList respondentHearingChannel5;

    @CCD(label = "${respondentSolicitor1}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList respondentSolicitorHearingChannel1;
    @CCD(label = "${respondentSolicitor2}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList respondentSolicitorHearingChannel2;
    @CCD(label = "${respondentSolicitor3}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList respondentSolicitorHearingChannel3;
    @CCD(label = "${respondentSolicitor4}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList respondentSolicitorHearingChannel4;
    @CCD(label = "${respondentSolicitor5}", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList respondentSolicitorHearingChannel5;

    @CCD(label = "applicantName1", showCondition = "applicantName1=\"DO_NOT_SHOW\"", searchable = false)
    private String applicantName1;
    @CCD(label = "applicantName2", showCondition = "applicantName2=\"DO_NOT_SHOW\"", searchable = false)
    private String applicantName2;
    @CCD(label = "applicantName3", showCondition = "applicantName3=\"DO_NOT_SHOW\"", searchable = false)
    private String applicantName3;
    @CCD(label = "applicantName4", showCondition = "applicantName4=\"DO_NOT_SHOW\"", searchable = false)
    private String applicantName4;
    @CCD(label = "applicantName5", showCondition = "applicantName5=\"DO_NOT_SHOW\"", searchable = false)
    private String applicantName5;

    @CCD(label = "applicant Solicitor1", showCondition = "applicantSolicitor1=\"DO_NOT_SHOW\"", searchable = false)
    private String applicantSolicitor1;
    @CCD(label = "applicant Solicitor2", showCondition = "applicantSolicitor2=\"DO_NOT_SHOW\"", searchable = false)
    private String applicantSolicitor2;
    @CCD(label = "applicant Solicitor3", showCondition = "applicantSolicitor3=\"DO_NOT_SHOW\"", searchable = false)
    private String applicantSolicitor3;
    @CCD(label = "applicant Solicitor4", showCondition = "applicantSolicitor4=\"DO_NOT_SHOW\"", searchable = false)
    private String applicantSolicitor4;
    @CCD(label = "applicant Solicitor5", showCondition = "applicantSolicitor5=\"DO_NOT_SHOW\"", searchable = false)
    private String applicantSolicitor5;

    @CCD(label = "respondent Name1", showCondition = "respondentName1=\"DO_NOT_SHOW\"", searchable = false)
    private String respondentName1;
    @CCD(label = "respondent Name2", showCondition = "respondentName2=\"DO_NOT_SHOW\"", searchable = false)
    private String respondentName2;
    @CCD(label = "respondent Name3", showCondition = "respondentName3=\"DO_NOT_SHOW\"", searchable = false)
    private String respondentName3;
    @CCD(label = "respondent Name4", showCondition = "respondentName4=\"DO_NOT_SHOW\"", searchable = false)
    private String respondentName4;
    @CCD(label = "respondent Name5", showCondition = "respondentName5=\"DO_NOT_SHOW\"", searchable = false)
    private String respondentName5;

    @CCD(label = "respondent Solicitor1", showCondition = "respondentSolicitor1=\"DO_NOT_SHOW\"", searchable = false)
    private String respondentSolicitor1;
    @CCD(label = "respondent Solicitor2", showCondition = "respondentSolicitor2=\"DO_NOT_SHOW\"", searchable = false)
    private String respondentSolicitor2;
    @CCD(label = "respondent Solicitor3", showCondition = "respondentSolicitor3=\"DO_NOT_SHOW\"", searchable = false)
    private String respondentSolicitor3;
    @CCD(label = "respondent Solicitor4", showCondition = "respondentSolicitor4=\"DO_NOT_SHOW\"", searchable = false)
    private String respondentSolicitor4;
    @CCD(label = "respondent Solicitor5", showCondition = "respondentSolicitor5=\"DO_NOT_SHOW\"", searchable = false)
    private String respondentSolicitor5;
    @CCD(label = " ", searchable = false)
    private List<Element<HearingDataFromTabToDocmosis>> hearingdataFromHearingTab;

    @CCD(
            label = "Is Cafcass or Cafcass Cymru?",
            showCondition = "isCafcassCymru=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isCafcassCymru;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("additionalDetailsForHearingDateOptions")
    private String additionalDetailsForHearingDateOptions;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "## Add new Hearing", searchable = false, typeOverride = FieldType.Label)
  private String addNewHearingLabel;
  @CCD(label = "**The date is confirmed in the Hearings tab**", searchable = false, typeOverride = FieldType.Label)
  private String dateIsConfirmedInTheHearingsTabLabel;
  @CCD(label = "Selected hearing", searchable = false)
  private String transientConfirmedHearingDetail;
  @CCD(label = "Selected hearing label", searchable = false, typeOverride = FieldType.Label)
  private String labelConfirmedHearingDetail;
  @CCD(label = "**The date is reserved with List Assist**", searchable = false, typeOverride = FieldType.Label)
  private String dateIsReservedWithListAssistLabel;
  @CCD(
          label = "**The date needs to be confirmed by the listing team before service**",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String dateNeedsToBeConfirmedByListingTeamBeforeServiceLabel;
  @CCD(
          label = "**This order will be served with the 'date to be fixed'**",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String orderWillBeServedWithTheDateToBeFixedLabel;
  @CCD(
          label = "${listWithoutNoticeHearingDetails.fillingFormRenderingInfo}",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String fillingFormRenderingInfoLabel;
  @CCD(label = "**Yes**", searchable = false, typeOverride = FieldType.Label)
  private String yesLabel;
  @CCD(label = "The hearing must take place at", searchable = false, typeOverride = FieldType.Label)
  private String theHearingMustTakePlaceAtLabel;
  @CCD(label = "**It needs to take place between certain dates**", searchable = false, typeOverride = FieldType.Label)
  private String hearingNeedsBetweenCertianDatesLabel;
  @CCD(
          label = "**Estimated time**\n\nA minimum of one input is required",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String estimatedTimeLabel;
  @CCD(label = "**Video**", searchable = false, typeOverride = FieldType.Label)
  private String hearingVideoChannelsLabel;
  @CCD(label = "**Telephone**", searchable = false, typeOverride = FieldType.Label)
  private String hearingTelephoneChannelsLabel;
  @CCD(
          label = "**This is test note, note to be confirmed service team or UCD**",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String infoToBeDisplayedFor3And4OptionsLabel;
  @CCD(
          label = "**The date needs to be confirmed by the listing team before service (Optional)**",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String hearingDateToBeConfirmedLabel;
  @CCD(
          label = "**This order will be served with the 'date to be fixed' (Optional)**",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String hearingDateToBeFixedLabel;
  // ==== end synthesised definition-only fields ====
}

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

@Data
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "automatedHearingDataBuilder")
@AllArgsConstructor
public class AutomatedHearingData {

    private DynamicList hearingTypes;

    private String hearingId;

    private DynamicList confirmedHearingDates;

    private DynamicList hearingChannels;

    private DynamicList hearingVideoChannels;

    private DynamicList hearingTelephoneChannels;

    private DynamicList courtList;

    private DynamicList localAuthorityHearingChannel;

    private DynamicList hearingListedLinkedCases;

    private DynamicList applicantSolicitorHearingChannel;

    private DynamicList respondentHearingChannel;

    private DynamicList respondentSolicitorHearingChannel;

    private DynamicList cafcassHearingChannel;

    private DynamicList cafcassCymruHearingChannel;

    private DynamicList applicantHearingChannel;

    @JsonSerialize(using = CustomEnumSerializer.class)
    @JsonProperty("hearingDateConfirmOptionEnum")
    private HearingDateConfirmOptionEnum hearingDateConfirmOptionEnum;

    @JsonProperty("additionalHearingDetails")
    private String additionalHearingDetails;

    @JsonProperty("instructionsForRemoteHearing")
    private String instructionsForRemoteHearing;

    @JsonProperty("hearingDateTimes")
    private List<Element<HearingDateTimeOption>> hearingDateTimes;

    @JsonProperty("hearingEstimatedHours")
    private final String hearingEstimatedHours;

    @JsonProperty("hearingEstimatedMinutes")
    private final String hearingEstimatedMinutes;

    @JsonProperty("hearingEstimatedDays")
    private final String hearingEstimatedDays;

    @JsonProperty("allPartiesAttendHearingSameWayYesOrNo")
    private final YesOrNo allPartiesAttendHearingSameWayYesOrNo;

    @JsonSerialize(using = CustomEnumSerializer.class)
    @JsonProperty("hearingAuthority")
    private DioBeforeAEnum hearingAuthority;

    @JsonSerialize(using = CustomEnumSerializer.class)
    @JsonProperty("hearingChannelsEnum")
    private HearingChannelsEnum hearingChannelsEnum;

    @JsonProperty("hearingJudgeNameAndEmail")
    private final JudicialUser hearingJudgeNameAndEmail;

    @JsonProperty("hearingJudgePersonalCode")
    private String hearingJudgePersonalCode;

    @JsonProperty("hearingJudgeLastName")
    private String hearingJudgeLastName;

    @JsonProperty("hearingJudgeEmailAddress")
    private String hearingJudgeEmailAddress;

    private String applicantName;
    private String applicantSolicitor;
    private String respondentName;
    private String respondentSolicitor;

    @JsonProperty("hearingSpecificDatesOptionsEnum")
    private HearingSpecificDatesOptionsEnum hearingSpecificDatesOptionsEnum;


    @JsonProperty("firstDateOfTheHearing")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate firstDateOfTheHearing;

    @JsonProperty("hearingMustTakePlaceAtHour")
    private String hearingMustTakePlaceAtHour;

    @JsonProperty("hearingMustTakePlaceAtMinute")
    private String hearingMustTakePlaceAtMinute;

    @JsonProperty("earliestHearingDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate earliestHearingDate;

    @JsonProperty("latestHearingDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate latestHearingDate;

    @JsonProperty("hearingPriorityTypeEnum")
    private HearingPriorityTypeEnum hearingPriorityTypeEnum;

    @JsonProperty("customDetails")
    private  String customDetails;

    @JsonProperty("isRenderingRequiredFlag")
    private YesOrNo isRenderingRequiredFlag;

    @JsonProperty("fillingFormRenderingInfo")
    private String fillingFormRenderingInfo;

    private List<Element<HearingDataFromTabToDocmosis>> hearingdataFromHearingTab;

    private AutomatedHearingDataApplicantDetails hearingDataApplicantDetails;

    private AutomatedHearingDataRespondentDetails hearingDataRespondentDetails;

    private final YesOrNo isCafcassCymru;

    @JsonProperty("additionalDetailsForHearingDateOptions")
    private String additionalDetailsForHearingDateOptions;


}

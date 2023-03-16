package uk.gov.hmcts.reform.prl.models.dto.ccd;

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
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class HearingData {

    private DynamicList hearingTypes;

    private final DynamicList confirmedHearingDates;

    private final DynamicList hearingChannels;

    private DynamicList hearingVideoChannels;

    private DynamicList hearingTelephoneChannels;

    private DynamicList courtList;

    private final DynamicList localAuthorityHearingChannel;

    private final DynamicList hearingListedLinkedCases;

    private final DynamicList applicantSolicitorHearingChannel;

    private final DynamicList respondentHearingChannel;

    private final DynamicList respondentSolicitorHearingChannel;

    private final DynamicList cafcassHearingChannel;

    private final DynamicList cafcassCymruHearingChannel;

    private final DynamicList applicantHearingChannel;
    private final DynamicList applicantHearingChannel1;
    private final DynamicList applicantHearingChannel2;
    private final DynamicList applicantHearingChannel3;
    private final DynamicList applicantHearingChannel4;
    private final DynamicList applicantHearingChannel5;

    @JsonSerialize(using = CustomEnumSerializer.class)
    @JsonProperty("hearingDateConfirmOptionEnum")
    private HearingDateConfirmOptionEnum hearingDateConfirmOptionEnum;

    @JsonProperty("additionalHearingDetails")
    private final String additionalHearingDetails;

    @JsonProperty("instructionsForRemoteHearing")
    private final String instructionsForRemoteHearing;

    @JsonProperty("hearingDateTimes")
    private final List<Element<LocalDateTime>> hearingDateTimes;

    @JsonProperty("hearingEstimatedHours")
    private final int hearingEstimatedHours;

    @JsonProperty("hearingEstimatedMinutes")
    private final int hearingEstimatedMinutes;

    @JsonProperty("hearingEstimatedDays")
    private final int  hearingEstimatedDays;

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

    private final String applicantName;
    private final String applicantName1;
    private final String applicantName2;
    private final String applicantName3;
    private final String applicantName4;
    private final String applicantName5;
    private final String applicantSolicitor;
    private final String applicantSolicitor1;
    private final String applicantSolicitor2;
    private final String applicantSolicitor3;
    private final String applicantSolicitor4;
    private final String applicantSolicitor5;
    private final String respondentName;
    private final String respondentName1;
    private final String respondentName2;
    private final String respondentName3;
    private final String respondentName4;
    private final String respondentName5;
    private final String respondentSolicitor;
    private final String respondentSolicitor1;
    private final String respondentSolicitor2;
    private final String respondentSolicitor3;
    private final String respondentSolicitor4;
    private final String respondentSolicitor5;

    @JsonProperty("hearingSpecificDatesOptionsEnum")
    private HearingSpecificDatesOptionsEnum hearingSpecificDatesOptionsEnum;


    @JsonProperty("firstDateOfTheHearing")
    private LocalDate firstDateOfTheHearing;

    @JsonProperty("hearingMustTakePlaceAtHour")
    private final int hearingMustTakePlaceAtHour;

    @JsonProperty("hearingMustTakePlaceAtMinute")
    private final int hearingMustTakePlaceAtMinute;

    @JsonProperty("earliestHearingDate")
    private LocalDate earliestHearingDate;

    @JsonProperty("latestHearingDate")
    private LocalDate latestHearingDate;

    @JsonProperty("hearingPriorityTypeEnum")
    private HearingPriorityTypeEnum hearingPriorityTypeEnum;

    @JsonProperty("customDetails")
    private  String customDetails;

    @JsonProperty("isRenderingRequiredFlag")
    private YesOrNo isRenderingRequiredFlag;

    @JsonProperty("fillingFormRenderingInfo")
    private String fillingFormRenderingInfo;
}

package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class HearingData {

    private final DynamicList hearingTypes;

    private final DynamicList confirmedHearingDates;

    private final DynamicList hearingChannels;

    private final DynamicList hearingVideoChannels;

    private final DynamicList hearingTelephoneChannels;

    private final DynamicList courtList;

    private final DynamicList localAuthorityHearingChannel;

    private final DynamicList hearingListedLinkedCases;

    private final DynamicList applicantSolicitorHearingChannel;

    private final DynamicList respondentHearingChannel;

    private final DynamicList respondentSolicitorHearingChannel;

    private final DynamicList cafcassHearingChannel;

    private final DynamicList cafcassCymruHearingChannel;

    private final DynamicList applicantHearingChannel;

    @JsonSerialize(using = CustomEnumSerializer.class)
    @JsonProperty("hearingDateConfirmOptionEnum")
    private HearingDateConfirmOptionEnum hearingDateConfirmOptionEnum;

    @JsonProperty("additionalHearingDetails")
    private final String additionalHearingDetails;

    @JsonProperty("instructionsForRemoteHearing")
    private final String instructionsForRemoteHearing;

    @JsonProperty("hearingScheduleDetails")
    private final List<HearingScheduleDetails> hearingScheduleDetails;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYYY-MM-dd HH:mm")
    @JsonProperty("hearingDateTime")
    private final LocalDateTime hearingDateTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYYY-MM-dd HH:mm")
    @JsonProperty("hearingDateTimes")
    private final  List<LocalDateTime> hearingDateTimes;

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
    private final String hearingJudgePersonalCode;

    @JsonProperty("hearingJudgeLastName")
    private final String hearingJudgeLastName;

    @JsonProperty("hearingJudgeEmailAddress")
    private final String hearingJudgeEmailAddress;

    private final String mainApplicantName;


}

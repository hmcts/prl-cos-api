package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.HearingChannelsEnum;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.HearingPriorityTypeEnum;
import uk.gov.hmcts.reform.prl.enums.HearingSpecificDatesOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class HearingDetails {
    private String hearingCourtName;
    private List<String> hearingType;
    private String hearingDate;
    private String hearingTime;
    private String hearingDuration;
    private DynamicList hearingTypes;

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

    private HearingDateConfirmOptionEnum hearingDateConfirmOptionEnum;

    private String additionalHearingDetails;

    private String instructionsForRemoteHearing;


    private  int hearingEstimatedHours;

    private  int hearingEstimatedMinutes;

    private  int  hearingEstimatedDays;

    private  YesOrNo allPartiesAttendHearingSameWayYesOrNo;

    private DioBeforeAEnum hearingAuthority;

    private HearingChannelsEnum hearingChannelsEnum;

    private  JudicialUser hearingJudgeNameAndEmail;

    private String hearingJudgePersonalCode;

    private String hearingJudgeLastName;

    private String hearingJudgeEmailAddress;

    private String applicantName;
    private String applicantSolicitor;
    private String respondentName;
    private String respondentSolicitor;

    private HearingSpecificDatesOptionsEnum hearingSpecificDatesOptionsEnum;


    private LocalDate firstDateOfTheHearing;

    private int hearingMustTakePlaceAtHour;

    private int hearingMustTakePlaceAtMinute;

    private LocalDate earliestHearingDate;

    private LocalDate latestHearingDate;

    private HearingPriorityTypeEnum hearingPriorityTypeEnum;

    private  String customDetails;

    private YesOrNo isRenderingRequiredFlag;

    private String fillingFormRenderingInfo;

    private  DynamicList applicantHearingChannel1;
    private  DynamicList applicantHearingChannel2;
    private  DynamicList applicantHearingChannel3;
    private  DynamicList applicantHearingChannel4;
    private  DynamicList applicantHearingChannel5;

    private  DynamicList applicantSolicitorHearingChannel1;
    private  DynamicList applicantSolicitorHearingChannel2;
    private  DynamicList applicantSolicitorHearingChannel3;
    private  DynamicList applicantSolicitorHearingChannel4;
    private  DynamicList applicantSolicitorHearingChannel5;

    private  DynamicList respondentHearingChannel1;
    private  DynamicList respondentHearingChannel2;
    private  DynamicList respondentHearingChannel3;
    private  DynamicList respondentHearingChannel4;
    private  DynamicList respondentHearingChannel5;

    private  DynamicList respondentSolicitorHearingChannel1;
    private  DynamicList respondentSolicitorHearingChannel2;
    private  DynamicList respondentSolicitorHearingChannel3;
    private  DynamicList respondentSolicitorHearingChannel4;
    private  DynamicList respondentSolicitorHearingChannel5;

    private  String applicantName1;
    private  String applicantName2;
    private  String applicantName3;
    private  String applicantName4;
    private  String applicantName5;

    private  String applicantSolicitor1;
    private  String applicantSolicitor2;
    private  String applicantSolicitor3;
    private  String applicantSolicitor4;
    private  String applicantSolicitor5;

    private  String respondentName1;
    private  String respondentName2;
    private  String respondentName3;
    private  String respondentName4;
    private  String respondentName5;

    private  String respondentSolicitor1;
    private  String respondentSolicitor2;
    private  String respondentSolicitor3;
    private  String respondentSolicitor4;
    private  String respondentSolicitor5;
}

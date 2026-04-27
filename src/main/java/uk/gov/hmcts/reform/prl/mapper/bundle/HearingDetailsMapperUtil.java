package uk.gov.hmcts.reform.prl.mapper.bundle;

import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleHearingInfo;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;

public class HearingDetailsMapperUtil {

    public static BundleHearingInfo mapHearingDetails(Hearings hearingDetails) {
        if (null != hearingDetails && null != hearingDetails.getCaseHearings()) {
            List<CaseHearing> listedCaseHearings = hearingDetails.getCaseHearings().stream()
                .filter(caseHearing -> LISTED.equalsIgnoreCase(caseHearing.getHmcStatus())).toList();
            if (null != listedCaseHearings && !listedCaseHearings.isEmpty()) {
                List<HearingDaySchedule> hearingDaySchedules = listedCaseHearings.get(0).getHearingDaySchedule();
                if (null != hearingDaySchedules && !hearingDaySchedules.isEmpty()) {
                    return BundleHearingInfo.builder().hearingVenueAddress(getHearingVenueAddress(hearingDaySchedules.get(0)))
                        .hearingDateAndTime(null != hearingDaySchedules.get(0).getHearingStartDateTime()
                            ? getBundleDateTime(hearingDaySchedules.get(0).getHearingStartDateTime()) : BLANK_STRING)
                        .hearingJudgeName(hearingDaySchedules.get(0).getHearingJudgeName()).build();
                }
            }
        }
        return BundleHearingInfo.builder().build();
    }

    public static String getBundleDateTime(LocalDateTime bundleDateTime) {
        StringBuilder newBundleDateTime = new StringBuilder();
        LocalDateTime ldt = CaseUtils.convertUtcToBst(bundleDateTime);

        return newBundleDateTime
            .append(bundleDateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)))
            .append(EMPTY_SPACE_STRING)
            .append(CaseUtils.convertLocalDateTimeToAmOrPmTime(ldt))
            .toString();
    }

    public static String getHearingVenueAddress(HearingDaySchedule hearingDaySchedule) {
        return null != hearingDaySchedule.getHearingVenueName()
            ? hearingDaySchedule.getHearingVenueName() + "\n" +  hearingDaySchedule.getHearingVenueAddress()
            : hearingDaySchedule.getHearingVenueAddress();
    }
}

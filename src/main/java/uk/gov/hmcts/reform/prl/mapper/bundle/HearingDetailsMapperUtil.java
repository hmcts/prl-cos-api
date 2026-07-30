package uk.gov.hmcts.reform.prl.mapper.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleHearingInfo;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.utils.HearingSelectionUtils;

import static uk.gov.hmcts.reform.prl.utils.CommonUtils.getBundleDateTime;
import static uk.gov.hmcts.reform.prl.utils.HearingSelectionUtils.getNextScheduledListedHearing;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingDetailsMapperUtil {

    public BundleHearingInfo mapHearingDetails(Hearings hearingDetails) {
        if (null != hearingDetails && null != hearingDetails.getCaseHearings()) {
            return getNextScheduledListedHearing(hearingDetails.getCaseHearings())
                .flatMap(HearingSelectionUtils::getNextScheduledHearingDay)
                .map(hearingDaySchedule -> BundleHearingInfo.builder()
                    .hearingVenueAddress(getHearingVenueAddress(hearingDaySchedule))
                    .hearingDateAndTime(getBundleDateTime(hearingDaySchedule.getHearingStartDateTime()))
                    .hearingJudgeName(hearingDaySchedule.getHearingJudgeName()).build())
                .orElse(BundleHearingInfo.builder().build());
        }
        return BundleHearingInfo.builder().build();
    }

    public String getHearingVenueAddress(HearingDaySchedule hearingDaySchedule) {
        return null != hearingDaySchedule.getHearingVenueName()
            ? hearingDaySchedule.getHearingVenueName() + "\n" +  hearingDaySchedule.getHearingVenueAddress()
            : hearingDaySchedule.getHearingVenueAddress();
    }
}

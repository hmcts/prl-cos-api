package uk.gov.hmcts.reform.prl.mapper.solicitor;

import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;

import java.util.List;

public class ReasonableAdjustmentsFlagMapper {

    public Flags reasonableAdjustmentFlags(Flags caseFlags, AttendHearing attendHearing) {

        List<Element<FlagDetail>> reasonableAdjustmentFlagDetails = caseFlags.getDetails();

        if (attendHearing.getIsWelshNeeded() != null && attendHearing.getIsWelshNeeded().equals(YesOrNo.Yes)) {
            FlagDetail welshDetails = FlagDetail.builder().build();
            Element<FlagDetail> welshFlagDetailsElement = Element.<FlagDetail>builder().value(welshDetails).build();
            reasonableAdjustmentFlagDetails.add(welshFlagDetailsElement);
        }
        if (attendHearing.getIsInterpreterNeeded() != null && attendHearing.getIsInterpreterNeeded().equals(YesOrNo.Yes)) {
            FlagDetail interpreterDetails = FlagDetail.builder().build();
            Element<FlagDetail> interpreterFlagDetailsElement = Element.<FlagDetail>builder().value(interpreterDetails).build();
            reasonableAdjustmentFlagDetails.add(interpreterFlagDetailsElement);
        }
        if (attendHearing.getIsDisabilityPresent() != null && attendHearing.getIsDisabilityPresent().equals(YesOrNo.Yes)) {
            FlagDetail disabilityDetails = FlagDetail.builder().build();
            Element<FlagDetail> disabilityFlagDetailsElement = Element.<FlagDetail>builder().value(disabilityDetails).build();
            reasonableAdjustmentFlagDetails.add(disabilityFlagDetailsElement);
        }
        if (attendHearing.getIsSpecialArrangementsRequired() != null && attendHearing.getIsSpecialArrangementsRequired().equals(YesOrNo.Yes)) {
            FlagDetail arrangementDetails = FlagDetail.builder().build();
            Element<FlagDetail> arrangementFlagDetailsElement = Element.<FlagDetail>builder().value(arrangementDetails).build();
            reasonableAdjustmentFlagDetails.add(arrangementFlagDetailsElement);
        }
        if (attendHearing.getIsIntermediaryNeeded() != null && attendHearing.getIsIntermediaryNeeded().equals(YesOrNo.Yes)) {
            FlagDetail intermediaryDetails = FlagDetail.builder().build();
            Element<FlagDetail> intermediaryFlagDetailsElement = Element.<FlagDetail>builder().value(intermediaryDetails).build();
            reasonableAdjustmentFlagDetails.add(intermediaryFlagDetailsElement);
        }

        caseFlags.setDetails(reasonableAdjustmentFlagDetails);
        return caseFlags;
    }
}

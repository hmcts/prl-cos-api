package uk.gov.hmcts.reform.prl.mapper.solicitor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.testng.Assert;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReasonableAdjustmentsFlagMapperTest {

    @InjectMocks
    ReasonableAdjustmentsFlagMapper reasonableAdjustmentsFlagMapper;

    @Test
    public void noReasonableAdjustmentsSelected() {
        List<Element<FlagDetail>> reasonableAdjustmentFlagDetails = new ArrayList<>();
        Flags flags = Flags.builder().details(reasonableAdjustmentFlagDetails).build();

        Assert.assertEquals(reasonableAdjustmentsFlagMapper
                                .reasonableAdjustmentFlags(flags, AttendHearing.builder().build()), flags);
    }

    @Test
    public void allReasonableAdjustmentsSelected() {
        List<Element<FlagDetail>> reasonableAdjustmentFlagDetails = new ArrayList<>();

        FlagDetail flagDetails = FlagDetail.builder().build();
        Element<FlagDetail> flagDetailsElement = Element.<FlagDetail>builder().value(flagDetails).build();
        for (int i = 0; i < 5; i++) {
            reasonableAdjustmentFlagDetails.add(flagDetailsElement);
        }

        Flags flags = Flags.builder().details(reasonableAdjustmentFlagDetails).build();

        AttendHearing attendHearing = AttendHearing.builder()
            .isDisabilityPresent(YesOrNo.Yes)
            .isIntermediaryNeeded(YesOrNo.Yes)
            .isInterpreterNeeded(YesOrNo.Yes)
            .isSpecialArrangementsRequired(YesOrNo.Yes)
            .isWelshNeeded(YesOrNo.Yes).build();

        Assert.assertEquals(reasonableAdjustmentsFlagMapper
                                .reasonableAdjustmentFlags(flags, attendHearing),
                            flags);
    }
}

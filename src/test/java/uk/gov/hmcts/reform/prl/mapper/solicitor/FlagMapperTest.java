package uk.gov.hmcts.reform.prl.mapper.solicitor;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FlagMapperTest {

    @InjectMocks
    private FlagMapper flagMapper;

    @Mock
    ReasonableAdjustmentsFlagMapper reasonableAdjustmentsFlagMapper;

    @Test
    public void testNoData() {
        Flags testFlag = flagMapper.buildCaseFlags(CaseData.builder().build(), Flags.builder().build());
        Assert.assertEquals(testFlag, Flags.builder().build());
    }
}

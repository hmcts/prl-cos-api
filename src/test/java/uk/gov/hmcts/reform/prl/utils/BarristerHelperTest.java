package uk.gov.hmcts.reform.prl.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class BarristerHelperTest {

    private BarristerHelper barristerHelper;

    @Before
    public void setUp() {
        barristerHelper = new BarristerHelper();
    }

    @Test
    public void testAllocatedBarristerDetailsNotSet() {
        CaseData spyCaseData = spy(CaseData.builder().build());
        barristerHelper.setAllocatedBarrister(null, spyCaseData, UUID.randomUUID());
        verify(spyCaseData).setAllocatedBarrister(null);
    }

    @Test
    public void testAllocatedBarristerDetailsIsSet() {
        CaseData spyCaseData = spy(CaseData.builder()
                                       .allocatedBarrister(AllocatedBarrister.builder().build())
                                       .build());
        barristerHelper.setAllocatedBarrister(PartyDetails.builder()
                                             .barrister(Barrister.builder()
                                                            .barristerId(UUID.randomUUID().toString())
                                                            .build())
                                             .build(),
                                         spyCaseData,
                                         UUID.randomUUID());
        verify(spyCaseData).setAllocatedBarrister(any());
    }

    @Test
    public void testAllocatedWhenBarristerNotPresent() {
        CaseData spyCaseData = spy(CaseData.builder()
                                       .allocatedBarrister(AllocatedBarrister.builder().build())
                                       .build());
        barristerHelper.setAllocatedBarrister(PartyDetails.builder()
                                             .build(),
                                         spyCaseData,
                                         UUID.randomUUID());
        verify(spyCaseData).setAllocatedBarrister(null);
    }
}

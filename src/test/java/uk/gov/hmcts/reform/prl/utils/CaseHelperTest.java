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
public class CaseHelperTest {

    private CaseHelper caseHelper;

    @Before
    public void setUp() {
        caseHelper = new CaseHelper();
    }

    @Test
    public void testAllocatedBarristerDetailsNotSet() {
        CaseData spyCaseData = spy(CaseData.builder().build());
        caseHelper.setAllocatedBarrister(null, spyCaseData, UUID.randomUUID());
        verify(spyCaseData).setAllocatedBarrister(null);
    }

    @Test
    public void testAllocatedBarristerDetailsIsSet() {
        CaseData spyCaseData = spy(CaseData.builder()
                                       .allocatedBarrister(AllocatedBarrister.builder().build())
                                       .build());
        caseHelper.setAllocatedBarrister(PartyDetails.builder()
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
        caseHelper.setAllocatedBarrister(PartyDetails.builder()
                                             .build(),
                                         spyCaseData,
                                         UUID.randomUUID());
        verify(spyCaseData).setAllocatedBarrister(null);
    }
}

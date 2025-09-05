package uk.gov.hmcts.reform.prl.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
        PartyDetails partyDetails = PartyDetails.builder()
            .barrister(Barrister.builder()
                           .barristerId(UUID.randomUUID().toString())
                           .barristerEmail("barristerEmail@test.com")
                           .barristerOrg(Organisation.builder()
                                             .organisationID(UUID.randomUUID().toString())
                                             .build())
                           .barristerFirstName("barristerFirstName")
                           .barristerLastName("barristerLastName")
                           .build())
            .solicitorEmail("solicitorEmail@test.com")
            .build();
        UUID partyId = UUID.randomUUID();
        AllocatedBarrister expectedAllocatedBarrister = AllocatedBarrister.builder()
            .partyList(
                DynamicList.builder()
                    .value(DynamicListElement.builder()
                               .code(partyId)
                               .build())
                    .build())
            .barristerOrg(partyDetails.getBarrister().getBarristerOrg())
            .barristerEmail(partyDetails.getBarrister().getBarristerEmail())
            .barristerFirstName(partyDetails.getBarrister().getBarristerFirstName())
            .barristerLastName(partyDetails.getBarrister().getBarristerLastName())
            .solicitorEmail(partyDetails.getSolicitorEmail())
            .build();


        barristerHelper.setAllocatedBarrister(
            partyDetails,
            spyCaseData,
            partyId
        );
        verify(spyCaseData).setAllocatedBarrister(any());
        assertThat(spyCaseData.getAllocatedBarrister())
            .isEqualTo(expectedAllocatedBarrister);
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

    @Test
    public void testHasBarristerPresentTrue() {
        PartyDetails partyDetails = PartyDetails.builder()
            .barrister(Barrister.builder()
                           .barristerId(UUID.randomUUID().toString())
                           .barristerEmail("barristerEmail@test.com")
                           .barristerOrg(Organisation.builder()
                                             .organisationID(UUID.randomUUID().toString())
                                             .build())
                           .barristerFirstName("barristerFirstName")
                           .barristerLastName("barristerLastName")
                           .build())
            .solicitorEmail("solicitorEmail@test.com")
            .build();
        boolean hasBarrister = barristerHelper.hasBarrister(partyDetails);
        assertThat(hasBarrister).isTrue();
    }

    @Test
    public void testHasBarristerPresentFalse() {
        PartyDetails partyDetails = PartyDetails.builder()
            .barrister(Barrister.builder()
                           .barristerEmail("barristerEmail@test.com")
                           .barristerOrg(Organisation.builder()
                                             .organisationID(UUID.randomUUID().toString())
                                             .build())
                           .barristerFirstName("barristerFirstName")
                           .barristerLastName("barristerLastName")
                           .build())
            .solicitorEmail("solicitorEmail@test.com")
            .build();
        boolean hasBarrister = barristerHelper.hasBarrister(partyDetails);
        assertThat(hasBarrister).isFalse();
    }

    @Test
    public void testHasBarristerWhenNoBarristerPresent() {
        boolean hasBarrister = barristerHelper.hasBarrister(PartyDetails.builder().build());
        assertThat(hasBarrister).isFalse();
    }
}

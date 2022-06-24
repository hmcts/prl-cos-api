package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class LitigationCapacityMapperTest {

    @InjectMocks
    LitigationCapacityMapper litigationCapacityMapper;

    @Test
    public void testLitigationCapacityMapperTest() {
        CaseData caseDataInput = CaseData.builder().litigationCapacityFactors("Litigation Capacity Factors")
            .litigationCapacityReferrals("Referrals")
            .litigationCapacityFactors("Other Factors").litigationCapacityOtherFactorsDetails("Other Factor Details")
            .build();
        assertNotNull(litigationCapacityMapper.map(caseDataInput));


    }
}

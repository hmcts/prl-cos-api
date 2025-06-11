package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
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

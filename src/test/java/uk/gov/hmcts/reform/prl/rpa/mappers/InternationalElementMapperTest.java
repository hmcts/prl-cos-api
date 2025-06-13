package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class InternationalElementMapperTest {

    @InjectMocks
    InternationalElementMapper internationalElementMapper;

    @Test
    void testInternationalElementMapper() {
        CaseData caseData = CaseData.builder().habitualResidentInOtherState(Yes)
            .habitualResidentInOtherStateGiveReason("Habitual Resident In Other Reason").jurisdictionIssueGiveReason(
                "Jurisdiction Reason")
            .requestToForeignAuthority(Yes).requestToForeignAuthorityGiveReason("Request To Foreign Authority Reason")
            .jurisdictionIssue(Yes).build();
        assertNotNull(internationalElementMapper.map(caseData));
    }
}

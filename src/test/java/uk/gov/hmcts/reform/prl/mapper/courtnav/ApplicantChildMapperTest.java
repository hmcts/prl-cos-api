package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ProtectedChild;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class ApplicantChildMapperTest {

    @Autowired
    ApplicantChildMapper applicantChildMapper;

    @Test
    void shouldMapChildCorrectly() {
        ProtectedChild child = ProtectedChild.builder()
            .fullName("Amy Test")
            .dateOfBirth(CourtNavDate.builder().day(1).month(6).year(2009).build())
            .relationship("Daughter")
            .parentalResponsibility(true)
            .respondentRelationship("Father")
            .build();

        List<Element<ApplicantChild>> result = applicantChildMapper.map(List.of(child));

        assertEquals(1, result.size());
        ApplicantChild mapped = result.getFirst().getValue();
        assertEquals("Amy Test", mapped.getFullName());
        assertEquals(LocalDate.of(2009, 6, 1), mapped.getDateOfBirth());
        assertEquals("Daughter", mapped.getApplicantChildRelationship());
        assertEquals(YesOrNo.Yes, mapped.getApplicantRespondentShareParental());
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        List<Element<ApplicantChild>> result = applicantChildMapper.map(null);
        assertNull(result);
    }

}

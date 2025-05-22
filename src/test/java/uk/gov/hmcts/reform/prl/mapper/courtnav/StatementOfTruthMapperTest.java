package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.reform.prl.enums.FL401Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavStatementOfTruth;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ConsentEnum;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatementOfTruthMapperTest {

    private final StatementOfTruthMapper mapper = Mappers.getMapper(StatementOfTruthMapper.class);

    @Test
    void shouldMapFullStatementOfTruthCorrectly() {
        CourtNavStatementOfTruth statement = CourtNavStatementOfTruth.builder()
            .declaration(List.of(ConsentEnum.applicantConfirm, ConsentEnum.legalAidConfirm))
            .signature("John Smith")
            .signatureFullName("John Alexander Smith")
            .signatureDate(new CourtNavDate(1, 12, 2024))
            .representativeFirmName("Legal Legends LLP")
            .representativePositionHeld("Solicitor")
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder().statementOfTruth(statement).build())
            .build();

        StatementOfTruth result = mapper.mapStatementOfTruth(source);

        assertNotNull(result);
        assertEquals("John Smith", result.getSignature());
        assertEquals("John Alexander Smith", result.getFullname());
        assertEquals(LocalDate.of(2024, 12, 1), result.getDate());
        assertEquals("Legal Legends LLP", result.getNameOfFirm());
        assertEquals("Solicitor", result.getSignOnBehalf());

        assertEquals(2, result.getApplicantConsent().size());
        assertTrue(result.getApplicantConsent().contains(FL401Consent.getDisplayedValueFromEnumString("applicantConfirm")));
        assertTrue(result.getApplicantConsent().contains(FL401Consent.getDisplayedValueFromEnumString("legalAidConfirm")));
    }
}

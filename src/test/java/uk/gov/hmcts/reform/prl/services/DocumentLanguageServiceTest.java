package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.welsh;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class DocumentLanguageServiceTest {

    CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .build();
    }

    @InjectMocks
    DocumentLanguageService documentLanguageService;

    @Test
    void whenWelshLanguageRequirementIsNotPresentIsGenEngReturnTrueAndIsGenWelshReturnFalse() {
        caseData = CaseData.builder().build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    void whenWelshLanguageRequirementIsPresentAndEqualsToNoIsGenEngReturnTrueIsGenWelshReturnFalse() {
        caseData = CaseData.builder().welshLanguageRequirement(No).build();
        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    void whenWelshLanguageRequirementApplicationIsNotPresentIsGenEngReturnTrueAndIsGenWelshReturnFalse() {
        caseData = caseData.toBuilder().build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    void whenApplicationLanguageIsEngReturnTrueAndIsGenWelshReturnFalse() {
        caseData = caseData.toBuilder()
            .welshLanguageRequirementApplication(english)
            .build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    void whenApplicationLanguageIsWelshReturnTrueAndIsGenEngReturnFalse() {
        caseData = caseData.toBuilder()
            .welshLanguageRequirementApplication(welsh)
            .build();

        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    void whenApplicationLanguageIsEngAndNeedWelshIsYesReturnTrue() {
        caseData = caseData.toBuilder()
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    void whenApplicationLanguageIsEngAndNeedWelshIsNoReturnTrueFalse() {
        caseData = caseData.toBuilder()
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(No)
            .build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    void whenApplicationLanguageIsWelshAndNeedEngIsNoReturnTrueFalse() {
        caseData = caseData.toBuilder()
            .welshLanguageRequirementApplication(welsh)
            .welshLanguageRequirementApplicationNeedEnglish(No)
            .build();

        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    void whenApplicationLanguageIsWelshAndNeedEngIsTesReturnTrueTrue() {
        caseData = caseData.toBuilder()
            .welshLanguageRequirementApplication(welsh)
            .welshLanguageRequirementApplicationNeedEnglish(Yes)
            .build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }
}

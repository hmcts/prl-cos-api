package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.welsh;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class DocumentLanguageServiceTest {

    CaseData caseData;

    @Before
    public void setUp() {
        caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .build();
    }

    @InjectMocks
    DocumentLanguageService documentLanguageService;

    @Test
    public void whenWelshLanguageRequirementIsNotPresentIsGenEngReturnTrueAndIsGenWelshReturnFalse() {
        caseData = CaseData.builder().build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    public void whenWelshLanguageRequirementIsPresentAndEqualsToNoIsGenEngReturnTrueIsGenWelshReturnFalse() {
        caseData = CaseData.builder().welshLanguageRequirement(No).build();
        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    public void whenWelshLanguageRequirementApplicationIsNotPresentIsGenEngReturnTrueAndIsGenWelshReturnFalse() {
        caseData = caseData.toBuilder().build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    public void whenApplicationLanguageIsEngReturnTrueAndIsGenWelshReturnFalse() {
        caseData = caseData.toBuilder()
            .welshLanguageRequirementApplication(english)
            .build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    public void whenApplicationLanguageIsWelshReturnTrueAndIsGenEngReturnFalse() {
        caseData = caseData.toBuilder()
            .welshLanguageRequirementApplication(welsh)
            .build();

        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    public void whenApplicationLanguageIsEngAndNeedWelshIsYesReturnTrue() {
        caseData = caseData.toBuilder()
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    public void whenApplicationLanguageIsEngAndNeedWelshIsNoReturnTrueFalse() {
        caseData = caseData.toBuilder()
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(No)
            .build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    public void whenApplicationLanguageIsWelshAndNeedEngIsNoReturnTrueFalse() {
        caseData = caseData.toBuilder()
            .welshLanguageRequirementApplication(welsh)
            .welshLanguageRequirementApplicationNeedEnglish(No)
            .build();

        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    public void whenApplicationLanguageIsWelshAndNeedEngIsTesReturnTrueTrue() {
        caseData = caseData.toBuilder()
            .welshLanguageRequirementApplication(welsh)
            .welshLanguageRequirementApplicationNeedEnglish(Yes)
            .build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }
}

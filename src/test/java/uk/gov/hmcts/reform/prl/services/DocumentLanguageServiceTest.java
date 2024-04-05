package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
    public void testDefaultEnglishWithoutWelshLanguageRequirement() {
        caseData = CaseData.builder().build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    public void testDefaultEnglishWithWelshLanguageRequirementAsNo() {
        caseData = CaseData.builder().welshLanguageRequirement(No).build();

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertFalse(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }

    @Test
    public void testEnglishAndWelshWithWelshLanguageRequirementAsYes() {

        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenEng());
        assertTrue(documentLanguageService.docGenerateLang(caseData).isGenWelsh());
    }
}

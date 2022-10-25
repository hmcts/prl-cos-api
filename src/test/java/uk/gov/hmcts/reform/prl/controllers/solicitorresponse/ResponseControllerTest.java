package uk.gov.hmcts.reform.prl.controllers.solicitorresponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ResponseControllerTest {

    @InjectMocks
    private ResponseController responseController;

    private CaseData caseData;

    @Before
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        caseData = CaseData.builder()
            .courtName("testcourt")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .build();
    }

    @Test
    public void testKeepDetailsPrivate() throws Exception {

    }
}

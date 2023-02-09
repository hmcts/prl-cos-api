package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentSolicitorMiamService;

import static org.junit.Assert.assertNotNull;


@RunWith(MockitoJUnitRunner.Silent.class)
public class RespondentSolicitorMiamServiceTest {

    @InjectMocks
    private RespondentSolicitorMiamService respondentSolicitorMiamService;

    @Test
    public void testCollapsableOfWhatIsMiamGettingPopulated() {

        String responseMap = respondentSolicitorMiamService.getCollapsableOfWhatIsMiamPlaceHolder();

        assertNotNull(responseMap);

    }

    @Test
    public void testCollapsableOfHelpMiamCostsExemptionsPlaceHolder() {

        String responseMap = respondentSolicitorMiamService.getCollapsableOfHelpMiamCostsExemptionsPlaceHolder();

        assertNotNull(responseMap);

    }
}

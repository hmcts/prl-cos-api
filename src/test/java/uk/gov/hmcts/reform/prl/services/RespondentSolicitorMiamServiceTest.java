package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

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

}

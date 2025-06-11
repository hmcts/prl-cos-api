package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentSolicitorMiamService;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
public class RespondentSolicitorMiamServiceTest {

    @InjectMocks
    private RespondentSolicitorMiamService respondentSolicitorMiamService;

    @Test
    public void testCollapsableOfWhatIsMiamGettingPopulated() {

        String responseMap = respondentSolicitorMiamService.getCollapsableOfWhatIsMiamPlaceHolder();

        assertNotNull(responseMap);

    }

    @Test
    public void testCollapsableOfWhatIsMiamGettingPopulatedWelsh() {

        String responseMap = respondentSolicitorMiamService.getCollapsableOfWhatIsMiamPlaceHolderWelsh();

        assertNotNull(responseMap);

    }

    @Test
    public void testCollapsableOfHelpMiamCostsExemptionsPlaceHolder() {

        String responseMap = respondentSolicitorMiamService.getCollapsableOfHelpMiamCostsExemptionsPlaceHolder();

        assertNotNull(responseMap);

    }

    @Test
    public void testCollapsableOfHelpMiamCostsExemptionsPlaceHolderWelsh() {

        String responseMap = respondentSolicitorMiamService.getCollapsableOfHelpMiamCostsExemptionsPlaceHolderWelsh();

        assertNotNull(responseMap);

    }
}

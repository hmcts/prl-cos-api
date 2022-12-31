package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentSolicitorMiamService;

import static org.junit.Assert.assertTrue;


@RunWith(MockitoJUnitRunner.Silent.class)
public class C100RespondentSolicitorControllerTest {


    @InjectMocks
    private C100RespondentSolicitorController c100RespondentSolicitorController;

    @Mock
    private RespondentSolicitorMiamService respondentSolicitorMiamService;

    @Mock
    private ObjectMapper objectMapper;


    @Test
    public void testServiceOfApplicationAboutToStart() {
        assertTrue(true);
    }
}




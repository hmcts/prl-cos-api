package uk.gov.hmcts.reform.prl.controllers.courtnav;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.courtnav.CaseCreationService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseCreationControllerTest {

    @InjectMocks
    private CaseCreationController caseCreationController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CaseCreationService caseCreationService;

    @Test
    public void shouldCreateCaseWhenCalled() {
        when(authorisationService.authorise(any())).thenReturn(true);
        when(caseCreationService.createCourtNavCase(any(), any(), any())).thenReturn(CaseDetails.builder().id(
            1234567891234567L).data(Map.of("id", "1234567891234567")).build());
        CaseData caseData = CaseData.builder().id(1234567891234567L).build();

        Map<String, String> callbackResponse = caseCreationController.createCase("Bearer:test", "s2s token", caseData);
        assertEquals("case created successfully", callbackResponse.get("status"));
        assertEquals("1234567891234567", callbackResponse.get("id"));

    }

    @Test
    public void shouldNotCreateCaseWhenCalledWithInvalidS2SToken() {
        CaseData caseData = CaseData.builder().id(1234567891234567L).build();
        Map<String, String> callbackResponse = caseCreationController.createCase("Bearer:test", "s2s token", caseData);
        assertEquals("Failure", callbackResponse.get("status"));
    }
}

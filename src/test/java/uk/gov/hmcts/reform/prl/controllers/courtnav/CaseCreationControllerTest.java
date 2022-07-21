package uk.gov.hmcts.reform.prl.controllers.courtnav;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
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
        when(caseCreationService.createCourtNavCase(any(), any())).thenReturn(CaseDetails.builder().id(
            1234567891234567L).data(Map.of("id", "1234567891234567")).build());
        CaseData caseData = CaseData.builder().id(1234567891234567L).build();

        ResponseEntity response = caseCreationController.createCase("Bearer:test", "s2s token", caseData);
        assertEquals(200, response.getStatusCodeValue());

    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotCreateCaseWhenCalledWithInvalidS2SToken() {
        CaseData caseData = CaseData.builder().id(1234567891234567L).build();
        ResponseEntity response = caseCreationController.createCase("Bearer:test", "s2s token", caseData);
    }
}

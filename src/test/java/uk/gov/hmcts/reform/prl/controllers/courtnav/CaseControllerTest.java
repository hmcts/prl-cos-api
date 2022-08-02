package uk.gov.hmcts.reform.prl.controllers.courtnav;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.courtnav.CaseService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseControllerTest {

    @InjectMocks
    private CaseController caseController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CaseService caseService;

    MockMultipartFile file;

    @Before
    public void setUp() {

        file = new MockMultipartFile(
            "file",
            "private-law.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "FL401 case".getBytes()
        );
    }

    @Test
    public void shouldCreateCaseWhenCalled() {
        when(authorisationService.authorise(any())).thenReturn(true);
        when(caseService.createCourtNavCase(any(), any())).thenReturn(CaseDetails.builder().id(
            1234567891234567L).data(Map.of("id", "1234567891234567")).build());
        CaseData caseData = CaseData.builder().id(1234567891234567L).build();

        ResponseEntity response = caseController.createCase("Bearer:test", "s2s token", caseData);
        assertEquals(201, response.getStatusCodeValue());

    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotCreateCaseWhenCalledWithInvalidS2SToken() {
        CaseData caseData = CaseData.builder().id(1234567891234567L).build();
        ResponseEntity response = caseController
            .createCase("Bearer:test", "s2s token", caseData);
    }

    @Test
    public void shouldUploadDocWhenCalledWithCorrectParameters() {
        when(authorisationService.authorise(any())).thenReturn(true);
        doNothing().when(caseService).uploadDocument(any(), any(), any(), any());
        ResponseEntity response = caseController
            .uploadDocument("Bearer:test", "s2s token",
                            "", file, "fl401Doc1");
        assertEquals(200, response.getStatusCodeValue());

    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotUploadDocWhenCalledWithInvalidS2SToken() {
        ResponseEntity response = caseController
            .uploadDocument("Bearer:test", "s2s token",
                            "", file, "fl401Doc1");
    }
}

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
import uk.gov.hmcts.reform.prl.courtnav.mappers.FL401ApplicationMapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.BeforeStart;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.courtnav.CaseService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseControllerTest {

    private static final String FILE_NAME = "fileName";
    private static final String CONTENT_TYPE = "application/json";
    private static final String AUTH = "auth";

    @InjectMocks
    private CaseController caseController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CaseService caseService;

    @Mock
    private FL401ApplicationMapper fl401ApplicationMapper;

    MockMultipartFile file;

    @Before
    public void setUp() {
        file
            = new MockMultipartFile(
            "file",
            "hello.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World!".getBytes()
        );
    }

    @Test
    public void shouldCreateCaseWhenCalled() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicantCaseName("test")
            .build();
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(caseService.createCourtNavCase(any(), any())).thenReturn(CaseDetails.builder().id(
            1234567891234567L).data(Map.of("id", "1234567891234567")).build());
        CourtNavCaseData courtNavCaseData = CourtNavCaseData.builder()
            .beforeStart(BeforeStart.builder().applicantHowOld(ApplicantAge.eighteenOrOlder).build()).build();
        when(fl401ApplicationMapper.mapCourtNavData(courtNavCaseData)).thenReturn(caseData);

        ResponseEntity response = caseController.createCase("Bearer:test", "s2s token", courtNavCaseData);
        assertEquals(201, response.getStatusCodeValue());

    }


    @Test
    public void shouldUploadDocumentWhenCalledWithValidS2sAndAuthToken() throws Exception {

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        doNothing().when(caseService).uploadDocument(any(), any(), any(), any());

        ResponseEntity response = caseController.uploadDocument(
            AUTH,
            "s2s token",
            "1234567891234567",
            file,
            "fl401Doc1"
        );
        assertEquals(200, response.getStatusCodeValue());

    }

    @Test(expected = ResponseStatusException.class)
    public void shouldGetForbiddenWhenCalledWithInvalidToken() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicantCaseName("test")
            .build();
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(caseService.createCourtNavCase(any(), any())).thenReturn(CaseDetails.builder().id(
            1234567891234567L).data(Map.of("id", "1234567891234567")).build());
        CourtNavCaseData courtNavCaseData = CourtNavCaseData.builder().applicantHowOld(ApplicantAge.eighteenOrOlder).build();
        when(fl401ApplicationMapper.mapCourtNavData(courtNavCaseData)).thenReturn(caseData);

        ResponseEntity response = caseController.createCase("Bearer:test", "s2s token", courtNavCaseData);
        assertEquals(403, response.getStatusCodeValue());

    }

    @Test(expected = ResponseStatusException.class)
    public void shouldGetForbiddenWhenCalledWithInvalidS2SToken() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicantCaseName("test")
            .build();
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(caseService.createCourtNavCase(any(), any())).thenReturn(CaseDetails.builder().id(
            1234567891234567L).data(Map.of("id", "1234567891234567")).build());
        CourtNavCaseData courtNavCaseData = CourtNavCaseData.builder().applicantHowOld(ApplicantAge.eighteenOrOlder).build();
        when(fl401ApplicationMapper.mapCourtNavData(courtNavCaseData)).thenReturn(caseData);

        ResponseEntity response = caseController.createCase("Bearer:test", "s2s token", courtNavCaseData);
        assertEquals(403, response.getStatusCodeValue());

    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotCreateCaseWhenCalledWithInvalidS2SToken() throws Exception {
        CourtNavCaseData caseData = CourtNavCaseData.builder()
            .beforeStart(BeforeStart.builder().applicantHowOld(ApplicantAge.eighteenOrOlder).build()).build();
        ResponseEntity response = caseController
            .createCase("Bearer:test", "s2s token", caseData);
    }

    @Test
    public void shouldUploadDocWhenCalledWithCorrectParameters() {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        doNothing().when(caseService).uploadDocument(any(), any(), any(), any());
        ResponseEntity response = caseController
            .uploadDocument("Bearer:test", "s2s token",
                            "", file, "fl401Doc1"
            );
        assertEquals(200, response.getStatusCodeValue());

    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotUploadDocWhenCalledWithInvalidAuthToken() {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        ResponseEntity response = caseController
            .uploadDocument("Bearer:invalid", "s2s token",
                            "", file, "fl401Doc1"
            );
    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotUploadDocWhenCalledWithInvalidS2SToken() {
        ResponseEntity response = caseController
            .uploadDocument("Bearer:test", "s2s token",
                            "", file, "fl401Doc1"
            );
    }
}

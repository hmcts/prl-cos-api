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
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.courtnav.mappers.FL401ApplicationMapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.BeforeStart;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassUploadDocService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.courtnav.CourtNavCaseService;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.CAFCASS_USER_ROLE;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CourtNavCaseControllerTest {

    private static final String FILE_NAME = "fileName";
    private static final String CONTENT_TYPE = "application/json";
    private static final String AUTH = "auth";

    @InjectMocks
    private CourtNavCaseController courtNavCaseController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CourtNavCaseService courtNavCaseService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private FL401ApplicationMapper fl401ApplicationMapper;

    @Mock
    private IdamClient idamClient;

    MockMultipartFile file;

    @Mock
    private CafcassUploadDocService cafcassUploadDocService;

    @Mock
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;

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
        when(courtNavCaseService.createCourtNavCase(any(), any())).thenReturn(CaseDetails.builder().id(
            1234567891234567L).data(Map.of("id", "1234567891234567")).build());
        CourtNavFl401 courtNavCaseData = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(BeforeStart.builder().applicantHowOld(
                           ApplicantAge.eighteenOrOlder).build()).build())
            .build();
        when(fl401ApplicationMapper.mapCourtNavData(courtNavCaseData)).thenReturn(caseData);

        ResponseEntity response = courtNavCaseController.createCase("Bearer:test", "s2s token", courtNavCaseData);
        assertEquals(201, response.getStatusCodeValue());

    }


    @Test
    public void shouldUploadDocumentWhenCalledWithValidS2sAndAuthToken() throws Exception {

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        doNothing().when(courtNavCaseService).uploadDocument(any(), any(), any(), any());

        UserInfo userInfo = UserInfo.builder().roles(
            List.of("COURTNAV")).build();
        when(authorisationService.getUserInfo()).thenReturn(userInfo);

        ResponseEntity response = courtNavCaseController.uploadDocument(
            AUTH,
            "s2s token",
            "1234567891234567",
            file,
            "fl401Doc1"
        );
        assertEquals(200, response.getStatusCodeValue());

    }

    @Test
    public void shouldUploadDocumentWhenCalledWithValidS2sAndAuthToken_Cafcass() throws Exception {

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        doNothing().when(cafcassUploadDocService).uploadDocument(any(), any(), any(), any());

        UserInfo userInfo = UserInfo.builder().roles(
            List.of(CAFCASS_USER_ROLE)).build();

        when(authorisationService.getUserInfo()).thenReturn(userInfo);

        ResponseEntity response = courtNavCaseController.uploadDocument(
            AUTH,
            "s2s token",
            "1234567891234567",
            file,
            "fl401Doc1"
        );
        assertEquals(200, response.getStatusCodeValue());

    }

    @Test
    public void shouldGetForbiddenWhenCalledWithInvalidToken() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicantCaseName("test")
            .build();
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(courtNavCaseService.createCourtNavCase(any(), any())).thenReturn(CaseDetails.builder().id(
            1234567891234567L).data(Map.of("id", "1234567891234567")).build());
        CourtNavFl401 courtNavCaseData = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(BeforeStart.builder().applicantHowOld(
                           ApplicantAge.eighteenOrOlder).build()).build())
            .build();
        when(fl401ApplicationMapper.mapCourtNavData(courtNavCaseData)).thenReturn(caseData);
        assertThrows(ResponseStatusException.class, () -> courtNavCaseController.createCase("Bearer:test", "s2s token", courtNavCaseData));
    }

    @Test
    public void shouldGetForbiddenWhenCalledWithInvalidS2SToken() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicantCaseName("test")
            .build();
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(courtNavCaseService.createCourtNavCase(any(), any())).thenReturn(CaseDetails.builder().id(
            1234567891234567L).data(Map.of("id", "1234567891234567")).build());
        CourtNavFl401 courtNavCaseData = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(BeforeStart.builder().applicantHowOld(
                           ApplicantAge.eighteenOrOlder).build()).build())
            .build();
        when(fl401ApplicationMapper.mapCourtNavData(courtNavCaseData)).thenReturn(caseData);
        assertThrows(ResponseStatusException.class, () -> courtNavCaseController.createCase("Bearer:test", "s2s token", courtNavCaseData));

    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotCreateCaseWhenCalledWithInvalidS2SToken() throws Exception {
        CourtNavFl401 courtNavCaseData = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(BeforeStart.builder().applicantHowOld(
                           ApplicantAge.eighteenOrOlder).build()).build())
            .build();
        ResponseEntity response = courtNavCaseController
            .createCase("Bearer:test", "s2s token", courtNavCaseData);
    }

    @Test
    public void shouldUploadDocWhenCalledWithCorrectParameters() {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        doNothing().when(courtNavCaseService).uploadDocument(any(), any(), any(), any());
        UserInfo userInfo = UserInfo.builder().roles(
            List.of("COURTNAV")).build();
        when(authorisationService.getUserInfo()).thenReturn(userInfo);

        ResponseEntity response = courtNavCaseController
            .uploadDocument("Bearer:test", "s2s token",
                            "", file, "fl401Doc1"
            );
        assertEquals(200, response.getStatusCodeValue());

    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotUploadDocWhenCalledWithInvalidAuthToken() {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        ResponseEntity response = courtNavCaseController
            .uploadDocument("Bearer:invalid", "s2s token",
                            "", file, "fl401Doc1"
            );
    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotUploadDocWhenCalledWithInvalidS2SToken() {
        ResponseEntity response = courtNavCaseController
            .uploadDocument("Bearer:test", "s2s token",
                            "", file, "fl401Doc1"
            );
    }
}

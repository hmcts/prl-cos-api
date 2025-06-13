package uk.gov.hmcts.reform.prl.controllers.courtnav;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.courtnav.mappers.FL401ApplicationMapper;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.BeforeStart;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.TheHome;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassUploadDocService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.courtnav.CourtNavCaseService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.CAFCASS_USER_ROLE;

@ExtendWith(MockitoExtension.class)
class CourtNavCaseControllerTest {

    private static final String AUTH = "auth";

    private MockMultipartFile file;

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

    @Mock
    private CafcassUploadDocService cafcassUploadDocService;

    @Mock
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    @BeforeEach
    void setUp() {
        file = new MockMultipartFile(
            "file",
            "hello.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World!".getBytes()
        );
    }

    @Test
    void shouldCreateCaseWhenCalled() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicantCaseName("test")
            .build();

        CourtNavFl401 courtNavCaseData = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(BeforeStart.builder().applicantHowOld(
                           ApplicantAge.eighteenOrOlder).build()).build())
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(courtNavCaseService.createCourtNavCase(any(), any())).thenReturn(
            CaseDetails.builder().id(1234567891234567L).data(Map.of("id", "1234567891234567")).build());
        when(fl401ApplicationMapper.mapCourtNavData(courtNavCaseData, "Bearer:test")).thenReturn(caseData);

        ResponseEntity response = courtNavCaseController.createCase("Bearer:test", "s2s token", courtNavCaseData);
        assertEquals(CREATED, response.getStatusCode());
    }

    @Test
    void shouldUploadDocumentWhenCalledWithValidS2sAndAuthToken() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("COURTNAV")).build();

        when(authorisationService.getUserInfo()).thenReturn(userInfo);
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        doNothing().when(courtNavCaseService).uploadDocument(any(), any(), any(), any());

        ResponseEntity response = courtNavCaseController.uploadDocument(
            AUTH,
            "s2s token",
            "1234567891234567",
            file,
            "fl401Doc1"
        );
        assertEquals(OK, response.getStatusCode());
    }

    @Test
    void shouldUploadDocumentWhenCalledWithValidS2sAndAuthToken_Cafcass() {
        UserInfo userInfo = UserInfo.builder().roles(List.of(CAFCASS_USER_ROLE)).build();

        when(authorisationService.getUserInfo()).thenReturn(userInfo);
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        doNothing().when(cafcassUploadDocService).uploadDocument(any(), any(), any(), any());

        ResponseEntity response = courtNavCaseController.uploadDocument(
            AUTH,
            "s2s token",
            "1234567891234567",
            file,
            "fl401Doc1"
        );
        assertEquals(OK, response.getStatusCode());

    }

    @Test
    void shouldGetForbiddenWhenCalledWithInvalidToken() {

        CourtNavFl401 courtNavCaseData = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(BeforeStart.builder().applicantHowOld(
                           ApplicantAge.eighteenOrOlder).build()).build())
            .build();

        assertThrows(ResponseStatusException.class, () ->
            courtNavCaseController.createCase("Bearer:test", "s2s token", courtNavCaseData)
        );
    }

    @Test
    void shouldGetForbiddenWhenCalledWithInvalidS2SToken() {
        CourtNavFl401 courtNavCaseData = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(BeforeStart.builder().applicantHowOld(
                           ApplicantAge.eighteenOrOlder).build()).build())
            .build();

        when(authorisationService.authoriseUser(any())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
            courtNavCaseController.createCase("Bearer:test", "s2s token", courtNavCaseData)
        );
    }

    @Test
    void shouldNotCreateCaseWhenCalledWithInvalidS2SToken() {
        CourtNavFl401 courtNavCaseData = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(BeforeStart.builder().applicantHowOld(
                           ApplicantAge.eighteenOrOlder).build()).build())
            .build();

        assertThrows(ResponseStatusException.class, () -> {
            courtNavCaseController.createCase("Bearer:test", "s2s token", courtNavCaseData);
        });
    }

    @Test
    void shouldUploadDocWhenCalledWithCorrectParameters() {
        UserInfo userInfo = UserInfo.builder().roles(List.of("COURTNAV")).build();

        when(authorisationService.getUserInfo()).thenReturn(userInfo);
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        doNothing().when(courtNavCaseService).uploadDocument(any(), any(), any(), any());

        ResponseEntity response = courtNavCaseController
            .uploadDocument(
                "Bearer:test", "s2s token",
                "", file, "fl401Doc1"
            );
        assertEquals(OK, response.getStatusCode());

    }

    @Test
    void shouldNotUploadDocWhenCalledWithInvalidAuthToken() {
        assertThrows(
            ResponseStatusException.class, () -> {
                courtNavCaseController.uploadDocument(
                    "Bearer:invalid", "s2s token",
                    "", file, "fl401Doc1"
                );
            }
        );
    }

    @Test
    void shouldNotUploadDocWhenCalledWithInvalidS2SToken() {
        assertThrows(
            ResponseStatusException.class, () -> {
                courtNavCaseController.uploadDocument(
                    "Bearer:test", "s2s token",
                    "", file, "fl401Doc1"
                );
            }
        );
    }

    @Test
    void shouldCreateCaseWhenFamilyHomeListIsEmpty() throws Exception {
        Home home = Home.builder()
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(No)
            .doAnyChildrenLiveAtAddress(No)
            .isPropertyRented(No)
            .isThereMortgageOnProperty(No)
            .isPropertyAdapted(No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(Collections.emptyList()).build();

        CaseData caseData = CaseData.builder()
            .applicantCaseName("test")
            .home(home)
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(courtNavCaseService.createCourtNavCase(any(), any())).thenReturn(
            CaseDetails.builder().id(1234567891234567L).data(Map.of("id", "1234567891234567")).build());

        CourtNavFl401 courtNavCaseData = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(BeforeStart.builder().applicantHowOld(
                           ApplicantAge.eighteenOrOlder).build())
                       .theHome(TheHome.builder()
                                    .wantToHappenWithFamilyHome(Collections.emptyList())
                                    .build())
                       .build())
            .build();
        when(fl401ApplicationMapper.mapCourtNavData(courtNavCaseData, "Bearer:test")).thenReturn(caseData);

        ResponseEntity<Object> response = courtNavCaseController.createCase(
            "Bearer:test",
            "s2s token",
            courtNavCaseData
        );

        assertEquals(CREATED, response.getStatusCode());
    }
}

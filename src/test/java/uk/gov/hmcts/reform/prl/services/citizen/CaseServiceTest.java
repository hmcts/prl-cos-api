package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataMapper;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsConverter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.DELETE_CASE;
import static uk.gov.hmcts.reform.prl.enums.State.AWAITING_SUBMISSION_TO_HMCTS;
import static uk.gov.hmcts.reform.prl.services.citizen.CaseService.SEARCH_CRITERIA;
import static uk.gov.hmcts.reform.prl.services.citizen.CaseService.SEARCH_CRITERIA_DESC;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseServiceTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "Bearer TestAuthToken";
    public static final String caseId = "1234567891234567";
    public static final String accessCode = "123456";

    @InjectMocks
    private CaseService caseService;
    @Mock
    CaseRepository caseRepository;

    @Mock
    CoreCaseDataApi coreCaseDataApi;

    @Mock
    CaseDetailsConverter caseDetailsConverter;

    @Mock
    IdamClient idamClient;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    SystemUserService systemUserService;

    @Mock
    CaseDataMapper caseDataMapper;

    @Mock
    CitizenEmailService citizenEmailService;

    @Mock
    AllTabServiceImpl allTabsService;

    @Mock
    CourtFinderService courtLocatorService;

    private CaseData caseData;
    private CaseDetails caseDetails;
    private UserDetails userDetails;
    private Map<String, Object> caseDataMap;
    private PartyDetails partyDetails;

    @Before
    public void setup() {
        partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .build();
        caseData = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.Yes)
                                                                         .partyId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                                                         .accessCode("123").build()).build()))
            .build();
        caseDataMap = new HashMap<>();
        caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .build();
        userDetails = UserDetails.builder().build();
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(Mockito.anyString(), Mockito.anyString())).thenReturn(caseDetails);
        when(caseRepository.updateCase(any(), any(), any(), any())).thenReturn(caseDetails);
        when(idamClient.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(coreCaseDataApi.getCase(any(), any(), any())).thenReturn(caseDetails);
    }

    @Test
    public void testupdateCase() throws JsonProcessingException {
        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(caseData, "", "","","linkCase","123");
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testGetCase() {
        assertNotNull(caseService.getCase("",""));
    }

    @Test
    public void testValidateAccessCode() {
        assertNotNull(caseService.validateAccessCode("","","",""));
    }

    @Test
    public void testRetrieveCases() {
        assertNotNull(caseService.retrieveCases("","","",""));
    }

    @Test
    public void testRetrieveCasesTwoParams() {
        assertNotNull(caseService.retrieveCases("",""));
    }

    @Test
    public void testupdateCaseCitizenUpdate() throws JsonProcessingException {
        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(caseData, "", "","","citizen-case-submit","123");
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void shouldCreateCase() {
        //Given
        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        when(caseRepository.createCase(authToken, caseData)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.createCase(caseData, authToken);

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void shouldUpdateCaseForSubmitEvent() throws JsonProcessingException, NotFoundException {
        //Given
        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();
        UserDetails userDetails = UserDetails
            .builder()
            .email("test@gmail.com")
            .build();

        CaseDetails caseDetails = mock(CaseDetails.class);

        CaseData updatedCaseData = caseData.toBuilder()
            .userInfo(wrapElements(UserInfo.builder().emailAddress(userDetails.getEmail()).build()))
            .courtName("Test Court")
            .build();

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(courtLocatorService.getNearestFamilyCourt(caseData)).thenReturn(Court.builder().courtName("Test Court").build());
        when(caseDataMapper.buildUpdatedCaseData(updatedCaseData)).thenReturn(updatedCaseData);
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_SUBMIT)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, s2sToken, caseId,
                                                                CITIZEN_CASE_SUBMIT.getValue(), accessCode);

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void shouldSendDeletionNotifications() {
        //Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put(SEARCH_CRITERIA, SEARCH_CRITERIA_DESC);
        caseDataMap.put("lastModifiedDate", LocalDateTime.now().minusDays(23L));
        caseDataMap.put("state", AWAITING_SUBMISSION_TO_HMCTS);
        UserDetails userDetails = UserDetails
                .builder()
                .id("test@gmail.com")
                .email("test@gmail.com")
                .build();
        CaseDetails caseDetails = CaseDetails.builder()
                .data(caseDataMap)
                .id(1234567891234567L)
                .state(AWAITING_SUBMISSION_TO_HMCTS.getValue())
                .lastModified(LocalDateTime.now().minusDays(23L))
                .build();
        CaseData caseData = CaseData.builder()
                .id(1234567891234567L)
                .lastModifiedDate(LocalDateTime.now().minusDays(23L))
                .state(AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
        when(coreCaseDataApi.searchForCaseworker(authToken, s2sToken, "test@gmail.com", JURISDICTION,
                CASE_TYPE, searchCriteria)).thenReturn(List.of(caseDetails));
        when(objectMapper.convertValue(caseData.toMap(objectMapper), CaseData.class)).thenReturn(caseData);

        //When
        caseService.sendDeletionNotification(s2sToken);

        //Then
        verify(citizenEmailService).sendCitizenCaseDeletionWarningEmail(any(CaseData.class), eq(authToken));
    }

    @Test
    public void shouldDeleteOldDraftCases() {
        //Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put(SEARCH_CRITERIA, SEARCH_CRITERIA_DESC);
        caseDataMap.put("lastModifiedDate", LocalDateTime.now().minusDays(31L));
        caseDataMap.put("state", AWAITING_SUBMISSION_TO_HMCTS);
        UserDetails userDetails = UserDetails
                .builder()
                .id("test@gmail.com")
                .email("test@gmail.com")
                .build();
        CaseDetails caseDetails = CaseDetails.builder()
                .data(caseDataMap)
                .id(1234567891234567L)
                .state(AWAITING_SUBMISSION_TO_HMCTS.getValue())
                .lastModified(LocalDateTime.now().minusDays(31L))
                .build();
        CaseData caseData = CaseData.builder()
                .id(1234567891234567L)
                .lastModifiedDate(LocalDateTime.now().minusDays(31L))
                .state(AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
        when(coreCaseDataApi.searchForCaseworker(authToken, s2sToken, "test@gmail.com", JURISDICTION,
                CASE_TYPE, searchCriteria)).thenReturn(List.of(caseDetails));
        when(objectMapper.convertValue(caseData.toMap(objectMapper), CaseData.class)).thenReturn(caseData);

        //When
        caseService.deleteOldDraftCases(s2sToken);

        //Then
        verify(caseRepository).updateCase(eq(authToken), eq("1234567891234567"), any(CaseData.class), eq(DELETE_CASE));
    }
}
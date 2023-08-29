package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.exception.CoreCaseDataStoreException;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_ROLE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CitizenCoreCaseDataServiceTest {

    @Mock
    private IdamClient idamClient;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CaseData caseDataMock;

    @Mock
    private StartEventResponse startEventResponse;

    @InjectMocks
    CitizenCoreCaseDataService citizenCoreCaseDataService;

    @Mock
    CcdCoreCaseDataService ccdCoreCaseDataService;

    private String serviceAuth = "serviceAuth";
    private CaseDetails caseDetails;
    private final String bearerToken = "Bearer token";
    private Map<String, Object> stringObjectMap;
    private UserDetails userDetails;
    private static final String LINK_CASE_TO_CITIZEN_SUMMARY = "Link case to Citizen account";
    private static final String LINK_CASE_TO_CITIZEN_DESCRIPTION = "Link case to Citizen account with access code";

    @Before
    public void setUp() {
        userDetails = UserDetails.builder()
            .id("testUser").build();
        stringObjectMap = new HashMap<>();
        caseDetails = CaseDetails.builder().id(12345L).data(stringObjectMap).build();
        when(caseDataMock.toMap(any())).thenReturn(stringObjectMap);
        when(authTokenGenerator.generate()).thenReturn(serviceAuth);
        startEventResponse = StartEventResponse.builder()
            .token(bearerToken).build();
        when(ccdCoreCaseDataService.submitUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(caseDetails);
        when(ccdCoreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(startEventResponse);
        when(ccdCoreCaseDataService.submitCreate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyBoolean()
        )).thenReturn(caseDetails);
        when(ccdCoreCaseDataService.startSubmitCreate(Mockito.anyString(),
                                                      Mockito.anyString(),
                                                      Mockito.any(),
                                                      Mockito.anyBoolean())).thenReturn(startEventResponse);
        when(coreCaseDataApi.getCase(
            bearerToken,
            "serviceAuth",
            "12345L"
        )).thenReturn(caseDetails);
    }

    @Test
    public void linkCitizenAccountAndUpdateCaseData() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder()
                                            .solicitorEmail("test@gmail.com")
                                            .representativeLastName("LastName")
                                            .representativeFirstName("FirstName")
                                            .build())))
            .respondents(List.of(element(PartyDetails.builder()
                                             .solicitorEmail("test@gmail.com")
                                             .representativeLastName("LastName")
                                             .representativeFirstName("FirstName")
                                             .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                             .build())))
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        when(idamClient.getUserDetails(bearerToken)).thenReturn(userDetails);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CaseDetails updatedDetails = citizenCoreCaseDataService.linkDefendant(
            bearerToken,
            12345L,
            caseDataMock,
            CaseEvent.LINK_CITIZEN,
            startEventResponse,
            caseDataUpdated
        );

        Assert.assertEquals(caseDetails, updatedDetails);
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void linkCitizenAccountThrowException() throws Exception {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        citizenCoreCaseDataService.linkDefendant(
            bearerToken,
            12345L,
            caseDataMock,
            CaseEvent.LINK_CITIZEN,
            startEventResponse,
            caseDataUpdated
        );
    }

    @Test
    public void citizenCoreCaseShouldBeUpdated() {
        userDetails = UserDetails.builder()
            .id("testUser").roles(List.of(CITIZEN_ROLE)).build();
        when(idamClient.getUserDetails(bearerToken)).thenReturn(userDetails);

        CaseDetails updatedDetails = citizenCoreCaseDataService.updateCase(bearerToken,
                                                                           12345L,
                                                                           caseDataMock,
                                                                           CaseEvent.LINK_CITIZEN);
        Assert.assertEquals(caseDetails, updatedDetails);
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void updateCitizenCoreCaseShouldThrowException() {
        citizenCoreCaseDataService.updateCase(bearerToken, 12345L, caseDataMock, CaseEvent.LINK_CITIZEN);
    }

    @Test
    public void citizenCoreCaseShouldBeCreatedForCitizen() {

        userDetails = UserDetails.builder()
            .id("testUser").roles(List.of(CITIZEN_ROLE)).build();
        when(idamClient.getUserDetails(bearerToken)).thenReturn(userDetails);

        CaseDetails createdCaseDetails = citizenCoreCaseDataService.createCase(bearerToken, caseDataMock);

        Assert.assertEquals(caseDetails, createdCaseDetails);
    }

    @Test
    public void citizenCoreCaseShouldBeCreatedForCaseworker() {

        userDetails = UserDetails.builder()
            .id("testUser").roles(emptyList()).build();
        when(idamClient.getUserDetails(bearerToken)).thenReturn(userDetails);

        CaseDetails createdCaseDetails = citizenCoreCaseDataService.createCase(bearerToken, caseDataMock);

        Assert.assertEquals(caseDetails, createdCaseDetails);
    }

    @Test
    public void shouldGetCitizenCoreCase() {
        CaseDetails retrievedCaseDetails = citizenCoreCaseDataService.getCase(bearerToken, "12345L");

        Assert.assertEquals(caseDetails, retrievedCaseDetails);
    }
}

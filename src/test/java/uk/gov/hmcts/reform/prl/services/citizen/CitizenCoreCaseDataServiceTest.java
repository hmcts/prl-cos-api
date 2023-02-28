package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.exception.CoreCaseDataStoreException;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
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
    SystemUserService systemUserService;

    @Mock
    CoreCaseDataService coreCaseDataService;

    @InjectMocks
    CitizenCoreCaseDataService citizenCoreCaseDataService;
    private String bearerToken;

    private EventRequestData eventRequestData;

    private final String userToken = "Bearer testToken";

    private final String systemUpdateUserId = "systemUserID";

    private static final String LINK_CASE_TO_CITIZEN_SUMMARY = "Link case to Citizen account";
    private static final String LINK_CASE_TO_CITIZEN_DESCRIPTION = "Link case to Citizen account with access code";

    @Test
    public void linkCitizenAccountAndUpdateCaseData() throws Exception {
        bearerToken = "Bearer token";
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("id","12345L");
        stringObjectMap.put("caseTypeOfApplication","C100");

        eventRequestData = EventRequestData.builder()
            .eventId(CaseEvent.LINK_CITIZEN.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(stringObjectMap)
            .build();

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

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUpdateUserId);

        UserDetails userDetails  = UserDetails.builder()
            .id("testUser").roles(Arrays.asList(CITIZEN_ROLE)).build();
        when(idamClient.getUserDetails(bearerToken)).thenReturn(userDetails);
        when(coreCaseDataService.eventRequest(CaseEvent.LINK_CITIZEN, userDetails.getId())).thenReturn(eventRequestData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(stringObjectMap)
            .state(State.CASE_ISSUE.getValue())
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .build();

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(bearerToken).build();
        when(coreCaseDataService.startUpdate(
            bearerToken,eventRequestData, String.valueOf(caseData.getId()),true))
            .thenReturn(startEventResponse);
        CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);
        when(coreCaseDataService.createCaseDataContent(startEventResponse,caseDataUpdated)).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(bearerToken, eventRequestData, caseDataContent,String.valueOf(caseData.getId()), true))
            .thenReturn(caseDetails);

        CaseDetails updatedDetails = citizenCoreCaseDataService.linkDefendant(bearerToken,caseData.getId(), CaseEvent.LINK_CITIZEN);

        Assert.assertEquals(caseDetails,updatedDetails);
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void linkCitizenAccountThrowException() throws Exception {

        bearerToken = "Bearer token";

        citizenCoreCaseDataService.linkDefendant(bearerToken,12345L, CaseEvent.LINK_CITIZEN);
    }

    @Test
    public void citizenCoreCaseShouldBeUpdated() {
        bearerToken = "Bearer token";
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("id","12345L");
        stringObjectMap.put("caseTypeOfApplication","C100");

        eventRequestData = EventRequestData.builder()
            .eventId(CaseEvent.LINK_CITIZEN.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(stringObjectMap)
            .build();

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

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUpdateUserId);

        UserDetails userDetails  = UserDetails.builder()
            .id("testUser").roles(Arrays.asList(CITIZEN_ROLE)).build();
        when(idamClient.getUserDetails(bearerToken)).thenReturn(userDetails);
        when(coreCaseDataService.eventRequest(CaseEvent.LINK_CITIZEN, userDetails.getId())).thenReturn(eventRequestData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(stringObjectMap)
            .state(State.CASE_ISSUE.getValue())
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .build();

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(bearerToken).build();
        when(coreCaseDataService.startUpdate(
            bearerToken,eventRequestData, String.valueOf(caseData.getId()),true))
            .thenReturn(startEventResponse);
        CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);
        when(coreCaseDataService.createCaseDataContent(startEventResponse,caseDataUpdated)).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(bearerToken, eventRequestData, caseDataContent,String.valueOf(caseData.getId()), true))
            .thenReturn(caseDetails);

        CaseDetails updatedDetails = citizenCoreCaseDataService.updateCase(bearerToken, 12345L, CaseEvent.LINK_CITIZEN);

        Assert.assertEquals(caseDetails,updatedDetails);
    }

    @Test (expected = CoreCaseDataStoreException.class)
    public void updateCitizenCoreCaseShouldThrowException() {

        bearerToken = "Bearer token";

        citizenCoreCaseDataService.updateCase(bearerToken,12345L, CaseEvent.LINK_CITIZEN);
    }

    @Test
    public void citizenCoreCaseShouldBeCreatedForCitizen() {

        UserDetails userDetails  = UserDetails.builder()
            .id("testUser").roles(Arrays.asList(CITIZEN_ROLE)).build();

        bearerToken = "Bearer token";

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token(bearerToken).build();
        CaseDataContent caseDataContent = caseDataContent(startEventResponse, new HashMap<String, Object>());
        Map<String, Object> stringObjectMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(stringObjectMap).build();
        when(caseDataMock.toMap(any())).thenReturn(stringObjectMap);
        when(idamClient.getUserDetails(bearerToken)).thenReturn(userDetails);
        String serviceAuth = "serviceAuth";
        EventRequestData eventRequestData = eventRequest(CaseEvent.CITIZEN_CASE_CREATE, "testUser");
        when(authTokenGenerator.generate()).thenReturn(serviceAuth);
        when(coreCaseDataApi.startForCitizen(bearerToken, serviceAuth,
                                                  eventRequestData.getUserId(),
                                                  eventRequestData.getJurisdictionId(),
                                                  eventRequestData.getCaseTypeId(),
                                                  eventRequestData.getEventId())).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitForCitizen(bearerToken,
                                                   serviceAuth,
                                                   eventRequestData.getUserId(),
                                                   eventRequestData.getJurisdictionId(),
                                                   eventRequestData.getCaseTypeId(),
                                                   false,
                                                   caseDataContent)).thenReturn(caseDetails);

        CaseDetails createdCaseDetails = citizenCoreCaseDataService.createCase(bearerToken,caseDataMock);

        Assert.assertEquals(caseDetails,createdCaseDetails);
    }

    @Test
    public void citizenCoreCaseShouldBeCreatedForCaseworker() {

        UserDetails userDetails  = UserDetails.builder()
            .id("testUser").roles(emptyList()).build();

        bearerToken = "Bearer token";

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token(bearerToken).build();
        CaseDataContent caseDataContent = caseDataContent(startEventResponse, new HashMap<String, Object>());
        Map<String, Object> stringObjectMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(stringObjectMap).build();
        when(caseDataMock.toMap(any())).thenReturn(stringObjectMap);
        when(idamClient.getUserDetails(bearerToken)).thenReturn(userDetails);
        String serviceAuth = "serviceAuth";
        EventRequestData eventRequestData = eventRequest(CaseEvent.CITIZEN_CASE_CREATE, "testUser");
        when(authTokenGenerator.generate()).thenReturn(serviceAuth);
        when(coreCaseDataApi.startForCaseworker(bearerToken, serviceAuth,
                                             eventRequestData.getUserId(),
                                             eventRequestData.getJurisdictionId(),
                                             eventRequestData.getCaseTypeId(),
                                             eventRequestData.getEventId())).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitForCaseworker(bearerToken,
                                              serviceAuth,
                                              eventRequestData.getUserId(),
                                              eventRequestData.getJurisdictionId(),
                                              eventRequestData.getCaseTypeId(),
                                              true,
                                              caseDataContent)).thenReturn(caseDetails);

        CaseDetails createdCaseDetails = citizenCoreCaseDataService.createCase(bearerToken,caseDataMock);

        Assert.assertEquals(caseDetails,createdCaseDetails);
    }

    @Test
    public void shouldGetCitizenCoreCase() {

        bearerToken = "Bearer token";

        Map<String, Object> stringObjectMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(stringObjectMap).build();
        when(caseDataMock.toMap(any())).thenReturn(stringObjectMap);
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(coreCaseDataApi.getCase(bearerToken,
                                              "serviceAuth",
                                     "12345L")).thenReturn(caseDetails);

        CaseDetails retrievedCaseDetails = citizenCoreCaseDataService.getCase(bearerToken,"12345L");

        Assert.assertEquals(caseDetails,retrievedCaseDetails);
    }

    private EventRequestData eventRequest(CaseEvent caseEvent, String userId) {
        return EventRequestData.builder()
            .userId(userId)
            .jurisdictionId(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .eventId(caseEvent.getValue())
            .ignoreWarning(true)
            .build();
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, Object content) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .summary(LINK_CASE_TO_CITIZEN_SUMMARY)
                       .description(LINK_CASE_TO_CITIZEN_DESCRIPTION)
                       .build())
            .data(content)
            .build();
    }

}

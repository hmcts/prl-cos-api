package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.exception.CoreCaseDataStoreException;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

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

    @InjectMocks
    CitizenCoreCaseDataService citizenCoreCaseDataService;
    private String bearerToken;

    private static final String LINK_CASE_TO_CITIZEN_SUMMARY = "Link case to Citizen account";
    private static final String LINK_CASE_TO_CITIZEN_DESCRIPTION = "Link case to Citizen account with access code";

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
        bearerToken = "Bearer token";
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token(bearerToken).build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(stringObjectMap).build();
        when(caseDataMock.toMap(any())).thenReturn(stringObjectMap);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        UserDetails userDetails  = UserDetails.builder()
            .id("testUser").build();
        when(idamClient.getUserDetails(bearerToken)).thenReturn(userDetails);
        String serviceAuth = "serviceAuth";
        EventRequestData eventRequestData = eventRequest(CaseEvent.LINK_CITIZEN, "testUser");
        when(authTokenGenerator.generate()).thenReturn(serviceAuth);
        when(coreCaseDataApi.startEventForCaseWorker(bearerToken, serviceAuth,
                                                     eventRequestData.getUserId(),
                                                     eventRequestData.getJurisdictionId(),
                                                     eventRequestData.getCaseTypeId(),
                                                     "12345",
                                                     eventRequestData.getEventId())).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyBoolean(),
            Mockito.any(CaseDataContent.class))).thenReturn(caseDetails);

        CaseDetails updatedDetails = citizenCoreCaseDataService.linkDefendant(bearerToken,12345L,caseDataMock,CaseEvent.LINK_CITIZEN);

        Assert.assertEquals(caseDetails,updatedDetails);
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void linkCitizenAccountThrowException() throws Exception {

        bearerToken = "Bearer token";

        citizenCoreCaseDataService.linkDefendant(bearerToken,12345L,caseDataMock,CaseEvent.LINK_CITIZEN);
    }

    @Test
    public void citizenCoreCaseShouldBeUpdated() {

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
        EventRequestData eventRequestData = eventRequest(CaseEvent.LINK_CITIZEN, "testUser");
        when(authTokenGenerator.generate()).thenReturn(serviceAuth);
        when(coreCaseDataApi.startEventForCitizen(bearerToken, serviceAuth,
                                                     eventRequestData.getUserId(),
                                                     eventRequestData.getJurisdictionId(),
                                                     eventRequestData.getCaseTypeId(),
                                                     "12345",
                                                     eventRequestData.getEventId())).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCitizen(bearerToken,
                                                      serviceAuth,
                                                      eventRequestData.getUserId(),
                                                      eventRequestData.getJurisdictionId(),
                                                      eventRequestData.getCaseTypeId(),
                                                      "12345",
                                                      eventRequestData.isIgnoreWarning(),
                                                      caseDataContent)).thenReturn(caseDetails);

        CaseDetails updatedDetails = citizenCoreCaseDataService.updateCase(bearerToken,12345L,caseDataMock,CaseEvent.LINK_CITIZEN);

        Assert.assertEquals(caseDetails,updatedDetails);
    }

    @Test (expected = CoreCaseDataStoreException.class)
    public void updateCitizenCoreCaseShouldThrowException() {

        bearerToken = "Bearer token";

        citizenCoreCaseDataService.updateCase(bearerToken,12345L,caseDataMock,CaseEvent.LINK_CITIZEN);
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

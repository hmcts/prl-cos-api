package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseControllerTest {

    @InjectMocks
    private CaseController caseController;

    @Mock
    private CaseService caseService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    private CaseData caseData;
    public static final String authToken = "Bearer TestAuthToken";
    public static final String servAuthToken = "Bearer TestServToken";

    @Before
    public void setUp() {

    }

    @Test
    public void testGetCase() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        String caseId = "1234567891234567";
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.getCase(authToken, caseId)).thenReturn(caseDetails);
        when(authTokenGenerator.generate()).thenReturn("servAuthToken");
        when(authorisationService.authoriseUser(authToken)).thenReturn(true);
        when(authorisationService.authoriseService(servAuthToken)).thenReturn(true);
        CaseData caseData1 = caseController.getCase(caseId, authToken, servAuthToken);
        assertEquals(caseData.getApplicantCaseName(), caseData1.getApplicantCaseName());

    }

    @Test
    public void testCitizenUpdateCase() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();


        String caseId = "1234567891234567";
        String eventId = "e3ceb507-0137-43a9-8bd3-85dd23720648";

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authTokenGenerator.generate()).thenReturn("TestToken");
        when(authorisationService.authoriseUser(authToken)).thenReturn(true);
        when(authorisationService.authoriseService(servAuthToken)).thenReturn(true);
        when(caseService.updateCase(caseData, authToken, "TestToken", caseId, eventId,
                                    "testAccessCode")).thenReturn(caseDetails);
        CaseData caseData1 = caseController.updateCase(
            caseData,
            caseId,
            eventId,
            authToken,
            servAuthToken,
            "testAccessCode"
        );
        assertEquals(caseData.getApplicantCaseName(), caseData1.getApplicantCaseName());

    }

    @Test
    public void testCitizenRetrieveCases() {

        List<CaseData> caseDataList = new ArrayList<>();


        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        caseDataList.add(CaseData.builder()
                             .id(1234567891234567L)
                             .applicantCaseName("test")
                             .build());

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);

        List<CaseDetails> caseDetails = new ArrayList<>();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        caseDetails.add(CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build());

        String userId = "12345";
        String role = "test role";

        List<CaseData> caseDataList1 = new ArrayList<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.retrieveCases(role, userId, authToken, servAuthToken)).thenReturn(caseDataList);
        caseDataList1 = caseController.retrieveCases(role, userId, authToken, servAuthToken);
        assertNotNull(caseDataList1);

    }

    @Test
    public void testCitizenLinkDefendantToClaim() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        String caseId = "1234567891234567";
        String accessCode = "e3ceb507";

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        doNothing().when(caseService).linkCitizenToCase(authToken, servAuthToken, caseId, accessCode);
        caseController.linkCitizenToCase(authToken, caseId, servAuthToken, accessCode);
        assertNotNull(caseData);

    }

    @Test
    public void testCitizenValidateAccessCode() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(servAuthToken);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        String caseId = "1234567891234567L";
        String accessCode = "e3ceb507";
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.validateAccessCode(authToken, servAuthToken, caseId, accessCode)).thenReturn("Valid");

        String data = caseController.validateAccessCode(authToken, servAuthToken, caseId, accessCode);
        assertNotNull(data);

    }
}

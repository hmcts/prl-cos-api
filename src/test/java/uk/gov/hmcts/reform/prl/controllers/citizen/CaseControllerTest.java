package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.UpdateCaseData;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CitizenCaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

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

    @Mock
    ConfidentialDetailsMapper confidentialDetailsMapper;

    @Mock
    HearingService hearingService;

    private CaseData caseData;
    Address address;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private UpdateCaseData updateCaseData;
    public static final String authToken = "Bearer TestAuthToken";
    public static final String servAuthToken = "Bearer TestServToken";

    @BeforeEach
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testGetCase() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(servAuthToken);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));
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
    public void testCitizenUpdateCase() throws JsonProcessingException, NotFoundException {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(servAuthToken);


        address = Address.builder()
            .addressLine1("AddressLine1")
            .postTown("Xyz town")
            .postCode("AB1 2YZ")
            .build();

        List<Element<ApplicantConfidentialityDetails>> expectedOutput = List
            .of(Element.<ApplicantConfidentialityDetails>builder()
                    .value(ApplicantConfidentialityDetails.builder()
                               .firstName("ABC 1")
                               .lastName("XYZ 2")
                               .email("abc1@xyz.com")
                               .phoneNumber("09876543211")
                               .address(address)
                               .build()).build());

        CaseData updatedCasedata = CaseData.builder()
            .applicantCaseName("test")
            .respondentConfidentialDetails(expectedOutput)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();


        String caseId = "1234567891234567";
        String eventId = "e3ceb507-0137-43a9-8bd3-85dd23720648";
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(confidentialDetailsMapper.mapConfidentialData(caseData, true)).thenReturn(updatedCasedata);
        when(authTokenGenerator.generate()).thenReturn("TestToken");
        when(authorisationService.authoriseUser(authToken)).thenReturn(true);
        when(authorisationService.authoriseService(servAuthToken)).thenReturn(true);
        when(caseService.updateCase(caseData, authToken, "TestToken", caseId, eventId,
                                    "testAccessCode"
        )).thenReturn(caseDetails);
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
    public void testCitizenUpdatingCase() throws JsonProcessingException, NotFoundException {

        PartyDetails partyDetails1 = PartyDetails.builder()
            .firstName("Test")
            .lastName("User")
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();

        PartyDetails partyDetails2 = PartyDetails.builder()
            .firstName("Test2")
            .lastName("User2")
            .user(User.builder()
                      .email("test2@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();
        updateCaseData = UpdateCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .applicantsFL401(partyDetails2)
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(servAuthToken);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .state("Submitted")
            .lastModified(LocalDateTime.now())
            .createdDate(LocalDateTime.now())
            .id(1234567891234567L).data(stringObjectMap).build();


        String caseId = "1234567891234567";
        String eventId = "e3ceb507-0137-43a9-8bd3-85dd23720648";

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authTokenGenerator.generate()).thenReturn("TestToken");
        when(authorisationService.authoriseUser(authToken)).thenReturn(true);
        when(authorisationService.authoriseService(servAuthToken)).thenReturn(true);
        when(caseService.updateCaseDetails(authToken, caseId, eventId, updateCaseData)).thenReturn(caseDetails);
        CaseData caseData1 = caseController.caseUpdate(
            updateCaseData,
            eventId,
            caseId,
            authToken,
            servAuthToken
        );
        assertEquals(caseData.getApplicantsFL401().getFirstName(), caseData1.getApplicantsFL401().getFirstName());

    }

    @Test(expected = RuntimeException.class)
    public void testCitizenUpdatingCaseForInvalidAuthToken() throws JsonProcessingException, NotFoundException {

        PartyDetails partyDetails1 = PartyDetails.builder()
            .firstName("Test")
            .lastName("User")
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();

        PartyDetails partyDetails2 = PartyDetails.builder()
            .firstName("Test2")
            .lastName("User2")
            .user(User.builder()
                      .email("test2@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();
        updateCaseData = UpdateCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .applicantsFL401(partyDetails2)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .state("Submitted")
            .lastModified(LocalDateTime.now())
            .createdDate(LocalDateTime.now())
            .id(1234567891234567L).data(stringObjectMap).build();


        String caseId = "1234567891234567";
        String eventId = "e3ceb507-0137-43a9-8bd3-85dd23720648";
        when(caseService.updateCaseDetails(authToken, caseId, eventId, updateCaseData)).thenReturn(caseDetails);
        CaseData caseData1 = caseController.caseUpdate(
            updateCaseData,
            eventId,
            caseId,
            "authToken",
            "servAuthToken"
        );
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
        when(caseService.retrieveCases(authToken, servAuthToken)).thenReturn(caseDataList);
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

    @Test
    public void testretrieveCitizenCases() {
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

        List<CitizenCaseData> citizenCaseDataList = new ArrayList<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.retrieveCases(authToken, servAuthToken)).thenReturn(caseDataList);
        citizenCaseDataList = caseController.retrieveCitizenCases(authToken, servAuthToken);
        assertNotNull(citizenCaseDataList);
    }

    @Test
    public void shouldCreateCase() {
        //Given
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .noOfDaysRemainingToSubmitCase(PrlAppsConstants.CASE_SUBMISSION_THRESHOLD)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        Mockito.when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        Mockito.when(caseService.createCase(caseData, authToken)).thenReturn(caseDetails);
        Mockito.when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        Mockito.when(authorisationService.authoriseService(servAuthToken)).thenReturn(Boolean.TRUE);
        Mockito.when(authTokenGenerator.generate()).thenReturn(servAuthToken);
        //When
        CaseData actualCaseData = caseController.createCase(authToken, servAuthToken, caseData);

        //Then
        assertThat(actualCaseData).isEqualTo(caseData);
    }

    @Test
    public void shouldWithdrawCase() {
        //Given
        String caseId = "1234567891234567";
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .state(State.CASE_WITHDRAWN)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).state(PrlAppsConstants.WITHDRAWN_STATE).build();

        Mockito.when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        Mockito.when(caseService.withdrawCase(caseData, caseId, authToken)).thenReturn(caseDetails);
        Mockito.when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        Mockito.when(authorisationService.authoriseService(servAuthToken)).thenReturn(Boolean.TRUE);
        //When
        CaseData actualCaseData = caseController.withdrawCase(caseData, caseId, authToken, servAuthToken);

        //Then
        assertThat(actualCaseData.getState()).isEqualTo(caseData.getState());
    }

    @Test
    public void withdrawCaseFailsWhenAuthFails() throws JsonProcessingException {

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Invalid Client");

        Mockito.when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.FALSE);
        Mockito.when(authorisationService.authoriseService(servAuthToken)).thenReturn(Boolean.TRUE);
        //When
        caseController.withdrawCase(caseData, "1234567891234567", authToken, servAuthToken);

        throw new RuntimeException("Invalid Client");
    }

    @Test
    public void testGetAllHearingsForCitizenCase() throws IOException {
        String caseId = "1234567891234567";
        Mockito.when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);


        Mockito.when(hearingService.getHearings(authToken, caseId)).thenReturn(
            Hearings.hearingsWith().build());
        Mockito.when(authorisationService.authoriseService(servAuthToken)).thenReturn(Boolean.TRUE);

        Hearings hearingForCase = caseController.getAllHearingsForCitizenCase(
            authToken, servAuthToken, caseId);
        Assert.assertNotNull(hearingForCase);
    }

    @Test
    public void testGetAllHearingsForCitizenCaseFailswhenAuthFails() throws IOException {

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Invalid Client");

        Mockito.when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.FALSE);
        Mockito.when(authorisationService.authoriseService(servAuthToken)).thenReturn(Boolean.TRUE);
        String caseId = "1234567891234567";

        caseController.getAllHearingsForCitizenCase(authToken, servAuthToken, caseId);

        throw new RuntimeException("Invalid Client");
    }
}

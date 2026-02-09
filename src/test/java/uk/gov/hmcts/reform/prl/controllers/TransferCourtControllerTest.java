package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CantFindCourtEnum;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AmendCourtService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassDateTimeService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;

@RunWith(MockitoJUnitRunner.Silent.class)
@PropertySource(value = "classpath:application.yaml")
public class TransferCourtControllerTest {

    @InjectMocks
    private TransferCourtController transferCourtController;

    @Mock
    private AmendCourtService amendCourtService;
    @Mock
    private CourtFinderService courtLocatorService;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;
    @Mock
    AllTabServiceImpl allTabsService;
    @Mock
    private CafcassDateTimeService cafcassDateTimeService;
    @Mock
    private EventService eventPublisher;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";

    @Test
    public void testPrePopulateCourtDetails() throws NotFoundException {
        String courtId = "1234";
        CaseData caseData = CaseData.builder().courtId(courtId).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(courtLocatorService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(Court.builder().build());
        when(courtLocatorService.getEmailAddress(Mockito.any(Court.class))).thenReturn(
            Optional.of(CourtEmailAddress.builder().address("123@gamil.com").build()));
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        DynamicListElement dle = DynamicListElement.builder()
            .code(courtId).label("courtLabel").build();
        List<DynamicListElement> allCourts = List.of(dle);
        when(locationRefDataService.getCourtLocations(anyString())).thenReturn(allCourts);
        when(locationRefDataService.getDisplayEntryFromEpimmsId(courtId, AUTH_TOKEN)).thenReturn(dle);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =  transferCourtController
            .prePopulateCourtDetails(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("localCourtAdmin"));
        Assertions.assertEquals("1234", ((DynamicList)aboutToStartOrSubmitCallbackResponse.getData().get("courtList"))
            .getValue().getCode());
    }

    @Test
    public void testPrePopulateCourtDetailsNotFound() throws NotFoundException {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(courtLocatorService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(Court.builder().build());
        when(courtLocatorService.getEmailAddress(Mockito.any(Court.class))).thenReturn(Optional.empty());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(locationRefDataService.getCourtLocations(Mockito.anyString())).thenReturn(List.of(DynamicListElement.EMPTY));
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =  transferCourtController
            .prePopulateCourtDetails(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        Assertions.assertNull(aboutToStartOrSubmitCallbackResponse.getData().get("localCourtAdmin"));
    }


    @Test
    public void testAmendCourtAboutToStart() throws Exception {
        String courtId = "1234";
        CaseData caseData = CaseData.builder().courtId(courtId).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        DynamicListElement dle = DynamicListElement.builder()
            .code(courtId).label("courtLabel").build();
        List<DynamicListElement> allCourts = List.of(dle);
        when(locationRefDataService.getCourtLocations(anyString())).thenReturn(allCourts);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(locationRefDataService.getDisplayEntryFromEpimmsId(courtId, AUTH_TOKEN)).thenReturn(dle);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =  transferCourtController
            .amendCourtAboutToStart(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("courtList"));
        Assertions.assertEquals("1234", ((DynamicList)aboutToStartOrSubmitCallbackResponse.getData().get("courtList"))
            .getValue().getCode());
    }

    @Test
    public void testAmendCourtAboutToStartTransferCourt() throws Exception {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(Event.TRANSFER_TO_ANOTHER_COURT.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(locationRefDataService.getFilteredCourtLocations(Mockito.anyString()))
            .thenReturn(List.of(DynamicListElement.EMPTY));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =  transferCourtController
            .amendCourtAboutToStart(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("courtList"));
    }

    @Test
    public void testAmendCourtAboutToSubmit() throws Exception {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("courtName", "testcourt");
        stringObjectMap.put("applicantCaseName", "test");
        stringObjectMap.put("caseTypeOfApplication", "C100_CASE_TYPE");
        CaseData caseData = CaseData.builder().id(123L).applicantCaseName("testName").courtName("test-court")
            .courtEmailAddress("testcourt@sdsd.com")
            .courtList(DynamicList.builder()
                           .value(DynamicListElement.builder().build()).build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =  transferCourtController
            .amendCourtAboutToSubmit(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("courtName"));
    }

    @Test
    public void updateApplicationTest() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();

        OtherPersonWhoLivesWithChild personWhoLivesWithChild = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        Element<OtherPersonWhoLivesWithChild> wrappedList = Element.<OtherPersonWhoLivesWithChild>builder().value(
            personWhoLivesWithChild).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = Collections.singletonList(
            wrappedList);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        transferCourtController.updateApplication(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        verify(allTabsService, times(1)).updateAllTabsIncludingConfTab(anyString());
    }

    @Test
    public void testExceptionForUpdateApplication() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            transferCourtController.updateApplication(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testAmendCourtAboutToStartTransferCourtForFL401Case() throws Exception {
        CaseData caseData = CaseData.builder().caseTypeOfApplication(FL401_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("caseTypeOfApplication", "FL401");
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(Event.TRANSFER_TO_ANOTHER_COURT.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(locationRefDataService.getFilteredCourtLocations(Mockito.anyString()))
            .thenReturn(List.of(DynamicListElement.EMPTY));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =  transferCourtController
            .amendCourtAboutToStart(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("courtList"));
    }

    @Test
    public void testValidateCourtShouldNotGiveError() throws Exception {
        CaseData caseData = CaseData.builder()
            .courtEmailAddress("email@test.com")
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt))
            .anotherCourt("test court").build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(amendCourtService.validateCourtFields(Mockito.any(),Mockito.any())).thenReturn(Boolean.FALSE);
        CallbackResponse response =  transferCourtController
            .validateCourtFields(callbackRequest);
        Assertions.assertNull(response.getErrors());
    }

    @Test
    public void testValidateCourtShouldGiveError() throws Exception {
        CaseData caseData = CaseData.builder()
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt)).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(amendCourtService.validateCourtFields(Mockito.any(),Mockito.any())).thenReturn(Boolean.TRUE);
        CallbackResponse response =  transferCourtController
            .validateCourtFields(callbackRequest);
        Assertions.assertNotNull(response.getErrors());
    }


    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testTransferCourtSubmitEvent() throws Exception {
        CaseData caseData = CaseData.builder()
            .courtList(DynamicList.builder().value(DynamicListElement.builder().code("test-test-test-test-test-test")
                                                       .build()).build())
            .courtCodeFromFact("123")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(allTabsService.updateAllTabsIncludingConfTab(any())).thenReturn(callbackRequest.getCaseDetails());
        ResponseEntity<SubmittedCallbackResponse> responseEntity =  transferCourtController
            .transferCourtConfirmation(AUTH_TOKEN, callbackRequest);
        Assertions.assertNotNull(responseEntity);
    }

    @Test
    public void testExceptionForPrePopulateCourtDetails() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            transferCourtController.prePopulateCourtDetails(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }
}

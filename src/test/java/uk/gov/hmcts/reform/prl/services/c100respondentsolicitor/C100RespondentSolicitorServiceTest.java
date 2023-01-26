package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators.ResponseSubmitChecker;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class C100RespondentSolicitorServiceTest {

    @InjectMocks
    C100RespondentSolicitorService respondentSolicitorService;

    CaseData caseData;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    CcdDataStoreService ccdDataStoreService;

    @Mock
    ResponseSubmitChecker responseSubmitChecker;

    FindUserCaseRolesResponse findUserCaseRolesResponse;

    boolean mandatoryFinished = false;

    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setUp() {

        List<ConfidentialityListEnum> confidentialityListEnums = new ArrayList<>();

        confidentialityListEnums.add(ConfidentialityListEnum.email);
        confidentialityListEnums.add(ConfidentialityListEnum.phoneNumber);

        PartyDetails respondent = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        DynamicListElement dynamicListElement = DynamicListElement
            .builder().code("1afdfa01-8280-4e2c-b810-ab7cf741988a").build();
        DynamicList chooseRespondent = DynamicList.builder().value(dynamicListElement).build();

        caseData = CaseData.builder().respondents(respondentList).id(1)
            .chooseRespondentDynamicList(chooseRespondent)
            .keepContactDetailsPrivateOther(KeepDetailsPrivate.builder()
                                           .confidentiality(Yes)
                                           .confidentialityList(confidentialityListEnums)
                                           .build())
            .build();
    }

    @Test
    public void populateAboutToStartCaseDataTest() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        when(ccdDataStoreService.findUserCaseRoles(String.valueOf(caseData.getId()),
                                                   authToken)).thenReturn(findUserCaseRolesResponse);

        List<String> errorList = new ArrayList<>();

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.populateAboutToStartCaseData(
            callbackRequest, authToken, errorList
        );

        assertTrue(response.containsKey("respondents"));
    }

    @Test
    public void populateAboutToSubmitCaseDataTest() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        List<String> errorList = new ArrayList<>();

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.populateAboutToSubmitCaseData(
            callbackRequest, authToken, errorList
        );

        assertTrue(response.containsKey("respondents"));
    }

    @Test
    public void populateSolicitorRespondentListTest() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.populateSolicitorRespondentList(
            callbackRequest, authToken
        );

        assertTrue(response.containsKey("chooseRespondentDynamicList"));

    }

    @Test
    public void updateActiveRespondentSelectionBySolicitor() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.updateActiveRespondentSelectionBySolicitor(
            callbackRequest, authToken
        );

        assertTrue(response.containsKey("respondents"));
    }

    @Test
    public void generateConfidentialityDynamicSelectionDisplayTest() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.generateConfidentialityDynamicSelectionDisplay(
            callbackRequest
        );

        assertTrue(response.containsKey("confidentialListDetails"));
    }

    @Test
    public void validateActiveRespondentResponse() {

        List<String> errorList = new ArrayList<>();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        when(responseSubmitChecker.hasMandatoryCompleted(caseData)).thenReturn(mandatoryFinished);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.validateActiveRespondentResponse(
            callbackRequest, errorList
        );

        assertTrue(response.containsKey("respondents"));

    }

    @Test
    public void submitC7ResponseForActiveRespondentTest() {

        List<String> errorList = new ArrayList<>();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.submitC7ResponseForActiveRespondent(
            callbackRequest, authToken, errorList
        );

        assertTrue(response.containsKey("respondents"));
    }

}

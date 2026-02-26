package uk.gov.hmcts.reform.prl.controllers.localauthority;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.localauthority.RemoveLocalAuthoritySolicitors;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOCAL_AUTHORITY_INVOLVED_IN_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOCAL_AUTHORITY_SOLICITOR_ORGANISATION_POLICY;

@Slf4j
@ExtendWith(MockitoExtension.class)
class LocalAuthorityControllerTest {

    public static final String authToken = "Bearer TestAuthToken";
    private static final String AUTH = "Bearer testAuth";
    private static final String S2S = "testS2S";
    private static final long CASE_ID = 1234567890123456L;

    @InjectMocks
    LocalAuthorityController controller;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EventService eventPublisher;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private RemoveLocalAuthoritySolicitors removeLocalAuthoritySolicitors;

    private CaseData buildCaseDataWithOrgPolicy(String orgId) {
        return CaseData.builder()
            .id(CASE_ID)
            .localAuthoritySolicitorOrganisationPolicy(
                OrganisationPolicy.builder().organisation(Organisation.builder().organisationID(orgId)
                                                              .organisationName("Some Org").build()).build())
            .build();
    }

    @Test
    void handleAddAboutToSubmit_authorised_setsLocalAuthoritySolicitorRoleAndReturnsUpdatedData() {
        // Arrange
        Map<String, Object> inputData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).data(inputData).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        CaseData caseData = buildCaseDataWithOrgPolicy("ORG-123");

        when(authorisationService.isAuthorized(AUTH, S2S)).thenReturn(true);
        when(objectMapper.convertValue(inputData, CaseData.class)).thenReturn(caseData);

        // Act
        AboutToStartOrSubmitCallbackResponse response =
            controller.handleAddAboutToSubmit(AUTH, S2S, callbackRequest);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getData(), "Response data map should not be null");

        assertTrue(
            response.getData().containsKey(LOCAL_AUTHORITY_INVOLVED_IN_CASE),
            "Expected LOCAL_AUTHORITY_INVOLVED_IN_CASE FLAG to be set in response map"
        );
        assertEquals(YesOrNo.Yes, response.getData().get(LOCAL_AUTHORITY_INVOLVED_IN_CASE));
        assertTrue(
            response.getData().containsKey(LOCAL_AUTHORITY_SOLICITOR_ORGANISATION_POLICY),
            "Expected policy to be set in response map"
        );

        Object policyObj = response.getData().get(LOCAL_AUTHORITY_SOLICITOR_ORGANISATION_POLICY);
        assertNotNull(policyObj, "OrganisationPolicy should not be null");

        assertInstanceOf(OrganisationPolicy.class, policyObj, "Policy object type should be OrganisationPolicy");
        OrganisationPolicy updatedPolicy = (OrganisationPolicy) policyObj;

        assertEquals(
            "[LASOLICITOR]", updatedPolicy.getOrgPolicyCaseAssignedRole(),
            "Case assigned role must be [LASOLICITOR]"
        );

        // Verify authorisation check and mapping were used
        verify(authorisationService, times(1)).isAuthorized(AUTH, S2S);
        verify(objectMapper, times(1)).convertValue(inputData, CaseData.class);
        verifyNoInteractions(removeLocalAuthoritySolicitors);
    }

    @Test
    void handleAddAboutToSubmit_unauthorised_throwsRuntimeException() {
        // Arrange
        Map<String, Object> inputData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).data(inputData).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(authorisationService.isAuthorized(AUTH, S2S)).thenReturn(false);

        // Act + Assert
        assertThrows(
            RuntimeException.class,
            () -> controller.handleAddAboutToSubmit(AUTH, S2S, callbackRequest),
            "Expected RuntimeException for unauthorised request"
        );

        verify(authorisationService, times(1)).isAuthorized(AUTH, S2S);
        verifyNoInteractions(objectMapper, removeLocalAuthoritySolicitors);
    }

    @Test
    void handleRemoveAboutToSubmit_authorised_callsService_andRemovesPolicyFromMap() {
        // Arrange
        Map<String, Object> inputData = new HashMap<>();
        // Seed the map with the policy key to verify it is removed
        inputData.put(
            LOCAL_AUTHORITY_SOLICITOR_ORGANISATION_POLICY,
            OrganisationPolicy.builder().build()
        );

        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).data(inputData).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        CaseData caseData = buildCaseDataWithOrgPolicy("ORG-456");

        when(authorisationService.isAuthorized(AUTH, S2S)).thenReturn(true);
        when(objectMapper.convertValue(inputData, CaseData.class)).thenReturn(caseData);


        // Act
        AboutToStartOrSubmitCallbackResponse response =
            controller.handleRemoveAboutToSubmit(AUTH, S2S, callbackRequest);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getData(), "Response data should not be null");
        assertTrue(
            response.getData().containsKey(LOCAL_AUTHORITY_INVOLVED_IN_CASE),
            "Expected LOCAL_AUTHORITY_INVOLVED_IN_CASE FLAG to be set in response map"
        );
        assertEquals(YesOrNo.No, response.getData().get(LOCAL_AUTHORITY_INVOLVED_IN_CASE));
        assertFalse(
            response.getData().containsKey(LOCAL_AUTHORITY_SOLICITOR_ORGANISATION_POLICY),
            "Policy key should be removed from map"
        );

        ArgumentCaptor<CaseData> caseDataCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(removeLocalAuthoritySolicitors, times(1))
            .removeLocalAuthoritySolicitors(caseDataCaptor.capture());

        CaseData passedToService = caseDataCaptor.getValue();
        assertNotNull(passedToService, "CaseData passed to service should not be null");
        assertEquals(CASE_ID, passedToService.getId(), "Case id passed to service should match");

        verify(authorisationService, times(1)).isAuthorized(AUTH, S2S);
        verify(objectMapper, times(1)).convertValue(inputData, CaseData.class);
    }

    @Test
    void handleRemoveAboutToSubmit_unauthorised_throwsRuntimeException() {
        // Arrange
        Map<String, Object> inputData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).data(inputData).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(authorisationService.isAuthorized(AUTH, S2S)).thenReturn(false);

        // Act + Assert
        assertThrows(
            RuntimeException.class,
            () -> controller.handleRemoveAboutToSubmit(AUTH, S2S, callbackRequest),
            "Expected RuntimeException for unauthorised request"
        );

        verify(authorisationService, times(1)).isAuthorized(AUTH, S2S);
        verifyNoInteractions(objectMapper, removeLocalAuthoritySolicitors);
    }
}

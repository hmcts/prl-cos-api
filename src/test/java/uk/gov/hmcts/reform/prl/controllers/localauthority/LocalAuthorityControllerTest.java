package uk.gov.hmcts.reform.prl.controllers.localauthority;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOCAL_AUTHORITY_SOCIAL_WORKER_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOCAL_AUTHORITY_SOLICITOR_ORGANISATION_POLICY;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
class LocalAuthorityControllerTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String serviceAuth = "serviceAuth";

    @InjectMocks
    LocalAuthorityController localAuthorityController;


    @Mock
    UserService userService;

    @Mock
    private ObjectMapper objectMapper;


    @Mock
    private AuthorisationService authorisationService;

    @Test
    void testAddingOrgPolicyForLocalAuthority() {
        CaseData caseData = CaseData.builder()
            .localAuthoritySolicitorOrganisationPolicy(
                OrganisationPolicy.builder()
                    .organisation(Organisation.builder().organisationID("someId").organisationName("someName").build())
                    .build())
            .caseTypeOfApplication("C100")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        when(userService.getUserDetails(anyString())).thenReturn(
            UserDetails.builder().email("abc@test.com").build());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(146L)
            .data(stringObjectMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        AboutToStartOrSubmitCallbackResponse response = localAuthorityController
            .handleAddAboutToSubmit(authToken, serviceAuth, callbackRequest);
        OrganisationPolicy solicitorOrganisationPolicy = (OrganisationPolicy) response.getData()
            .get(LOCAL_AUTHORITY_SOLICITOR_ORGANISATION_POLICY);
        assertEquals("[LASOLICITOR]", solicitorOrganisationPolicy.getOrgPolicyCaseAssignedRole());

        OrganisationPolicy socialWorkerOrganisationPolicy = (OrganisationPolicy) response.getData()
            .get(LOCAL_AUTHORITY_SOCIAL_WORKER_ORGANISATION_POLICY);
        assertEquals("[LASOCIALWORKER]", socialWorkerOrganisationPolicy.getOrgPolicyCaseAssignedRole());

    }
}

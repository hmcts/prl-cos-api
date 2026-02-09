package uk.gov.hmcts.reform.prl.controllers.localauthority;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOCAL_AUTHORITY_SOLICITOR_ORGANISATION_POLICY;

@Slf4j
@ExtendWith(MockitoExtension.class)
class LocalAuthorityControllerTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String serviceAuth = "serviceAuth";

    @InjectMocks
    LocalAuthorityController localAuthorityController;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EventService eventPublisher;

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

        CaseDetails caseDetails = CaseDetails.builder()
            .id(146L)
            .data(stringObjectMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = localAuthorityController
            .handleAddAboutToSubmit(authToken, serviceAuth, callbackRequest);
        OrganisationPolicy solicitorOrganisationPolicy = (OrganisationPolicy) response.getData()
            .get(LOCAL_AUTHORITY_SOLICITOR_ORGANISATION_POLICY);
        assertEquals("[LASOLICITOR]", solicitorOrganisationPolicy.getOrgPolicyCaseAssignedRole());

    }
}

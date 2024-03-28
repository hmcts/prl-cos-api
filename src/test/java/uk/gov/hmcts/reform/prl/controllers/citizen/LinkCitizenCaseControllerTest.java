package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.citizen.AccessCodeRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@Ignore
public class LinkCitizenCaseControllerTest {

    @InjectMocks
    private LinkCitizenCaseController linkCitizenCaseController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private LinkCitizenCaseService linkCitizenCaseService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CaseUtils caseUtils;

    public static final String authToken = "Bearer TestAuthToken";

    public static final String s2sToken = "s2s AuthToken";

    AccessCodeRequest accessCodeRequest = new AccessCodeRequest();

    @Before
    public void setUp() {
        accessCodeRequest = accessCodeRequest.toBuilder()
            .caseId("123")
            .accessCode("123")
            .build();
    }

    @Test
    public void testLinkCitizenToCase() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(linkCitizenCaseService.linkCitizenToCase(authToken,
            accessCodeRequest.getCaseId(),
            accessCodeRequest.getAccessCode())).thenReturn(Optional.ofNullable(CaseDetails.builder().build()));
        when(CaseUtils.getCaseData(CaseDetails.builder().build(), objectMapper)).thenReturn(CaseData.builder().build());

        linkCitizenCaseController.linkCitizenToCase(authToken, s2sToken, accessCodeRequest);
    }
}

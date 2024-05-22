package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.citizen.AccessCodeRequest;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService;

import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
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
    private HearingService hearingService;

    public static final String authToken = "Bearer TestAuthToken";

    public static final String s2sToken = "s2s AuthToken";

    AccessCodeRequest accessCodeRequest = new AccessCodeRequest();

    AccessCodeRequest accessCodeRequestWithHearing = new AccessCodeRequest();

    CaseDetails caseDetails;

    Hearings hearings;

    @Before
    public void setUp() {
        accessCodeRequest = accessCodeRequest.toBuilder()
            .caseId("123")
            .accessCode("123")
            .build();

        accessCodeRequestWithHearing = accessCodeRequest.toBuilder()
            .caseId("123")
            .accessCode("123")
            .hearingNeeded("Yes")
            .build();

        hearings = Hearings.hearingsWith().build();

        caseDetails = CaseDetails.builder().id(Long.valueOf("1223")).build();
    }

    @Test
    public void testLinkCitizenToCase() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(linkCitizenCaseService.linkCitizenToCase(authToken,
            accessCodeRequest.getCaseId(),
            accessCodeRequest.getAccessCode())).thenReturn(Optional.ofNullable(caseDetails));
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());

        CaseData caseData = linkCitizenCaseController.linkCitizenToCase(authToken, s2sToken, accessCodeRequest);
        Assert.assertEquals(1223, caseData.getId());
    }

    @Test(expected = RuntimeException.class)
    public void testLinkCitizenToCaseCantLink() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);

        linkCitizenCaseController.linkCitizenToCase(authToken, s2sToken, accessCodeRequest);
    }

    @Test(expected = RuntimeException.class)
    public void testLinkCitizenToCaseInvalidClient() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        linkCitizenCaseController.linkCitizenToCase(authToken, s2sToken, accessCodeRequest);
    }

    @Test
    public void testLinkCitizenToCaseWithHearing() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(linkCitizenCaseService.linkCitizenToCase(authToken,
            accessCodeRequest.getCaseId(),
            accessCodeRequest.getAccessCode())).thenReturn(Optional.ofNullable(caseDetails));
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        when(hearingService.getHearings(authToken, accessCodeRequestWithHearing.getCaseId())).thenReturn(hearings);

        CaseDataWithHearingResponse caseData = linkCitizenCaseController
            .linkCitizenToCaseWithHearing(authToken, s2sToken, accessCodeRequestWithHearing);
        Assert.assertEquals(hearings, caseData.getHearings());
    }

    @Test
    public void testLinkCitizenToCaseWithHearingButNoHearingNeeded() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(linkCitizenCaseService.linkCitizenToCase(authToken,
            accessCodeRequest.getCaseId(),
            accessCodeRequest.getAccessCode())).thenReturn(Optional.ofNullable(caseDetails));
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        when(hearingService.getHearings(authToken, accessCodeRequest.getCaseId())).thenReturn(hearings);

        CaseDataWithHearingResponse caseData = linkCitizenCaseController
            .linkCitizenToCaseWithHearing(authToken, s2sToken, accessCodeRequest);
        Assert.assertNull(caseData.getHearings());
    }

    @Test(expected = RuntimeException.class)
    public void testLinkCitizenToCaseWithhearingsCantLink() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);

        linkCitizenCaseController.linkCitizenToCaseWithHearing(authToken, s2sToken, accessCodeRequestWithHearing);
    }

    @Test(expected = RuntimeException.class)
    public void testLinkCitizenToCaseWithhearingsInvalidClient() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        linkCitizenCaseController.linkCitizenToCaseWithHearing(authToken, s2sToken, accessCodeRequestWithHearing);
    }

    @Test
    public void testValidateAccessCode() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);

        when(linkCitizenCaseService.validateAccessCode(accessCodeRequest.getCaseId(), accessCodeRequest.getAccessCode()))
            .thenReturn("test");
        String accessCode = linkCitizenCaseController.validateAccessCode(authToken, s2sToken, accessCodeRequest);
        Assert.assertEquals("test", accessCode);
    }

    @Test(expected = RuntimeException.class)
    public void testValidateAccessCodeInvalidClient() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        linkCitizenCaseController.validateAccessCode(authToken, s2sToken, accessCodeRequest);
    }
}

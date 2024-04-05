package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LinkCitizenCaseServiceTest {

    @InjectMocks
    LinkCitizenCaseService linkCitizenCaseService;

    @Mock
    SystemUserService systemUserService;

    @Mock
    CcdCoreCaseDataService ccdCoreCaseDataService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    IdamClient idamClient;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    CaseAccessApi caseAccessApi;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";
    public static final String caseId = "case id";
    public static final String accessCode = "access code";
    private final UUID testUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
    CaseDetails caseDetails;
    CaseData caseData;
    CaseData caseDataAlreadyLinked;
    CaseData caseDataEmptyCaseInvites;

    CaseData caseDataNullCaseInvites;
    UserDetails userDetails;

    @Before
    public void setUp() {
        PartyDetails applicant = PartyDetails.builder().partyId(testUuid).build();
        PartyDetails applicant2 = PartyDetails.builder()
            .partyId(UUID.fromString("00000000-0000-0000-0000-000000000001")).build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(applicant2).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        applicantList.add(wrappedApplicant2);

        caseDetails = CaseDetails.builder().build();
        caseData = CaseData.builder()
            .applicants(applicantList)
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.Yes)
                                                                         .partyId(testUuid)
                                                                         .accessCode(accessCode).build()).build()))
            .build();
        caseDataAlreadyLinked = CaseData.builder()
            .applicants(applicantList)
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.Yes)
                                                                         .partyId(testUuid)
                                                                         .hasLinked("Yes")
                                                                         .accessCode(accessCode).build()).build()))
            .build();
        caseDataEmptyCaseInvites = CaseData.builder()
            .applicants(applicantList)
            .caseInvites(new ArrayList<>())
            .build();
        caseDataNullCaseInvites = CaseData.builder()
            .applicants(applicantList)
            .caseInvites(null)
            .build();
        userDetails = UserDetails.builder().id("123").build();
    }

    @Test
    public void testLinkCitizenToCase() {
        when(systemUserService.getSysUserToken()).thenReturn(s2sToken);
        when(ccdCoreCaseDataService.findCaseById(s2sToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap, caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(caseId, CaseEvent.LINK_CITIZEN.getValue())).thenReturn(startAllTabsUpdateDataContent);
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(authToken);

        Optional<CaseDetails> returnedCaseDetails = linkCitizenCaseService.linkCitizenToCase(authToken, caseId, accessCode);
        Assert.assertNotNull(returnedCaseDetails);
    }

    @Test
    public void testLinkCitizenToCaseAlreadyLinked() {
        when(systemUserService.getSysUserToken()).thenReturn(s2sToken);
        when(ccdCoreCaseDataService.findCaseById(s2sToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataAlreadyLinked);

        Optional<CaseDetails> returnedCaseDetails = linkCitizenCaseService.linkCitizenToCase(authToken, caseId, accessCode);
        Assert.assertNotNull(returnedCaseDetails);
    }

    @Test
    public void testLinkCitizenToCaseInvalidCodeDueToEmpty() {
        when(systemUserService.getSysUserToken()).thenReturn(s2sToken);
        when(ccdCoreCaseDataService.findCaseById(s2sToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataEmptyCaseInvites);

        Optional<CaseDetails> returnedCaseDetails = linkCitizenCaseService.linkCitizenToCase(authToken, caseId, accessCode);
        Assert.assertNotNull(returnedCaseDetails);
    }

    @Test
    public void testLinkCitizenToCaseInvalidCodeDueToNull() {
        when(systemUserService.getSysUserToken()).thenReturn(s2sToken);
        when(ccdCoreCaseDataService.findCaseById(s2sToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataNullCaseInvites);

        Optional<CaseDetails> returnedCaseDetails = linkCitizenCaseService.linkCitizenToCase(authToken, caseId, accessCode);
        Assert.assertNotNull(returnedCaseDetails);
    }

    @Test
    public void testValidateAccessCode() {
        when(systemUserService.getSysUserToken()).thenReturn(s2sToken);
        when(ccdCoreCaseDataService.findCaseById(s2sToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataNullCaseInvites);

        String returnedCaseStatus = linkCitizenCaseService.validateAccessCode(caseId, accessCode);
        Assert.assertEquals("Invalid", returnedCaseStatus);
    }

    @Test
    public void testValidateAccessCodeNull() {
        when(systemUserService.getSysUserToken()).thenReturn(s2sToken);
        when(ccdCoreCaseDataService.findCaseById(s2sToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(null);

        String returnedCaseStatus = linkCitizenCaseService.validateAccessCode(caseId, accessCode);
        Assert.assertEquals("Invalid", returnedCaseStatus);
    }

    @Test
    public void testLinkCitizenToCasePartyIdNull() {
        PartyDetails applicant = PartyDetails.builder().partyId(null)
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(YesOrNo.Yes)
                      .build()).build();
        CaseDetails caseDetails1 = CaseDetails.builder().build();
        CaseData caseData1 = CaseData.builder()
            .applicantsFL401(applicant)
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.Yes)
                                                                         .partyId(null)
                                                                         .accessCode(accessCode).build()).build()))
            .build();
        UserDetails userDetails1 = UserDetails.builder().id("123").build();
        when(systemUserService.getSysUserToken()).thenReturn(s2sToken);
        when(ccdCoreCaseDataService.findCaseById(s2sToken, caseId)).thenReturn(caseDetails1);
        when(objectMapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData1);

        Map<String, Object> stringObjectMap = caseData1.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap, caseData1, null);
        when(allTabService.getStartUpdateForSpecificEvent(caseId, CaseEvent.LINK_CITIZEN.getValue())).thenReturn(startAllTabsUpdateDataContent);
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails1);
        when(authTokenGenerator.generate()).thenReturn(authToken);

        Optional<CaseDetails> returnedCaseDetails = linkCitizenCaseService.linkCitizenToCase(authToken, caseId, accessCode);
        Assert.assertNotNull(returnedCaseDetails);
    }

    @Test
    public void testLinkCitizenToCaseNotRepresentbySolicitor() {
        PartyDetails applicant = PartyDetails.builder().partyId(null)
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(YesOrNo.No)
                      .build()).build();
        CaseDetails caseDetails1 = CaseDetails.builder().build();
        CaseData caseData1 = CaseData.builder()
            .respondentsFL401(applicant)
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.No)
                                                                         .partyId(null)
                                                                         .accessCode(accessCode).build()).build()))
            .build();
        UserDetails userDetails1 = UserDetails.builder().id("123").email("test@gmail.com").build();
        when(systemUserService.getSysUserToken()).thenReturn(s2sToken);
        when(ccdCoreCaseDataService.findCaseById(s2sToken, caseId)).thenReturn(caseDetails1);
        when(objectMapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData1);

        Map<String, Object> stringObjectMap = caseData1.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap, caseData1, null);
        when(allTabService.getStartUpdateForSpecificEvent(caseId, CaseEvent.LINK_CITIZEN.getValue())).thenReturn(startAllTabsUpdateDataContent);
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails1);
        when(authTokenGenerator.generate()).thenReturn(authToken);

        Optional<CaseDetails> returnedCaseDetails = linkCitizenCaseService.linkCitizenToCase(authToken, caseId, accessCode);
        Assert.assertNotNull(returnedCaseDetails);
    }

    @Test
    public void testLinkCitizenToCaseRespondent() {
        PartyDetails applicant = PartyDetails.builder().partyId(testUuid)
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(YesOrNo.No)
                      .build()).build();
        CaseDetails caseDetails1 = CaseDetails.builder().build();
        CaseData caseData1 = CaseData.builder()
            .respondents(Arrays.asList(element(applicant)))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.No)
                                                                         .partyId(testUuid)
                                                                         .accessCode(accessCode).build()).build()))
            .build();
        when(systemUserService.getSysUserToken()).thenReturn(s2sToken);
        when(ccdCoreCaseDataService.findCaseById(s2sToken, caseId)).thenReturn(caseDetails1);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData1);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap, caseData1, null);
        when(allTabService.getStartUpdateForSpecificEvent(caseId, CaseEvent.LINK_CITIZEN.getValue())).thenReturn(startAllTabsUpdateDataContent);
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(authToken);

        Optional<CaseDetails> returnedCaseDetails = linkCitizenCaseService.linkCitizenToCase(authToken, caseId, accessCode);
        Assert.assertNotNull(returnedCaseDetails);
    }
}

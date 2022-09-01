package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;

import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseServiceTest {

    private final String authToken = "Bearer abc";
    private final String s2sToken = "s2s token";
    private final String randomUserId = "e3ceb507-0137-43a9-8bd3-85dd23720648";
    private static final String randomAlphaNumeric = "Abc123EFGH";


    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseAccessApi caseAccessApi;

    @Mock
    private SystemUserService systemUserService;

    @InjectMocks
    private CaseService caseService;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private IdamClient idamClient;

    @Mock
    private CaseUtils caseUtils;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private DocumentGenService documentGenService;

    @Mock
    private ObjectMapper objectMapper;


    @Mock
    private AllTabServiceImpl allTabService;

    private CaseData caseData;

    public MultipartFile file;

    @Before
    public void setup() {

        caseData = CaseData.builder().id(1234567891234567L).applicantCaseName("xyz").build();
        when(idamClient.getUserInfo(Mockito.any())).thenReturn(UserInfo.builder().uid(randomUserId).build());
        when(authTokenGenerator.generate()).thenReturn(s2sToken);

    }

    @Test
    public void testValidateAccessCode() {
        CaseDetails tempCaseDetails = CaseDetails.builder().data(Map.of("id", "1234567891234567"))
            .state("SUBMITTED_PAID")
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .id(1234567891234567L)
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .respondentCaseInvites(List.of(element(CaseInvite.builder().accessCode("e3ceb507").build())))
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        String caseId = "1234567891234567";
        String accessCode = "e3ceb507";
        when(objectMapper.convertValue(tempCaseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(coreCaseDataApi.getCase(authToken, s2sToken, "1234567891234567")).thenReturn(tempCaseDetails);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        String data = caseService.validateAccessCode(authToken, s2sToken, caseId, accessCode);
        assertNotNull(data);
    }

    @Test
    public void testUpdateCase() {
        CaseDetails tempCaseDetails = CaseDetails.builder().data(Map.of("id", "1234567891234567"))
            .state("SUBMITTED_PAID")
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .id(1234567891234567L)
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .respondentCaseInvites(List.of(element(CaseInvite.builder().accessCode("e3ceb507").build())))
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        String caseId = "1234567891234567";
        String accessCode = "e3ceb507";
        when(coreCaseDataApi.getCase(authToken, s2sToken, "1234567891234567")).thenReturn(tempCaseDetails);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(idamClient.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().build());
        when(objectMapper.convertValue(this, new TypeReference<>() {})).thenReturn(stringObjectMap);
        when(coreCaseDataApi.startEventForCaseWorker(Mockito.anyString(),
                                                     Mockito.anyString(),
                                                     Mockito.anyString(),
                                                     Mockito.anyString(),
                                                     Mockito.anyString(),
                                                     Mockito.anyString(),
                                                     Mockito.anyString())).thenReturn(StartEventResponse.builder().build());
        when(coreCaseDataApi.submitEventForCaseWorker(Mockito.anyString(),
                                                     Mockito.anyString(),
                                                     Mockito.anyString(),
                                                     Mockito.anyString(),
                                                     Mockito.anyString(),
                                                     Mockito.anyString(),
                                                     Mockito.anyBoolean(),
                                                      Mockito.any(CaseDataContent.class))).thenReturn(caseDetails);
        CaseDetails caseDetails1 = caseService.updateCase(caseData,authToken, s2sToken, caseId, "123");
        assertNotNull(caseDetails1);
    }

    @Test
    public void testRetrieveCase() {
        CaseDetails tempCaseDetails = CaseDetails.builder().data(Map.of("id", "1234567891234567"))
            .state("SUBMITTED_PAID")
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .id(1234567891234567L)
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .respondentCaseInvites(List.of(element(CaseInvite.builder().accessCode("e3ceb507").build())))
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        String caseId = "1234567891234567";
        String accessCode = "e3ceb507";
        when(coreCaseDataApi.getCase(authToken, s2sToken, "1234567891234567")).thenReturn(tempCaseDetails);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(idamClient.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().id("123456789").build());
        when(objectMapper.convertValue(this, new TypeReference<>() {})).thenReturn(stringObjectMap);
        List<CaseData> data = caseService.retrieveCases(authToken, s2sToken, caseId, "123");
        assertNotNull(data);
    }

    @Test
    public void testLinkToCase() {
        CaseDetails tempCaseDetails = CaseDetails.builder().data(Map.of("id", "1234567891234567"))
            .state("SUBMITTED_PAID")
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .id(1234567891234567L)
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .respondentCaseInvites(List.of(element(CaseInvite.builder().accessCode("e3ceb507").build())))
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        String caseId = "1234567891234567";
        String accessCode = "e3ceb507";
        UserDetails userDetails = UserDetails.builder()
            .id("123456789")
            .email("test@gmail.com").build();
        when(coreCaseDataApi.getCase(authToken, s2sToken, "1234567891234567")).thenReturn(tempCaseDetails);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(systemUserService.getSysUserToken()).thenReturn("testSysUserToken");
        when(idamClient.getUserDetails(Mockito.anyString())).thenReturn(userDetails);

        when(objectMapper.convertValue(this, new TypeReference<>() {})).thenReturn(stringObjectMap);
        when(coreCaseDataApi.startEventForCitizen(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString()
        )).thenReturn(StartEventResponse.builder().build());
        when(coreCaseDataApi.submitEventForCitizen(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyBoolean(),
            Mockito.any(CaseDataContent.class)
        )).thenReturn(caseDetails);
        caseService.linkCitizenToCase(authToken, s2sToken, accessCode, caseId);
        verify(caseAccessApi).grantAccessToCase(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(UserId.class));
        verify(coreCaseDataApi).submitEventForCitizen(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyBoolean(),
            Mockito.any(CaseDataContent.class)
        );
        verify(coreCaseDataApi).startEventForCitizen(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString()
        );
    }
}

package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

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
//
//        when(coreCaseDataApi.getCase(authToken, s2sToken, "1234567891234567")).thenReturn(tempCaseDetails);
//        when(objectMapper.convertValue(tempCaseDetails.getData(), CaseData.class)).thenReturn(caseData);
//        String result = caseService.validateAccessCode("userToken", "s2sToken", "caseId", "accessCode");
//        Assertions.assertEquals("replaceMeWithExpectedResult", result);

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        String caseId = "1234567891234567";
        String accessCode = "e3ceb507";
        when(objectMapper.convertValue(tempCaseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(coreCaseDataApi.getCase(authToken, s2sToken, "1234567891234567")).thenReturn(tempCaseDetails);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        //when(caseService.validateAccessCode(authToken, s2sToken, caseId, accessCode)).thenReturn("test");
        String data = caseService.validateAccessCode(authToken, s2sToken, caseId, accessCode);
        assertNotNull(data);
    }



}

package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns.SafetyConcerns;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CaseApplicationResponseService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenResponseNotificationEmailService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService.IS_CONFIDENTIAL_DATA_PRESENT;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseApplicationResponseControllerTest {

    @InjectMocks
    private CaseApplicationResponseController caseApplicationResponseController;

    @Mock
    private CaseService caseService;

    @Mock
    private CaseApplicationResponseService caseApplicationResponseService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    DocumentGenService documentGenService;

    @Mock
    CitizenResponseNotificationEmailService solicitorNotificationService;

    @Mock
    IdamClient idamClient;

    @Mock
    C100RespondentSolicitorService c100RespondentSolicitorService;

    private CaseData caseData;
    private CaseDetails caseDetails;
    public static final String authToken = "Bearer TestAuthToken";
    public static final String servAuthToken = "Bearer TestServToken";
    private static final String caseId = "1234567891234567";
    private static final String partyId = "e3ceb507-0137-43a9-8bd3-85dd23720648";

    private  final  Map<String, Object> dataMap = new HashMap<>();

    private Map<String, Object> stringObjectMap;

    @Before
    public void setUp() throws Exception {
        dataMap.put(IS_CONFIDENTIAL_DATA_PRESENT, true);
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .respondents(List.of(Element.<PartyDetails>builder()
                                     .id(UUID.fromString(partyId))
                                     .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                                                .response(Response.builder().safetyConcerns(
                                         SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
                                     .build()))
            .build();
        stringObjectMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        when(documentGenService.generateSingleDocument(
            Mockito.anyString(),
            Mockito.any(CaseData.class),
            Mockito.anyString(),
            Mockito.anyBoolean()
        ))
            .thenReturn(Document.builder().build());
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(coreCaseDataApi.getCase(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.updateCase(Mockito.any(CaseData.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString()
        )).thenReturn(caseDetails);

        when(idamClient.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().build());
        when(c100RespondentSolicitorService.populateDataMap(any(), any())).thenReturn(new HashMap<>());
    }

    @Test
    public void testGenerateC7DraftDocument() throws Exception {
        when(caseApplicationResponseService.generateC7DraftDocument(authToken, caseData, false))
            .thenReturn(Document.builder().build());
        Document document = caseApplicationResponseController
            .generateC7DraftDocument(caseId, partyId, authToken, servAuthToken);
        assertNotNull(document);
    }

    @Test
    public void testGenerateC7FinalDocument() throws Exception {
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        CaseData returnedCaseData = caseApplicationResponseController
            .generateC7FinalDocument(caseId, partyId, authToken, servAuthToken);
        assertNotNull(returnedCaseData);
    }

    @Test
    public void testGenerateC7FinalDocumentCaseDetailsNotNull() throws Exception {
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseApplicationResponseService.generateCitizenResponseFinalDocuments(caseData, caseDetails, authToken, partyId, caseId))
            .thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        CaseData returnedCaseData = caseApplicationResponseController
            .generateC7FinalDocument(caseId, partyId, authToken, servAuthToken);
        assertNotNull(returnedCaseData);
    }
}

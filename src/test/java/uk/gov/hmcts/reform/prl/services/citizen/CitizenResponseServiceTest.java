package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenPartyDetailsMapper;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C7_DRAFT_HINT;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CitizenResponseServiceTest {

    @InjectMocks
    CitizenResponseService citizenResponseService;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    CcdCoreCaseDataService ccdCoreCaseDataService;
    @Mock
    AllTabServiceImpl allTabService;
    @Mock
    C100RespondentSolicitorService c100RespondentSolicitorService;
    @Mock
    DocumentGenService documentGenService;
    @Mock
    DocumentLanguageService documentLanguageService;
    @Mock
    CitizenPartyDetailsMapper citizenPartyDetailsMapper;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String caseId = "123";
    CaseData caseData;
    CaseData noneActiveCaseData;
    String uuid = "1afdfa01-8280-4e2c-b810-ab7cf741988a";
    CaseDetails caseDetails;
    CitizenUpdatedCaseData citizenUpdatedCaseData;
    StartAllTabsUpdateDataContent startAllTabsUpdateDataContent;
    StartAllTabsUpdateDataContent noneActiveStartAllTabsUpdateDataContent;

    @Before
    public void setUp() {
        UUID uuid2 = UUID.randomUUID();

        //Set up two respondents, one tp be the active respondent and one to not.
        PartyDetails activeRespondent = PartyDetails.builder()
            .partyId(UUID.fromString(uuid))
            .user(User.builder().idamId(uuid).build())
            .build();
        PartyDetails respondent = PartyDetails.builder()
            .partyId(uuid2)
            .user(User.builder().idamId(uuid2.toString()).build())
            .build();
        Element<PartyDetails> activeWrappedRespondent = Element.<PartyDetails>builder()
            .id(UUID.fromString(uuid))
            .value(activeRespondent).build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder()
            .id(uuid2)
            .value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = new ArrayList<>();
        listOfRespondents.add(activeWrappedRespondent);
        listOfRespondents.add(wrappedRespondent);
        List<Element<PartyDetails>> listOfNonActiveRespondents = new ArrayList<>();
        listOfNonActiveRespondents.add(wrappedRespondent);

        noneActiveCaseData = CaseData.builder().respondents(listOfNonActiveRespondents).build();
        caseData = CaseData.builder().respondents(listOfRespondents).build();
        Map<String, Object> arrayMap = new HashMap<>();
        caseDetails = CaseDetails.builder().data(arrayMap).id(123456789L).build();

        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication("C100")
            .partyType(PartyEnum.respondent)
            .partyDetails(PartyDetails.builder().user(User.builder().idamId(uuid).build()).build())
            .build();
        noneActiveStartAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), arrayMap, noneActiveCaseData, null);
    }

    @Test
    public void testGenerateDraftC7() throws Exception {
        when(ccdCoreCaseDataService.findCaseById(authToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(documentGenService.generateSingleDocument(authToken, caseData,  DOCUMENT_C7_DRAFT_HINT, false))
            .thenReturn(Document.builder().documentFileName("testDoc").build());

        Document document = citizenResponseService.generateAndReturnDraftC7(caseId, uuid, authToken);
        Assert.assertNotNull(document);
        Assert.assertEquals("testDoc", document.getDocumentFileName());
    }

    @Test(expected = RuntimeException.class)
    public void testGenerateAndSubmitCitizenResponseFL401() throws Exception {
        citizenResponseService.generateAndSubmitCitizenResponse(authToken, caseId, CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication("FL401").build());
    }

    @Test(expected = RuntimeException.class)
    public void testGenerateAndSubmitCitizenResponseButApplicant() throws Exception {
        citizenResponseService.generateAndSubmitCitizenResponse(authToken, caseId, CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication("C100").partyType(PartyEnum.applicant).build());
    }

    @Test
    public void testGenerateAndSubmitCitizenResponse() throws Exception {
        when(allTabService.getStartUpdateForSpecificUserEvent(caseId, CaseEvent.REVIEW_AND_SUBMIT.getValue(), authToken))
            .thenReturn(noneActiveStartAllTabsUpdateDataContent);
        when(documentLanguageService.docGenerateLang(noneActiveCaseData)).thenReturn(DocumentLanguage.builder()
            .isGenEng(true).isGenWelsh(true).build());
        when(documentGenService.generateSingleDocument(authToken, noneActiveCaseData,  "c7FinalEng", true))
            .thenReturn(Document.builder().documentFileName("testDoc").build());
        when(documentGenService.generateSingleDocument(authToken, noneActiveCaseData,  "c7FinalEng", false))
            .thenReturn(Document.builder().documentFileName("testDocWelsh").build());
        when(allTabService.submitUpdateForSpecificUserEvent(noneActiveStartAllTabsUpdateDataContent.authorisation(),
            caseId,  noneActiveStartAllTabsUpdateDataContent
            .startEventResponse(),  noneActiveStartAllTabsUpdateDataContent.eventRequestData(), new HashMap<>(),
            noneActiveStartAllTabsUpdateDataContent.userDetails()))
            .thenReturn(caseDetails);
        CaseDetails returnedCaseDetails = citizenResponseService.generateAndSubmitCitizenResponse(authToken, caseId,
            citizenUpdatedCaseData);
        Assert.assertNotNull(returnedCaseDetails);
        Assert.assertEquals(new HashMap<>(), returnedCaseDetails.getData());
    }
}

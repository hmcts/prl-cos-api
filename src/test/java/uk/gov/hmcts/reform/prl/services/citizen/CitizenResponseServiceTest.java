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
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenPartyDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
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
    CaseData caseData;
    String uuid = "1afdfa01-8280-4e2c-b810-ab7cf741988a";

    CaseDetails caseDetails;

    @Before
    public void setUp() {
        UUID uuid2 = UUID.randomUUID();

        //Set up two respondents, one tp be the active respondent and one to not.
        PartyDetails activeRespondent = PartyDetails.builder()
            .partyId(UUID.fromString(uuid))
            .build();
        PartyDetails respondent = PartyDetails.builder()
            .partyId(uuid2)
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

        caseData = CaseData.builder().respondents(listOfRespondents).build();
        Map<String, Object> arrayMap = new HashMap<>();
        caseDetails = CaseDetails.builder().data(arrayMap).id(123456789L).build();
    }

    @Test
    public void testGenerateDraftC7() throws Exception {
        when(ccdCoreCaseDataService.findCaseById(authToken, "123")).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(documentGenService.generateSingleDocument(authToken, caseData,  DOCUMENT_C7_DRAFT_HINT, false))
            .thenReturn(Document.builder().documentFileName("testDoc").build());

        Document document = citizenResponseService.generateAndReturnDraftC7("123", uuid, authToken);
        Assert.assertNotNull(document);
        Assert.assertEquals("testDoc", document.getDocumentFileName());
    }
}

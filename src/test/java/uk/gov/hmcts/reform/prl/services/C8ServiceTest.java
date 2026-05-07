package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;

@ExtendWith(MockitoExtension.class)
class C8ServiceTest {
    @Mock
    private ManageOrderService manageOrderService;
    @Mock
    private DocumentLanguageService documentLanguageService;
    @Mock
    private DocumentGenService documentGenService;
    @Spy
    private ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();

    @InjectMocks
    private C8Service c8Service;

    private static CaseData baseCaseDataWithOtherParty() {
        UUID otherId = UUID.randomUUID();
        PartyDetails partyDetails = PartyDetails.builder()
            .partyId(otherId)
            .firstName("John")
            .lastName("Doe")
            .isAddressConfidential(uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes)
            .build();
        Element<PartyDetails> partyElement = ElementUtils.element(otherId, partyDetails);
        List<Element<PartyDetails>> otherPartyList = List.of(partyElement);
        List<Element<PartyDetails>> applicants = List.of(ElementUtils.element(UUID.randomUUID(),
            PartyDetails.builder().firstName("Applicant").lastName("One").build()));
        return CaseData.builder()
            .id(1000L)
            .courtName("Swansea")
            .issueDate(LocalDate.now())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .taskListVersion(TASK_LIST_VERSION_V3)
            .otherPartyInTheCaseRevised(otherPartyList)
            .applicants(applicants)
            .state(State.CASE_ISSUED)
            .loggedInUserRole("CITIZEN")
            .newChildDetails(List.of())
            .build();
    }

    @Test
    void testGenerateOtherPartiesC8s_confidentialInfoAdded_generatesAndArchivesCorrectly() {
        // Arrange
        CaseData caseData = baseCaseDataWithOtherParty().toBuilder()
            .id(123L)
            .courtName("Test Court")
            .build();
        CaseData caseDataBefore = caseData.toBuilder().build();

        DocumentLanguage docLang = DocumentLanguage.builder()
            .isGenWelsh(false)
            .build();
        when(documentLanguageService.docGenerateLang(any())).thenReturn(docLang);
        Document doc = Document.builder()
            .documentUrl("url")
            .documentBinaryUrl("binary-url")
            .documentFileName("c8DocumentFileName")
            .build();
        when(documentGenService.generateSingleDocument(any(), any(), any(), anyBoolean(), anyMap())).thenReturn(doc);
        // Act
        Map<String, Object> result = c8Service.generateOtherPartiesC8s(caseData, caseDataBefore, "auth");
        // Assert
        assertTrue(result.containsKey("otherPartyC8Documents"));
        List<?> docs = (List<?>) result.get("otherPartyC8Documents");
        assertEquals(1, docs.size());
    }

    @Test
    void testGenerateOtherPartiesC8s_firstWelshC8GeneratedCorrectly() {
        // Arrange
        CaseData caseData = baseCaseDataWithOtherParty().toBuilder()
            .id(456L)
            .build();
        CaseData caseDataBefore = caseData.toBuilder().build();

        DocumentLanguage docLang = DocumentLanguage.builder()
            .isGenWelsh(true)
            .build();
        when(documentLanguageService.docGenerateLang(any())).thenReturn(docLang);
        Document englishDoc = Document.builder()
            .documentUrl("url-en")
            .documentBinaryUrl("binary-url-en")
            .documentFileName("c8DocumentFileName-en")
            .build();
        Document welshDoc = Document.builder()
            .documentUrl("url-cy")
            .documentBinaryUrl("binary-url-cy")
            .documentFileName("c8DocumentFileName-cy")
            .build();
        // The service generates Welsh first, then English
        when(documentGenService.generateSingleDocument(any(), any(), any(), eq(true), anyMap())).thenReturn(welshDoc);
        when(documentGenService.generateSingleDocument(any(), any(), any(), eq(false), anyMap())).thenReturn(englishDoc);
        // Act
        Map<String, Object> result = c8Service.generateOtherPartiesC8s(caseData, caseDataBefore, "auth");
        // Assert
        assertTrue(result.containsKey("otherPartyC8Documents"));
        List<?> docs = (List<?>) result.get("otherPartyC8Documents");
        assertEquals(1, docs.size());
        Element<?> responseElement = (Element<?>) docs.getFirst();
        ResponseDocuments respDocs = (ResponseDocuments) responseElement.getValue();
        assertEquals(englishDoc, respDocs.getRespondentC8Document());
        assertEquals(welshDoc, respDocs.getRespondentC8DocumentWelsh());
    }

    @Test
    void testGenerateOtherPartiesC8s_archivesOldC8WhenConfidentialInfoStillPresent() {
        // Arrange
        CaseData caseData = baseCaseDataWithOtherParty();
        Element<PartyDetails> partyDetails = caseData.getOtherPartyInTheCaseRevised().getFirst();
        String partyName = partyDetails.getValue().getFirstName() + " " + partyDetails.getValue().getLastName();
        Document oldEnglishDoc = Document.builder()
            .documentUrl("old-url-en")
            .documentBinaryUrl("old-binary-url-en")
            .documentFileName("old-c8DocumentFileName-en")
            .build();
        Document oldWelshDoc = Document.builder()
            .documentUrl("old-url-cy")
            .documentBinaryUrl("old-binary-url-cy")
            .documentFileName("old-c8DocumentFileName-cy")
            .build();
        ResponseDocuments oldRespDocs = ResponseDocuments.builder()
            .respondentC8Document(oldEnglishDoc)
            .respondentC8DocumentWelsh(oldWelshDoc)
            .partyName(partyName)
            .build();
        Element<ResponseDocuments> oldRespElement = ElementUtils.element(partyDetails.getId(), oldRespDocs);
        List<Element<ResponseDocuments>> oldDocs = List.of(oldRespElement);

        caseData = caseData.toBuilder()
            .otherPartyC8Documents(oldDocs)
            .build();
        CaseData caseDataBefore = caseData.toBuilder().build();

        DocumentLanguage docLang = DocumentLanguage.builder()
            .isGenWelsh(true)
            .build();
        when(documentLanguageService.docGenerateLang(any())).thenReturn(docLang);
        Document englishDoc = Document.builder()
            .documentUrl("url-en")
            .documentBinaryUrl("binary-url-en")
            .documentFileName("c8DocumentFileName-en")
            .build();
        Document welshDoc = Document.builder()
            .documentUrl("url-cy")
            .documentBinaryUrl("binary-url-cy")
            .documentFileName("c8DocumentFileName-cy")
            .build();
        when(documentGenService.generateSingleDocument(any(), any(), any(), eq(true), anyMap())).thenReturn(welshDoc);
        when(documentGenService.generateSingleDocument(any(), any(), any(), eq(false), anyMap())).thenReturn(englishDoc);
        // Act
        Map<String, Object> result = c8Service.generateOtherPartiesC8s(caseData, caseDataBefore, "auth");
        // Assert
        assertTrue(result.containsKey("otherPartyC8Documents"));
        assertTrue(result.containsKey("otherPartyC8DocumentsArchived"));
        List<?> docs = (List<?>) result.get("otherPartyC8Documents");
        List<?> archived = (List<?>) result.get("otherPartyC8DocumentsArchived");
        assertEquals(1, docs.size());
        assertEquals(1, archived.size());
        // Check new docs are the new ones
        Element<?> responseElement = (Element<?>) docs.getFirst();
        ResponseDocuments respDocs = (ResponseDocuments) responseElement.getValue();
        assertEquals(englishDoc, respDocs.getRespondentC8Document());
        assertEquals(welshDoc, respDocs.getRespondentC8DocumentWelsh());
        // Check archived docs are the old ones
        Element<?> archivedElement = (Element<?>) archived.getFirst();
        ResponseDocuments archivedRespDocs = (ResponseDocuments) archivedElement.getValue();
        assertEquals(oldEnglishDoc, archivedRespDocs.getRespondentC8Document());
        assertEquals(oldWelshDoc, archivedRespDocs.getRespondentC8DocumentWelsh());
        assertEquals(partyName, archivedRespDocs.getPartyName());
    }

    @Test
    void testGenerateOtherPartiesC8s_draftStatePutsDocsInDraftField() {
        // Arrange
        CaseData caseData = baseCaseDataWithOtherParty().toBuilder()
            .id(321L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        CaseData caseDataBefore = caseData.toBuilder().build();

        DocumentLanguage docLang = DocumentLanguage.builder()
            .isGenWelsh(false)
            .build();
        when(documentLanguageService.docGenerateLang(any())).thenReturn(docLang);
        Document doc = Document.builder()
            .documentUrl("draft-url")
            .documentBinaryUrl("draft-binary-url")
            .documentFileName("draft-c8DocumentFileName")
            .build();
        when(documentGenService.generateSingleDocument(any(), any(), any(), anyBoolean(), anyMap())).thenReturn(doc);
        // Act
        Map<String, Object> result = c8Service.generateOtherPartiesC8s(caseData, caseDataBefore, "auth");
        // Assert
        assertTrue(result.containsKey("otherPartyC8DocumentsDraft"));
        List<?> docs = (List<?>) result.get("otherPartyC8DocumentsDraft");
        assertEquals(1, docs.size());
    }
}

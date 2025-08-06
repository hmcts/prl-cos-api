package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@ExtendWith(MockitoExtension.class)
class C8ArchiveServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ConfidentialDetailsChangeHelper confidentialDetailsChangeHelper;

    @Mock
    private CallbackRequest callbackRequest;

    @Mock
    private CaseDetails caseDetailsBefore;

    @InjectMocks
    private C8ArchiveService c8ArchiveService;


    @Test
    void shouldArchiveC8DocumentWhenConfidentialDetailsChanged() {

        Map<String, Object> caseDataBeforeRaw = new HashMap<>();

        when(callbackRequest.getCaseDetailsBefore()).thenReturn(caseDetailsBefore);
        when(caseDetailsBefore.getData()).thenReturn(caseDataBeforeRaw);

        CaseData caseDataBefore = CaseData.builder().id(1234L).build();
        when(objectMapper.convertValue(caseDataBeforeRaw, CaseData.class)).thenReturn(caseDataBefore);

        Document c8Document = Document.builder()
            .documentUrl("url")
            .documentBinaryUrl("binary-url")
            .documentFileName("c8DocumentFileName")
            .build();

        Document c8DocumentWelsh = Document.builder()
            .documentUrl("url")
            .documentBinaryUrl("binary-url")
            .documentFileName("c8DocumentFileNameWelsh")
            .build();

        CaseData caseData = CaseData.builder()
            .id(5678L)
            .c8Document(c8Document)
            .c8WelshDocument(c8DocumentWelsh)
            .build();

        when(confidentialDetailsChangeHelper.haveConfidentialDetailsChanged(caseData, caseDataBefore)).thenReturn(true);

        Map<String, Object> caseDataUpdated = new HashMap<>();

        c8ArchiveService.archiveC8DocumentIfConfidentialChanged(callbackRequest, caseData, caseDataUpdated);

        List<Element<Document>> archivedDocs = (List<Element<Document>>) caseDataUpdated.get("c8ArchivedDocuments");

        assertThat(archivedDocs)
            .isNotNull()
            .hasSize(2);

        Document archivedDoc = archivedDocs.get(0).getValue();
        assertThat(archivedDoc.getDocumentFileName()).isEqualTo("C8ArchivedDocument.pdf");
        assertThat(archivedDoc.getDocumentUrl()).isEqualTo("url");
        assertThat(archivedDoc.getDocumentBinaryUrl()).isEqualTo("binary-url");
    }

    @Test
    void shouldAddToExistingArchivedDocumentsWhenPresent() {
        Document existingArchivedDoc = Document.builder()
            .documentUrl("existing-doc")
            .documentBinaryUrl("existing-binary")
            .documentFileName("existing.pdf")
            .build();

        List<Element<Document>> existingArchivedDocs = List.of(buildElement(existingArchivedDoc));

        Map<String, Object> caseDataBeforeRaw = new HashMap<>();
        when(callbackRequest.getCaseDetailsBefore()).thenReturn(caseDetailsBefore);
        when(caseDetailsBefore.getData()).thenReturn(caseDataBeforeRaw);

        CaseData caseDataBefore = CaseData.builder().id(1234L).build();
        when(objectMapper.convertValue(caseDataBeforeRaw, CaseData.class)).thenReturn(caseDataBefore);

        Document c8Document = Document.builder()
            .documentUrl("url")
            .documentBinaryUrl("binary-url")
            .documentFileName("c8DocumentFileName")
            .build();

        CaseData caseData = CaseData.builder()
            .id(5678L)
            .c8Document(c8Document)
            .c8ArchivedDocuments(existingArchivedDocs)
            .build();

        when(confidentialDetailsChangeHelper.haveConfidentialDetailsChanged(caseData, caseDataBefore)).thenReturn(true);

        Map<String, Object> caseDataUpdated = new HashMap<>();

        c8ArchiveService.archiveC8DocumentIfConfidentialChanged(callbackRequest, caseData, caseDataUpdated);

        List<Element<Document>> archivedDocs = (List<Element<Document>>) caseDataUpdated.get("c8ArchivedDocuments");

        assertThat(archivedDocs)
            .isNotNull()
            .hasSize(2);

        assertThat(archivedDocs.get(0).getValue().getDocumentFileName()).isEqualTo("existing.pdf");
        assertThat(archivedDocs.get(1).getValue().getDocumentFileName()).isEqualTo("C8ArchivedDocument.pdf");
    }

    @Test
    void shouldLogWhenNoC8DocumentsExistButConfidentialDetailsChanged() {
        Map<String, Object> caseDataBeforeRaw = new HashMap<>();
        when(callbackRequest.getCaseDetailsBefore()).thenReturn(caseDetailsBefore);
        when(caseDetailsBefore.getData()).thenReturn(caseDataBeforeRaw);

        CaseData caseDataBefore = CaseData.builder().id(1234L).build();
        when(objectMapper.convertValue(caseDataBeforeRaw, CaseData.class)).thenReturn(caseDataBefore);

        CaseData caseData = CaseData.builder().id(5678L).build();

        when(confidentialDetailsChangeHelper.haveConfidentialDetailsChanged(caseData, caseDataBefore)).thenReturn(true);

        Map<String, Object> caseDataUpdated = new HashMap<>();

        c8ArchiveService.archiveC8DocumentIfConfidentialChanged(callbackRequest, caseData, caseDataUpdated);

        assertThat(caseDataUpdated.containsKey("c8ArchivedDocuments")).isFalse();
    }

    @Test
    void shouldFailArchiveC8DocumentsWhenPreviousAndCurrentPartyIdAreDifferent() {
        PartyDetails previousApplicant = PartyDetails.builder()
            .partyId(UUID.randomUUID())
            .firstName("previous")
            .build();

        List<Element<PartyDetails>> listPartyDetails = List.of(
            Element.<PartyDetails>builder()
                .id(UUID.randomUUID())
                .value(previousApplicant)
                .build()
        );

        PartyDetails currentApplicant = PartyDetails.builder()
            .partyId(UUID.randomUUID())
            .firstName("New")
            .build();

        CaseData caseData = CaseData.builder()
            .id(1234L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(listPartyDetails)
            .build();

        CitizenUpdatedCaseData citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .partyDetails(currentApplicant)
            .build();

        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();

        c8ArchiveService.archiveC8DocumentIfConfidentialChangedFromCitizen(
            caseData, citizenUpdatedCaseData, caseDataMapToBeUpdated
        );

        verify(confidentialDetailsChangeHelper, never()).haveContactDetailsChanged(any(), any());
    }

    @Test
    void shouldArchiveC8DocumentsWhenPreviousAndCurrentPartyIDsAreTheSame() {
        UUID previousCurrentPartyId = UUID.randomUUID();
        PartyDetails previousApplicant = PartyDetails.builder()
            .partyId(previousCurrentPartyId)
            .firstName("previous")
            .build();

        List<Element<PartyDetails>> listPartyDetails = List.of(
            Element.<PartyDetails>builder()
                .id(previousCurrentPartyId)
                .value(previousApplicant)
                .build()
        );

        PartyDetails currentApplicant = PartyDetails.builder()
            .partyId(previousCurrentPartyId)
            .firstName("New")
            .build();

        CaseData caseData = CaseData.builder()
            .id(1234L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(listPartyDetails)
            .build();

        CitizenUpdatedCaseData citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .partyDetails(currentApplicant)
            .build();

        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();

        c8ArchiveService.archiveC8DocumentIfConfidentialChangedFromCitizen(
            caseData, citizenUpdatedCaseData, caseDataMapToBeUpdated
        );

        verify(confidentialDetailsChangeHelper, times(1)).haveContactDetailsChanged(any(), any());
    }

    private Element<Document> buildElement(Document document) {
        return Element.<Document>builder()
            .id(randomUUID())
            .value(document)
            .build();
    }
}


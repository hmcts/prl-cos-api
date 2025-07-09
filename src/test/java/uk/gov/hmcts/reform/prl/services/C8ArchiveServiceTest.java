package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
            .documentUrl("http://doc-url")
            .documentBinaryUrl("http://doc-binary-url")
            .documentFileName("original-c8.pdf")
            .build();

        CaseData caseData = CaseData.builder()
            .id(5678L)
            .c8Document(c8Document)
            .build();

        when(confidentialDetailsChangeHelper.haveConfidentialDetailsChanged(caseData, caseDataBefore)).thenReturn(true);

        Map<String, Object> caseDataUpdated = new HashMap<>();

        // Act
        c8ArchiveService.archiveC8DocumentIfConfidentialChanged(callbackRequest, caseData, caseDataUpdated);

        // Assert
        List<Element<Document>> archivedDocs = (List<Element<Document>>) caseDataUpdated.get("c8ArchivedDocuments");

        assertThat(archivedDocs)
            .isNotNull()
            .hasSize(1);

        Document archivedDoc = archivedDocs.get(0).getValue();
        assertThat(archivedDoc.getDocumentFileName()).isEqualTo("C8ArchivedDocument.pdf");
        assertThat(archivedDoc.getDocumentUrl()).isEqualTo("http://doc-url");
        assertThat(archivedDoc.getDocumentBinaryUrl()).isEqualTo("http://doc-binary-url");
    }
}


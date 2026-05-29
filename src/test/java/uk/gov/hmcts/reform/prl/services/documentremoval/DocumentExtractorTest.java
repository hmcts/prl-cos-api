package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.prl.exception.DocumentExtractorException;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class DocumentExtractorTest {

    private DocumentExtractor documentRetriever;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = Mockito.spy(CcdObjectMapper.getObjectMapper());
        documentRetriever = new DocumentExtractor(objectMapper);
    }

    @Test
    void getCaseDocuments() {
        CaseData caseData = DocumentRemovalTestUtils.createTestCase();

        List<Document> caseDocuments = documentRetriever.getCaseDocuments(caseData);
        assertThat(caseDocuments)
            .extracting(Document::getDocumentId, Document::getDocumentFileName, Document::getUploadTimeStamp)
            .containsExactlyInAnyOrder(
                tuple("1", "c1a.pdf", LocalDateTime.parse("2007-12-03T10:15:30")),
                tuple("2", "order1.pdf", LocalDateTime.parse("2006-12-03T10:15:30")),
                tuple("3", "order2.pdf", LocalDateTime.parse("2008-12-03T10:15:30")),
                tuple("4", "servedApp1.pdf", LocalDateTime.parse("2005-12-03T10:15:30")),
                tuple("5", "servedApp2.pdf", LocalDateTime.parse("2009-12-03T10:15:30")),
                tuple("6", "servedApp3.pdf", LocalDateTime.parse("2009-12-03T14:15:30")),
                tuple("10001", "supporting-document.pdf", LocalDateTime.parse("2015-12-03T10:15:30")),
                tuple("10002", "supporting-document.pdf", LocalDateTime.parse("2006-08-03T10:15:30"))
            );
    }

    @Test
    void getCaseDocuments_whenJsonProcessingException_thenThrowsException() throws JsonProcessingException {
        CaseData caseData = DocumentRemovalTestUtils.createTestCase();

        when(objectMapper.treeToValue(
            any(),
            eq(Document.class)
        )).thenThrow(Mockito.mock(JsonProcessingException.class));

        assertThatThrownBy(() -> documentRetriever.getCaseDocuments(caseData))
            .isInstanceOf(DocumentExtractorException.class);
    }
}

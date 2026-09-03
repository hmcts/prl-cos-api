package uk.gov.hmcts.reform.prl.services.document.docmosis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.prl.clients.DocmosisClient;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.docmosis.DocmosisRenderRequest;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocmosisRenderServiceTest {

    @Mock
    private DocmosisClient docmosisClient;
    @Mock
    private DocmosisTemplatesConfig templatesConfig;
    @Mock
    private TemplateDataMapper templateDataMapper;
    @Mock
    private UploadDocumentService uploadDocumentService;

    private DocmosisRenderService docmosisRenderService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-05-02T10:15:30.00Z"), ZoneId.of("Europe/London"));

        docmosisRenderService = new DocmosisRenderService(
            docmosisClient,
            templatesConfig,
            templateDataMapper,
            uploadDocumentService,
            clock
        );
    }

    @Test
    void shouldRenderAndStoreDocument_withStaticTemplateName() {
        // Arrange
        Map<String, Object> values = new HashMap<>();
        GenerateDocumentRequest request = GenerateDocumentRequest.builder()
            .template("template1")
            .values(values)
            .caseId("123")
            .build();

        byte[] pdfBytes = new byte[]{1, 2, 3};
        when(templatesConfig.getFilenameByTemplateName("template1")).thenReturn(Optional.of("file1.pdf"));
        when(docmosisClient.render(any(DocmosisRenderRequest.class))).thenReturn(pdfBytes);

        var uploadedDocument = mockUploadedDocument();
        when(uploadDocumentService.uploadDocument(any(), any(), any(), any())).thenReturn(uploadedDocument);

        // Act
        GeneratedDocumentInfo result = docmosisRenderService.renderAndStoreDocument("auth", request);

        // Assert
        assertThat(result.getUrl()).isEqualTo("http://doc/self");
        assertThat(result.getMimeType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(result.getHashToken()).isEqualTo("hash123");
        assertThat(result.getBinaryUrl()).isEqualTo("http://doc/binary");
        assertThat(result.getDocName()).isEqualTo("file1.pdf");

        // Check that current_date is added to placeholders
        ArgumentCaptor<DocmosisRenderRequest> captor = ArgumentCaptor.forClass(DocmosisRenderRequest.class);
        verify(docmosisClient).render(captor.capture());
        Map<String, Object> data = captor.getValue().getData();
        assertThat(data).contains(entry("current_date", "2026-05-02T11:15:30.000"));
    }

    @Test
    void shouldRenderAndStoreDocument_withDynamicFileName() {
        // Arrange
        Map<String, Object> values = new HashMap<>();
        values.put(DocmosisRenderService.DYNAMIC_FILE_NAME, "dynamicName.pdf");
        GenerateDocumentRequest request = GenerateDocumentRequest.builder()
            .template("template2")
            .values(values)
            .caseId("456")
            .build();

        byte[] pdfBytes = new byte[]{4, 5, 6};
        when(docmosisClient.render(any(DocmosisRenderRequest.class))).thenReturn(pdfBytes);

        var uploadedDocument = mockUploadedDocument();
        when(uploadDocumentService.uploadDocument(any(), any(), any(), any())).thenReturn(uploadedDocument);

        // Act
        GeneratedDocumentInfo result = docmosisRenderService.renderAndStoreDocument("auth2", request);

        // Assert
        assertThat(result)
            .extracting(GeneratedDocumentInfo::getDocName, GeneratedDocumentInfo::getUrl)
            .containsExactly("dynamicName.pdf", "http://doc/self");
    }

    @Test
    void shouldThrowException_whenTemplateNotFound() {
        // Arrange
        Map<String, Object> values = new HashMap<>();
        GenerateDocumentRequest request = GenerateDocumentRequest.builder()
            .template("missingTemplate")
            .values(values)
            .caseId("789")
            .build();

        when(templatesConfig.getFilenameByTemplateName("missingTemplate")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> docmosisRenderService.renderAndStoreDocument("auth3", request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("missingTemplate template not found");
    }

    private uk.gov.hmcts.reform.ccd.document.am.model.Document mockUploadedDocument() {
        var links = new Document.Links();
        links.self = new Document.Link();
        links.self.href = "http://doc/self";
        links.binary = new Document.Link();
        links.binary.href = "http://doc/binary";
        return uk.gov.hmcts.reform.ccd.document.am.model.Document.builder()
            .links(links)
            .mimeType(MediaType.APPLICATION_PDF_VALUE)
            .hashToken("hash123")
            .build();
    }
}


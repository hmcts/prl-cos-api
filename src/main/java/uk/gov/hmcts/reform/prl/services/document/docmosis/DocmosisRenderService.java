package uk.gov.hmcts.reform.prl.services.document.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.DocmosisClient;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.docmosis.DocmosisRenderRequest;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;

import java.time.Clock;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocmosisRenderService {

    public static final String DYNAMIC_FILE_NAME = "dynamic_fileName";
    private static final String CURRENT_DATE_KEY = "current_date";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSS";

    private final DocmosisClient docmosisClient;
    private final DocmosisTemplatesConfig templatesConfig;
    private final UploadDocumentService uploadDocumentService;
    private final Clock clock;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

    public GeneratedDocumentInfo renderAndStoreDocument(String authToken, GenerateDocumentRequest request) {
        byte[] pdf = generateDocument(request);
        String templateFilename = getTemplateFilename(request);
        return storeDocument(pdf, authToken, templateFilename);
    }

    private byte[] generateDocument(GenerateDocumentRequest generateDocumentRequest) {
        String templateFilename = getTemplateFilename(generateDocumentRequest);
        log.info("Case ID {}: Generating document {}", generateDocumentRequest.getCaseId(), templateFilename);

        Map<String, Object> placeholders = generateDocumentRequest.getValues();
        placeholders.put(CURRENT_DATE_KEY, dateTimeFormatter.format(clock.instant().atZone(ZoneId.systemDefault())));

        DocmosisRenderRequest request = DocmosisRenderRequest.builder()
            .data(placeholders)
            .outputName("result.pdf")
            .templateName(generateDocumentRequest.getTemplate())
            .build();

        return docmosisClient.render(request);
    }

    private String getTemplateFilename(GenerateDocumentRequest request) {
        String templateName = request.getTemplate();
        Map<String, Object> placeholders = request.getValues();
        if (placeholders.containsKey(DYNAMIC_FILE_NAME)) {
            return String.valueOf(placeholders.get(DYNAMIC_FILE_NAME));
        } else {
            Optional<String> filenameOptional = templatesConfig.getFilenameByTemplateName(templateName);
            return filenameOptional.orElseThrow(() -> new IllegalArgumentException(templateName + " template not found"));
        }
    }

    private GeneratedDocumentInfo storeDocument(byte[] pdf, String authToken, String filename) {
        var document = uploadDocumentService.uploadDocument(pdf, filename, MediaType.APPLICATION_PDF_VALUE, authToken);

        return GeneratedDocumentInfo.builder()
            .url(document.links.self.href)
            .mimeType(document.mimeType)
            .hashToken(document.hashToken)
            .binaryUrl(document.links.binary.href)
            .docName(filename)
            .build();
    }
}

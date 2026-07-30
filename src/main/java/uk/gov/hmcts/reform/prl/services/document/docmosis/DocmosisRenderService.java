package uk.gov.hmcts.reform.prl.services.document.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.DocmosisClient;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.docmosis.DocmosisRenderRequest;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocmosisRenderService {

    static final String DYNAMIC_FILE_NAME = "dynamic_fileName";
    private static final String CURRENT_DATE_KEY = "current_date";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSS";

    private final DocmosisClient docmosisClient;
    private final DocmosisTemplatesConfig templatesConfig;
    private final TemplateDataMapper templateDataMapper;
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
        applyRtfValues(generateDocumentRequest);
        Map<String, Object> placeholders = templateDataMapper.map(generateDocumentRequest.getValues());
        log.info("placeholders.rtfValue: {}", placeholders.get("rtfValue"));
        placeholders.put(CURRENT_DATE_KEY, dateTimeFormatter.format(ZonedDateTime.now(clock)));

        DocmosisRenderRequest request = DocmosisRenderRequest.builder()
            .data(placeholders)
            .outputName("result.pdf")
            .templateName(generateDocumentRequest.getTemplate())
            .build();

        return docmosisClient.render(request);
    }

    private void applyRtfValues(GenerateDocumentRequest generateDocumentRequest) {
        Map<String, Object> caseDetailsMap = (((Map<String, Object>)
            (generateDocumentRequest.getValues().get("caseDetails"))));
        if (caseDetailsMap != null) {
            Map<String, Object> caseDataMap = ((Map<String, Object>) caseDetailsMap
                .get("case_data"));
            if (caseDataMap != null) {
                String rtfValue = caseDataMap.get("recitalsOrPreamble") == null
                    ? "" : caseDataMap.get("recitalsOrPreamble").toString();
                if (StringUtils.isEmpty(rtfValue)) {
                    rtfValue = "<p><strong>This is sample Bold text for demonstration purposes. "
                        + "It can be used to verify formatting, layout, and content rendering within the application.</strong></p>"
                        + "<p></p><p><em>This is sample Italic text for demonstration purposes. "
                        + "It can be used to verify formatting, layout, and content rendering within the application.</em></p>"
                        + "<p></p><p><u>This is sample Underline text for demonstration purposes. "
                        + "It can be used to verify formatting, layout, and content rendering within the application.</u></p>"
                        + "<p></p><p>This is sample paragraph text for demonstration purposes. "
                        + "It can be used to verify formatting, layout, and content rendering within the application.</p>"
                        + "<ol><li><p>Create a new case.</p></li><li><p>Enter the required details."
                        + "</p></li><li><p>Review the information.</p></li><li><p>Submit the application.</p>"
                        + "</li><li><p>Receive confirmation.</p></li></ol><p></p><ul><li><p>Gather requirements.</p></li></ul>"
                        + "<ul><li><p>Design the solution.</p></li><li><p>Implement the changes.</p></li><li><p>Perform testing.</p></li><"
                        + "li><p>Deploy to production.</p></li></ul><p></p>";

                }
                caseDataMap.put("rtfValue", rtfValue);
            }
        }
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

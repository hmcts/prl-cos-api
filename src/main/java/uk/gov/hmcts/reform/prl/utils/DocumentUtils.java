package uk.gov.hmcts.reform.prl.utils;

import org.apache.commons.io.IOUtils;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;

import java.io.IOException;
import java.io.InputStream;

public class DocumentUtils {

    public static GeneratedDocumentInfo toGeneratedDocumentInfo(Document document) {
        return GeneratedDocumentInfo.builder()
            .url(document.getDocumentUrl())
            .binaryUrl(document.getDocumentBinaryUrl())
            .hashToken(document.getDocumentHash())
            .build();
    }

    public static Document toCoverLetterDocument(GeneratedDocumentInfo generatedDocumentInfo) {
        if (null != generatedDocumentInfo) {
            return Document.builder().documentUrl(generatedDocumentInfo.getUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentFileName("coverletter.pdf")
                .build();
        }
        return null;
    }


    public static Document toDocument(GeneratedDocumentInfo generateDocument) {
        if (null != generateDocument) {
            return Document.builder().documentUrl(generateDocument.getUrl())
                .documentHash(generateDocument.getHashToken())
                .documentBinaryUrl(generateDocument.getBinaryUrl())
                .documentFileName(generateDocument.getDocName())
                .build();
        }
        return null;
    }

    public static Document toPrlDocument(uk.gov.hmcts.reform.ccd.document.am.model.Document document) {
        if (null != document) {
            return Document.builder()
                .documentUrl(document.links.self.href)
                .documentBinaryUrl(document.links.binary.href)
                .documentHash(document.hashToken)
                .documentFileName(document.originalDocumentName).build();
        }
        return null;
    }

    public static byte[] readBytes(String resourcePath) {
        try (InputStream inputStream = ResourceReader.class.getResourceAsStream(resourcePath)) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Unable to read resource: " + resourcePath, e);
        }
    }
}

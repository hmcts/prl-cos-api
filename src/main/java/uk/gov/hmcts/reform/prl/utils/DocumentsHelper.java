package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import uk.gov.hmcts.reform.prl.models.documents.Document;

@Slf4j
public class DocumentsHelper {

    private DocumentsHelper() {
        // No OP
    }

    private static boolean hasDocumentPresent(Document document) {
        return document != null;
    }


    public static boolean hasDocumentUploaded(Document document) {
        return hasDocumentPresent(document) && document != null;
    }

    public static boolean hasExtension(Document document, String extension) {
        return hasExtension(document.getDocumentFileName(), extension);
    }

    public static boolean hasExtension(String filename, String extension) {
        return extension.equalsIgnoreCase(FilenameUtils.getExtension(filename));
    }

    public static String updateExtension(String filename, String newExtension) {
        if (!hasExtension(filename, newExtension)) {
            return FilenameUtils.removeExtension(filename).concat("." + newExtension);
        }
        return filename;
    }
}

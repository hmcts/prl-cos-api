package uk.gov.hmcts.reform.prl.utils;

import com.deepoove.poi.xwpf.NiceXWPFDocument;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utility class for combining DOCX documents.
 * Uses poi-tl's NiceXWPFDocument which handles merging of styles, numbering,
 * images, and other formatting elements automatically.
 */
@Slf4j
@UtilityClass
public class DocxCombineUtils {

    /**
     * Combines a header document with user content document.
     * Uses poi-tl's merge functionality which properly handles:
     * - Styles
     * - Numbering (lists)
     * - Images
     * - Comments
     * - Footnotes/Endnotes
     *
     * @param headerBytes Header document bytes
     * @param userContentBytes User's uploaded document bytes
     * @return Combined document bytes
     * @throws IOException if document processing fails
     */
    public static byte[] combineDocuments(byte[] headerBytes, byte[] userContentBytes) throws IOException {
        log.info("Combining header ({} bytes) with user content ({} bytes)",
            headerBytes.length, userContentBytes.length);

        try (NiceXWPFDocument headerDoc = new NiceXWPFDocument(new ByteArrayInputStream(headerBytes));
             NiceXWPFDocument userDoc = new NiceXWPFDocument(new ByteArrayInputStream(userContentBytes));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // NiceXWPFDocument.merge() handles all the complexity:
            // - Remaps numbering IDs to avoid conflicts
            // - Copies styles, images, comments, footnotes
            // - Appends all body elements (paragraphs, tables, etc.)
            NiceXWPFDocument merged = headerDoc.merge(userDoc);

            merged.write(out);
            log.info("Combined document size: {} bytes", out.size());
            return out.toByteArray();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to merge documents", e);
            throw new IOException("Failed to merge documents: " + e.getMessage(), e);
        }
    }
}

package uk.gov.hmcts.reform.prl.utils;

import com.deepoove.poi.xwpf.NiceXWPFDocument;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

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

            // Security validation - reject documents with potentially dangerous content
            validateDocumentSecurity(userDoc);

            // Lock in formatting on header doc before merge to prevent user doc styles overriding it
            lockInFormatting(headerDoc);

            // NiceXWPFDocument.merge() handles all the complexity:
            // - Remaps numbering IDs to avoid conflicts
            // - Copies styles, images, comments, footnotes
            // - Appends all body elements (paragraphs, tables, etc.)
            // BUT it does NOT copy headers/footers from the merged document
            NiceXWPFDocument merged = headerDoc.merge(userDoc);

            // Copy headers and footers from user's uploaded document to preserve them
            copyHeadersAndFooters(userDoc, merged);

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

    private static void copyHeadersAndFooters(NiceXWPFDocument source, NiceXWPFDocument target) {
        copyHeaders(source, target);
        copyFooters(source, target);
    }

    private static void copyHeaders(NiceXWPFDocument source, NiceXWPFDocument target) {
        List<XWPFHeader> sourceHeaders = source.getHeaderList();
        if (sourceHeaders == null || sourceHeaders.isEmpty()) {
            log.info("No headers found in user document");
            return;
        }

        try {
            // Find first header with actual text content
            XWPFHeader sourceHeader = sourceHeaders.stream()
                .filter(h -> h.getParagraphs().stream()
                    .anyMatch(p -> p.getText() != null && !p.getText().trim().isEmpty()))
                .findFirst()
                .orElse(sourceHeaders.getFirst());
            XWPFHeader targetHeader = target.createHeader(HeaderFooterType.DEFAULT);
            for (XWPFParagraph sourcePara : sourceHeader.getParagraphs()) {
                XWPFParagraph targetPara = targetHeader.createParagraph();
                copyParagraph(sourcePara, targetPara);
            }
            log.info("Copied header to merged document");
        } catch (Exception e) {
            log.warn("Failed to copy headers: {}", e.getMessage());
        }
    }

    private static void copyFooters(NiceXWPFDocument source, NiceXWPFDocument target) {
        List<XWPFFooter> sourceFooters = source.getFooterList();
        if (sourceFooters == null || sourceFooters.isEmpty()) {
            log.info("No footers found in user document");
            return;
        }

        try {
            XWPFFooter sourceFooter = sourceFooters.getFirst();
            XWPFFooter targetFooter = target.createFooter(HeaderFooterType.DEFAULT);
            for (XWPFParagraph sourcePara : sourceFooter.getParagraphs()) {
                XWPFParagraph targetPara = targetFooter.createParagraph();
                copyParagraph(sourcePara, targetPara);
            }
            log.info("Copied footer to merged document");
        } catch (Exception e) {
            log.warn("Failed to copy footers: {}", e.getMessage());
        }
    }

    private static void copyParagraph(XWPFParagraph source, XWPFParagraph target) {
        // Copy paragraph properties via XML
        if (source.getCTP().getPPr() != null) {
            target.getCTP().setPPr(source.getCTP().getPPr());
        }
        // Copy runs (text content with formatting)
        for (XWPFRun sourceRun : source.getRuns()) {
            XWPFRun targetRun = target.createRun();
            targetRun.setText(sourceRun.getText(0));
            if (sourceRun.isBold()) {
                targetRun.setBold(true);
            }
            if (sourceRun.isItalic()) {
                targetRun.setItalic(true);
            }
            if (sourceRun.getFontFamily() != null) {
                targetRun.setFontFamily(sourceRun.getFontFamily());
            }
            if (sourceRun.getFontSizeAsDouble() != null) {
                targetRun.setFontSize(sourceRun.getFontSizeAsDouble());
            }
        }
    }

    /**
     * Locks in formatting on all paragraphs in the header document.
     * This prevents user document styles from overriding the header's formatting.
     */
    private static void lockInFormatting(NiceXWPFDocument doc) {
        for (XWPFParagraph para : doc.getParagraphs()) {
            setDirectFormatting(para);
        }
        for (XWPFTable table : doc.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph para : cell.getParagraphs()) {
                        setDirectFormatting(para);
                    }
                }
            }
        }
    }

    private static void setDirectFormatting(XWPFParagraph para) {
        // Lock alignment
        ParagraphAlignment alignment = para.getAlignment();
        if (alignment == null) {
            alignment = ParagraphAlignment.LEFT;
        }
        para.setAlignment(alignment);

        // Lock indentation - set explicitly to prevent style inheritance
        // Use current value if set, otherwise default to 0
        int indentLeft = para.getIndentationLeft();
        int indentRight = para.getIndentationRight();
        int indentFirstLine = para.getIndentationFirstLine();
        int indentHanging = para.getIndentationHanging();

        para.setIndentationLeft(indentLeft != -1 ? indentLeft : 0);
        para.setIndentationRight(indentRight != -1 ? indentRight : 0);
        para.setIndentationFirstLine(indentFirstLine != -1 ? indentFirstLine : 0);
        para.setIndentationHanging(indentHanging != -1 ? indentHanging : 0);
    }

    /**
     * Validates that the uploaded document does not contain potentially dangerous content.
     * Checks for:
     * - VBA macros
     * - OLE objects (embedded executables)
     * - External relationships (links to external resources)
     *
     * @param doc The document to validate
     * @throws IOException if the document contains dangerous content
     */
    private static void validateDocumentSecurity(NiceXWPFDocument doc) throws IOException {
        try {
            // Check for VBA macros (macro-enabled documents)
            if (!doc.getPackage().getPartsByContentType("application/vnd.ms-office.vbaProject").isEmpty()) {
                log.error("Document contains VBA macros - rejecting");
                throw new IOException("Document contains macros which are not permitted");
            }

            // Check for OLE objects (embedded files/executables)
            if (!doc.getPackage().getPartsByContentType("application/vnd.openxmlformats-officedocument.oleObject").isEmpty()) {
                log.error("Document contains OLE objects - rejecting");
                throw new IOException("Document contains embedded objects which are not permitted");
            }

            // Check for external relationships that could reference malicious resources
            for (PackagePart part : doc.getPackage().getParts()) {
                try {
                    if (part.getRelationships() != null) {
                        part.getRelationships().forEach(rel -> {
                            if (rel.getTargetMode() != null
                                && "External".equalsIgnoreCase(rel.getTargetMode().toString())) {
                                log.warn("Document contains external relationship: {} -> {}",
                                    rel.getRelationshipType(), rel.getTargetURI());
                            }
                        });
                    }
                } catch (Exception e) {
                    log.debug("Could not check relationships for part: {}", part.getPartName());
                }
            }

            log.info("Document security validation passed");
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during security validation: {}", e.getMessage());
            throw new IOException("Failed to validate document security", e);
        }
    }
}

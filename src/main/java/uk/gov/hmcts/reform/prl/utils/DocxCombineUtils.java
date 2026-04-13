package uk.gov.hmcts.reform.prl.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFootnote;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for combining DOCX documents.
 * Handles merging of header documents with user-uploaded content while preserving
 * formatting elements like styles, numbering, images, and footnotes.
 */
@Slf4j
@UtilityClass
public class DocxCombineUtils {

    /**
     * Combines a header document with user content document.
     * Copies formatting definitions (styles, numbering, images, footnotes) from user content
     * to preserve formatting in the combined document.
     *
     * @param headerBytes Header document bytes
     * @param userContentBytes User's uploaded document bytes
     * @return Combined document bytes
     * @throws IOException if document processing fails
     */
    public static byte[] combineDocuments(byte[] headerBytes, byte[] userContentBytes) throws IOException {
        log.info("Combining header ({} bytes) with user content ({} bytes)",
            headerBytes.length, userContentBytes.length);

        try (XWPFDocument headerDoc = new XWPFDocument(new ByteArrayInputStream(headerBytes));
             XWPFDocument userDoc = new XWPFDocument(new ByteArrayInputStream(userContentBytes));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Copy formatting definitions from user doc to preserve formatting
            copyStyles(userDoc, headerDoc);
            copyNumberingDefinitions(userDoc, headerDoc);
            copyImages(userDoc, headerDoc);
            copyFootnotes(userDoc, headerDoc);

            // Append user content paragraphs to header document
            userDoc.getParagraphs().forEach(paragraph -> {
                var newPara = headerDoc.createParagraph();
                newPara.getCTP().set(paragraph.getCTP().copy());
            });

            // Also copy tables if any
            userDoc.getTables().forEach(table -> {
                var newTable = headerDoc.createTable();
                newTable.getCTTbl().set(table.getCTTbl().copy());
            });

            headerDoc.write(out);
            log.info("Combined document size: {} bytes", out.size());
            return out.toByteArray();
        }
    }

    /**
     * Copies custom styles from source document to target document.
     * This preserves custom paragraph and character formatting.
     */
    static void copyStyles(XWPFDocument sourceDoc, XWPFDocument targetDoc) {
        try {
            XWPFStyles sourceStyles = sourceDoc.getStyles();
            if (sourceStyles == null) {
                log.info("Source document has no custom styles");
                return;
            }

            XWPFStyles targetStyles = targetDoc.getStyles();
            if (targetStyles == null) {
                targetStyles = targetDoc.createStyles();
            }

            // Get list of used style IDs from source document paragraphs
            List<String> usedStyleIds = sourceDoc.getParagraphs().stream()
                .map(p -> p.getStyle())
                .filter(s -> s != null)
                .distinct()
                .collect(Collectors.toList());

            int copiedCount = 0;
            for (String styleId : usedStyleIds) {
                XWPFStyle sourceStyle = sourceStyles.getStyle(styleId);
                if (sourceStyle != null && targetStyles.getStyle(styleId) == null) {
                    targetStyles.addStyle(sourceStyle);
                    copiedCount++;
                }
            }
            log.info("Copied {} styles from source document", copiedCount);
        } catch (Exception e) {
            log.warn("Failed to copy styles: {}", e.getMessage());
        }
    }

    /**
     * Copies numbering definitions from source document to target document.
     * This preserves numbered/bulleted lists when combining documents.
     */
    static void copyNumberingDefinitions(XWPFDocument sourceDoc, XWPFDocument targetDoc) {
        try {
            XWPFNumbering sourceNumbering = sourceDoc.getNumbering();
            if (sourceNumbering == null) {
                log.info("Source document has no numbering definitions");
                return;
            }

            XWPFNumbering targetNumbering = targetDoc.getNumbering();
            if (targetNumbering == null) {
                targetNumbering = targetDoc.createNumbering();
            }

            int copiedAbstract = 0;

            // POI doesn't expose direct iteration, so we try to copy by known IDs
            // Most documents use IDs starting from 0 or 1, try a reasonable range
            for (int i = 0; i < 50; i++) {
                try {
                    var abstractNum = sourceNumbering.getAbstractNum(BigInteger.valueOf(i));
                    if (abstractNum != null) {
                        targetNumbering.addAbstractNum(abstractNum);
                        copiedAbstract++;
                    }
                } catch (Exception ignored) {
                    // Abstract num doesn't exist at this ID
                }
            }

            log.info("Copied {} abstract numbering definitions", copiedAbstract);
        } catch (Exception e) {
            log.warn("Failed to copy numbering definitions, numbered lists may not render correctly: {}",
                e.getMessage());
        }
    }

    /**
     * Copies embedded images from source document to target document.
     * This preserves pictures embedded in the document.
     */
    static void copyImages(XWPFDocument sourceDoc, XWPFDocument targetDoc) {
        try {
            List<XWPFPictureData> pictures = sourceDoc.getAllPictures();
            if (pictures.isEmpty()) {
                log.info("Source document has no embedded images");
                return;
            }

            for (XWPFPictureData picture : pictures) {
                targetDoc.addPictureData(picture.getData(), picture.getPictureType());
            }
            log.info("Copied {} images from source document", pictures.size());
        } catch (Exception e) {
            log.warn("Failed to copy images: {}", e.getMessage());
        }
    }

    /**
     * Copies footnotes from source document to target document.
     */
    static void copyFootnotes(XWPFDocument sourceDoc, XWPFDocument targetDoc) {
        try {
            List<XWPFFootnote> footnotes = sourceDoc.getFootnotes();
            if (footnotes == null || footnotes.isEmpty()) {
                log.info("Source document has no footnotes");
                return;
            }

            for (XWPFFootnote footnote : footnotes) {
                // Skip the separator and continuation separator footnotes (IDs 0 and 1)
                if (footnote.getId().intValue() > 1) {
                    XWPFFootnote newFootnote = targetDoc.createFootnote();
                    newFootnote.getCTFtnEdn().set(footnote.getCTFtnEdn().copy());
                }
            }
            log.info("Copied {} footnotes from source document", footnotes.size() - 2);
        } catch (Exception e) {
            log.warn("Failed to copy footnotes: {}", e.getMessage());
        }
    }
}

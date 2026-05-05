package uk.gov.hmcts.reform.prl.services.sealaudit;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class SealDetectionService {

    public enum SealStatus {
        PRESENT,
        MISSING,
        ERROR
    }

    public SealStatus detectSeal(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return SealStatus.ERROR;
        }

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            if (document.getNumberOfPages() == 0) {
                log.warn("PDF has no pages");
                return SealStatus.ERROR;
            }

            PDPage firstPage = document.getPage(0);
            boolean hasImage = hasSealImage(firstPage.getResources(), 0);

            if (hasImage) {
                log.debug("Image found on first page - seal likely present");
                return SealStatus.PRESENT;
            } else {
                log.debug("No images found on first page");
                return SealStatus.MISSING;
            }

        } catch (IOException e) {
            log.error("Error processing PDF for seal detection", e);
            return SealStatus.ERROR;
        }
    }

    private boolean hasSealImage(PDResources resources, int depth) throws IOException {
        if (resources == null || depth > 5) {
            return false;
        }

        for (COSName name : resources.getXObjectNames()) {
            PDXObject xobject = resources.getXObject(name);

            if (xobject instanceof PDImageXObject) {
                PDImageXObject image = (PDImageXObject) xobject;
                int width = image.getWidth();
                int height = image.getHeight();
                double ratio = (double) width / height;

                log.debug("Found image: {} ({}x{}) ratio={}", name.getName(), width, height, ratio);

                // Seal is square (25mm x 25mm), crest is not
                if (ratio >= 0.9 && ratio <= 1.1) {
                    log.debug("Square image found - seal detected");
                    return true;
                }
            }

            if (xobject instanceof PDFormXObject) {
                PDFormXObject form = (PDFormXObject) xobject;
                if (hasSealImage(form.getResources(), depth + 1)) {
                    return true;
                }
            }
        }

        return false;
    }
}

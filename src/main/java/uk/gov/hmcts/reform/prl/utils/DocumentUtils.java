package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;

@Data
@AllArgsConstructor
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

    public static QuarantineLegalDoc getQuarantineUploadDocument(String categoryId,
                                                                 Document document,
                                                                 ObjectMapper objectMapper) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(categoryId + "Document", document);
        objectMapper.registerModule(new ParameterNamesModule());
        return objectMapper.convertValue(hashMap, QuarantineLegalDoc.class);
    }

    private static Document getDocumentByCategoryId(String categoryConstant,
                                                    String categoryId,
                                                    Document document) {
        return categoryConstant.equalsIgnoreCase(categoryId) ? document : null;
    }

    public static QuarantineLegalDoc addQuarantineFields(QuarantineLegalDoc quarantineLegalDoc,
                                                         ManageDocuments manageDocument) {
        return quarantineLegalDoc.toBuilder()
            .documentParty(manageDocument.getDocumentParty().getDisplayedValue())
            .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
            .restrictCheckboxCorrespondence(manageDocument.getDocumentRestrictCheckbox())
            .notes(manageDocument.getDocumentDetails())
            .categoryId(DocumentPartyEnum.COURT.equals(manageDocument.getDocumentParty())
                            ? ManageDocumentsCategoryConstants.INTERNAL_CORRESPONDENCE
                            : manageDocument.getDocumentCategories().getValueCode())
            .categoryName(DocumentPartyEnum.COURT.equals(manageDocument.getDocumentParty())
                              ? PrlAppsConstants.INTERNAL_CORRESPONDENCE_LABEL
                              : manageDocument.getDocumentCategories().getValueLabel())
            .build();
    }

    public static QuarantineLegalDoc addConfFields(QuarantineLegalDoc quarantineLegalDoc,
                                                         ManageDocuments manageDocument) {
        return quarantineLegalDoc.toBuilder()
            .documentParty(manageDocument.getDocumentParty().getDisplayedValue())
            .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
            .notes(manageDocument.getDocumentDetails())
            .categoryId(manageDocument.getDocumentCategories().getValueCode())
            .categoryName(manageDocument.getDocumentCategories().getValueLabel())
            //move document into confidential category/folder
            .confidentialDocument(manageDocument.getDocument())
            .build();
    }

}

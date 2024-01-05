package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;

@Data
@AllArgsConstructor
@Slf4j
public class DocumentUtils {

    public static final String DOCUMENT_UUID_REGEX = "\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}";

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

    public static QuarantineLegalDoc addQuarantineFields(QuarantineLegalDoc quarantineLegalDoc,
                                                         ManageDocuments manageDocument,
                                                         UserDetails userDetails) {
        return quarantineLegalDoc.toBuilder()
            .documentParty(manageDocument.getDocumentParty().getDisplayedValue())
            .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
            .restrictCheckboxCorrespondence(manageDocument.getDocumentRestrictCheckbox()) //TO BE REMOVED
            .notes(manageDocument.getDocumentDetails()) //TO BE REMOVED
            .categoryId(DocumentPartyEnum.COURT.equals(manageDocument.getDocumentParty())
                            ? ManageDocumentsCategoryConstants.INTERNAL_CORRESPONDENCE
                            : manageDocument.getDocumentCategories().getValueCode())
            .categoryName(DocumentPartyEnum.COURT.equals(manageDocument.getDocumentParty())
                              ? PrlAppsConstants.INTERNAL_CORRESPONDENCE_LABEL
                              : manageDocument.getDocumentCategories().getValueLabel())
            //PRL-4320 - Manage documents redesign
            .isConfidential(manageDocument.getIsConfidential())
            .isRestricted(manageDocument.getIsRestricted())
            .restrictedDetails(manageDocument.getRestrictedDetails())
            .uploadedBy(userDetails.getFullName())
            .uploadedByIdamId(userDetails.getId())
            .build();
    }

    public static QuarantineLegalDoc addConfFields(QuarantineLegalDoc quarantineLegalDoc,
                                                   ManageDocuments manageDocument,
                                                   UserDetails userDetails) {
        return quarantineLegalDoc.toBuilder()
            .documentParty(manageDocument.getDocumentParty().getDisplayedValue())
            .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
            .notes(manageDocument.getDocumentDetails())
            .categoryId(manageDocument.getDocumentCategories().getValueCode())
            .categoryName(manageDocument.getDocumentCategories().getValueLabel())
            //move document into confidential category/folder
            .confidentialDocument(manageDocument.getDocument())
            .notes(manageDocument.getDocumentDetails())
            //PRL-4320 - Manage documents redesign
            .isConfidential(manageDocument.getIsConfidential())
            .isRestricted(manageDocument.getIsRestricted())
            .restrictedDetails(manageDocument.getRestrictedDetails())
            .uploadedBy(userDetails.getFullName())
            .uploadedByIdamId(userDetails.getId())
            .build();
    }

    public static QuarantineLegalDoc addQuarantineFieldsWithConfidentialFlag(String categoryId,
                                                                             Document document,
                                                                             ObjectMapper objectMapper,
                                                                             ManageDocuments manageDocument,
                                                                             UserDetails userDetails,
                                                                             boolean confidentialFlag) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(categoryId + "Document", document);
        objectMapper.registerModule(new ParameterNamesModule());
        QuarantineLegalDoc quarantineLegalDoc = objectMapper.convertValue(hashMap, QuarantineLegalDoc.class);

        return quarantineLegalDoc.toBuilder()
            .documentParty(manageDocument.getDocumentParty().getDisplayedValue())
            .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
            .notes(manageDocument.getDocumentDetails())
            .categoryId(manageDocument.getDocumentCategories().getValueCode())
            .categoryName(manageDocument.getDocumentCategories().getValueLabel())
            //move document into confidential category/folder
            .confidentialDocument(confidentialFlag ? manageDocument.getDocument() : null)
            .notes(manageDocument.getDocumentDetails())
            //PRL-4320 - Manage documents redesign
            .isConfidential(confidentialFlag ? manageDocument.getIsConfidential() : null)
            .isRestricted(confidentialFlag ? manageDocument.getIsRestricted() : null)
            .restrictedDetails(confidentialFlag ? manageDocument.getRestrictedDetails() : null)
            .uploadedBy(userDetails.getFullName())
            .uploadedByIdamId(userDetails.getId())
            .build();
    }

    public static String getDocumentId(String url) {
        Pattern pairRegex = Pattern.compile(DOCUMENT_UUID_REGEX);
        Matcher matcher = pairRegex.matcher(url);
        String documentId = "";
        if (matcher.find()) {
            documentId = matcher.group(0);
        }
        log.info("document id {}", documentId);
        return documentId;
    }

}

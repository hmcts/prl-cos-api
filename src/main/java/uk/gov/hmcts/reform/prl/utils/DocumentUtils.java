package uk.gov.hmcts.reform.prl.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BULK_SCAN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURTNAV;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_PROFESSIONAL;

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

    public static Document toCoverSheetDocument(GeneratedDocumentInfo generatedDocumentInfo) {
        if (null != generatedDocumentInfo) {
            return Document.builder().documentUrl(generatedDocumentInfo.getUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentFileName("coversheet.pdf")
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

    public static String populateAttributeNameFromCategoryId(String categoryId, String userRole) {
        if (BULK_SCAN.equals(userRole)) {
            return "url";
        }
        String wierdAttributeName = returnAttributeNameForWierdCategories(categoryId);
        if (wierdAttributeName == null) {
            String[] splittedCategory = StringUtils.splitByCharacterTypeCamelCase(categoryId);
            String finalCategory = "";

            for (int i = 0; i < splittedCategory.length; i++) {
                if (i == 0) {
                    finalCategory = finalCategory.concat(splittedCategory[i].toLowerCase());
                } else {
                    finalCategory = finalCategory.concat(splittedCategory[i]);
                }
            }
            return finalCategory + "Document";
        }
        return wierdAttributeName;
    }

    private static String returnAttributeNameForWierdCategories(String categoryId) {

        Map<String, String> wierdCategory = new HashMap<>();
        wierdCategory.put("16aRiskAssessment", "sixteenARiskAssessmentDocument");
        wierdCategory.put("requestForFASFormsToBeChanged", "requestForFasFormsToBeChangedDocument");
        wierdCategory.put("SPIPReferralRequests", "spipReferralRequestsDocument");
        wierdCategory.put("homeOfficeDWPResponses", "homeOfficeDwpResponsesDocument");
        wierdCategory.put("DNAReports_expertReport", "dnaReportsExpertReportDocument");
        wierdCategory.put("drugAndAlcoholTest(toxicology)", "drugAndAlcoholTestDocument");

        return wierdCategory.get(categoryId);

    }

    public static String getDocumentId(String url) {
        Pattern pairRegex = Pattern.compile(DOCUMENT_UUID_REGEX);
        Matcher matcher = pairRegex.matcher(url);
        String documentId = "";
        if (matcher.find()) {
            documentId = matcher.group(0);
        }
        return documentId;
    }

    public static String getLoggedInUserType(UserDetails userDetails) {
        String loggedInUserType;
        List<String> roles = userDetails.getRoles();
        if (roles.contains(Roles.JUDGE.getValue()) || roles.contains(Roles.LEGAL_ADVISER.getValue()) || roles.contains(
            Roles.COURT_ADMIN.getValue())) {
            loggedInUserType = COURT_STAFF;
        } else if (roles.contains(Roles.SOLICITOR.getValue())) {
            loggedInUserType = LEGAL_PROFESSIONAL;
        } else if (roles.contains(Roles.CITIZEN.getValue())) {
            loggedInUserType = CITIZEN;
        } else if (roles.contains(Roles.BULK_SCAN.getValue())) {
            loggedInUserType = BULK_SCAN;
        } else if (roles.contains(Roles.COURTNAV.getValue())) {
            loggedInUserType = COURTNAV;
        } else {
            loggedInUserType = CAFCASS;
        }

        return loggedInUserType;
    }
}

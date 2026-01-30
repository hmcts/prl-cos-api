package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.document.PoiTlDocxRenderer;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

/**
 * Service for handling custom order document operations.
 * Uses DocumentGenService for document access.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomOrderService {

    private static final String HEADER_TEMPLATE_PATH = "templates/CustomOrderHeader.docx";

    private final ObjectMapper objectMapper;
    private final AuthTokenGenerator authTokenGenerator;
    private final HearingDataService hearingDataService;
    private final PoiTlDocxRenderer poiTlDocxRenderer;
    private final UploadDocumentService uploadService;
    private final DocumentGenService documentGenService;
    private final SystemUserService systemUserService;
    private final AllTabServiceImpl allTabService;
    private final DgsApiClient dgsApiClient;
    private final DocumentSealingService documentSealingService;

    /**
     * Renders an uploaded custom order template with case data placeholders and stores it.
     *
     * @param authorisation User authorization token (used for upload)
     * @param caseId The case ID
     * @param caseData The case data
     * @param caseDataUpdated Map to store updated case data
     * @param populateJudgeNames Function to populate judge names on case data
     * @param populatePartyDetails Function to populate party details on case data
     * @return Updated case data map with rendered document
     * @throws IOException If document processing fails
     */
    public Map<String, Object> renderUploadedCustomOrderAndStoreOnManageOrders(
        String authorisation,
        Long caseId,
        CaseData caseData,
        Map<String, Object> caseDataUpdated,
        java.util.function.Function<CaseData, CaseData> populateJudgeNames,
        java.util.function.Function<CaseData, CaseData> populatePartyDetails
    ) throws IOException {
        log.info("into render with caseId: {}", caseId);

        // Read customOrderDoc from raw map (not in CaseData model due to constructor param limit)
        uk.gov.hmcts.reform.prl.models.documents.Document templateDoc = objectMapper.convertValue(
            caseDataUpdated.get("customOrderDoc"),
            uk.gov.hmcts.reform.prl.models.documents.Document.class
        );
        if (templateDoc == null || templateDoc.getDocumentBinaryUrl() == null) {
            throw new IllegalArgumentException("customOrderDoc is missing from case data");
        }

        String documentUrl = templateDoc.getDocumentBinaryUrl();
        log.info("Document URL: {}", documentUrl);

        // Persist document to case so CDAM associates it (required for document access)
        // Uses dedicated internal event to avoid callback recursion
        // This increments case version, so we'll need fresh event submission in aboutToSubmit
        associateDocumentWithCase(String.valueOf(caseId), templateDoc);
        log.info("Document associated with case via internal-custom-order-submit, now downloading...");

        // Download template bytes using DocumentGenService (works now that doc is case-associated)
        byte[] templateBytes;
        String systemAuthorisation = systemUserService.getSysUserToken();
        templateBytes = documentGenService.getDocumentBytes(documentUrl, systemAuthorisation, authTokenGenerator.generate());
        log.info("Successfully downloaded document, size: {} bytes", templateBytes != null ? templateBytes.length : 0);

        // Mark that we need fresh event submission due to CDAM association (version increment)
        caseDataUpdated.put("customOrderUsedCdamAssociation", "Yes");

        // 2) Build placeholders (minimal POC + names/solicitors if possible)
        Map<String, Object> data = buildCustomOrderPlaceholders(caseId, caseData);

        log.info("Custom order placeholders from buildCustomOrderPlaceholders: {}", data);

        caseData = populateJudgeNames.apply(caseData);
        caseData = populatePartyDetails.apply(caseData);

        // Get party names - this puts data under "tempPartyNamesForDocGen" key
        Map<String, Object> temp = new HashMap<>();
        hearingDataService.populatePartiesAndSolicitorsNames(caseData, temp);

        // Flatten the nested map into our data map
        @SuppressWarnings("unchecked")
        Map<String, Object> partyNames = (Map<String, Object>) temp.get("tempPartyNamesForDocGen");
        if (partyNames != null) {
            data.putAll(partyNames);
            log.info("Added party names to placeholders: {}", partyNames.keySet());
        }

        log.info("Final placeholder keys for custom order: {}", data.keySet());

        // 3) Render filled docx
        byte[] filledDocxBytes = poiTlDocxRenderer.render(templateBytes, data);

        // 4) Upload rendered docx
        String orderName = (caseData.getNameOfOrder() != null
            && !caseData.getNameOfOrder().isBlank())
            ? caseData.getNameOfOrder()
            : "custom_order";

        String outName = orderName + "_" + caseId + ".docx";
        log.info("before upload doc of rendered doc: {}", outName);
        uk.gov.hmcts.reform.ccd.document.am.model.Document uploaded =
            uploadService.uploadDocument(
                filledDocxBytes,
                outName,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                authorisation
            );

        uk.gov.hmcts.reform.prl.models.documents.Document renderedDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl(uploaded.links.binary.href)
                .documentUrl(uploaded.links.self.href)
                .documentFileName(uploaded.originalDocumentName)
                .documentCreatedOn(uploaded.createdOn)
                .build();

        // 5) Store the transformed doc (header preview)
        caseDataUpdated.put("customOrderTransformedDoc", renderedDoc);
        // Keep customOrderDoc - it's needed in the submitted callback to combine with header
        // caseDataUpdated.remove("customOrderDoc"); // DO NOT REMOVE - needed for submitted callback
        return caseDataUpdated;
    }

    /**
     * Safely builds placeholder map for custom order template.
     * Returns empty string for any null values or exceptions.
     */
    private Map<String, Object> buildCustomOrderPlaceholders(Long caseId, CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        log.info("Building custom order placeholders for caseId: {}", caseId);

        // Use caseId directly from controller (caseData.getId() may not be populated)
        data.put("caseNumber", String.valueOf(caseId));
        log.info("Placeholder 'caseNumber' = '{}'", caseId);
        safePut(data, "courtName", caseData::getCourtName);
        safePut(data, "orderName", caseData::getNameOfOrder);
        safePut(data, "respondent1Name", () -> {
            var respondent = caseData.getRespondents().getFirst().getValue();
            return (respondent.getFirstName() + " " + respondent.getLastName()).trim();
        });
        safePut(data, "hearingDate", () ->
            caseData.getManageOrders().getOrdersHearingDetails().getLast()
        );

        return data;
    }

    private void safePut(Map<String, Object> map, String key, Supplier<Object> valueSupplier) {
        try {
            Object value = valueSupplier.get();
            if (value != null && !"null".equals(String.valueOf(value)) && !"0".equals(String.valueOf(value))) {
                map.put(key, value);
                log.info("Placeholder '{}' = '{}'", key, value);
            } else {
                map.put(key, "");
                log.warn("Placeholder '{}' resolved to null/0/empty, using empty string. Raw value was: {}", key, value);
            }
        } catch (Exception e) {
            map.put(key, "");
            log.warn("Placeholder '{}' failed to resolve: {}", key, e.getMessage());
        }
    }

    /**
     * Associates the document with the case by submitting an internal-custom-order-submit event
     * that includes the document in the case data.
     * This triggers CDAM to recognize the document belongs to this case, allowing subsequent downloads.
     *
     * @param caseId The case ID
     * @param document The document to associate with the case
     */
    private void associateDocumentWithCase(String caseId, uk.gov.hmcts.reform.prl.models.documents.Document document) {
        log.info("Triggering internal-custom-order-submit for case {} to establish CDAM association for document", caseId);

        StartAllTabsUpdateDataContent startContent = allTabService.getStartUpdateForSpecificEvent(
            caseId,
            CaseEvent.INTERNAL_CUSTOM_ORDER_SUBMIT.getValue()
        );

        // Include the document in the case data submission so CDAM associates it with the case
        Map<String, Object> caseDataWithDoc = new HashMap<>(startContent.caseDataMap());
        caseDataWithDoc.put("customOrderDoc", document);
        log.info("Including customOrderDoc in internal-custom-order-submit submission");

        allTabService.submitAllTabsUpdate(
            startContent.authorisation(),
            caseId,
            startContent.startEventResponse(),
            startContent.eventRequestData(),
            caseDataWithDoc
        );

        log.info("internal-custom-order-submit completed, CDAM association should now be established");
    }

    /**
     * Submits a fresh ManageOrders event with the provided case data.
     * This is used to work around the optimistic locking issue that occurs when
     * we submit UPDATE_ALL_TABS mid-event to establish CDAM association.
     *
     * Because UPDATE_ALL_TABS increments the case version, CCD's final save of
     * the original ManageOrders event would fail. This method starts a new
     * ManageOrders event with the current case version and submits the data directly.
     *
     * @param caseId The case ID
     * @param caseDataUpdated The fully processed case data to save
     */
    public void submitFreshManageOrdersEvent(String caseId, Map<String, Object> caseDataUpdated) {
        log.info("Submitting fresh internal-custom-order-submit event for case {} to bypass version conflict", caseId);

        // Use dedicated internal event to avoid recursive callback invocation
        StartAllTabsUpdateDataContent startContent = allTabService.getStartUpdateForSpecificEvent(
            caseId,
            CaseEvent.INTERNAL_CUSTOM_ORDER_SUBMIT.getValue()
        );

        // Ensure customOrderDoc is NOT in the final saved state
        caseDataUpdated.remove("customOrderDoc");
        caseDataUpdated.remove("customOrderUsedCdamAssociation");

        allTabService.submitAllTabsUpdate(
            startContent.authorisation(),
            caseId,
            startContent.startEventResponse(),
            startContent.eventRequestData(),
            caseDataUpdated
        );

        log.info("Fresh internal-custom-order-submit event submitted successfully for case {}", caseId);
    }

    /**
     * Checks if this case had CDAM association established via UPDATE_ALL_TABS.
     * This means the case version has changed and we need to submit a fresh event.
     *
     * @param caseDataUpdated The case data map
     * @return true if CDAM association was used (version conflict requires fresh event)
     */
    public boolean requiresFreshEventSubmission(Map<String, Object> caseDataUpdated) {
        // Only need fresh event if we had to use CDAM association (which increments case version)
        // If user's auth token worked for download, no version conflict
        return "Yes".equals(caseDataUpdated.get("customOrderUsedCdamAssociation"));
    }

    // ========== NEW FLOW: Header from resources + User's uploaded content ==========

    /**
     * Renders and uploads the header preview document.
     * Returns a Document that can be shown to the user.
     *
     * @param authorisation Auth token for upload
     * @param caseId Case ID
     * @param caseData Case data for placeholders
     * @return Document reference for preview display
     */
    public uk.gov.hmcts.reform.prl.models.documents.Document renderAndUploadHeaderPreview(
        String authorisation,
        Long caseId,
        CaseData caseData
    ) throws IOException {
        log.info("Rendering and uploading header preview for case {}", caseId);

        // Render header
        byte[] headerBytes = renderHeaderPreview(caseId, caseData);

        // Upload for preview
        String previewName = "custom_order_header_preview_" + caseId + ".docx";
        uk.gov.hmcts.reform.ccd.document.am.model.Document uploaded = uploadService.uploadDocument(
            headerBytes,
            previewName,
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            authorisation
        );

        return uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentBinaryUrl(uploaded.links.binary.href)
            .documentUrl(uploaded.links.self.href)
            .documentFileName(uploaded.originalDocumentName)
            .documentCreatedOn(uploaded.createdOn)
            .build();
    }

    /**
     * Renders the header template from resources with case data placeholders.
     *
     * @param caseId The case ID
     * @param caseData The case data
     * @return Rendered header as byte array
     */
    public byte[] renderHeaderPreview(Long caseId, CaseData caseData) throws IOException {
        log.info("Rendering header preview for case {}", caseId);

        // Load template from resources
        byte[] templateBytes = loadTemplateFromResources();

        // Build placeholders from case data
        Map<String, Object> placeholders = buildHeaderPlaceholders(caseId, caseData);
        log.info("Header placeholders: {}", placeholders.keySet());

        // Render template with placeholders
        return poiTlDocxRenderer.render(templateBytes, placeholders);
    }

    /**
     * Loads the header template from resources.
     */
    private byte[] loadTemplateFromResources() throws IOException {
        log.info("Loading header template from resources: {}", HEADER_TEMPLATE_PATH);
        ClassPathResource resource = new ClassPathResource(HEADER_TEMPLATE_PATH);
        try (InputStream is = resource.getInputStream()) {
            return is.readAllBytes();
        }
    }

    /**
     * Builds placeholder map for the header template from case data.
     */
    private Map<String, Object> buildHeaderPlaceholders(Long caseId, CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        // Case details - format as xxxx-xxxx-xxxx-xxxx
        data.put("caseNumber", formatCaseNumber(String.valueOf(caseId)));
        safePut(data, "courtName", caseData::getCourtName);
        safePut(data, "orderName", caseData::getNameOfOrder);

        // Judge details
        safePut(data, "judgeName", () -> caseData.getJudgeOrMagistratesLastName());

        // Hearing/Order date details - prefer hearing date, fall back to order date
        safePut(data, "hearingDate", () -> {
            // First try hearing details
            if (caseData.getManageOrders() != null
                && caseData.getManageOrders().getOrdersHearingDetails() != null
                && !caseData.getManageOrders().getOrdersHearingDetails().isEmpty()) {
                var hearingDate = caseData.getManageOrders().getOrdersHearingDetails().get(0).getValue().getHearingDateConfirmOptionEnum();
                if (hearingDate != null) {
                    return hearingDate;
                }
            }
            // Fall back to dateOrderMade
            if (caseData.getDateOrderMade() != null) {
                return caseData.getDateOrderMade().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            return "";
        });
        safePut(data, "hearingType", () -> "hearing");

        // Determine case type
        String caseType = CaseUtils.getCaseTypeOfApplication(caseData);
        boolean isFL401 = FL401_CASE_TYPE.equalsIgnoreCase(caseType);

        // Applicant details - handle FL401 vs C100
        if (isFL401 && caseData.getApplicantsFL401() != null) {
            PartyDetails applicant = caseData.getApplicantsFL401();
            data.put("applicantName", getFullName(applicant.getFirstName(), applicant.getLastName()));
            String appRep = getRepresentativeName(applicant);
            data.put("applicantRepresentativeName", appRep);
            data.put("applicantRepresentativeClause", formatRepresentativeClause(appRep));
        } else if (caseData.getApplicants() != null && !caseData.getApplicants().isEmpty()) {
            var applicant = caseData.getApplicants().get(0).getValue();
            data.put("applicantName", getFullName(applicant.getFirstName(), applicant.getLastName()));
            String appRep = getRepresentativeName(applicant);
            data.put("applicantRepresentativeName", appRep);
            data.put("applicantRepresentativeClause", formatRepresentativeClause(appRep));
        } else {
            data.put("applicantName", "");
            data.put("applicantRepresentativeName", "");
            data.put("applicantRepresentativeClause", "");
        }

        // Respondent details - handle FL401 vs C100
        populateRespondents(data, caseData, isFL401);

        // Guardian details - populated from Cafcass officer data
        populateChildrensGuardian(data, caseData);

        // Children details - build table rows for selected children
        populateChildrenPlaceholders(data, caseData);

        return data;
    }

    /**
     * Populates children placeholders based on order selection (all children or selected children).
     * Provides both list format for poi-tl table looping and individual placeholders.
     */
    private void populateChildrenPlaceholders(Map<String, Object> data, CaseData caseData) {
        List<ChildDetailsRevised> selectedChildren = getSelectedChildren(caseData);

        // Build children table data for poi-tl table row looping
        List<Map<String, String>> childrenRows = new ArrayList<>();
        int index = 1;

        for (ChildDetailsRevised child : selectedChildren) {
            String fullName = getFullName(child.getFirstName(), child.getLastName());
            String gender = child.getGender() != null ? child.getGender().getDisplayedValue() : "";
            String dob = child.getDateOfBirth() != null
                ? child.getDateOfBirth().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "";

            Map<String, String> childRow = new HashMap<>();
            childRow.put("fullName", fullName);
            childRow.put("gender", gender);
            childRow.put("dob", dob);
            childrenRows.add(childRow);

            // Individual placeholders for each child (child1Name, child1Gender, child1Dob, etc.)
            data.put("child" + index + "Name", fullName);
            data.put("child" + index + "Gender", gender);
            data.put("child" + index + "Dob", dob);
            index++;
        }

        // Fill empty slots up to 10 children
        for (int i = index; i <= 10; i++) {
            data.put("child" + i + "Name", "");
            data.put("child" + i + "Gender", "");
            data.put("child" + i + "Dob", "");
        }

        data.put("children", childrenRows);
        log.info("Populated {} children rows: {}", childrenRows.size(), childrenRows);
    }

    /**
     * Populates respondent placeholders including solicitors and relationship to children.
     * Handles both C100 (list of respondents) and FL401 (single respondent).
     */
    private void populateRespondents(Map<String, Object> data, CaseData caseData, boolean isFL401) {
        List<Map<String, String>> respondentRows = new ArrayList<>();

        if (isFL401 && caseData.getRespondentsFL401() != null) {
            // FL401 has a single respondent
            PartyDetails respondent = caseData.getRespondentsFL401();
            log.debug(" FL401 Respondent: relationshipToChildren='{}', representativeFirstName='{}', representativeLastName='{}'",
                respondent.getRelationshipToChildren(),
                respondent.getRepresentativeFirstName(),
                respondent.getRepresentativeLastName());

            String name = getFullName(respondent.getFirstName(), respondent.getLastName());
            // Try party details first, then fall back to Relations data
            String relationship = nullToEmpty(respondent.getRelationshipToChildren());
            if (relationship.isEmpty()) {
                relationship = getRespondentRelationshipFromRelations(caseData, 1, null, name);
            }
            String representative = getRepresentativeName(respondent);

            Map<String, String> row = new HashMap<>();
            row.put("name", name);
            row.put("relationship", relationship);
            row.put("representative", representative);
            respondentRows.add(row);

            data.put("respondent1Name", name);
            data.put("respondent1RelationshipToChild", relationship);
            data.put("respondent1RepresentativeName", representative);
            data.put("respondent1RepresentativeClause", formatRepresentativeClause(representative));

        } else if (caseData.getRespondents() != null) {
            // C100 has a list of respondents
            int index = 1;
            for (Element<PartyDetails> respondentElement : caseData.getRespondents()) {
                PartyDetails respondent = respondentElement.getValue();
                Map<String, String> row = new HashMap<>();

                // Get respondent ID from Element for matching in Relations
                String respondentId = respondentElement.getId() != null
                    ? respondentElement.getId().toString() : null;

                // Debug logging for respondent fields
                log.debug(" Respondent {}: id='{}', relationshipToChildren='{}', representativeFirstName='{}', representativeLastName='{}'",
                    index,
                    respondentId,
                    respondent.getRelationshipToChildren(),
                    respondent.getRepresentativeFirstName(),
                    respondent.getRepresentativeLastName());

                String name = getFullName(respondent.getFirstName(), respondent.getLastName());
                // Try party details first, then fall back to Relations data
                String relationship = nullToEmpty(respondent.getRelationshipToChildren());
                if (relationship.isEmpty()) {
                    relationship = getRespondentRelationshipFromRelations(caseData, index, respondentId, name);
                }
                String representative = getRepresentativeName(respondent);

                row.put("name", name);
                row.put("relationship", relationship);
                row.put("representative", representative);
                respondentRows.add(row);

                // Also populate individual placeholders
                data.put("respondent" + index + "Name", name);
                data.put("respondent" + index + "RelationshipToChild", relationship);
                data.put("respondent" + index + "RepresentativeName", representative);
                data.put("respondent" + index + "RepresentativeClause", formatRepresentativeClause(representative));
                index++;
            }
        }

        // Fill empty slots for respondents 1-5
        for (int i = 1; i <= 5; i++) {
            data.putIfAbsent("respondent" + i + "Name", "");
            data.putIfAbsent("respondent" + i + "RelationshipToChild", "");
            data.putIfAbsent("respondent" + i + "RepresentativeName", "");
            data.putIfAbsent("respondent" + i + "RepresentativeClause", "");
        }

        data.put("respondents", respondentRows);
        log.info("Populated {} respondent rows", respondentRows.size());
    }

    /**
     * Gets the respondent's relationship to children from the Relations data.
     * Uses childAndRespondentRelations which stores per-respondent relationship to each child.
     * If a respondent has different relationships to different children, all unique relationships are returned.
     *
     * @param caseData The case data
     * @param respondentIndex 1-based index of the respondent (1 for first respondent, etc.)
     * @param respondentId The respondent's ID if available
     * @param respondentName The respondent's name for matching if ID doesn't match
     * @return The relationship display value (e.g., "Mother", "Father", or "Mother, Step-mother") or empty string
     */
    private String getRespondentRelationshipFromRelations(CaseData caseData, int respondentIndex,
                                                          String respondentId, String respondentName) {
        // Try Relations data first (new model - childAndRespondentRelations)
        if (caseData.getRelations() != null
            && caseData.getRelations().getChildAndRespondentRelations() != null
            && !caseData.getRelations().getChildAndRespondentRelations().isEmpty()) {

            log.debug(" Found {} childAndRespondentRelations entries. Looking for respondent index={}, id='{}', name='{}'",
                caseData.getRelations().getChildAndRespondentRelations().size(),
                respondentIndex, respondentId, respondentName);

            // Log all entries for debugging
            for (Element<uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation> relElement
                : caseData.getRelations().getChildAndRespondentRelations()) {
                var relation = relElement.getValue();
                log.debug(" Relation entry - respondentId='{}', respondentFullName='{}', relation='{}'",
                    relation.getRespondentId(), relation.getRespondentFullName(),
                    relation.getChildAndRespondentRelation());
            }

            // Collect all unique relationships for the target respondent
            java.util.Set<String> relationshipsForRespondent = new java.util.LinkedHashSet<>();

            // First try to match by respondent name (more reliable than ID which might not match)
            if (respondentName != null && !respondentName.isEmpty()) {
                for (Element<uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation> relElement
                    : caseData.getRelations().getChildAndRespondentRelations()) {
                    var relation = relElement.getValue();
                    if (respondentName.equalsIgnoreCase(relation.getRespondentFullName())
                        && relation.getChildAndRespondentRelation() != null) {
                        relationshipsForRespondent.add(relation.getChildAndRespondentRelation().getDisplayedValue());
                    }
                }
                if (!relationshipsForRespondent.isEmpty()) {
                    String result = String.join(", ", relationshipsForRespondent);
                    log.debug(" Found respondent '{}' relationships by name match: '{}'", respondentName, result);
                    return result;
                }
            }

            // Fall back to ID match
            if (respondentId != null && !respondentId.isEmpty()) {
                for (Element<uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation> relElement
                    : caseData.getRelations().getChildAndRespondentRelations()) {
                    var relation = relElement.getValue();
                    if (respondentId.equals(relation.getRespondentId())
                        && relation.getChildAndRespondentRelation() != null) {
                        relationshipsForRespondent.add(relation.getChildAndRespondentRelation().getDisplayedValue());
                    }
                }
                if (!relationshipsForRespondent.isEmpty()) {
                    String result = String.join(", ", relationshipsForRespondent);
                    log.debug(" Found respondent {} relationships by ID match: '{}'", respondentIndex, result);
                    return result;
                }
            }

            // Last resort: use index-based matching
            java.util.Map<String, java.util.Set<String>> respondentToRelationships = new java.util.LinkedHashMap<>();
            for (Element<uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation> relElement
                : caseData.getRelations().getChildAndRespondentRelations()) {
                var relation = relElement.getValue();
                String respName = relation.getRespondentFullName();
                if (respName != null && relation.getChildAndRespondentRelation() != null) {
                    respondentToRelationships
                        .computeIfAbsent(respName, k -> new java.util.LinkedHashSet<>())
                        .add(relation.getChildAndRespondentRelation().getDisplayedValue());
                }
            }

            int count = 0;
            for (var entry : respondentToRelationships.entrySet()) {
                count++;
                if (count == respondentIndex) {
                    String result = String.join(", ", entry.getValue());
                    log.debug(" Found respondent {} ('{}') relationships by index: '{}'",
                        respondentIndex, entry.getKey(), result);
                    return result;
                }
            }
        }

        // Fallback to old children model
        if (caseData.getChildren() != null && !caseData.getChildren().isEmpty()) {
            for (Element<uk.gov.hmcts.reform.prl.models.complextypes.Child> childElement : caseData.getChildren()) {
                uk.gov.hmcts.reform.prl.models.complextypes.Child child = childElement.getValue();
                if (child.getRespondentsRelationshipToChild() != null) {
                    String displayValue = child.getRespondentsRelationshipToChild().getDisplayedValue();
                    log.debug(" Found respondent relationship from children model: '{}'", displayValue);
                    return displayValue;
                }
            }
        }

        log.debug(" No respondent relationship found for respondent {}", respondentIndex);
        return "";
    }

    /**
     * Populates children's guardian placeholders from Cafcass officer data.
     * The children's guardian (Cafcass officer) represents the child's interests in proceedings.
     */
    private void populateChildrensGuardian(Map<String, Object> data, CaseData caseData) {
        String guardianName = "";

        // Try to get guardian from childAndCafcassOfficers list first
        if (caseData.getChildAndCafcassOfficers() != null && !caseData.getChildAndCafcassOfficers().isEmpty()) {
            var firstOfficer = caseData.getChildAndCafcassOfficers().get(0).getValue();
            if (firstOfficer != null && firstOfficer.getCafcassOfficerName() != null) {
                guardianName = firstOfficer.getCafcassOfficerName();
            }
        }

        // If not found, try to get from children's individual Cafcass officer fields
        if (guardianName.isEmpty() && caseData.getNewChildDetails() != null) {
            for (Element<ChildDetailsRevised> childElement : caseData.getNewChildDetails()) {
                ChildDetailsRevised child = childElement.getValue();
                if (child.getCafcassOfficerName() != null && !child.getCafcassOfficerName().isEmpty()) {
                    guardianName = child.getCafcassOfficerName();
                    break;
                }
            }
        }

        log.debug(" Children's guardian name: '{}'", guardianName);

        data.put("childrensGuardianName", guardianName);
        // Representative of the children's guardian (their solicitor) - not typically in case data
        data.put("childrensGuardianRepresentativeName", "");
        data.put("childrensGuardianRepresentativeClause", "");

        // Build full clause for children as respondents (only if guardian is assigned)
        // This covers the template section: "The 3rd respondent is the child (by their children's guardian...)"
        if (!guardianName.isEmpty()) {
            data.put("childrenAsRespondentsClause",
                "The child (by their children's guardian " + guardianName + ")");
        } else {
            data.put("childrenAsRespondentsClause", "");
        }
    }

    /**
     * Gets representative name, handling nulls properly.
     */
    private String getRepresentativeName(PartyDetails party) {
        if (party == null) {
            return "";
        }
        String repName = party.getRepresentativeFullName();
        // getRepresentativeFullName can return "null null" if both names are null
        if (repName == null || "null null".equals(repName.trim()) || repName.trim().isEmpty()) {
            return "";
        }
        return repName.trim();
    }

    /**
     * Gets full name from first and last name, handling nulls.
     */
    private String getFullName(String firstName, String lastName) {
        String first = nullToEmpty(firstName);
        String last = nullToEmpty(lastName);
        return (first + " " + last).trim();
    }

    /**
     * Converts null to empty string.
     */
    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    /**
     * Formats the representative clause - returns "represented by [name]" or empty if no representative.
     */
    private String formatRepresentativeClause(String representativeName) {
        if (representativeName == null || representativeName.trim().isEmpty()) {
            return "";
        }
        return "represented by " + representativeName;
    }

    /**
     * Formats a 16-digit case number with hyphens every 4 digits.
     * e.g., "1234567890123456" becomes "1234-5678-9012-3456"
     */
    private String formatCaseNumber(String caseNumber) {
        if (caseNumber == null || caseNumber.length() != 16) {
            return caseNumber;
        }
        return caseNumber.substring(0, 4) + "-"
            + caseNumber.substring(4, 8) + "-"
            + caseNumber.substring(8, 12) + "-"
            + caseNumber.substring(12, 16);
    }

    /**
     * Gets the list of children based on order selection.
     * C100: isTheOrderAboutAllChildren = Yes → all children, No → selected from childOption
     * FL401: isTheOrderAboutChildren = Yes → selected from childOption, No → none
     */
    private List<ChildDetailsRevised> getSelectedChildren(CaseData caseData) {
        List<ChildDetailsRevised> result = new ArrayList<>();

        log.info("getSelectedChildren: newChildDetails={}, manageOrders={}",
            caseData.getNewChildDetails() != null ? caseData.getNewChildDetails().size() : "null",
            caseData.getManageOrders() != null ? "present" : "null");

        if (caseData.getNewChildDetails() == null || caseData.getNewChildDetails().isEmpty()) {
            log.info("No children found in newChildDetails");
            return result;
        }

        if (caseData.getManageOrders() == null) {
            log.info("manageOrders is null, returning empty");
            return result;
        }

        // Get both field values
        YesOrNo isAboutAllChildren = caseData.getManageOrders().getIsTheOrderAboutAllChildren();
        YesOrNo isAboutChildren = caseData.getManageOrders().getIsTheOrderAboutChildren();

        log.info("isTheOrderAboutAllChildren={}, isTheOrderAboutChildren={}",
            isAboutAllChildren, isAboutChildren);

        // C100 logic: Yes = all children
        if (YesOrNo.Yes.equals(isAboutAllChildren)) {
            for (Element<ChildDetailsRevised> childElement : caseData.getNewChildDetails()) {
                result.add(childElement.getValue());
            }
            log.info("isTheOrderAboutAllChildren=Yes, returning all {} children", result.size());
            return result;
        }

        // FL401 logic: No = no children
        if (YesOrNo.No.equals(isAboutChildren)) {
            log.info("isTheOrderAboutChildren=No, returning no children");
            return result;
        }

        // Either C100 with No (select specific) or FL401 with Yes (select specific)
        // Both use childOption for selection
        DynamicMultiSelectList childOption = caseData.getManageOrders().getChildOption();

        log.info("childOption={}, childOption.getValue()={}",
            childOption != null ? "present" : "null",
            childOption != null && childOption.getValue() != null ? childOption.getValue().size() : "null");

        if (childOption != null && childOption.getValue() != null && !childOption.getValue().isEmpty()) {
            Set<String> selectedIds = childOption.getValue().stream()
                .map(DynamicMultiselectListElement::getCode)
                .collect(Collectors.toSet());

            log.info("Selected child IDs: {}", selectedIds);

            for (Element<ChildDetailsRevised> childElement : caseData.getNewChildDetails()) {
                log.info("Checking child ID: {}", childElement.getId());
                if (selectedIds.contains(childElement.getId().toString())) {
                    result.add(childElement.getValue());
                }
            }
        }

        log.info("Returning {} selected children", result.size());
        return result;
    }

    /**
     * Combines the header template with user's uploaded content.
     * Called from submitted callback after CDAM is established.
     *
     * @param headerBytes Rendered header document bytes
     * @param userContentBytes User's uploaded document bytes
     * @return Combined document bytes
     */
    public byte[] combineHeaderAndContent(byte[] headerBytes, byte[] userContentBytes) throws IOException {
        log.info("Combining header ({} bytes) with user content ({} bytes)",
            headerBytes.length, userContentBytes.length);

        try (XWPFDocument headerDoc = new XWPFDocument(new ByteArrayInputStream(headerBytes));
             XWPFDocument userDoc = new XWPFDocument(new ByteArrayInputStream(userContentBytes));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

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
     * Processes custom order in the submitted callback.
     * Downloads user's content, combines with header, saves to order collection.
     *
     * @param authorisation Auth token
     * @param caseId Case ID
     * @param caseData Case data
     * @param userDocUrl URL of user's uploaded document
     * @param headerDocUrl URL of the pre-rendered header document (previewOrderDoc)
     * @return The combined document
     */
    public uk.gov.hmcts.reform.prl.models.documents.Document processCustomOrderOnSubmitted(
        String authorisation,
        Long caseId,
        CaseData caseData,
        String userDocUrl,
        String headerDocUrl
    ) throws IOException {
        log.info("Processing custom order on submitted callback for case {}", caseId);

        // Get system auth for document downloads
        String systemAuth = systemUserService.getSysUserToken();
        String s2sToken = authTokenGenerator.generate();

        // 1. Download the pre-rendered header (rendered in mid-event before cleanup, has children info)
        byte[] headerBytes = documentGenService.getDocumentBytes(headerDocUrl, systemAuth, s2sToken);
        log.info("Downloaded pre-rendered header: {} bytes", headerBytes.length);

        // 2. Download user's uploaded content (CDAM works now after submit)
        byte[] userContentBytes = documentGenService.getDocumentBytes(userDocUrl, systemAuth, s2sToken);
        log.info("Downloaded user content: {} bytes", userContentBytes.length);

        // 3. Combine header + user content
        byte[] combinedBytes = combineHeaderAndContent(headerBytes, userContentBytes);
        log.info("Combined document: {} bytes", combinedBytes.length);

        // 4. Upload combined document (sealing happens via separate event after CDAM association)
        String orderName = (caseData.getNameOfOrder() != null && !caseData.getNameOfOrder().isBlank())
            ? caseData.getNameOfOrder()
            : "custom_order";
        String fileName = orderName + "_" + caseId + ".docx";

        uk.gov.hmcts.reform.ccd.document.am.model.Document uploaded = uploadService.uploadDocument(
            combinedBytes,
            fileName,
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            authorisation
        );

        return uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentBinaryUrl(uploaded.links.binary.href)
            .documentUrl(uploaded.links.self.href)
            .documentFileName(uploaded.originalDocumentName)
            .documentCreatedOn(uploaded.createdOn)
            .build();
    }

    /**
     * Gets the existing order document from orderCollection or draftOrderCollection.
     * This document contains the pre-rendered header with children info (set in aboutToSubmit).
     *
     * @param caseData The case data
     * @return The existing order document, or null if not found
     */
    public uk.gov.hmcts.reform.prl.models.documents.Document getExistingOrderDocument(CaseData caseData) {
        // Try orderCollection first (final orders)
        if (caseData.getOrderCollection() != null && !caseData.getOrderCollection().isEmpty()) {
            uk.gov.hmcts.reform.prl.models.documents.Document doc =
                caseData.getOrderCollection().get(0).getValue().getOrderDocument();
            if (doc != null) {
                log.info("Found existing order document in orderCollection: {}", doc.getDocumentFileName());
                return doc;
            }
        }

        // Try draftOrderCollection (draft orders)
        if (caseData.getDraftOrderCollection() != null && !caseData.getDraftOrderCollection().isEmpty()) {
            log.info("draftOrderCollection has {} items", caseData.getDraftOrderCollection().size());
            uk.gov.hmcts.reform.prl.models.documents.Document doc =
                caseData.getDraftOrderCollection().get(0).getValue().getOrderDocument();
            if (doc != null) {
                log.info("Found existing order document in draftOrderCollection[0]: {}", doc.getDocumentFileName());
                return doc;
            } else {
                log.warn("draftOrderCollection[0].orderDocument is null");
            }
        }

        log.warn("No existing order document found in orderCollection or draftOrderCollection");
        return null;
    }

    /**
     * Updates the most recent order in caseData.orderCollection with the combined document.
     * This allows the standard addSealToOrders flow to seal the document.
     *
     * @param caseData The case data object (modified in place)
     * @param combinedDoc The combined header + user content document
     */
    public void updateOrderDocumentInCaseData(CaseData caseData, uk.gov.hmcts.reform.prl.models.documents.Document combinedDoc,
                                               Map<String, Object> caseDataUpdated) {
        log.info("Updating order document in caseData - combinedDoc filename: {}", combinedDoc.getDocumentFileName());

        int orderCollectionSize = caseData.getOrderCollection() != null ? caseData.getOrderCollection().size() : 0;
        int draftCollectionSize = caseData.getDraftOrderCollection() != null ? caseData.getDraftOrderCollection().size() : 0;
        log.info("orderCollection size: {}, draftOrderCollection size: {}", orderCollectionSize, draftCollectionSize);

        if (caseData.getOrderCollection() != null && !caseData.getOrderCollection().isEmpty()) {
            // Update the first (most recent) order's document
            Element<uk.gov.hmcts.reform.prl.models.OrderDetails> firstOrderElement = caseData.getOrderCollection().get(0);
            log.info("Found order in orderCollection - current doc: {}, doesOrderDocumentNeedSeal: {}",
                firstOrderElement.getValue().getOrderDocument() != null
                    ? firstOrderElement.getValue().getOrderDocument().getDocumentFileName() : "null",
                firstOrderElement.getValue().getDoesOrderDocumentNeedSeal());

            uk.gov.hmcts.reform.prl.models.OrderDetails updatedOrder = firstOrderElement.getValue().toBuilder()
                .orderDocument(combinedDoc)
                // Set to No initially - async sealing will handle it after CDAM association
                .doesOrderDocumentNeedSeal(YesOrNo.No)
                .build();

            caseData.getOrderCollection().set(0, Element.<uk.gov.hmcts.reform.prl.models.OrderDetails>builder()
                .id(firstOrderElement.getId())
                .value(updatedOrder)
                .build());

            // Also update caseDataUpdated map so changes are persisted
            caseDataUpdated.put("orderCollection", caseData.getOrderCollection());
            log.info("Updated orderDocument in caseData.orderCollection to: {}, doesOrderDocumentNeedSeal=Yes",
                combinedDoc.getDocumentFileName());
            return;
        }

        // Try draftOrderCollection if orderCollection is empty
        if (caseData.getDraftOrderCollection() != null && !caseData.getDraftOrderCollection().isEmpty()) {
            Element<uk.gov.hmcts.reform.prl.models.DraftOrder> firstDraftElement = caseData.getDraftOrderCollection().get(0);
            log.info("Found order in draftOrderCollection - current doc: {}",
                firstDraftElement.getValue().getOrderDocument() != null
                    ? firstDraftElement.getValue().getOrderDocument().getDocumentFileName() : "null");

            uk.gov.hmcts.reform.prl.models.DraftOrder updatedDraft = firstDraftElement.getValue().toBuilder()
                .orderDocument(combinedDoc)
                .build();

            caseData.getDraftOrderCollection().set(0, Element.<uk.gov.hmcts.reform.prl.models.DraftOrder>builder()
                .id(firstDraftElement.getId())
                .value(updatedDraft)
                .build());

            // Also update caseDataUpdated map so changes are persisted
            caseDataUpdated.put("draftOrderCollection", caseData.getDraftOrderCollection());
            log.info("Updated draftOrderCollection in caseDataUpdated - new doc: {}",
                caseData.getDraftOrderCollection().get(0).getValue().getOrderDocument().getDocumentFileName());
            return;
        }

        log.warn("Could not find order to update in caseData - both collections are empty!");
    }

    /**
     * Seals the custom order document synchronously and returns the updated case data.
     * Called after submitAllTabsUpdate to ensure CDAM association is complete.
     *
     * @param caseId The case ID
     * @return Map containing the sealed orderCollection, or null if sealing fails
     */
    public Map<String, Object> sealCustomOrderSynchronously(String caseId) {
        log.info("Sealing custom order synchronously for case {}", caseId);

        try {
            // Start the internal seal event to get fresh case data
            StartAllTabsUpdateDataContent startContent = allTabService.getStartUpdateForSpecificEvent(
                caseId,
                CaseEvent.INTERNAL_CUSTOM_ORDER_SEAL.getValue()
            );

            String authorisation = startContent.authorisation();
            CaseData caseData = startContent.caseData();
            Map<String, Object> caseDataMap = new HashMap<>(startContent.caseDataMap());

            if (caseData.getOrderCollection() == null || caseData.getOrderCollection().isEmpty()) {
                log.warn("No orders to seal in case {}", caseId);
                return null;
            }

            // Find the most recent order (first in collection)
            Element<uk.gov.hmcts.reform.prl.models.OrderDetails> orderElement =
                caseData.getOrderCollection().get(0);
            uk.gov.hmcts.reform.prl.models.OrderDetails orderDetails = orderElement.getValue();

            if (orderDetails.getOrderDocument() == null) {
                log.warn("Order document is null for case {}, skipping seal", caseId);
                return null;
            }

            log.info("Sealing order document: {}", orderDetails.getOrderDocument().getDocumentFileName());

            // Seal the document
            uk.gov.hmcts.reform.prl.models.documents.Document sealedDoc =
                documentSealingService.sealDocument(
                    orderDetails.getOrderDocument(),
                    caseData,
                    authorisation
                );

            log.info("Document sealed successfully: {}", sealedDoc.getDocumentFileName());

            // Update the order with the sealed document
            uk.gov.hmcts.reform.prl.models.OrderDetails updatedOrder = orderDetails.toBuilder()
                .orderDocument(sealedDoc)
                .doesOrderDocumentNeedSeal(YesOrNo.No)
                .build();

            List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>> updatedOrderCollection =
                new ArrayList<>(caseData.getOrderCollection());
            updatedOrderCollection.set(0, Element.<uk.gov.hmcts.reform.prl.models.OrderDetails>builder()
                .id(orderElement.getId())
                .value(updatedOrder)
                .build());

            caseDataMap.put("orderCollection", updatedOrderCollection);

            // Submit the update
            allTabService.submitAllTabsUpdate(
                authorisation,
                caseId,
                startContent.startEventResponse(),
                startContent.eventRequestData(),
                caseDataMap
            );

            log.info("Custom order sealed and saved for case {}", caseId);

            // Return the updated orderCollection so it can be included in the response
            Map<String, Object> result = new HashMap<>();
            result.put("orderCollection", updatedOrderCollection);
            return result;

        } catch (Exception e) {
            log.error("Failed to seal custom order synchronously for case {}: {}", caseId, e.getMessage(), e);
            throw new RuntimeException("Synchronous sealing failed", e);
        }
    }

    /**
     * Schedules an async sealing event for the custom order.
     * This is called after the main submitted callback completes, allowing CDAM to associate
     * the document with the case before we attempt to seal it.
     *
     * @param caseId The case ID
     */
    public void scheduleSealingEvent(String caseId) {
        log.info("Scheduling custom order sealing event for case {}", caseId);

        // Run sealing in a separate thread to allow the main callback to complete first
        // This ensures CDAM has time to associate the document with the case
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // Small delay to ensure CDAM association is complete
                Thread.sleep(2000);
                sealCustomOrderViaInternalEvent(caseId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Sealing thread interrupted for case {}", caseId);
            } catch (Exception e) {
                log.error("Failed to seal custom order for case {}: {}", caseId, e.getMessage(), e);
            }
        });
    }

    /**
     * Seals the custom order document via an internal event.
     * This method fetches fresh case data, seals the order document, and saves it.
     *
     * @param caseId The case ID
     */
    private void sealCustomOrderViaInternalEvent(String caseId) {
        log.info("Starting internal sealing event for case {}", caseId);

        try {
            // Start the internal seal event to get fresh case data
            StartAllTabsUpdateDataContent startContent = allTabService.getStartUpdateForSpecificEvent(
                caseId,
                CaseEvent.INTERNAL_CUSTOM_ORDER_SEAL.getValue()
            );

            String authorisation = startContent.authorisation();
            CaseData caseData = startContent.caseData();
            Map<String, Object> caseDataMap = new HashMap<>(startContent.caseDataMap());

            log.info("Fetched case data for sealing - orderCollection size: {}",
                caseData.getOrderCollection() != null ? caseData.getOrderCollection().size() : 0);

            if (caseData.getOrderCollection() == null || caseData.getOrderCollection().isEmpty()) {
                log.warn("No orders to seal in case {}", caseId);
                return;
            }

            // Find the most recent order (first in collection) that needs sealing
            Element<uk.gov.hmcts.reform.prl.models.OrderDetails> orderElement =
                caseData.getOrderCollection().get(0);
            uk.gov.hmcts.reform.prl.models.OrderDetails orderDetails = orderElement.getValue();

            if (orderDetails.getOrderDocument() == null) {
                log.warn("Order document is null for case {}, skipping seal", caseId);
                return;
            }

            log.info("Sealing order document: {}", orderDetails.getOrderDocument().getDocumentFileName());

            // Seal the document
            uk.gov.hmcts.reform.prl.models.documents.Document sealedDoc =
                documentSealingService.sealDocument(
                    orderDetails.getOrderDocument(),
                    caseData,
                    authorisation
                );

            log.info("Document sealed successfully: {}", sealedDoc.getDocumentFileName());

            // Update the order with the sealed document
            uk.gov.hmcts.reform.prl.models.OrderDetails updatedOrder = orderDetails.toBuilder()
                .orderDocument(sealedDoc)
                .doesOrderDocumentNeedSeal(YesOrNo.No)
                .build();

            List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>> updatedOrderCollection =
                new ArrayList<>(caseData.getOrderCollection());
            updatedOrderCollection.set(0, Element.<uk.gov.hmcts.reform.prl.models.OrderDetails>builder()
                .id(orderElement.getId())
                .value(updatedOrder)
                .build());

            caseDataMap.put("orderCollection", updatedOrderCollection);

            // Submit the update
            allTabService.submitAllTabsUpdate(
                authorisation,
                caseId,
                startContent.startEventResponse(),
                startContent.eventRequestData(),
                caseDataMap
            );

            log.info("Custom order sealed and saved for case {}", caseId);

        } catch (Exception e) {
            log.error("Failed to seal custom order via internal event for case {}: {}",
                caseId, e.getMessage(), e);
        }
    }
}

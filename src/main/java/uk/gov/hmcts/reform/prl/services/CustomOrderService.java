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
import uk.gov.hmcts.reform.prl.enums.manageorders.CustomOrderNameOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CUSTOM_ORDER_DOC;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MM_YYYY_DATE_FORMAT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_COLLECTION;

/**
 * Service for handling custom order document operations.
 * Uses DocumentGenService for document access.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomOrderService {

    private static final String HEADER_TEMPLATE_PATH = "templates/CustomOrderHeader.docx";
    private static final String DOCX_EXTENSION = ".docx";
    private static final String DOCX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String CUSTOM_ORDER_USED_CDAM_ASSOCIATION = "customOrderUsedCdamAssociation";

    // Placeholder key constants for respondent fields
    private static final String RESPONDENT_PREFIX = "respondent";
    private static final String NAME_SUFFIX = "Name";
    private static final String RELATIONSHIP_TO_CHILD_SUFFIX = "RelationshipToChild";
    private static final String REPRESENTATIVE_NAME_SUFFIX = "RepresentativeName";
    private static final String REPRESENTATIVE_CLAUSE_SUFFIX = "RepresentativeClause";

    // Placeholder key constants for child fields
    private static final String CHILD_PREFIX = "child";
    private static final String GENDER_SUFFIX = "Gender";
    private static final String DOB_SUFFIX = "Dob";

    /**
     * Gets the effective order name for a custom order.
     * If a standard order name is selected from the dropdown, returns that display value.
     * If "Other" is selected or no dropdown selection, falls back to the nameOfOrder text field.
     *
     * <p><b>NOTE: This method is currently NOT IN USE externally.</b></p>
     * <p>Used internally by {@link #renderUploadedCustomOrderAndStoreOnManageOrders} which is part of the
     * CDAM-based approach that was not used - see that method for details.</p>
     *
     * @param caseData The case data
     * @param caseDataMap The raw case data map (needed because customOrderNameOption is not in CaseData due to param limit)
     * @return The effective order name, or "custom_order" as fallback
     */
    public String getEffectiveOrderName(CaseData caseData, Map<String, Object> caseDataMap) {
        // Read customOrderNameOption from raw map (not in CaseData model due to constructor param limit)
        CustomOrderNameOptionsEnum selectedOption = null;
        Object rawOption = caseDataMap != null ? caseDataMap.get("customOrderNameOption") : null;
        if (rawOption != null) {
            try {
                if (rawOption instanceof String stringOption) {
                    selectedOption = CustomOrderNameOptionsEnum.getValue(stringOption);
                } else if (rawOption instanceof CustomOrderNameOptionsEnum enumOption) {
                    selectedOption = enumOption;
                }
            } catch (IllegalArgumentException e) {
                log.warn("Could not parse customOrderNameOption: {}", rawOption);
            }
        }

        // If a dropdown option is selected and it's not "Other", use the dropdown display value
        if (selectedOption != null && !selectedOption.isOther()) {
            log.info("Using order name from dropdown: {}", selectedOption.getDisplayedValue());
            return selectedOption.getDisplayedValue();
        }

        // Otherwise, use the text field (for "Other" selection or backwards compatibility)
        String textFieldName = caseData.getNameOfOrder();
        if (textFieldName != null && !textFieldName.isBlank()) {
            log.info("Using order name from text field: {}", textFieldName);
            return textFieldName;
        }

        log.info("No order name found, using default: custom_order");
        return "custom_order";
    }

    /**
     * Overload for backwards compatibility when map is not available.
     * Falls back to nameOfOrder text field only.
     */
    public String getEffectiveOrderName(CaseData caseData) {
        return getEffectiveOrderName(caseData, null);
    }

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
     * <p><b>NOTE: This method is currently NOT IN USE.</b></p>
     * <p>This approach requires CDAM (Case Document Access Management) to be enabled for the environment,
     * as it needs to associate the uploaded document with the case before downloading it for processing.
     * Since CDAM was not available when custom orders were implemented, an alternative approach was used:
     * <ul>
     *   <li>User uploads their content document (customOrderDoc)</li>
     *   <li>System generates a separate header from a fixed template (renderAndUploadHeaderPreview)</li>
     *   <li>In submitted callback, header and user content are combined (processCustomOrderOnSubmitted)</li>
     * </ul>
     * This method could be revisited if/when CDAM is fully enabled and the placeholder-based template
     * approach is preferred over the header+content combination approach.</p>
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
            caseDataUpdated.get(CUSTOM_ORDER_DOC),
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
        caseDataUpdated.put(CUSTOM_ORDER_USED_CDAM_ASSOCIATION, "Yes");

        // 2) Build placeholders (minimal POC + names/solicitors if possible)
        Map<String, Object> data = buildCustomOrderPlaceholders(caseId, caseData, caseDataUpdated);

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
        String orderName = getEffectiveOrderName(caseData, caseDataUpdated);

        String outName = orderName + "_" + caseId + DOCX_EXTENSION;
        log.info("before upload doc of rendered doc: {}", outName);
        uk.gov.hmcts.reform.ccd.document.am.model.Document uploaded =
            uploadService.uploadDocument(
                filledDocxBytes,
                outName,
                DOCX_CONTENT_TYPE,
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
        return caseDataUpdated;
    }

    /**
     * Safely builds placeholder map for custom order template.
     * Returns empty string for any null values or exceptions.
     */
    private Map<String, Object> buildCustomOrderPlaceholders(Long caseId, CaseData caseData, Map<String, Object> caseDataMap) {
        Map<String, Object> data = new HashMap<>();

        log.info("Building custom order placeholders for caseId: {}", caseId);

        // Use caseId directly from controller (caseData.getId() may not be populated)
        data.put("caseNumber", String.valueOf(caseId));
        log.info("Placeholder 'caseNumber' = '{}'", caseId);
        safePut(data, "courtName", caseData::getCourtName);
        safePut(data, "orderName", () -> getEffectiveOrderName(caseData, caseDataMap));
        safePut(data, RESPONDENT_PREFIX + "1" + NAME_SUFFIX, () -> {
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
        caseDataWithDoc.put(CUSTOM_ORDER_DOC, document);
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
     * <p><b>NOTE: This method is currently NOT IN USE.</b></p>
     * <p>Part of the CDAM-based approach - see {@link #renderUploadedCustomOrderAndStoreOnManageOrders}
     * for details on why this approach was not used.</p>
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
        caseDataUpdated.remove(CUSTOM_ORDER_DOC);
        caseDataUpdated.remove(CUSTOM_ORDER_USED_CDAM_ASSOCIATION);

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
     * <p><b>NOTE: This method is currently NOT IN USE.</b></p>
     * <p>Part of the CDAM-based approach - see {@link #renderUploadedCustomOrderAndStoreOnManageOrders}
     * for details on why this approach was not used.</p>
     *
     * @param caseDataUpdated The case data map
     * @return true if CDAM association was used (version conflict requires fresh event)
     */
    public boolean requiresFreshEventSubmission(Map<String, Object> caseDataUpdated) {
        // Only need fresh event if we had to use CDAM association (which increments case version)
        // If user's auth token worked for download, no version conflict
        return "Yes".equals(caseDataUpdated.get(CUSTOM_ORDER_USED_CDAM_ASSOCIATION));
    }

    // ========== NEW FLOW: Header from resources + User's uploaded content ==========

    /**
     * Renders and uploads the header preview document.
     * Returns a Document that can be shown to the user.
     * Note: This only renders the header - the full document (header + user content)
     * is combined in the submitted callback via processCustomOrderOnSubmitted.
     *
     * @param authorisation Auth token for upload
     * @param caseId Case ID
     * @param caseData Case data for placeholders
     * @param caseDataMap Raw case data map for reading fields not in CaseData model
     * @return Document reference for preview display
     */
    public uk.gov.hmcts.reform.prl.models.documents.Document renderAndUploadHeaderPreview(
        String authorisation,
        Long caseId,
        CaseData caseData,
        Map<String, Object> caseDataMap
    ) throws IOException {
        log.info("Rendering and uploading header preview for case {}", caseId);

        // Render header
        byte[] headerBytes = renderHeaderPreview(caseId, caseData, caseDataMap);

        // Upload for preview
        String previewName = "custom_order_header_preview_" + caseId + DOCX_EXTENSION;
        uk.gov.hmcts.reform.ccd.document.am.model.Document uploaded = uploadService.uploadDocument(
            headerBytes,
            previewName,
            DOCX_CONTENT_TYPE,
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
     * @param caseDataMap Raw case data map for reading fields not in CaseData model
     * @return Rendered header as byte array
     */
    public byte[] renderHeaderPreview(Long caseId, CaseData caseData, Map<String, Object> caseDataMap) throws IOException {
        log.info("Rendering header preview for case {}", caseId);

        // Load template from resources
        byte[] templateBytes = loadTemplateFromResources();

        // Build placeholders from case data
        Map<String, Object> placeholders = buildHeaderPlaceholders(caseId, caseData, caseDataMap);
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
    private Map<String, Object> buildHeaderPlaceholders(Long caseId, CaseData caseData, Map<String, Object> caseDataMap) {
        Map<String, Object> data = new HashMap<>();

        // Populate different sections using helper methods
        populateCaseDetails(data, caseId, caseData, caseDataMap);
        populateJudgeDetails(data, caseData, caseDataMap);
        populateOrderAndHearingDates(data, caseData, caseDataMap);
        populateApplicantDetails(data, caseData);

        // Determine case type for respondent handling
        String caseType = CaseUtils.getCaseTypeOfApplication(caseData);
        boolean isFL401 = FL401_CASE_TYPE.equalsIgnoreCase(caseType);

        // Respondent, guardian, and children details
        populateRespondents(data, caseData, isFL401);
        populateChildrensGuardian(data, caseData);
        populateChildrenPlaceholders(data, caseData);

        return data;
    }

    /**
     * Populates case details placeholders (case number, court name, order name).
     */
    private void populateCaseDetails(Map<String, Object> data, Long caseId, CaseData caseData, Map<String, Object> caseDataMap) {
        data.put("caseNumber", formatCaseNumber(String.valueOf(caseId)));
        safePut(data, "courtName", caseData::getCourtName);
        safePut(data, "orderName", () -> getEffectiveOrderName(caseData, caseDataMap));
    }

    /**
     * Populates judge details placeholder.
     * Reads from map first (set during aboutToStart callback), then falls back to caseData.
     */
    private void populateJudgeDetails(Map<String, Object> data, CaseData caseData, Map<String, Object> caseDataMap) {
        String judgeName = null;
        if (caseDataMap != null && caseDataMap.get("judgeOrMagistratesLastName") != null) {
            judgeName = caseDataMap.get("judgeOrMagistratesLastName").toString();
        } else if (caseData.getJudgeOrMagistratesLastName() != null) {
            judgeName = caseData.getJudgeOrMagistratesLastName();
        }
        if (judgeName != null && !judgeName.isEmpty()) {
            data.put("judgeName", judgeName);
        }
    }

    /**
     * Populates order date and hearing date placeholders.
     */
    private void populateOrderAndHearingDates(Map<String, Object> data, CaseData caseData, Map<String, Object> caseDataMap) {
        String orderDate = extractOrderDate(caseData, caseDataMap);
        if (orderDate != null && !orderDate.isEmpty()) {
            data.put("orderDate", orderDate);
        }

        safePut(data, "hearingDate", () -> extractHearingDate(caseData, caseDataMap));
        safePut(data, "hearingType", () -> "hearing");
    }

    /**
     * Extracts order date from map or caseData.
     */
    private String extractOrderDate(CaseData caseData, Map<String, Object> caseDataMap) {
        if (caseDataMap != null && caseDataMap.get("dateOrderMade") != null) {
            Object dateValue = caseDataMap.get("dateOrderMade");
            if (dateValue instanceof java.time.LocalDate localDate) {
                return localDate.format(java.time.format.DateTimeFormatter.ofPattern(DD_MM_YYYY_DATE_FORMAT));
            }
            return dateValue.toString();
        }
        if (caseData.getDateOrderMade() != null) {
            return caseData.getDateOrderMade().format(java.time.format.DateTimeFormatter.ofPattern(DD_MM_YYYY_DATE_FORMAT));
        }
        return null;
    }

    /**
     * Extracts hearing date, preferring hearing details over order date.
     */
    private Object extractHearingDate(CaseData caseData, Map<String, Object> caseDataMap) {
        if (caseData.getManageOrders() != null
            && caseData.getManageOrders().getOrdersHearingDetails() != null
            && !caseData.getManageOrders().getOrdersHearingDetails().isEmpty()) {
            var hearingDate = caseData.getManageOrders().getOrdersHearingDetails().get(0).getValue().getHearingDateConfirmOptionEnum();
            if (hearingDate != null) {
                return hearingDate;
            }
        }
        String orderDate = extractOrderDate(caseData, caseDataMap);
        return orderDate != null ? orderDate : "";
    }

    /**
     * Populates applicant details placeholders. Handles FL401 vs C100 case types.
     */
    private void populateApplicantDetails(Map<String, Object> data, CaseData caseData) {
        String caseType = CaseUtils.getCaseTypeOfApplication(caseData);
        boolean isFL401 = FL401_CASE_TYPE.equalsIgnoreCase(caseType);

        if (isFL401 && caseData.getApplicantsFL401() != null) {
            populateApplicantFromParty(data, caseData.getApplicantsFL401());
        } else if (caseData.getApplicants() != null && !caseData.getApplicants().isEmpty()) {
            populateApplicantFromParty(data, caseData.getApplicants().get(0).getValue());
        } else {
            data.put("applicantName", "");
            data.put("applicantRepresentativeName", "");
            data.put("applicantRepresentativeClause", "");
        }
    }

    /**
     * Populates applicant placeholders from a PartyDetails object.
     */
    private void populateApplicantFromParty(Map<String, Object> data, PartyDetails applicant) {
        data.put("applicantName", getFullName(applicant.getFirstName(), applicant.getLastName()));
        String appRep = getRepresentativeName(applicant);
        data.put("applicantRepresentativeName", appRep);
        data.put("applicantRepresentativeClause", formatRepresentativeClause(appRep));
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
                ? child.getDateOfBirth().format(java.time.format.DateTimeFormatter.ofPattern(DD_MM_YYYY_DATE_FORMAT))
                : "";

            Map<String, String> childRow = new HashMap<>();
            childRow.put("fullName", fullName);
            childRow.put("gender", gender);
            childRow.put("dob", dob);
            childrenRows.add(childRow);

            // Individual placeholders for each child (child1Name, child1Gender, child1Dob, etc.)
            data.put(CHILD_PREFIX + index + NAME_SUFFIX, fullName);
            data.put(CHILD_PREFIX + index + GENDER_SUFFIX, gender);
            data.put(CHILD_PREFIX + index + DOB_SUFFIX, dob);
            index++;
        }

        // Fill empty slots up to 10 children
        for (int i = index; i <= 10; i++) {
            data.put(CHILD_PREFIX + i + NAME_SUFFIX, "");
            data.put(CHILD_PREFIX + i + GENDER_SUFFIX, "");
            data.put(CHILD_PREFIX + i + DOB_SUFFIX, "");
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

            data.put(RESPONDENT_PREFIX + "1" + NAME_SUFFIX, name);
            data.put(RESPONDENT_PREFIX + "1" + RELATIONSHIP_TO_CHILD_SUFFIX, relationship);
            data.put(RESPONDENT_PREFIX + "1" + REPRESENTATIVE_NAME_SUFFIX, representative);
            data.put(RESPONDENT_PREFIX + "1" + REPRESENTATIVE_CLAUSE_SUFFIX, formatRepresentativeClause(representative));

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
                data.put(RESPONDENT_PREFIX + index + NAME_SUFFIX, name);
                data.put(RESPONDENT_PREFIX + index + RELATIONSHIP_TO_CHILD_SUFFIX, relationship);
                data.put(RESPONDENT_PREFIX + index + REPRESENTATIVE_NAME_SUFFIX, representative);
                data.put(RESPONDENT_PREFIX + index + REPRESENTATIVE_CLAUSE_SUFFIX, formatRepresentativeClause(representative));
                index++;
            }
        }

        // Fill empty slots for respondents 1-5
        for (int i = 1; i <= 5; i++) {
            data.putIfAbsent(RESPONDENT_PREFIX + i + NAME_SUFFIX, "");
            data.putIfAbsent(RESPONDENT_PREFIX + i + RELATIONSHIP_TO_CHILD_SUFFIX, "");
            data.putIfAbsent(RESPONDENT_PREFIX + i + REPRESENTATIVE_NAME_SUFFIX, "");
            data.putIfAbsent(RESPONDENT_PREFIX + i + REPRESENTATIVE_CLAUSE_SUFFIX, "");
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
        List<Element<ChildrenAndRespondentRelation>> relations = getChildAndRespondentRelations(caseData);
        if (!relations.isEmpty()) {
            logRelationsDebugInfo(relations, respondentIndex, respondentId, respondentName);

            // Try matching strategies in order of reliability
            String result = findRelationshipsByName(relations, respondentName);
            if (!result.isEmpty()) {
                return result;
            }

            result = findRelationshipsById(relations, respondentId, respondentIndex);
            if (!result.isEmpty()) {
                return result;
            }

            result = findRelationshipsByIndex(relations, respondentIndex);
            if (!result.isEmpty()) {
                return result;
            }
        }

        // Fallback to old children model
        return findRelationshipFromChildrenModel(caseData, respondentIndex);
    }

    /**
     * Gets the child and respondent relations list, or empty list if not available.
     */
    private List<Element<ChildrenAndRespondentRelation>> getChildAndRespondentRelations(CaseData caseData) {
        if (caseData.getRelations() != null
            && caseData.getRelations().getChildAndRespondentRelations() != null
            && !caseData.getRelations().getChildAndRespondentRelations().isEmpty()) {
            return caseData.getRelations().getChildAndRespondentRelations();
        }
        return List.of();
    }

    /**
     * Logs debug information about relations being searched.
     */
    private void logRelationsDebugInfo(List<Element<ChildrenAndRespondentRelation>> relations,
                                       int respondentIndex, String respondentId, String respondentName) {
        log.debug(" Found {} childAndRespondentRelations entries. Looking for respondent index={}, id='{}', name='{}'",
            relations.size(), respondentIndex, respondentId, respondentName);

        for (Element<ChildrenAndRespondentRelation> relElement : relations) {
            var relation = relElement.getValue();
            log.debug(" Relation entry - respondentId='{}', respondentFullName='{}', relation='{}'",
                relation.getRespondentId(), relation.getRespondentFullName(),
                relation.getChildAndRespondentRelation());
        }
    }

    /**
     * Finds relationships by matching respondent name.
     */
    private String findRelationshipsByName(List<Element<ChildrenAndRespondentRelation>> relations, String respondentName) {
        if (respondentName == null || respondentName.isEmpty()) {
            return "";
        }
        String result = collectRelationships(relations,
            rel -> respondentName.equalsIgnoreCase(rel.getRespondentFullName()));
        if (!result.isEmpty()) {
            log.debug(" Found respondent '{}' relationships by name match: '{}'", respondentName, result);
        }
        return result;
    }

    /**
     * Finds relationships by matching respondent ID.
     */
    private String findRelationshipsById(List<Element<ChildrenAndRespondentRelation>> relations,
                                         String respondentId, int respondentIndex) {
        if (respondentId == null || respondentId.isEmpty()) {
            return "";
        }
        String result = collectRelationships(relations,
            rel -> respondentId.equals(rel.getRespondentId()));
        if (!result.isEmpty()) {
            log.debug(" Found respondent {} relationships by ID match: '{}'", respondentIndex, result);
        }
        return result;
    }

    /**
     * Collects unique relationship display values matching the given predicate.
     */
    private String collectRelationships(List<Element<ChildrenAndRespondentRelation>> relations,
                                        java.util.function.Predicate<ChildrenAndRespondentRelation> matcher) {
        String result = relations.stream()
            .map(Element::getValue)
            .filter(rel -> matcher.test(rel) && rel.getChildAndRespondentRelation() != null)
            .map(rel -> rel.getChildAndRespondentRelation().getDisplayedValue())
            .distinct()
            .collect(Collectors.joining(", "));
        return result;
    }

    /**
     * Finds relationships by index position (last resort).
     */
    private String findRelationshipsByIndex(List<Element<ChildrenAndRespondentRelation>> relations, int respondentIndex) {
        Map<String, String> respondentToRelationships = buildRespondentRelationshipMap(relations);

        int count = 0;
        for (var entry : respondentToRelationships.entrySet()) {
            count++;
            if (count == respondentIndex) {
                log.debug(" Found respondent {} ('{}') relationships by index: '{}'",
                    respondentIndex, entry.getKey(), entry.getValue());
                return entry.getValue();
            }
        }
        return "";
    }

    /**
     * Builds a map of respondent name to their comma-separated relationships.
     */
    private Map<String, String> buildRespondentRelationshipMap(List<Element<ChildrenAndRespondentRelation>> relations) {
        return relations.stream()
            .map(Element::getValue)
            .filter(rel -> rel.getRespondentFullName() != null && rel.getChildAndRespondentRelation() != null)
            .collect(Collectors.groupingBy(
                ChildrenAndRespondentRelation::getRespondentFullName,
                java.util.LinkedHashMap::new,
                Collectors.mapping(
                    rel -> rel.getChildAndRespondentRelation().getDisplayedValue(),
                    Collectors.collectingAndThen(
                        Collectors.toCollection(java.util.LinkedHashSet::new),
                        set -> String.join(", ", set)
                    )
                )
            ));
    }

    /**
     * Fallback: finds relationship from old children model.
     */
    private String findRelationshipFromChildrenModel(CaseData caseData, int respondentIndex) {
        if (caseData.getChildren() == null || caseData.getChildren().isEmpty()) {
            log.debug(" No respondent relationship found for respondent {}", respondentIndex);
            return "";
        }

        return caseData.getChildren().stream()
            .map(Element::getValue)
            .filter(child -> child.getRespondentsRelationshipToChild() != null)
            .findFirst()
            .map(child -> {
                String displayValue = child.getRespondentsRelationshipToChild().getDisplayedValue();
                log.debug(" Found respondent relationship from children model: '{}'", displayValue);
                return displayValue;
            })
            .orElseGet(() -> {
                log.debug(" No respondent relationship found for respondent {}", respondentIndex);
                return "";
            });
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
        log.info("getSelectedChildren: newChildDetails={}, manageOrders={}",
            caseData.getNewChildDetails() != null ? caseData.getNewChildDetails().size() : "null",
            caseData.getManageOrders() != null ? "present" : "null");

        if (!hasChildrenData(caseData)) {
            return new ArrayList<>();
        }

        YesOrNo isAboutAllChildren = caseData.getManageOrders().getIsTheOrderAboutAllChildren();
        YesOrNo isAboutChildren = caseData.getManageOrders().getIsTheOrderAboutChildren();

        log.info("isTheOrderAboutAllChildren={}, isTheOrderAboutChildren={}",
            isAboutAllChildren, isAboutChildren);

        // C100 logic: Yes = all children
        if (YesOrNo.Yes.equals(isAboutAllChildren)) {
            return getAllChildren(caseData);
        }

        // FL401 logic: No = no children
        if (YesOrNo.No.equals(isAboutChildren)) {
            log.info("isTheOrderAboutChildren=No, returning no children");
            return new ArrayList<>();
        }

        // Either C100 with No (select specific) or FL401 with Yes (select specific)
        return getChildrenBySelection(caseData);
    }

    /**
     * Checks if case data has children and manage orders data available.
     */
    private boolean hasChildrenData(CaseData caseData) {
        if (caseData.getNewChildDetails() == null || caseData.getNewChildDetails().isEmpty()) {
            log.info("No children found in newChildDetails");
            return false;
        }
        if (caseData.getManageOrders() == null) {
            log.info("manageOrders is null, returning empty");
            return false;
        }
        return true;
    }

    /**
     * Returns all children from case data.
     */
    private List<ChildDetailsRevised> getAllChildren(CaseData caseData) {
        List<ChildDetailsRevised> result = caseData.getNewChildDetails().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        log.info("isTheOrderAboutAllChildren=Yes, returning all {} children", result.size());
        return result;
    }

    /**
     * Returns children selected via childOption multi-select.
     */
    private List<ChildDetailsRevised> getChildrenBySelection(CaseData caseData) {
        List<ChildDetailsRevised> result = new ArrayList<>();
        DynamicMultiSelectList childOption = caseData.getManageOrders().getChildOption();

        log.info("childOption={}, childOption.getValue()={}",
            childOption != null ? "present" : "null",
            childOption != null && childOption.getValue() != null ? childOption.getValue().size() : "null");

        if (childOption == null || childOption.getValue() == null || childOption.getValue().isEmpty()) {
            log.info("Returning {} selected children", result.size());
            return result;
        }

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
     * @param caseDataMap Raw case data map for reading fields not in CaseData model
     * @return The combined document
     */
    public uk.gov.hmcts.reform.prl.models.documents.Document processCustomOrderOnSubmitted(
        String authorisation,
        Long caseId,
        CaseData caseData,
        String userDocUrl,
        String headerDocUrl,
        Map<String, Object> caseDataMap
    ) throws IOException {
        log.info("Processing custom order on submitted callback for case {}", caseId);

        // Get system auth for document downloads
        String systemAuth = systemUserService.getSysUserToken();
        String s2sToken = authTokenGenerator.generate();

        // 1. Download the pre-rendered header (rendered in mid-event before cleanup, has children info)
        log.info("Downloading header from URL: {}", headerDocUrl);
        byte[] headerBytes = documentGenService.getDocumentBytes(headerDocUrl, systemAuth, s2sToken);
        log.info("Downloaded pre-rendered header: {} bytes, first bytes: [{}, {}, {}, {}]",
            headerBytes.length,
            headerBytes.length > 0 ? headerBytes[0] : "N/A",
            headerBytes.length > 1 ? headerBytes[1] : "N/A",
            headerBytes.length > 2 ? headerBytes[2] : "N/A",
            headerBytes.length > 3 ? headerBytes[3] : "N/A");

        // 2. Download user's uploaded content (CDAM works now after submit)
        log.info("Downloading user content from URL: {}", userDocUrl);
        byte[] userContentBytes = documentGenService.getDocumentBytes(userDocUrl, systemAuth, s2sToken);
        log.info("Downloaded user content: {} bytes, first bytes: [{}, {}, {}, {}]",
            userContentBytes.length,
            userContentBytes.length > 0 ? userContentBytes[0] : "N/A",
            userContentBytes.length > 1 ? userContentBytes[1] : "N/A",
            userContentBytes.length > 2 ? userContentBytes[2] : "N/A",
            userContentBytes.length > 3 ? userContentBytes[3] : "N/A");

        // DOCX files start with PK (0x50, 0x4B) - ZIP signature
        // PDF files start with %PDF (0x25, 0x50, 0x44, 0x46)
        boolean headerLooksLikeDocx = headerBytes.length > 1 && headerBytes[0] == 0x50 && headerBytes[1] == 0x4B;
        boolean userLooksLikeDocx = userContentBytes.length > 1 && userContentBytes[0] == 0x50 && userContentBytes[1] == 0x4B;
        log.info("Header looks like DOCX (PK signature): {}, User content looks like DOCX: {}",
            headerLooksLikeDocx, userLooksLikeDocx);

        if (!headerLooksLikeDocx) {
            log.error("Header document is NOT a valid DOCX file! First 4 bytes suggest different format.");
        }
        if (!userLooksLikeDocx) {
            log.error("User content document is NOT a valid DOCX file! First 4 bytes suggest different format.");
        }

        // 3. Combine header + user content
        byte[] combinedBytes = combineHeaderAndContent(headerBytes, userContentBytes);
        log.info("Combined document: {} bytes", combinedBytes.length);

        // 4. Upload combined document (sealing happens via separate event after CDAM association)
        String orderName = getEffectiveOrderName(caseData, caseDataMap);
        String fileName = orderName + "_" + caseId + DOCX_EXTENSION;

        uk.gov.hmcts.reform.ccd.document.am.model.Document uploaded = uploadService.uploadDocument(
            combinedBytes,
            fileName,
            DOCX_CONTENT_TYPE,
            authorisation
        );

        return uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentBinaryUrl(uploaded.links.binary.href)
            .documentUrl(uploaded.links.self.href)
            .documentFileName(uploaded.originalDocumentName)
            .documentCreatedOn(uploaded.createdOn)
            .build();
    }

    private static final String HEADER_PREVIEW_FILENAME_PATTERN = "custom_order_header_preview";

    /**
     * Result of finding a custom order header preview, including which collection it was found in.
     */
    public record CustomOrderLocation(
        uk.gov.hmcts.reform.prl.models.documents.Document document,
        boolean isInDraftCollection,
        int index
    ) {}

    /**
     * Finds the custom order header preview document and its location.
     * Searches orderCollection first (for final orders), then draftOrderCollection.
     * Returns location info so updateOrderDocumentInCaseData can update the correct collection.
     *
     * @param caseData The case data
     * @return Location info including document, collection type, and index; or null if not found
     */
    public CustomOrderLocation findCustomOrderHeaderPreview(CaseData caseData) {
        // Check orderCollection first (final orders take precedence)
        CustomOrderLocation location = searchOrderCollection(caseData);
        if (location != null) {
            return location;
        }

        // Then check draftOrderCollection
        location = searchDraftOrderCollection(caseData);
        if (location != null) {
            return location;
        }

        log.warn("No custom order header preview document found in either collection");
        return null;
    }

    /**
     * Searches orderCollection for custom order header preview.
     */
    private CustomOrderLocation searchOrderCollection(CaseData caseData) {
        if (caseData.getOrderCollection() == null || caseData.getOrderCollection().isEmpty()) {
            return null;
        }

        log.info("Searching orderCollection ({} items) for custom order header preview",
            caseData.getOrderCollection().size());

        for (int i = 0; i < caseData.getOrderCollection().size(); i++) {
            uk.gov.hmcts.reform.prl.models.documents.Document doc =
                caseData.getOrderCollection().get(i).getValue().getOrderDocument();
            if (isCustomOrderHeaderPreview(doc, false)) {
                log.info("Found custom order header preview in orderCollection[{}]: {}", i, doc.getDocumentFileName());
                return new CustomOrderLocation(doc, false, i);
            }
        }
        return null;
    }

    /**
     * Searches draftOrderCollection for custom order header preview.
     */
    private CustomOrderLocation searchDraftOrderCollection(CaseData caseData) {
        if (caseData.getDraftOrderCollection() == null || caseData.getDraftOrderCollection().isEmpty()) {
            return null;
        }

        log.info("Searching draftOrderCollection ({} items) for custom order header preview",
            caseData.getDraftOrderCollection().size());
        logDraftOrdersForDebugging(caseData);

        for (int i = 0; i < caseData.getDraftOrderCollection().size(); i++) {
            uk.gov.hmcts.reform.prl.models.documents.Document doc =
                caseData.getDraftOrderCollection().get(i).getValue().getOrderDocument();
            if (isCustomOrderHeaderPreview(doc, true)) {
                log.info("Found custom order header preview in draftOrderCollection[{}]: {}", i, doc.getDocumentFileName());
                return new CustomOrderLocation(doc, true, i);
            }
        }
        return null;
    }

    /**
     * Checks if a document is a custom order header preview.
     * For draft orders, also checks that it ends with .docx (not .pdf).
     */
    private boolean isCustomOrderHeaderPreview(uk.gov.hmcts.reform.prl.models.documents.Document doc, boolean requireDocxExtension) {
        if (doc == null || doc.getDocumentFileName() == null) {
            return false;
        }
        boolean matchesPattern = doc.getDocumentFileName().contains(HEADER_PREVIEW_FILENAME_PATTERN);
        if (requireDocxExtension) {
            return matchesPattern && doc.getDocumentFileName().toLowerCase().endsWith(DOCX_EXTENSION);
        }
        return matchesPattern;
    }

    /**
     * Logs all draft orders for debugging purposes.
     */
    private void logDraftOrdersForDebugging(CaseData caseData) {
        for (int i = 0; i < caseData.getDraftOrderCollection().size(); i++) {
            uk.gov.hmcts.reform.prl.models.DraftOrder draft = caseData.getDraftOrderCollection().get(i).getValue();
            uk.gov.hmcts.reform.prl.models.documents.Document doc = draft.getOrderDocument();
            log.info("draftOrderCollection[{}]: fileName={}, dateCreated={}",
                i,
                doc != null ? doc.getDocumentFileName() : "null",
                draft.getOtherDetails() != null ? draft.getOtherDetails().getDateCreated() : "null");
        }
    }

    /**
     * Gets the custom order header preview document.
     * @deprecated Use findCustomOrderHeaderPreview() instead to get location info
     */
    @Deprecated
    public uk.gov.hmcts.reform.prl.models.documents.Document getExistingOrderDocument(CaseData caseData) {
        CustomOrderLocation location = findCustomOrderHeaderPreview(caseData);
        return location != null ? location.document() : null;
    }

    /**
     * Updates the custom order at the specified location with the combined document.
     *
     * @param caseData The case data object (modified in place)
     * @param combinedDoc The combined header + user content document
     * @param caseDataUpdated Map to store updated case data for persistence
     * @param location The location of the order to update (from findCustomOrderHeaderPreview)
     */
    public void updateOrderDocumentInCaseData(CaseData caseData, uk.gov.hmcts.reform.prl.models.documents.Document combinedDoc,
                                               Map<String, Object> caseDataUpdated, CustomOrderLocation location) {
        log.info("Updating order document at location: isInDraftCollection={}, index={}, combinedDoc={}",
            location.isInDraftCollection(), location.index(), combinedDoc.getDocumentFileName());

        if (location.isInDraftCollection()) {
            // Update draft order collection - create mutable copy to avoid immutable list issues
            List<Element<uk.gov.hmcts.reform.prl.models.DraftOrder>> updatedDraftCollection =
                new ArrayList<>(caseData.getDraftOrderCollection());

            Element<uk.gov.hmcts.reform.prl.models.DraftOrder> draftElement =
                updatedDraftCollection.get(location.index());

            uk.gov.hmcts.reform.prl.models.DraftOrder updatedDraft = draftElement.getValue().toBuilder()
                .orderDocument(combinedDoc)
                .build();

            updatedDraftCollection.set(location.index(),
                Element.<uk.gov.hmcts.reform.prl.models.DraftOrder>builder()
                    .id(draftElement.getId())
                    .value(updatedDraft)
                    .build());

            caseDataUpdated.put(DRAFT_ORDER_COLLECTION, updatedDraftCollection);
            log.info("Updated draftOrderCollection[{}] with combined doc: {}",
                location.index(), combinedDoc.getDocumentFileName());
        } else {
            // Update final order collection - create mutable copy to avoid immutable list issues
            List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>> updatedOrderCollection =
                new ArrayList<>(caseData.getOrderCollection());

            Element<uk.gov.hmcts.reform.prl.models.OrderDetails> orderElement =
                updatedOrderCollection.get(location.index());

            uk.gov.hmcts.reform.prl.models.OrderDetails updatedOrder = orderElement.getValue().toBuilder()
                .orderDocument(combinedDoc)
                .doesOrderDocumentNeedSeal(YesOrNo.No)
                .build();

            updatedOrderCollection.set(location.index(),
                Element.<uk.gov.hmcts.reform.prl.models.OrderDetails>builder()
                    .id(orderElement.getId())
                    .value(updatedOrder)
                    .build());

            caseDataUpdated.put(ORDER_COLLECTION, updatedOrderCollection);
            log.info("Updated orderCollection[{}] with combined doc: {}",
                location.index(), combinedDoc.getDocumentFileName());
        }
    }

    /**
     * Populates the custom order header preview if a custom order document has been uploaded.
     * This method handles extracting court name from various sources and rendering the header preview.
     *
     * @param authorisation Auth token for upload
     * @param caseId Case ID
     * @param caseData Case data (will be modified with court name if found)
     * @param caseDataUpdated Map to store updated case data (previewOrderDoc will be added)
     * @return Optional containing error message if failed, empty if successful or no document uploaded
     */
    public java.util.Optional<String> populateCustomOrderHeaderPreview(
            String authorisation,
            Long caseId,
            CaseData caseData,
            Map<String, Object> caseDataUpdated) {

        Object customOrderDocObj = caseDataUpdated.get(CUSTOM_ORDER_DOC);
        if (customOrderDocObj == null) {
            log.info("Custom order document not yet uploaded, skipping header preview render");
            return java.util.Optional.empty();
        }

        try {
            String courtNameValue = extractCourtName(caseData, caseDataUpdated);
            if (courtNameValue != null && !courtNameValue.isEmpty()) {
                caseData.setCourtName(courtNameValue);
            } else {
                log.warn("Could not find court name for custom order");
            }

            uk.gov.hmcts.reform.prl.models.documents.Document previewDoc =
                renderAndUploadHeaderPreview(authorisation, caseId, caseData, caseDataUpdated);
            caseDataUpdated.put("previewOrderDoc", previewDoc);

            log.info("Custom order header preview uploaded: {}", previewDoc.getDocumentFileName());
            return java.util.Optional.empty();

        } catch (Exception e) {
            log.error("Failed to render custom order header preview", e);
            return java.util.Optional.of("Failed to render custom order header: " + e.getMessage());
        }
    }

    /**
     * Extracts court name from various possible sources in the case data.
     */
    private String extractCourtName(CaseData caseData, Map<String, Object> caseDataUpdated) {
        // Try caseData first
        if (caseData.getCourtName() != null && !caseData.getCourtName().isEmpty()) {
            return caseData.getCourtName();
        }

        // Try raw map field
        Object rawCourtName = caseDataUpdated.get("courtName");
        if (rawCourtName != null && !"null".equals(rawCourtName.toString())
            && !rawCourtName.toString().isEmpty()) {
            return rawCourtName.toString();
        }

        // Try allocatedJudgeDetails
        Object allocatedJudgeObj = caseDataUpdated.get("allocatedJudgeDetails");
        if (allocatedJudgeObj != null) {
            String courtName = extractCourtNameFromAllocatedJudge(allocatedJudgeObj);
            if (courtName != null) {
                return courtName;
            }
        }

        // Try courtList dynamic list
        Object courtListObj = caseDataUpdated.get("courtList");
        if (courtListObj != null) {
            return extractCourtNameFromDynamicList(courtListObj);
        }

        return null;
    }

    /**
     * Extracts court name from allocatedJudgeDetails map structure.
     */
    private String extractCourtNameFromAllocatedJudge(Object allocatedJudgeObj) {
        if (allocatedJudgeObj == null) {
            log.info("extractCourtNameFromAllocatedJudge: object is null");
            return null;
        }
        try {
            log.info("extractCourtNameFromAllocatedJudge: type={}, value={}",
                allocatedJudgeObj.getClass().getSimpleName(), allocatedJudgeObj);
            if (allocatedJudgeObj instanceof Map<?, ?> judgeMap) {
                log.info("extractCourtNameFromAllocatedJudge: map keys={}", judgeMap.keySet());
                Object courtName = judgeMap.get("courtName");
                log.info("extractCourtNameFromAllocatedJudge: courtName='{}' (type={})",
                    courtName, courtName != null ? courtName.getClass().getSimpleName() : "null");
                if (courtName != null
                    && !"null".equals(courtName.toString())
                    && !courtName.toString().trim().isEmpty()) {
                    return courtName.toString().trim();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract court name from allocatedJudgeDetails: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extracts court name label from a DynamicList object.
     */
    private String extractCourtNameFromDynamicList(Object courtListObj) {
        if (courtListObj == null) {
            return null;
        }
        try {
            if (courtListObj instanceof Map<?, ?> courtListMap) {
                Object valueObj = courtListMap.get("value");
                if (valueObj instanceof Map<?, ?> valueMap) {
                    Object label = valueMap.get("label");
                    if (label != null) {
                        return label.toString();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract court name from courtList: {}", e.getMessage());
        }
        return null;
    }
}

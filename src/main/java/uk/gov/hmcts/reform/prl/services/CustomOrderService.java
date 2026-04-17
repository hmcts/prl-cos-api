package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.HearingChannelsEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CustomOrderNameOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.document.PoiTlDocxRenderer;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocxCombineUtils;
import uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CUSTOM_C21_ORDER_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CUSTOM_C43_ORDER_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CUSTOM_ORDER_NAME_OPTION;
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
    private static final String DATE_FORMAT_PATTERN = "dd/MM/yyyy";
    private static final String JUDGE_OR_MAGISTRATES_LAST_NAME = "judgeOrMagistratesLastName";
    private static final String DATE_ORDER_MADE = "dateOrderMade";
    private static final String RESPONDENT_PREFIX = "respondent";
    private static final String DEFAULT_ORDER_NAME = "custom_order";
    private static final String DOCX_EXTENSION = ".docx";
    private static final String COURT_NAME = "courtName";
    private static final String HEADER_PREVIEW_FILENAME_PATTERN = "custom_order_header_preview";
    private static final String ORDER_COLLECTION = "orderCollection";
    private static final String DRAFT_ORDER_COLLECTION = "draftOrderCollection";
    private static final String CUSTOM_ORDER_DOC = "customOrderDoc";
    private static final String CUSTOM_ORDER_USED_CDAM_ASSOCIATION = "customOrderUsedCdamAssociation";
    private static final String DOCX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String APPLICANT_NAME = "applicantName";
    private static final java.time.format.DateTimeFormatter DATE_FORMATTER =
        java.time.format.DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);

    private final ObjectMapper objectMapper;
    private final AuthTokenGenerator authTokenGenerator;
    private final HearingDataService hearingDataService;
    private final PoiTlDocxRenderer poiTlDocxRenderer;
    private final UploadDocumentService uploadService;
    private final DocumentGenService documentGenService;
    private final SystemUserService systemUserService;
    private final AllTabServiceImpl allTabService;
    private final DocumentSealingService documentSealingService;

    /**
     * Gets the effective order name for a custom order.
     * If a standard order name is selected from the dropdown, returns that display value.
     * If no dropdown selection, falls back to the nameOfOrder text field.
     *
     * @param caseData The case data
     * @param caseDataMap The raw case data map (needed because customOrderNameOption is not in CaseData due to param limit)
     * @return The effective order name, or "custom_order" as fallback
     */
    public String getEffectiveOrderName(CaseData caseData, Map<String, Object> caseDataMap) {
        CustomOrderNameOptionsEnum selectedOption = parseCustomOrderNameOption(caseDataMap);

        if (selectedOption != null) {
            return getOrderNameFromSelection(selectedOption, caseDataMap);
        }

        String textFieldName = caseData.getNameOfOrder();
        if (textFieldName != null && !textFieldName.isBlank()) {
            log.info("Using order name from text field: {}", textFieldName);
            return textFieldName;
        }

        log.info("No order name found, using default: {}", DEFAULT_ORDER_NAME);
        return DEFAULT_ORDER_NAME;
    }

    private CustomOrderNameOptionsEnum parseCustomOrderNameOption(Map<String, Object> caseDataMap) {
        Object rawOption = caseDataMap != null ? caseDataMap.get(CUSTOM_ORDER_NAME_OPTION) : null;
        if (rawOption == null) {
            return null;
        }
        try {
            if (rawOption instanceof String rawOptionStr) {
                return CustomOrderNameOptionsEnum.getValue(rawOptionStr);
            } else if (rawOption instanceof CustomOrderNameOptionsEnum customOrderNameOptionsEnum) {
                return customOrderNameOptionsEnum;
            }
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse customOrderNameOption: {}", rawOption);
        }
        return null;
    }

    private String getOrderNameFromSelection(CustomOrderNameOptionsEnum selectedOption, Map<String, Object> caseDataMap) {
        if (CustomOrderNameOptionsEnum.blankOrderOrDirections == selectedOption) {
            String c21SubOption = getC21SubOptionDisplayValue(caseDataMap);
            if (c21SubOption != null) {
                log.info("Using C21 sub-option order name: {}", c21SubOption);
                return c21SubOption;
            }
        }
        if (CustomOrderNameOptionsEnum.childArrangementsSpecificProhibitedOrder == selectedOption) {
            String c43OrdersName = getC43OrdersDisplayValue(caseDataMap);
            if (c43OrdersName != null) {
                log.info("Using C43 combined order name: {}", c43OrdersName);
                return c43OrdersName;
            }
        }
        log.info("Using order name from dropdown: {}", selectedOption.getDisplayedValue());
        return selectedOption.getDisplayedValue();
    }

    /**
     * Returns the act reference to display above the order name in the header.
     * Returns null/empty for orders that don't require an act reference.
     */
    /**
     * Returns the form number prefix for an order (e.g., "C45A", "FL404A").
     */
    private String getFormNumberForOrder(CustomOrderNameOptionsEnum selectedOption) {
        if (selectedOption == null) {
            return null;
        }
        return switch (selectedOption) {
            case appointmentOfGuardian -> "C47A";
            case parentalResponsibility -> "C45A";
            case blankOrderOrDirections -> "C21";
            case standardDirectionsOrder -> "SDO";
            case specialGuardianShip -> "C43A";
            case childArrangementsSpecificProhibitedOrder -> "C43";
            case nonMolestation -> "FL404A";
            case occupation -> "FL404";
            case powerOfArrest -> "FL406";
            case amendDischargedVaried, blank -> "FL404B";
            case noticeOfProceedingsParties -> "C6";
            case noticeOfProceedingsNonParties -> "C6a";
            default -> null;
        };
    }

    /**
     * Returns the act reference for an order (e.g., "Children Act 1989").
     */
    private String getActReferenceForOrder(CustomOrderNameOptionsEnum selectedOption) {
        if (selectedOption == null) {
            return null;
        }
        return switch (selectedOption) {
            case appointmentOfGuardian -> "Family Procedure Rules 2010";
            case parentalResponsibility, blankOrderOrDirections,
                 standardDirectionsOrder, specialGuardianShip, directionOnIssue -> "Children Act 1989";
            case childArrangementsSpecificProhibitedOrder -> "Section 8 Children Act 1989";
            case nonMolestation -> "Section 42 Family Law Act 1996";
            case occupation -> "Section 33 to 38 Family Law Act 1996";
            case powerOfArrest, amendDischargedVaried, blank -> "Family Law Act 1996";
            case noticeOfProceedingsParties, noticeOfProceedingsNonParties, noticeOfProceedings,
                 generalForm -> null;  // C6, N117, FL402 - no act
            default -> null;
        };
    }

    /**
     * Strips the form number in parentheses from an order description.
     * E.g., "Parental responsibility order (C45A)" -> "Parental responsibility order"
     */
    String stripFormNumberFromDescription(String description, String formNumber) {
        if (description == null) {
            return null;
        }

        if (StringUtils.isNotBlank(formNumber)) {
            return description.replace("(" + formNumber + ")", "").trim();
        }

        return description;
    }

    /**
     * Extracts C21 sub-option display value from customC21OrderDetails ComplexType.
     */
    String getC21SubOptionDisplayValue(Map<String, Object> caseDataMap) {
        if (caseDataMap == null) {
            return null;
        }

        Object customC21Details = caseDataMap.get(CUSTOM_C21_ORDER_DETAILS);
        if (customC21Details instanceof Map<?, ?> c21Map) {
            Object orderOptions = c21Map.get("orderOptions");
            if (orderOptions != null) {
                try {
                    C21OrderOptionsEnum c21Option = switch (orderOptions) {
                        case String orderOptionsStr -> C21OrderOptionsEnum.getValue(orderOptionsStr);
                        case C21OrderOptionsEnum c21OrderOptionsEnum -> c21OrderOptionsEnum;
                        default -> null;
                    };
                    return c21Option != null ? c21Option.getDisplayedValue() : null;
                } catch (IllegalArgumentException e) {
                    log.warn("Could not parse C21 order option: {}", orderOptions);
                }
            }
        }
        return null;
    }

    /**
     * Extracts C43 order types display value from customC43OrderDetails ComplexType.
     * Uses the same format as existing create order journey for consistency.
     */
    String getC43OrdersDisplayValue(Map<String, Object> caseDataMap) {
        if (caseDataMap == null) {
            return null;
        }

        Object customC43Details = caseDataMap.get(CUSTOM_C43_ORDER_DETAILS);
        if (!(customC43Details instanceof Map<?, ?> c43Map)) {
            return null;
        }

        Object ordersToIssue = c43Map.get("ordersToIssue");
        if (!(ordersToIssue instanceof List<?> ordersList) || ordersList.isEmpty()) {
            return null;
        }

        List<OrderTypeEnum> orderTypes = parseOrderTypes(ordersList);
        ChildArrangementOrderTypeEnum childArrangementsSubType = parseChildArrangementsSubType(c43Map);
        return ManageOrdersUtils.buildC43OrderName(orderTypes, childArrangementsSubType);
    }

    private List<OrderTypeEnum> parseOrderTypes(List<?> ordersList) {
        List<OrderTypeEnum> orderTypes = new ArrayList<>();
        for (Object order : ordersList) {
            OrderTypeEnum parsed = parseOrderType(order);
            if (parsed != null) {
                orderTypes.add(parsed);
            }
        }
        return orderTypes;
    }

    private OrderTypeEnum parseOrderType(Object order) {
        try {
            return switch (order) {
                case String orderStr -> OrderTypeEnum.getValue(orderStr);
                case OrderTypeEnum ote -> ote;
                default -> null;
            };
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse C43 order type: {}", order);
            return null;
        }
    }

    /**
     * Parses the child arrangements sub-type from the C43 map.
     */
    private ChildArrangementOrderTypeEnum parseChildArrangementsSubType(Map<?, ?> c43Map) {
        Object childArrangementsOrderType = c43Map.get("childArrangementsOrderType");
        if (childArrangementsOrderType != null) {
            try {
                if (childArrangementsOrderType instanceof String str) {
                    return ChildArrangementOrderTypeEnum.getValue(str);
                } else if (childArrangementsOrderType instanceof ChildArrangementOrderTypeEnum cat) {
                    return cat;
                }
            } catch (IllegalArgumentException e) {
                log.warn("Could not parse child arrangements order type: {}", childArrangementsOrderType);
            }
        }
        return null;
    }

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
     */
    public Map<String, Object> renderUploadedCustomOrderAndStoreOnManageOrders(
        String authorisation,
        Long caseId,
        CaseData caseData,
        Map<String, Object> caseDataUpdated,
        java.util.function.UnaryOperator<CaseData> populateJudgeNames,
        java.util.function.UnaryOperator<CaseData> populatePartyDetails
    ) {
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
        // caseDataUpdated.remove("customOrderDoc"); // DO NOT REMOVE - needed for submitted callback
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
        safePut(data, COURT_NAME, caseData::getCourtName);

        // Format order name with form number, description and act reference
        CustomOrderNameOptionsEnum selectedOption = parseCustomOrderNameOption(caseDataMap);
        String orderDescription = getEffectiveOrderName(caseData, caseDataMap);
        String actReference = getActReferenceForOrder(selectedOption);
        String displayOrderName = getDisplayOrderName(caseData, caseDataMap, selectedOption, orderDescription);
        String formattedOrderName = StringUtils.isNotBlank(actReference)
            ? displayOrderName + "\n" + actReference
            : displayOrderName;

        data.put("orderName", formattedOrderName);

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
     * <p>Because UPDATE_ALL_TABS increments the case version, CCD's final save of
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
        String previewName = HEADER_PREVIEW_FILENAME_PATTERN + "_" + caseId + DOCX_EXTENSION;
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
     * Extracts judge name from case data map or CaseData object.
     * For magistrates, extracts from magistrateLastName list and joins with "and".
     */
    private String extractJudgeName(CaseData caseData, Map<String, Object> caseDataMap) {
        // Try judgeOrMagistratesLastName first
        if (caseDataMap != null && caseDataMap.get(JUDGE_OR_MAGISTRATES_LAST_NAME) != null) {
            String judgeName = caseDataMap.get(JUDGE_OR_MAGISTRATES_LAST_NAME).toString();
            if (StringUtils.isNotEmpty(judgeName)) {
                return judgeName;
            }
        }
        String judgeName = caseData.getJudgeOrMagistratesLastName();
        if (StringUtils.isNotEmpty(judgeName)) {
            return judgeName;
        }

        // Try magistrateLastName list (for multiple magistrates)
        String magistrateNames = extractMagistrateNames(caseData, caseDataMap);
        if (StringUtils.isNotEmpty(magistrateNames)) {
            return magistrateNames;
        }

        return null;
    }

    /**
     * Extracts magistrate names from the magistrateLastName list.
     * Note: Despite the field name "lastName", users enter full names in the UI
     * (CCD label is "Magistrate's full name").
     */
    @SuppressWarnings("unchecked")
    private String extractMagistrateNames(CaseData caseData, Map<String, Object> caseDataMap) {
        List<Element<MagistrateLastName>> magistrateList = null;

        // Try from caseDataMap first
        if (caseDataMap != null && caseDataMap.get("magistrateLastName") != null) {
            Object magistrateObj = caseDataMap.get("magistrateLastName");
            if (magistrateObj instanceof List<?> rawList && !rawList.isEmpty()) {
                Object firstItem = rawList.getFirst();
                if (firstItem instanceof Element) {
                    // Already Element objects
                    magistrateList = (List<Element<MagistrateLastName>>) magistrateObj;
                } else if (firstItem instanceof Map) {
                    // Convert from list of maps (CCD format)
                    magistrateList = rawList.stream()
                        .filter(item -> item instanceof Map)
                        .map(item -> {
                            Map<String, Object> map = (Map<String, Object>) item;
                            Object value = map.get("value");
                            if (value instanceof Map) {
                                Map<String, Object> valueMap = (Map<String, Object>) value;
                                String lastName = valueMap.get("lastName") != null
                                    ? valueMap.get("lastName").toString() : null;
                                return Element.<MagistrateLastName>builder()
                                    .value(MagistrateLastName.builder().lastName(lastName).build())
                                    .build();
                            }
                            return null;
                        })
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toList());
                }
            }
        }

        // Fall back to caseData
        if ((magistrateList == null || magistrateList.isEmpty()) && caseData.getMagistrateLastName() != null) {
            magistrateList = caseData.getMagistrateLastName();
        }

        if (magistrateList == null || magistrateList.isEmpty()) {
            return null;
        }

        // Extract and join names
        List<String> names = magistrateList.stream()
            .map(Element::getValue)
            .filter(java.util.Objects::nonNull)
            .map(MagistrateLastName::getLastName)
            .filter(StringUtils::isNotEmpty)
            .collect(java.util.stream.Collectors.toList());

        if (names.isEmpty()) {
            return null;
        } else if (names.size() == 1) {
            return names.get(0);
        } else {
            // Join with "and" for multiple names: "Smith, Jones and Brown"
            String allButLast = String.join(", ", names.subList(0, names.size() - 1));
            return allButLast + " and " + names.get(names.size() - 1);
        }
    }

    String extractLegalAdviserName(CaseData caseData, Map<String, Object> caseDataMap) {
        if (caseDataMap != null && caseDataMap.get("justiceLegalAdviserFullName") != null) {
            String name = caseDataMap.get("justiceLegalAdviserFullName").toString();
            return name;
        }
        String name = caseData.getJusticeLegalAdviserFullName();
        return name;
    }

    String extractJudgeTitle(CaseData caseData, Map<String, Object> caseDataMap) {
        if (caseDataMap != null && caseDataMap.get("judgeOrMagistrateTitle") != null) {
            Object titleObj = caseDataMap.get("judgeOrMagistrateTitle");
            String title = extractEnumDisplayValue(titleObj);
            log.info("Judge title from caseDataMap: {}", title);
            return title;
        }
        if (caseData.getManageOrders() != null && caseData.getManageOrders().getJudgeOrMagistrateTitle() != null) {
            String title = caseData.getManageOrders().getJudgeOrMagistrateTitle().getDisplayedValue();
            log.info("Judge title from caseData.manageOrders: {}", title);
            return title;
        }
        return null;
    }

    private String extractEnumDisplayValue(Object enumObj) {
        if (enumObj instanceof JudgeOrMagistrateTitleEnum titleEnum) {
            return titleEnum.getDisplayedValue();
        } else if (enumObj instanceof String enumStr) {
            try {
                return JudgeOrMagistrateTitleEnum.getValue(enumStr).getDisplayedValue();
            } catch (IllegalArgumentException e) {
                // May already be the display value
                return enumStr;
            }
        }
        return enumObj != null ? enumObj.toString() : null;
    }

    /**
     * Extracts and formats order date.
     * Priority: hearing date (if approved at hearing) > dateOrderMade > current date.
     */
    String extractOrderDate(CaseData caseData, Map<String, Object> caseDataMap) {
        // If approved at a hearing with a hearing selected, use the hearing date
        String hearingDate = extractHearingDateFromSelection(caseDataMap);
        if (hearingDate != null) {
            log.info("Order date from selected hearing: {}", hearingDate);
            return hearingDate;
        }

        // Fall back to dateOrderMade if set
        if (caseDataMap != null && caseDataMap.get(DATE_ORDER_MADE) != null) {
            Object dateValue = caseDataMap.get(DATE_ORDER_MADE);
            String result;
            if (dateValue instanceof LocalDate localDate) {
                result = localDate.format(DATE_FORMATTER);
            } else {
                result = reformatDateToUkFormat(dateValue.toString());
            }
            log.info("Order date from dateOrderMade (map): {}", result);
            return result;
        }
        if (caseData.getDateOrderMade() != null) {
            String result = caseData.getDateOrderMade().format(DATE_FORMATTER);
            log.info("Order date from dateOrderMade (caseData): {}", result);
            return result;
        }

        // Final fallback - current date
        String currentDate = LocalDate.now().format(DATE_FORMATTER);
        log.info("No hearing or dateOrderMade, using current date: {}", currentDate);
        return currentDate;
    }

    /**
     * Extracts the hearing date if a hearing is selected AND wasTheOrderApprovedAtHearing is Yes.
     * The hearingsType label format is "hearingType - dd/MM/yyyy hh:mm:ss"
     * Returns null if no hearing is selected or wasTheOrderApprovedAtHearing is not Yes.
     * For custom orders, hearingsType is at root level in the map (not in CaseData object).
     */
    @SuppressWarnings("unchecked")
    private String extractHearingDateFromSelection(Map<String, Object> caseDataMap) {
        if (caseDataMap == null) {
            return null;
        }

        // Check if approved at hearing - for custom orders, use the custom field names
        Object approvedValue = caseDataMap.get("customOrderWasApprovedAtHearing");
        Object hearingsTypeObj = caseDataMap.get("customOrderHearingsType");

        log.info("extractHearingDateFromSelection - approved: {}, hearingsType: {}, type: {}",
            approvedValue, hearingsTypeObj, hearingsTypeObj != null ? hearingsTypeObj.getClass().getName() : "null");

        if (!"Yes".equals(String.valueOf(approvedValue))) {
            return null;
        }

        // hearingsType may be DynamicList or Map
        String hearingLabel = null;

        if (hearingsTypeObj instanceof uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList dynamicList) {
            if (dynamicList.getValue() != null) {
                hearingLabel = dynamicList.getValue().getLabel();
            }
        } else if (hearingsTypeObj instanceof Map) {
            Object valueObj = ((Map<String, Object>) hearingsTypeObj).get("value");
            if (valueObj instanceof Map) {
                Object label = ((Map<String, Object>) valueObj).get("label");
                if (label != null) {
                    hearingLabel = label.toString();
                }
            }
        }

        if (hearingLabel != null && hearingLabel.contains(" - ")) {
            // Label format: "hearingType - dd/MM/yyyy hh:mm:ss"
            String[] parts = hearingLabel.split(" - ");
            if (parts.length >= 2) {
                String dateTimePart = parts[1].trim();
                // Extract just the date part (dd/MM/yyyy) from "dd/MM/yyyy hh:mm:ss"
                if (dateTimePart.length() >= 10) {
                    return dateTimePart.substring(0, 10);
                }
            }
        }
        return null;
    }

    /**
     * Extracts hearing date from manage orders or falls back to order date.
     */
    private String extractHearingDate(CaseData caseData, Map<String, Object> caseDataMap) {
        if (caseData.getManageOrders() != null
            && caseData.getManageOrders().getOrdersHearingDetails() != null
            && !caseData.getManageOrders().getOrdersHearingDetails().isEmpty()) {
            var hearingDate = caseData.getManageOrders().getOrdersHearingDetails().getFirst()
                .getValue().getHearingDateConfirmOptionEnum();
            if (hearingDate != null) {
                return hearingDate.toString();
            }
        }
        return extractOrderDate(caseData, caseDataMap);
    }

    /**
     * Checks if the hearing channel is "on the papers" from ordersHearingDetails.
     * Returns true if hearingChannelsEnum is ONPPRS or hearingChannels contains ONPPRS.
     */
    private boolean isHearingOnThePapers(CaseData caseData) {
        try {
            if (caseData.getManageOrders() != null
                && caseData.getManageOrders().getOrdersHearingDetails() != null
                && !caseData.getManageOrders().getOrdersHearingDetails().isEmpty()) {
                var hearingDataElement = caseData.getManageOrders().getOrdersHearingDetails().getFirst();
                if (hearingDataElement == null || hearingDataElement.getValue() == null) {
                    log.info("isHearingOnThePapers - hearingDataElement or value is null");
                    return false;
                }
                var hearingData = hearingDataElement.getValue();

                // Get selected value from hearingChannels DynamicList if present
                String hearingChannelsValue = null;
                if (hearingData.getHearingChannels() != null
                    && hearingData.getHearingChannels().getValue() != null
                    && hearingData.getHearingChannels().getValue().getCode() != null) {
                    hearingChannelsValue = hearingData.getHearingChannels().getValue().getCode();
                }

                log.info("isHearingOnThePapers - HearingData fields: "
                        + "hearingChannelsEnum={}, "
                        + "hearingChannelsValue={}, "
                        + "allPartiesAttendSameWay={}, "
                        + "hearingDateConfirmOptionEnum={}, "
                        + "confirmedHearingDates={}",
                    hearingData.getHearingChannelsEnum(),
                    hearingChannelsValue,
                    hearingData.getAllPartiesAttendHearingSameWayYesOrNo(),
                    hearingData.getHearingDateConfirmOptionEnum(),
                    hearingData.getConfirmedHearingDates());

                // Check both the enum and the DynamicList for "on the papers"
                boolean isOnPapersEnum = HearingChannelsEnum.ONPPRS.equals(hearingData.getHearingChannelsEnum());
                boolean isOnPapersList = "ONPPRS".equalsIgnoreCase(hearingChannelsValue);

                return isOnPapersEnum || isOnPapersList;
            }
            log.info("isHearingOnThePapers - no ordersHearingDetails found");
        } catch (Exception e) {
            log.warn("isHearingOnThePapers - error checking hearing channel, defaulting to false", e);
        }
        return false;
    }

    /**
     * Populates FamilyMan case number placeholders for the header template.
     * Only includes the label and value if familymanCaseNumber exists.
     */
    private void populateFamilymanPlaceholders(Map<String, Object> data, CaseData caseData) {
        String familymanNumber = caseData.getFamilymanCaseNumber();
        if (familymanNumber != null && !familymanNumber.isBlank()) {
            log.info("FamilyMan case number found: {}", familymanNumber);
            data.put("familymanLabel", "FamilyMan:");
            data.put("familymanCaseNumber", familymanNumber);
        } else {
            log.info("No FamilyMan case number, leaving placeholders empty");
            data.put("familymanLabel", "");
            data.put("familymanCaseNumber", "");
        }
    }

    /**
     * Populates applicant placeholders for the header template.
     */
    private void populateApplicantPlaceholders(Map<String, Object> data, CaseData caseData, boolean isFL401) {
        log.info("Populating applicant placeholders. isFL401={}, hasApplicantsFL401={}, hasApplicants={}",
            isFL401, caseData.getApplicantsFL401() != null,
            caseData.getApplicants() != null && !caseData.getApplicants().isEmpty());
        if (isFL401 && caseData.getApplicantsFL401() != null) {
            PartyDetails applicant = caseData.getApplicantsFL401();
            log.info("Using FL401 applicant: {} {}", applicant.getFirstName(), applicant.getLastName());
            populateApplicantData(data, applicant);
        } else if (caseData.getApplicants() != null && !caseData.getApplicants().isEmpty()) {
            var applicant = caseData.getApplicants().getFirst().getValue();
            log.info("Using C100 applicant: {} {}", applicant.getFirstName(), applicant.getLastName());
            populateApplicantData(data, applicant);
        } else {
            log.warn("No applicant data found, using empty placeholders");
            setEmptyApplicantPlaceholders(data);
        }
    }

    private void populateApplicantData(Map<String, Object> data, PartyDetails applicant) {
        data.put(APPLICANT_NAME, getFullName(applicant.getFirstName(), applicant.getLastName()));
        String appRep = getRepresentativeName(applicant);
        data.put("applicantRepresentativeName", appRep);
        data.put("applicantRepresentativeClause", formatRepresentativeClause(appRep));
    }

    private void setEmptyApplicantPlaceholders(Map<String, Object> data) {
        data.put(APPLICANT_NAME, "");
        data.put("applicantRepresentativeName", "");
        data.put("applicantRepresentativeClause", "");
    }

    /**
     * Builds placeholder map for the header template from case data.
     */
    private Map<String, Object> buildHeaderPlaceholders(Long caseId, CaseData caseData, Map<String, Object> caseDataMap) {
        Map<String, Object> data = new HashMap<>();

        // Case details
        data.put("caseNumber", formatCaseNumber(String.valueOf(caseId)));
        populateFamilymanPlaceholders(data, caseData);
        safePut(data, COURT_NAME, caseData::getCourtName);

        CustomOrderNameOptionsEnum selectedOption = parseCustomOrderNameOption(caseDataMap);
        String orderDescription = getEffectiveOrderName(caseData, caseDataMap);
        String actReference = getActReferenceForOrder(selectedOption);
        String displayOrderName = getDisplayOrderName(caseData, caseDataMap, selectedOption, orderDescription);
        String formattedOrderName = StringUtils.isNotBlank(actReference)
            ? displayOrderName + "\n" + actReference
            : displayOrderName;

        data.put("orderName", formattedOrderName);

        // Judge details
        String judgeName = extractJudgeName(caseData, caseDataMap);
        if (judgeName != null && !judgeName.isEmpty()) {
            data.put("judgeName", judgeName);
        }
        String judgeTitle = extractJudgeTitle(caseData, caseDataMap);
        if (judgeTitle != null && !judgeTitle.isEmpty()) {
            data.put("judgeTitle", judgeTitle);
        }

        // Legal adviser clause (e.g. "sitting with Justices' Legal Adviser Jane Smith")
        String legalAdviserName = extractLegalAdviserName(caseData, caseDataMap);
        if (StringUtils.isNotEmpty(legalAdviserName)) {
            data.put("sittingWithLegalAdviser", " sitting with Justices' Legal Adviser " + legalAdviserName);
        } else {
            data.put("sittingWithLegalAdviser", "");
        }

        // Order date and hearing/papers text
        String hearingDate = extractHearingDateFromSelection(caseDataMap);
        boolean hasHearing = hearingDate != null;
        String orderDate = extractOrderDate(caseData, caseDataMap);
        if (orderDate != null && !orderDate.isEmpty()) {
            data.put("orderDate", orderDate);
        }
        // "at a hearing" if a hearing was selected, unless explicitly marked as "on the papers"
        // Defaults to "at a hearing" if hearing channel check fails or is inconclusive
        boolean isOnThePapers = !hasHearing || isHearingOnThePapers(caseData);
        data.put("hearingOrPapers", isOnThePapers ? "on the papers" : "at a hearing");

        // Hearing date with fallback to order date
        safePut(data, "hearingDate", () -> {
            String extractedHearingDate = extractHearingDate(caseData, caseDataMap);
            return extractedHearingDate != null ? extractedHearingDate : "";
        });
        safePut(data, "hearingType", () -> "hearing");

        // Determine case type and populate party details
        boolean isFL401 = FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData));
        populateApplicantPlaceholders(data, caseData, isFL401);
        populateRespondents(data, caseData, isFL401);
        populateChildrensGuardian(data, caseData);
        populateChildrenPlaceholders(data, caseData, isFL401);

        log.info("Final header placeholders - judgeTitle={}, judgeName={}, orderDate={}, applicantName={}, respondent1Name={}",
            data.get("judgeTitle"), data.get("judgeName"), data.get("orderDate"), data.get(APPLICANT_NAME), data.get("respondent1Name"));
        return data;
    }

    /**
     * Populates children placeholders based on order selection (all children or selected children).
     * Provides both list format for poi-tl table looping and individual placeholders.
     * Handles both C100 (newChildDetails) and FL401 (applicantChildDetails) case types.
     */
    private void populateChildrenPlaceholders(Map<String, Object> data, CaseData caseData, boolean isFL401) {
        List<Map<String, String>> childrenRows = new ArrayList<>();

        if (isFL401) {
            populateFl401Children(childrenRows, caseData);
        } else {
            populateC100Children(childrenRows, caseData);
        }

        data.put("children", childrenRows);
        data.put("hasChildren", !childrenRows.isEmpty());
        log.info("Populated {} children for dynamic table rows, hasChildren={}", childrenRows.size(), !childrenRows.isEmpty());
    }

    private void populateC100Children(List<Map<String, String>> childrenRows, CaseData caseData) {
        List<ChildDetailsRevised> selectedChildren = getSelectedChildren(caseData);

        for (ChildDetailsRevised child : selectedChildren) {
            String fullName = getFullName(child.getFirstName(), child.getLastName());
            String gender = child.getGender() != null ? child.getGender().getDisplayedValue() : "";
            String dob = child.getDateOfBirth() != null
                ? child.getDateOfBirth().format(DATE_FORMATTER)
                : "";

            Map<String, String> childRow = new HashMap<>();
            childRow.put("fullName", fullName);
            childRow.put("gender", gender);
            childRow.put("dob", dob);
            childrenRows.add(childRow);
        }
    }

    private void populateFl401Children(List<Map<String, String>> childrenRows, CaseData caseData) {
        if (caseData.getApplicantChildDetails() == null || caseData.getApplicantChildDetails().isEmpty()) {
            log.info("No children found in applicantChildDetails for FL401");
            return;
        }

        // Check if order is about children
        if (caseData.getManageOrders() != null
            && YesOrNo.No.equals(caseData.getManageOrders().getIsTheOrderAboutChildren())) {
            log.info("FL401 order is not about children, skipping children population");
            return;
        }

        // Get selected child IDs if specific children were selected
        Set<String> selectedIds = getSelectedChildIds(caseData);

        for (Element<ApplicantChild> childElement : caseData.getApplicantChildDetails()) {
            // Filter by selection if specific children were chosen, otherwise include all
            if (!selectedIds.isEmpty() && !selectedIds.contains(childElement.getId().toString())) {
                continue;
            }

            ApplicantChild child = childElement.getValue();
            String fullName = StringUtils.defaultString(child.getFullName());
            String dob = child.getDateOfBirth() != null
                ? child.getDateOfBirth().format(DATE_FORMATTER)
                : "";

            Map<String, String> childRow = new HashMap<>();
            childRow.put("fullName", fullName);
            childRow.put("gender", ""); // FL401 ApplicantChild does not have gender field
            childRow.put("dob", dob);
            childrenRows.add(childRow);
        }
        log.info("Populated {} FL401 children from applicantChildDetails", childrenRows.size());
    }

    private Set<String> getSelectedChildIds(CaseData caseData) {
        if (caseData.getManageOrders() == null) {
            return Collections.emptySet();
        }
        DynamicMultiSelectList childOption = caseData.getManageOrders().getChildOption();
        if (childOption == null || childOption.getValue() == null || childOption.getValue().isEmpty()) {
            return Collections.emptySet();
        }
        return childOption.getValue().stream()
            .map(DynamicMultiselectListElement::getCode)
            .collect(Collectors.toSet());
    }

    /**
     * Populates respondent placeholders including solicitors and relationship to children.
     * Handles both C100 (list of respondents) and FL401 (single respondent).
     */
    private void populateRespondents(Map<String, Object> data, CaseData caseData, boolean isFL401) {
        List<Map<String, String>> respondentRows = new ArrayList<>();

        if (isFL401 && caseData.getRespondentsFL401() != null) {
            populateFl401Respondent(data, caseData, respondentRows);
        } else if (caseData.getRespondents() != null) {
            populateC100Respondents(data, caseData, respondentRows);
        }

        fillEmptyRespondentSlots(data);
        data.put("respondents", respondentRows);
        log.info("Populated {} respondent rows", respondentRows.size());
    }

    private void populateFl401Respondent(Map<String, Object> data, CaseData caseData, List<Map<String, String>> respondentRows) {
        PartyDetails respondent = caseData.getRespondentsFL401();
        log.debug(" FL401 Respondent: relationshipToChildren='{}', representativeFirstName='{}', representativeLastName='{}'",
            respondent.getRelationshipToChildren(), respondent.getRepresentativeFirstName(), respondent.getRepresentativeLastName());

        String name = getFullName(respondent.getFirstName(), respondent.getLastName());
        String relationship = getEffectiveRelationship(caseData, respondent, 1, null, name);
        String representative = getRepresentativeName(respondent);

        respondentRows.add(buildRespondentRow(1, name, relationship, representative));
        putRespondentPlaceholders(data, 1, name, relationship, representative);
    }

    private void populateC100Respondents(Map<String, Object> data, CaseData caseData, List<Map<String, String>> respondentRows) {
        int index = 1;
        for (Element<PartyDetails> respondentElement : caseData.getRespondents()) {
            PartyDetails respondent = respondentElement.getValue();
            String respondentId = respondentElement.getId() != null ? respondentElement.getId().toString() : null;

            log.debug(" Respondent {}: id='{}', relationshipToChildren='{}', representativeFirstName='{}', representativeLastName='{}'",
                index, respondentId, respondent.getRelationshipToChildren(),
                respondent.getRepresentativeFirstName(), respondent.getRepresentativeLastName());

            String name = getFullName(respondent.getFirstName(), respondent.getLastName());
            String relationship = getEffectiveRelationship(caseData, respondent, index, respondentId, name);
            String representative = getRepresentativeName(respondent);

            respondentRows.add(buildRespondentRow(index, name, relationship, representative));
            putRespondentPlaceholders(data, index, name, relationship, representative);
            index++;
        }
    }

    private String getEffectiveRelationship(CaseData caseData, PartyDetails respondent, int index, String respondentId, String name) {
        String relationship = StringUtils.defaultString(respondent.getRelationshipToChildren());
        if (relationship.isEmpty()) {
            relationship = getRespondentRelationshipFromRelations(caseData, index, respondentId, name);
        }
        return relationship;
    }

    private Map<String, String> buildRespondentRow(int index, String name, String relationship, String representative) {
        Map<String, String> row = new HashMap<>();
        row.put("ordinal", getOrdinal(index));
        row.put("name", name);
        row.put("relationship", relationship);
        row.put("relationshipClause", relationship.isEmpty() ? "" : ", the " + relationship);
        row.put("representative", representative);
        row.put("representativeClause", formatRepresentativeClause(representative));
        return row;
    }

    private String getOrdinal(int index) {
        return switch (index) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> index + "th";
        };
    }

    private void putRespondentPlaceholders(Map<String, Object> data, int index, String name, String relationship, String representative) {
        data.put(RESPONDENT_PREFIX + index + "Name", name);
        data.put(RESPONDENT_PREFIX + index + "RelationshipToChild", relationship);
        data.put(RESPONDENT_PREFIX + index + "RelationshipClause", relationship.isEmpty() ? "" : "(" + relationship + ")");
        data.put(RESPONDENT_PREFIX + index + "RepresentativeName", representative);
        data.put(RESPONDENT_PREFIX + index + "RepresentativeClause", formatRepresentativeClause(representative));
    }

    private void fillEmptyRespondentSlots(Map<String, Object> data) {
        for (int i = 1; i <= 5; i++) {
            data.putIfAbsent(RESPONDENT_PREFIX + i + "Name", "");
            data.putIfAbsent(RESPONDENT_PREFIX + i + "RelationshipToChild", "");
            data.putIfAbsent(RESPONDENT_PREFIX + i + "RelationshipClause", "");
            data.putIfAbsent(RESPONDENT_PREFIX + i + "RepresentativeName", "");
            data.putIfAbsent(RESPONDENT_PREFIX + i + "RepresentativeClause", "");
        }
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
        String result = findRelationshipFromNewModel(caseData, respondentIndex, respondentId, respondentName);
        if (!result.isEmpty()) {
            return result;
        }

        result = findRelationshipFromOldModel(caseData);
        if (!result.isEmpty()) {
            return result;
        }

        log.debug(" No respondent relationship found for respondent {}", respondentIndex);
        return "";
    }

    private String findRelationshipFromNewModel(CaseData caseData, int respondentIndex,
                                                 String respondentId, String respondentName) {
        if (!hasChildAndRespondentRelations(caseData)) {
            return "";
        }

        var relations = caseData.getRelations().getChildAndRespondentRelations();
        logRelationEntries(relations, respondentIndex, respondentId, respondentName);

        String result = findRelationshipByName(relations, respondentName);
        if (!result.isEmpty()) {
            return result;
        }

        result = findRelationshipById(relations, respondentId, respondentIndex);
        if (!result.isEmpty()) {
            return result;
        }

        return findRelationshipByIndex(relations, respondentIndex);
    }

    private boolean hasChildAndRespondentRelations(CaseData caseData) {
        return caseData.getRelations() != null
            && caseData.getRelations().getChildAndRespondentRelations() != null
            && !caseData.getRelations().getChildAndRespondentRelations().isEmpty();
    }

    private void logRelationEntries(List<Element<uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation>> relations,
                                    int respondentIndex, String respondentId, String respondentName) {
        log.debug(" Found {} childAndRespondentRelations entries. Looking for respondent index={}, id='{}', name='{}'",
            relations.size(), respondentIndex, respondentId, respondentName);
        for (var relElement : relations) {
            var relation = relElement.getValue();
            log.debug(" Relation entry - respondentId='{}', respondentFullName='{}', relation='{}'",
                relation.getRespondentId(), relation.getRespondentFullName(), relation.getChildAndRespondentRelation());
        }
    }

    private String findRelationshipByName(List<Element<uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation>> relations,
                                          String respondentName) {
        if (respondentName == null || respondentName.isEmpty()) {
            return "";
        }
        java.util.Set<String> relationships = new java.util.LinkedHashSet<>();
        for (var relElement : relations) {
            var relation = relElement.getValue();
            if (respondentName.equalsIgnoreCase(relation.getRespondentFullName())
                && relation.getChildAndRespondentRelation() != null) {
                relationships.add(relation.getChildAndRespondentRelation().getDisplayedValue());
            }
        }
        if (!relationships.isEmpty()) {
            String result = String.join(", ", relationships);
            log.debug(" Found respondent '{}' relationships by name match: '{}'", respondentName, result);
            return result;
        }
        return "";
    }

    private String findRelationshipById(List<Element<uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation>> relations,
                                        String respondentId, int respondentIndex) {
        if (respondentId == null || respondentId.isEmpty()) {
            return "";
        }
        java.util.Set<String> relationships = new java.util.LinkedHashSet<>();
        for (var relElement : relations) {
            var relation = relElement.getValue();
            if (respondentId.equals(relation.getRespondentId()) && relation.getChildAndRespondentRelation() != null) {
                relationships.add(relation.getChildAndRespondentRelation().getDisplayedValue());
            }
        }
        if (!relationships.isEmpty()) {
            String result = String.join(", ", relationships);
            log.debug(" Found respondent {} relationships by ID match: '{}'", respondentIndex, result);
            return result;
        }
        return "";
    }

    private String findRelationshipByIndex(List<Element<uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation>> relations,
                                           int respondentIndex) {
        java.util.Map<String, java.util.Set<String>> respondentToRelationships = new java.util.LinkedHashMap<>();
        for (var relElement : relations) {
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
                log.debug(" Found respondent {} ('{}') relationships by index: '{}'", respondentIndex, entry.getKey(), result);
                return result;
            }
        }
        return "";
    }

    private String findRelationshipFromOldModel(CaseData caseData) {
        if (caseData.getChildren() == null || caseData.getChildren().isEmpty()) {
            return "";
        }
        for (var childElement : caseData.getChildren()) {
            var child = childElement.getValue();
            if (child.getRespondentsRelationshipToChild() != null) {
                String displayValue = child.getRespondentsRelationshipToChild().getDisplayedValue();
                log.debug(" Found respondent relationship from children model: '{}'", displayValue);
                return displayValue;
            }
        }
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
            var firstOfficer = caseData.getChildAndCafcassOfficers().getFirst().getValue();
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
        String first = StringUtils.defaultString(firstName);
        String last = StringUtils.defaultString(lastName);
        return (first + " " + last).trim();
    }

    /**
     * Formats the representative clause - returns ", legally represented" or empty if no representative.
     */
    private String formatRepresentativeClause(String representativeName) {
        if (representativeName == null || representativeName.trim().isEmpty()) {
            return "";
        }
        return ", legally represented";
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
     * Reformats a date string from ISO format (yyyy-MM-dd) to UK format (dd/MM/yyyy).
     * If parsing fails, returns the original string.
     */
    private String reformatDateToUkFormat(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return dateStr;
        }
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            return date.format(DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("Could not parse date '{}', returning as-is", dateStr);
            return dateStr;
        }
    }

    /**
     * Gets the list of children based on order selection.
     * C100: isTheOrderAboutAllChildren = Yes → all children, No → selected from childOption
     * FL401: isTheOrderAboutChildren = Yes → selected from childOption, No → none
     */
    private List<ChildDetailsRevised> getSelectedChildren(CaseData caseData) {
        if (!hasChildrenData(caseData)) {
            return new ArrayList<>();
        }

        YesOrNo isAboutAllChildren = caseData.getManageOrders().getIsTheOrderAboutAllChildren();
        YesOrNo isAboutChildren = caseData.getManageOrders().getIsTheOrderAboutChildren();
        log.info("isTheOrderAboutAllChildren={}, isTheOrderAboutChildren={}", isAboutAllChildren, isAboutChildren);

        if (YesOrNo.Yes.equals(isAboutAllChildren)) {
            return getAllChildren(caseData);
        }

        if (YesOrNo.No.equals(isAboutChildren)) {
            log.info("isTheOrderAboutChildren=No, returning no children");
            return new ArrayList<>();
        }

        return getChildrenBySelection(caseData);
    }

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

    private List<ChildDetailsRevised> getAllChildren(CaseData caseData) {
        List<ChildDetailsRevised> result = caseData.getNewChildDetails().stream()
            .map(Element::getValue)
            .toList();
        log.info("isTheOrderAboutAllChildren=Yes, returning all {} children", result.size());
        return result;
    }

    private List<ChildDetailsRevised> getChildrenBySelection(CaseData caseData) {
        DynamicMultiSelectList childOption = caseData.getManageOrders().getChildOption();
        if (childOption == null || childOption.getValue() == null || childOption.getValue().isEmpty()) {
            log.info("No child selection found, returning empty");
            return new ArrayList<>();
        }

        Set<String> selectedIds = childOption.getValue().stream()
            .map(DynamicMultiselectListElement::getCode)
            .collect(Collectors.toSet());
        log.info("Selected child IDs: {}", selectedIds);

        List<ChildDetailsRevised> result = caseData.getNewChildDetails().stream()
            .filter(childElement -> selectedIds.contains(childElement.getId().toString()))
            .map(Element::getValue)
            .toList();

        log.info("Returning {} selected children", result.size());
        return result;
    }

    /**
     * Combines the header template with user's uploaded content.
     * Delegates to DocxCombineUtils for the actual document manipulation.
     *
     * @param headerBytes Rendered header document bytes
     * @param userContentBytes User's uploaded document bytes
     * @return Combined document bytes
     */
    public byte[] combineHeaderAndContent(byte[] headerBytes, byte[] userContentBytes) throws IOException {
        return DocxCombineUtils.combineDocuments(headerBytes, userContentBytes);
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
        Map<String, Object> caseDataMap,
        boolean isDraftOrder
    ) throws IOException {
        log.info("Processing custom order on submitted callback for case {}, isDraft={}", caseId, isDraftOrder);

        // Get system auth for document downloads
        String systemAuth = systemUserService.getSysUserToken();
        String s2sToken = authTokenGenerator.generate();

        // 1. Download the pre-rendered header (rendered in mid-event before cleanup, has children info)
        log.info("Downloading header from URL: {}", headerDocUrl);
        byte[] headerBytes = documentGenService.getDocumentBytes(headerDocUrl, systemAuth, s2sToken);
        log.info("Downloaded pre-rendered header: {} bytes", headerBytes.length);

        // 2. Download user's uploaded content (CDAM works now after submit)
        log.info("Downloading user content from URL: {}", userDocUrl);
        byte[] userContentBytes = documentGenService.getDocumentBytes(userDocUrl, systemAuth, s2sToken);
        log.info("Downloaded user content: {} bytes", userContentBytes.length);

        // 3. Combine header + user content
        byte[] combinedBytes = combineHeaderAndContent(headerBytes, userContentBytes);
        log.info("Combined document: {} bytes", combinedBytes.length);

        // 4. For final orders, seal before uploading to avoid timing issues
        String orderName = getEffectiveOrderName(caseData, caseDataMap);
        String fileName = orderName + "_" + caseId + DOCX_EXTENSION;

        // Upload DOCX first
        uk.gov.hmcts.reform.ccd.document.am.model.Document uploaded = uploadService.uploadDocument(
            combinedBytes,
            fileName,
            DOCX_CONTENT_TYPE,
            authorisation
        );

        uk.gov.hmcts.reform.prl.models.documents.Document uploadedDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl(uploaded.links.binary.href)
                .documentUrl(uploaded.links.self.href)
                .documentFileName(uploaded.originalDocumentName)
                .documentCreatedOn(uploaded.createdOn)
                .build();

        if (!isDraftOrder) {
            // Final orders - seal the uploaded document using original proven approach
            log.info("Sealing final order document: {}", uploadedDoc.getDocumentUrl());
            return documentSealingService.sealDocument(uploadedDoc, caseData, authorisation);
        }

        return uploadedDoc;
    }

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
        CustomOrderLocation orderLocation = findInOrderCollection(caseData);
        if (orderLocation != null) {
            return orderLocation;
        }

        // Then check draftOrderCollection
        CustomOrderLocation draftLocation = findInDraftOrderCollection(caseData);
        if (draftLocation != null) {
            return draftLocation;
        }

        log.warn("No custom order header preview document found in either collection");
        return null;
    }

    private CustomOrderLocation findInOrderCollection(CaseData caseData) {
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

    private CustomOrderLocation findInDraftOrderCollection(CaseData caseData) {
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

    private boolean isCustomOrderHeaderPreview(uk.gov.hmcts.reform.prl.models.documents.Document doc, boolean requireDocxExtension) {
        if (doc == null || doc.getDocumentFileName() == null) {
            return false;
        }
        boolean hasPattern = doc.getDocumentFileName().contains(HEADER_PREVIEW_FILENAME_PATTERN);
        if (!requireDocxExtension) {
            return hasPattern;
        }
        return hasPattern && doc.getDocumentFileName().toLowerCase().endsWith(DOCX_EXTENSION);
    }

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

    // ========== Court Name Resolution Methods (moved from ManageOrdersController) ==========

    /**
     * Resolves the court name from various possible sources in the case data.
     * Tries multiple fallback strategies to find the court name.
     *
     * @param caseData The case data object
     * @param caseDataMap The raw case data map
     * @return The resolved court name, or null if not found
     */
    public String resolveCourtName(CaseData caseData, Map<String, Object> caseDataMap) {
        // Try caseData.courtName first
        if (caseData.getCourtName() != null && !caseData.getCourtName().isEmpty()) {
            log.info("Court name found in caseData: {}", caseData.getCourtName());
            return caseData.getCourtName();
        }

        // Try raw courtName from map
        Object rawCourtName = caseDataMap != null ? caseDataMap.get(COURT_NAME) : null;
        if (rawCourtName != null && !"null".equals(rawCourtName.toString())
            && !rawCourtName.toString().isEmpty()) {
            log.info("Court name found in caseDataMap: {}", rawCourtName);
            return rawCourtName.toString();
        }

        // Try allocatedJudgeDetails
        Object allocatedJudgeObj = caseDataMap != null ? caseDataMap.get("allocatedJudgeDetails") : null;
        if (allocatedJudgeObj != null) {
            String courtName = extractCourtNameFromAllocatedJudge(allocatedJudgeObj);
            if (courtName != null) {
                log.info("Court name found in allocatedJudgeDetails: {}", courtName);
                return courtName;
            }
        }

        // Try courtList dynamic list
        Object courtListObj = caseDataMap != null ? caseDataMap.get("courtList") : null;
        if (courtListObj != null) {
            String courtName = extractCourtNameFromDynamicList(courtListObj);
            if (courtName != null) {
                log.info("Court name found in courtList: {}", courtName);
                return courtName;
            }
        }

        log.warn("Could not find court name from any source");
        return null;
    }

    /**
     * Extracts the court name from allocatedJudgeDetails object.
     * The allocatedJudgeDetails is stored in the summary tab and contains courtName.
     */
    private String extractCourtNameFromAllocatedJudge(Object allocatedJudgeObj) {
        if (allocatedJudgeObj == null) {
            log.debug("extractCourtNameFromAllocatedJudge: object is null");
            return null;
        }
        try {
            log.debug("extractCourtNameFromAllocatedJudge: type={}, value={}",
                allocatedJudgeObj.getClass().getSimpleName(), allocatedJudgeObj);
            if (allocatedJudgeObj instanceof Map<?, ?> judgeMap) {
                log.debug("extractCourtNameFromAllocatedJudge: map keys={}", judgeMap.keySet());
                Object courtNameValue = judgeMap.get(COURT_NAME);
                log.debug("extractCourtNameFromAllocatedJudge: courtName='{}' (type={})",
                    courtNameValue, courtNameValue != null ? courtNameValue.getClass().getSimpleName() : "null");
                if (courtNameValue != null
                    && !"null".equals(courtNameValue.toString())
                    && !courtNameValue.toString().trim().isEmpty()) {
                    return courtNameValue.toString().trim();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract court name from allocatedJudgeDetails: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extracts the court name label from a DynamicList object.
     * The courtList field is stored as a Map with structure: {value={code=..., label=...}, list_items=...}
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

    // ========== Combine and Finalize Custom Order (moved from ManageOrdersController) ==========

    /**
     * Combines the header preview document with user content and updates the appropriate order collection.
     * For non-draft orders, also seals the combined document.
     *
     * @param authorisation Auth token
     * @param caseData The case data
     * @param caseDataUpdated Map to store updated case data
     * @param isDraftOrder Whether this is a draft order (determines collection and sealing)
     */
    public void combineAndFinalizeCustomOrder(
        String authorisation,
        CaseData caseData,
        Map<String, Object> caseDataUpdated,
        boolean isDraftOrder
    ) {
        try {
            // Get the user's uploaded document
            Object customOrderDocObj = caseDataUpdated.get(CUSTOM_ORDER_DOC);
            uk.gov.hmcts.reform.prl.models.documents.Document customOrderDoc = null;
            if (customOrderDocObj != null) {
                customOrderDoc = objectMapper.convertValue(customOrderDocObj,
                    uk.gov.hmcts.reform.prl.models.documents.Document.class);
            }

            // Get the header preview from caseDataUpdated (where mid-event stored it) or fall back to caseData
            uk.gov.hmcts.reform.prl.models.documents.Document headerPreview = null;
            Object previewDocObj = caseDataUpdated.get("previewOrderDoc");
            if (previewDocObj != null) {
                headerPreview = objectMapper.convertValue(previewDocObj,
                    uk.gov.hmcts.reform.prl.models.documents.Document.class);
            }
            if (headerPreview == null) {
                headerPreview = caseData.getPreviewOrderDoc();
            }

            log.info("Custom order combining: customOrderDoc={}, headerPreview={}",
                customOrderDoc != null ? customOrderDoc.getDocumentFileName() : "null",
                headerPreview != null ? headerPreview.getDocumentFileName() : "null");

            if (customOrderDoc == null || customOrderDoc.getDocumentBinaryUrl() == null
                || headerPreview == null || headerPreview.getDocumentBinaryUrl() == null) {
                log.warn("Skipping custom order combine - customOrderDoc or headerPreview not available");
                return;
            }

            // Combine header preview + user content (sealing happens inside for final orders)
            uk.gov.hmcts.reform.prl.models.documents.Document finalDoc =
                processCustomOrderOnSubmitted(
                    authorisation,
                    caseData.getId(),
                    caseData,
                    customOrderDoc.getDocumentBinaryUrl(),
                    headerPreview.getDocumentBinaryUrl(),
                    caseDataUpdated,
                    isDraftOrder
                );

            log.info("Custom order processed: {}, isDraftOrder={}", finalDoc.getDocumentFileName(), isDraftOrder);

            // Update the appropriate collection
            // Note: caseDataUpdated may not have the collection if it was created in about-to-submit.
            // In that case, we need to read from caseData (persisted) and update caseDataUpdated.
            String orderName = getEffectiveOrderName(caseData, caseDataUpdated);
            if (isDraftOrder) {
                updateDraftOrderCollection(caseData, caseDataUpdated, finalDoc, orderName);
            } else {
                updateFinalOrderCollection(caseData, caseDataUpdated, finalDoc, orderName);
            }

            // Clean up previewOrderDoc now that we've used it
            caseDataUpdated.put("previewOrderDoc", null);

        } catch (Exception e) {
            log.error("Failed to process custom order in submitted callback", e);
        }
    }

    void updateDraftOrderCollection(CaseData caseData, Map<String, Object> caseDataUpdated,
                                    uk.gov.hmcts.reform.prl.models.documents.Document docToStore,
                                    String orderName) {
        // Follow existing pattern: read from caseData (typed), create mutable copy, put into caseDataUpdated
        List<Element<uk.gov.hmcts.reform.prl.models.DraftOrder>> originalDrafts = caseData.getDraftOrderCollection();
        if (originalDrafts == null || originalDrafts.isEmpty()) {
            log.error("draftOrderCollection is null/empty - draft should have been created in about-to-submit callback");
            return;
        }

        int originalSize = originalDrafts.size();
        log.info("Updating draftOrderCollection: original size={}", originalSize);

        // Create mutable copy
        List<Element<uk.gov.hmcts.reform.prl.models.DraftOrder>> updatedDrafts = new ArrayList<>(originalDrafts);

        // Update the first draft (newest) with combined doc and orderTypeId
        Element<uk.gov.hmcts.reform.prl.models.DraftOrder> firstElement = updatedDrafts.get(0);
        uk.gov.hmcts.reform.prl.models.DraftOrder updatedDraft = firstElement.getValue().toBuilder()
            .orderDocument(docToStore)
            .orderTypeId(orderName)
            .build();

        updatedDrafts.set(0, Element.<uk.gov.hmcts.reform.prl.models.DraftOrder>builder()
            .id(firstElement.getId())
            .value(updatedDraft)
            .build());

        // Safety check: ensure we're not overwriting with fewer drafts
        Object existingInUpdated = caseDataUpdated.get(DRAFT_ORDER_COLLECTION);
        int existingCount = existingInUpdated != null ? ((List<?>) existingInUpdated).size() : 0;
        if (updatedDrafts.size() < existingCount) {
            throw new IllegalStateException(
                String.format("Cannot update draft: would lose drafts (existing=%d, updated=%d)", existingCount, updatedDrafts.size()));
        }

        caseDataUpdated.put(DRAFT_ORDER_COLLECTION, updatedDrafts);
        log.info("Updated draftOrderCollection[0] with doc: {}, orderTypeId: {}. Total drafts: {}",
            docToStore.getDocumentFileName(), orderName, updatedDrafts.size());
    }

    void updateFinalOrderCollection(CaseData caseData, Map<String, Object> caseDataUpdated,
                                    uk.gov.hmcts.reform.prl.models.documents.Document docToStore,
                                    String orderName) {
        // Follow existing pattern: read from caseData (typed), create mutable copy, put into caseDataUpdated
        List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>> originalOrders = caseData.getOrderCollection();
        if (originalOrders == null || originalOrders.isEmpty()) {
            log.error("orderCollection is null/empty - order should have been created in about-to-submit callback");
            return;
        }

        int originalSize = originalOrders.size();
        log.info("Updating orderCollection: original size={}", originalSize);

        // Create mutable copy
        List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>> updatedOrders = new ArrayList<>(originalOrders);

        // Update the first order (newest) with combined doc and orderTypeId
        Element<uk.gov.hmcts.reform.prl.models.OrderDetails> firstElement = updatedOrders.get(0);
        uk.gov.hmcts.reform.prl.models.OrderDetails updatedOrder = firstElement.getValue().toBuilder()
            .orderDocument(docToStore)
            .orderTypeId(orderName)
            .doesOrderDocumentNeedSeal(YesOrNo.No)
            .build();

        updatedOrders.set(0, Element.<uk.gov.hmcts.reform.prl.models.OrderDetails>builder()
            .id(firstElement.getId())
            .value(updatedOrder)
            .build());

        // Safety check: ensure we're not overwriting with fewer orders
        Object existingInUpdated = caseDataUpdated.get(ORDER_COLLECTION);
        int existingCount = existingInUpdated != null ? ((List<?>) existingInUpdated).size() : 0;
        if (updatedOrders.size() < existingCount) {
            throw new IllegalStateException(
                String.format("Cannot update order: would lose orders (existing=%d, updated=%d)", existingCount, updatedOrders.size()));
        }

        caseDataUpdated.put(ORDER_COLLECTION, updatedOrders);
        log.info("Updated orderCollection[0] with doc: {}, orderTypeId: {}. Total orders: {}",
            docToStore.getDocumentFileName(), orderName, updatedOrders.size());
    }

    C21OrderOptionsEnum getC21OrderOption(Map<String, Object> caseDataMap) {
        if (caseDataMap == null) {
            return null;
        }

        Object customC21Details = caseDataMap.get(CUSTOM_C21_ORDER_DETAILS);
        if (!(customC21Details instanceof Map<?, ?> c21Map)) {
            return null;
        }

        Object orderOptions = c21Map.get("orderOptions");
        if (orderOptions == null) {
            return null;
        }

        try {
            return switch (orderOptions) {
                case String orderOptionsStr -> C21OrderOptionsEnum.getValue(orderOptionsStr);
                case C21OrderOptionsEnum c21OrderOptionsEnum -> c21OrderOptionsEnum;
                default -> null;
            };
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse C21 order option: {}", orderOptions);
            return null;
        }
    }

    String getDisplayOrderName(CaseData caseData, Map<String, Object> caseDataMap,
                               CustomOrderNameOptionsEnum selectedOption, String orderDescription) {
        if (CustomOrderNameOptionsEnum.blankOrderOrDirections == selectedOption) {
            C21OrderOptionsEnum c21Option = getC21OrderOption(caseDataMap);

            if (c21Option == null || C21OrderOptionsEnum.c21other == c21Option) {
                return StringUtils.isNotBlank(caseData.getNameOfOrder())
                    ? caseData.getNameOfOrder()
                    : "C21 - General order or directions";
            }

            return c21Option.getHeaderValue();
        }

        String formNumber = getFormNumberForOrder(selectedOption);
        String strippedDescription = stripFormNumberFromDescription(orderDescription, formNumber);

        if (formNumber != null && StringUtils.isNotBlank(strippedDescription)) {
            return formNumber + " - " + strippedDescription;
        }

        return StringUtils.defaultString(strippedDescription);
    }
}

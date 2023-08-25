package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioOtherEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;
import uk.gov.hmcts.reform.prl.exception.ManageOrderRuntimeException;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.SdoDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio.DioApplicationToApplyPermission;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.SdoDisclosureOfPapersCaseNumber;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.PartiesListGenerator;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AFTER_SECOND_GATEKEEPING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_CYMRU_NEXT_STEPS_CONTENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_NEXT_STEPS_CONTENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CROSS_EXAMINATION_EX740;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CROSS_EXAMINATION_PROHIBITION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CROSS_EXAMINATION_QUALIFIED_LEGAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_APPLICATION_TO_APPLY_PERMISSION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_CASE_REVIEW;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_PARENT_WITHCARE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_PARTICIPATION_DIRECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_PERMISSION_HEARING_DIRECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_POSITION_STATEMENT_DIRECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_RIGHT_TO_ASK;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_SAFEGUARDING_CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_SAFEGUARING_CAFCASS_CYMRU;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_NOT_NEEDED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_PAGE_NEEDED_ORDER_IDS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JOINING_INSTRUCTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOCAL_AUTHORUTY_LETTER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARENT_WITHCARE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTICIPATION_DIRECTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RIGHT_TO_ASK_COURT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SAFE_GUARDING_LETTER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_CROSS_EXAMINATION_EX741;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_DRA_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_FHDRA_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_PERMISSION_HEARING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_PERMISSION_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_SECOND_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_SETTLEMENT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_URGENT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SECTION7_DA_OCCURED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SECTION7_EDIT_CONTENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SECTION7_INTERIM_ORDERS_FACTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SPECIFIED_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SPIP_ATTENDANCE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SWANSEA_COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776", "java:S6204"})
public class DraftAnOrderService {

    private static final String SOLICITOR_ORDERS_HEARING_DETAILS = "solicitorOrdersHearingDetails";
    private static final String ORDERS_HEARING_DETAILS = "ordersHearingDetails";
    private final Time dateTime;
    private final ElementUtils elementUtils;
    private final ObjectMapper objectMapper;
    private final ManageOrderService manageOrderService;
    private final DgsService dgsService;
    private final DocumentLanguageService documentLanguageService;
    private final LocationRefDataService locationRefDataService;
    private final PartiesListGenerator partiesListGenerator;
    private final DynamicMultiSelectListService dynamicMultiSelectListService;
    private final HearingDataService hearingDataService;
    private final HearingService hearingService;

    private static final String DRAFT_ORDER_COLLECTION = "draftOrderCollection";
    private static final String MANAGE_ORDER_SDO_FAILURE
        = "Failed to update SDO order details";
    private static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";
    private static final String IS_HEARING_PAGE_NEEDED = "isHearingPageNeeded";
    private static final String IS_ORDER_CREATED_BY_SOLICITOR = "isOrderCreatedBySolicitor";

    private final WelshCourtEmail welshCourtEmail;


    public Map<String, Object> generateDraftOrderCollection(CaseData caseData, String authorisation) {
        String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
        List<Element<DraftOrder>> draftOrderList = new ArrayList<>();
        Element<DraftOrder> orderDetails = element(getCurrentOrderDetails(caseData, loggedInUserType));
        if (caseData.getDraftOrderCollection() != null) {
            draftOrderList.addAll(caseData.getDraftOrderCollection());
            draftOrderList.add(orderDetails);
        } else {
            draftOrderList.add(orderDetails);
        }
        draftOrderList.sort(Comparator.comparing(
            m -> m.getValue().getOtherDetails().getDateCreated(),
            Comparator.reverseOrder()
        ));
        return Map.of(DRAFT_ORDER_COLLECTION, draftOrderList
        );
    }

    private DraftOrder getCurrentOrderDetails(CaseData caseData, String loggedInUserType) {
        return manageOrderService.getCurrentCreateDraftOrderDetails(caseData, loggedInUserType);
    }

    public Map<String, Object> getDraftOrderDynamicList(CaseData caseData) {

        Map<String, Object> caseDataMap = new HashMap<>();
        List<Element<DraftOrder>> supportedDraftOrderList = caseData.getDraftOrderCollection().stream().filter(
                draftOrderElement -> ObjectUtils.isNotEmpty(draftOrderElement.getValue().getOrderDocument()))
            .collect(Collectors.toList());
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            supportedDraftOrderList,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        String cafcassCymruEmailAddress = welshCourtEmail
            .populateCafcassCymruEmailInManageOrders(caseData);
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        if (null != cafcassCymruEmailAddress) {
            caseDataMap.put("cafcassCymruEmail", cafcassCymruEmailAddress);
        }
        return caseDataMap;
    }

    public Map<String, Object> removeDraftOrderAndAddToFinalOrder(String authorisation, CaseData caseData, String eventId) {
        Map<String, Object> updatedCaseData = new HashMap<>();
        List<Element<DraftOrder>> draftOrderCollection = caseData.getDraftOrderCollection();
        UUID selectedOrderId = elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper);
        String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
        updatedCaseData.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        for (Element<DraftOrder> e : caseData.getDraftOrderCollection()) {
            DraftOrder draftOrder = e.getValue();
            log.info("*** eid, Selected order id {} {}", e.getId(), selectedOrderId);
            log.info("*** Equals {}", e.getId().equals(selectedOrderId));
            if (e.getId().equals(selectedOrderId)) {
                if (Yes.equals(draftOrder.getIsOrderCreatedBySolicitor())) {
                    updatedCaseData.put(
                        ORDER_HEARING_DETAILS,
                        caseData.getManageOrders().getSolicitorOrdersHearingDetails()
                    );
                } else {
                    updatedCaseData.put(ORDER_HEARING_DETAILS, caseData.getManageOrders().getOrdersHearingDetails());
                }
                updatedCaseData.put("orderUploadedAsDraftFlag", draftOrder.getIsOrderUploadedByJudgeOrAdmin());
                if (YesOrNo.Yes.equals(caseData.getDoYouWantToEditTheOrder()) || (caseData.getManageOrders() != null
                    && Yes.equals(caseData.getManageOrders().getMakeChangesToUploadedOrder()))) {
                    if (caseData.getManageOrders().getOrdersHearingDetails() == null) {
                        caseData.getManageOrders()
                            .setOrdersHearingDetails(caseData.getOrderCollection().get(0)
                                                         .getValue().getManageOrderHearingDetails());
                    }
                    if (caseData.getManageOrders().getOrdersHearingDetails() != null
                        && !Yes.equals(draftOrder.getIsOrderCreatedBySolicitor())) {
                        Hearings hearings = hearingService.getHearings(authorisation, String.valueOf(caseData.getId()));
                        caseData.getManageOrders().setOrdersHearingDetails(hearingDataService
                                                                               .getHearingDataForSelectedHearing(
                                                                                   caseData,
                                                                                   hearings
                                                                               ));
                        log.info("Updated order hearing details for docmosis");
                    }
                    draftOrder = getUpdatedDraftOrder(draftOrder, caseData, loggedInUserType, eventId);
                } else {
                    draftOrder = getDraftOrderWithUpdatedStatus(caseData, eventId, loggedInUserType, draftOrder);
                }
                log.info("***manage o h d from draft order {}", draftOrder.getManageOrderHearingDetails());
                updatedCaseData.put(
                    "orderCollection",
                    getFinalOrderCollection(authorisation, caseData, draftOrder, eventId)
                );
                draftOrderCollection.remove(
                    draftOrderCollection.indexOf(e)
                );
                break;
            }
        }

        draftOrderCollection.sort(Comparator.comparing(
            m -> m.getValue().getOtherDetails().getDateCreated(),
            Comparator.reverseOrder()
        ));
        updatedCaseData.put(DRAFT_ORDER_COLLECTION, draftOrderCollection);
        return updatedCaseData;

    }

    private List<Element<OrderDetails>> getFinalOrderCollection(String auth, CaseData caseData, DraftOrder draftOrder, String eventId) {

        List<Element<OrderDetails>> orderCollection;
        if (caseData.getOrderCollection() != null) {
            orderCollection = caseData.getOrderCollection();
        } else {
            orderCollection = new ArrayList<>();
        }
        orderCollection.add(convertDraftOrderToFinal(auth, caseData, draftOrder, eventId));
        orderCollection.sort(Comparator.comparing(m -> m.getValue().getDateCreated(), Comparator.reverseOrder()));
        return orderCollection;
    }

    private Element<OrderDetails> convertDraftOrderToFinal(String auth, CaseData caseData, DraftOrder draftOrder, String eventId) {
        GeneratedDocumentInfo generatedDocumentInfo = null;
        GeneratedDocumentInfo generatedDocumentInfoWelsh = null;
        String loggedInUserType = manageOrderService.getLoggedInUserType(auth);
        ServeOrderData serveOrderData = CaseUtils.getServeOrderData(caseData);
        SelectTypeOfOrderEnum typeOfOrder = CaseUtils.getSelectTypeOfOrder(caseData);
        OrderDetails orderDetails = OrderDetails.builder()
            .orderType(draftOrder.getOrderTypeId())
            .orderTypeId(draftOrder.getOrderTypeId())
            .typeOfOrder(typeOfOrder != null
                             ? typeOfOrder.getDisplayedValue() : null)
            .doesOrderClosesCase(caseData.getDoesOrderClosesCase())
            .serveOrderDetails(ManageOrderService.buildServeOrderDetails(serveOrderData))
            .adminNotes(caseData.getCourtAdminNotes())
            .dateCreated(draftOrder.getOtherDetails().getDateCreated())
            .judgeNotes(draftOrder.getJudgeNotes())
            .otherDetails(
                OtherOrderDetails.builder().createdBy(draftOrder.getOtherDetails().getCreatedBy())
                    .orderCreatedDate(dateTime.now().format(DateTimeFormatter.ofPattern(
                        PrlAppsConstants.D_MMM_YYYY,
                        Locale.UK
                    )))
                    .orderMadeDate(draftOrder.getDateOrderMade() != null ? draftOrder.getDateOrderMade().format(
                        DateTimeFormatter.ofPattern(
                            PrlAppsConstants.D_MMM_YYYY,
                            Locale.UK
                        )) : null)
                    .approvalDate(draftOrder.getApprovalDate() != null ? draftOrder.getDateOrderMade().format(
                        DateTimeFormatter.ofPattern(
                            PrlAppsConstants.D_MMM_YYYY,
                            Locale.UK
                        )) : null)
                    .orderRecipients(manageOrderService.getAllRecipients(caseData))
                    .status(manageOrderService.getOrderStatus(
                        draftOrder.getOrderSelectionType(),
                        loggedInUserType,
                        eventId,
                        draftOrder.getOtherDetails() != null ? draftOrder.getOtherDetails().getStatus() : null
                    ))
                    .build())
            .isTheOrderAboutChildren(draftOrder.getIsTheOrderAboutChildren())
            .isTheOrderAboutAllChildren(draftOrder.getIsTheOrderAboutAllChildren())
            .childrenList(draftOrder.getChildrenList())
            .sdoDetails(CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(draftOrder.getOrderType())
                            ? draftOrder.getSdoDetails() : null)
            .selectedHearingType(null != draftOrder.getHearingsType() ? draftOrder.getHearingsType().getValueCode() : null)
            .isOrderCreatedBySolicitor(draftOrder.getIsOrderCreatedBySolicitor())
            .manageOrderHearingDetails(draftOrder.getManageOrderHearingDetails())
            .c21OrderOptions(draftOrder.getC21OrderOptions())
            .childArrangementsOrdersToIssue(draftOrder.getChildArrangementsOrdersToIssue())
            .selectChildArrangementsOrder(draftOrder.getSelectChildArrangementsOrder())
            .childOption(draftOrder.getChildOption())
            .build();
        if (Yes.equals(draftOrder.getIsOrderUploadedByJudgeOrAdmin())) {
            orderDetails = orderDetails.toBuilder()
                .orderDocument(draftOrder.getOrderDocument())
                .build();
        } else {
            manageOrderService.populateChildrenListForDocmosis(caseData);
            caseData = caseData.toBuilder().manageOrders(
                caseData.getManageOrders().toBuilder()
                    .ordersHearingDetails(draftOrder.getManageOrderHearingDetails())
                    .build()
            ).build();
            if (caseData.getManageOrders().getOrdersHearingDetails() != null) {
                caseData = manageOrderService.filterEmptyHearingDetails(caseData);
            }
            DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
            Map<String, String> fieldMap = manageOrderService.getOrderTemplateAndFile(draftOrder.getOrderType());
            log.info("** Before final *** {}", caseData.getManageOrders().getOrdersHearingDetails());
            log.info("** Before final *** {}", caseData.getManageOrders().getSolicitorOrdersHearingDetails());
            try {
                if (documentLanguage.isGenEng()) {
                    log.info("before generating english document");
                    generatedDocumentInfo = dgsService.generateDocument(
                        auth,
                        CaseDetails.builder().caseData(caseData).build(),
                        fieldMap.get(PrlAppsConstants.FINAL_TEMPLATE_NAME)
                    );
                }
                if (documentLanguage.isGenWelsh() && fieldMap.get(PrlAppsConstants.FINAL_TEMPLATE_WELSH) != null) {
                    log.info("before generating welsh document");
                    generatedDocumentInfoWelsh = dgsService.generateDocument(
                        auth,
                        CaseDetails.builder().caseData(caseData).build(),
                        fieldMap.get(PrlAppsConstants.FINAL_TEMPLATE_WELSH)
                    );
                }
                orderDetails = orderDetails.toBuilder()
                    .orderDocument(getGeneratedDocument(generatedDocumentInfo, false, fieldMap))
                    .orderDocumentWelsh(getGeneratedDocument(
                        generatedDocumentInfoWelsh,
                        documentLanguage.isGenWelsh(),
                        fieldMap
                    ))
                    .build();
            } catch (Exception e) {
                log.error(
                    "Error while generating the final document for case {} and  order {}",
                    caseData.getId(),
                    draftOrder.getOrderType()
                );
            }
        }
        return element(orderDetails);

    }

    private Document getGeneratedDocument(GeneratedDocumentInfo generatedDocumentInfo,
                                          boolean isWelsh, Map<String, String> fieldMap) {
        if (generatedDocumentInfo != null) {
            return Document.builder().documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName(!isWelsh ? fieldMap.get(PrlAppsConstants.GENERATE_FILE_NAME)
                                      : fieldMap.get(PrlAppsConstants.WELSH_FILE_NAME)).build();
        }
        return null;
    }

    public CaseData populateCustomFields(CaseData caseData) {
        switch (caseData.getCreateSelectOrderOptions()) {
            case blankOrderOrDirections:
                return null;
            case nonMolestation:
                return null;
            default:
                return caseData;

        }
    }

    public Map<String, Object> populateDraftOrderDocument(CaseData caseData) {
        Map<String, Object> caseDataMap = new HashMap<>();
        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData);
        caseDataMap.put("previewUploadedOrder", selectedOrder.getOrderDocument());
        if (caseData.getManageOrders() != null && caseData.getManageOrders().getJudgeDirectionsToAdminAmendOrder() != null) {
            caseDataMap.put("uploadOrAmendDirectionsFromJudge", selectedOrder.getJudgeNotes());
        }
        caseDataMap.put("orderUploadedAsDraftFlag", selectedOrder.getIsOrderUploadedByJudgeOrAdmin());
        caseDataMap.put("manageOrderOptionType", selectedOrder.getOrderSelectionType());
        DocumentLanguage language = documentLanguageService.docGenerateLang(caseData);
        if (language.isGenEng()) {
            caseDataMap.put("previewDraftOrder", selectedOrder.getOrderDocument());
        }
        if (language.isGenWelsh()) {
            caseDataMap.put("previewDraftOrderWelsh", selectedOrder.getOrderDocumentWelsh());
        }
        if (selectedOrder.getJudgeNotes() != null) {
            caseDataMap.put("instructionsFromJudge", selectedOrder.getJudgeNotes());
        }
        caseDataMap.put(IS_HEARING_PAGE_NEEDED, isHearingPageNeeded(selectedOrder) ? Yes : No);
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, caseData.getCaseTypeOfApplication());
        return caseDataMap;
    }

    public Map<String, Object> populateDraftOrderCustomFields(CaseData caseData, String authorisation,
                                                              String caseEventId) {
        Map<String, Object> caseDataMap = new HashMap<>();
        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData);
        if (!CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(selectedOrder.getOrderType())) {
            caseDataMap.put("fl404CustomFields", selectedOrder.getFl404CustomFields());
            caseDataMap.put("parentName", selectedOrder.getParentName());
            caseDataMap.put("childArrangementsOrdersToIssue", selectedOrder.getChildArrangementsOrdersToIssue());
            caseDataMap.put("selectChildArrangementsOrder", selectedOrder.getSelectChildArrangementsOrder());
            caseDataMap.put("cafcassOfficeDetails", selectedOrder.getCafcassOfficeDetails());
            caseDataMap.put("appointedGuardianName", selectedOrder.getAppointedGuardianName());
            caseDataMap.put("manageOrdersFl402CourtName", selectedOrder.getManageOrdersFl402CourtName());
            caseDataMap.put("manageOrdersFl402CourtAddress", selectedOrder.getManageOrdersFl402CourtAddress());
            caseDataMap.put("manageOrdersFl402CaseNo", selectedOrder.getManageOrdersFl402CaseNo());
            caseDataMap.put("manageOrdersFl402Applicant", selectedOrder.getManageOrdersFl402Applicant());
            caseDataMap.put("manageOrdersFl402ApplicantRef", selectedOrder.getManageOrdersFl402ApplicantRef());
            caseDataMap.put("fl402HearingCourtname", selectedOrder.getFl402HearingCourtname());
            caseDataMap.put("fl402HearingCourtAddress", selectedOrder.getFl402HearingCourtAddress());
            caseDataMap.put("manageOrdersDateOfhearing", selectedOrder.getManageOrdersDateOfhearing());
            caseDataMap.put("dateOfHearingTime", selectedOrder.getDateOfHearingTime());
            caseDataMap.put("dateOfHearingTimeEstimate", selectedOrder.getDateOfHearingTimeEstimate());
            caseDataMap.put("manageOrdersCourtName", selectedOrder.getManageOrdersCourtName());
            caseDataMap.put("manageOrdersCourtAddress", selectedOrder.getManageOrdersCourtAddress());
            caseDataMap.put("manageOrdersCaseNo", selectedOrder.getManageOrdersCaseNo());
            caseDataMap.put("manageOrdersApplicant", selectedOrder.getManageOrdersApplicant());
            caseDataMap.put("manageOrdersApplicantReference", selectedOrder.getManageOrdersApplicantReference());
            caseDataMap.put("manageOrdersRespondent", selectedOrder.getManageOrdersRespondent());
            caseDataMap.put("manageOrdersRespondentReference", selectedOrder.getManageOrdersRespondentReference());
            caseDataMap.put("manageOrdersRespondentDob", selectedOrder.getManageOrdersRespondentDob());
            caseDataMap.put("manageOrdersRespondentAddress", selectedOrder.getManageOrdersRespondentAddress());
            caseDataMap.put("manageOrdersUnderTakingRepr", selectedOrder.getManageOrdersUnderTakingRepr());
            caseDataMap.put("underTakingSolicitorCounsel", selectedOrder.getUnderTakingSolicitorCounsel());
            caseDataMap.put("manageOrdersUnderTakingPerson", selectedOrder.getManageOrdersUnderTakingPerson());
            caseDataMap.put("manageOrdersUnderTakingAddress", selectedOrder.getManageOrdersUnderTakingAddress());
            caseDataMap.put("manageOrdersUnderTakingTerms", selectedOrder.getManageOrdersUnderTakingTerms());
            caseDataMap.put("manageOrdersDateOfUnderTaking", selectedOrder.getManageOrdersDateOfUnderTaking());
            caseDataMap.put("underTakingDateExpiry", selectedOrder.getUnderTakingDateExpiry());
            caseDataMap.put("underTakingExpiryTime", selectedOrder.getUnderTakingExpiryTime());
            caseDataMap.put("underTakingFormSign", selectedOrder.getUnderTakingFormSign());
            populateOrderHearingDetails(
                authorisation,
                caseData,
                caseDataMap,
                selectedOrder.getManageOrderHearingDetails(),
                caseEventId
            );
            caseDataMap.put(CASE_TYPE_OF_APPLICATION, caseData.getCaseTypeOfApplication());
            caseDataMap.put(IS_ORDER_CREATED_BY_SOLICITOR, selectedOrder.getIsOrderCreatedBySolicitor());
            caseDataMap.put("hasJudgeProvidedHearingDetails", selectedOrder.getHasJudgeProvidedHearingDetails());
            caseDataMap.put(IS_HEARING_PAGE_NEEDED, isHearingPageNeeded(selectedOrder) ? Yes : No);
        } else {
            caseDataMap.putAll(objectMapper.convertValue(selectedOrder.getSdoDetails(), Map.class));
        }
        return caseDataMap;
    }

    public Map<String, Object> populateStandardDirectionOrder(String authorisation, CaseData caseData) {
        Map<String, Object> standardDirectionOrderMap = new HashMap<>();
        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData);
        if (null != selectedOrder.getSdoDetails()) {
            StandardDirectionOrder standardDirectionOrder;
            try {
                SdoDetails updatedSdoDetails = selectedOrder.getSdoDetails().toBuilder()
                    .sdoPreamblesList(caseData.getStandardDirectionOrder().getSdoPreamblesList())
                    .sdoHearingsAndNextStepsList(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList())
                    .sdoCafcassOrCymruList(caseData.getStandardDirectionOrder().getSdoCafcassOrCymruList())
                    .sdoLocalAuthorityList(caseData.getStandardDirectionOrder().getSdoLocalAuthorityList())
                    .sdoCourtList(caseData.getStandardDirectionOrder().getSdoCourtList())
                    .sdoDocumentationAndEvidenceList(caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList())
                    .sdoFurtherList(caseData.getStandardDirectionOrder().getSdoFurtherList())
                    .sdoOtherList(caseData.getStandardDirectionOrder().getSdoOtherList())
                    .build();

                standardDirectionOrder = copyPropertiesToStandardDirectionOrder(updatedSdoDetails);
                caseData = caseData.toBuilder().standardDirectionOrder(standardDirectionOrder).build();
                standardDirectionOrderMap = objectMapper.convertValue(standardDirectionOrder, Map.class);
                populateStandardDirectionOrderDefaultFields(authorisation, caseData, standardDirectionOrderMap);
            } catch (JsonProcessingException exception) {
                throw new ManageOrderRuntimeException(MANAGE_ORDER_SDO_FAILURE, exception);
            }
        }
        return standardDirectionOrderMap;
    }

    public StandardDirectionOrder copyPropertiesToStandardDirectionOrder(SdoDetails updatedSdoDetails) throws JsonProcessingException {
        StandardDirectionOrder standardDirectionOrder;
        String sdoDetailsJson = objectMapper.writeValueAsString(updatedSdoDetails);
        standardDirectionOrder = objectMapper.readValue(sdoDetailsJson, StandardDirectionOrder.class);
        return standardDirectionOrder;
    }

    public Map<String, Object> populateCommonDraftOrderFields(String authorization, CaseData caseData, String caseEventId) {
        Map<String, Object> caseDataMap = new HashMap<>();

        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData);
        caseDataMap.put("orderName", selectedOrder.getTypeOfOrder());
        caseDataMap.put("orderType", selectedOrder.getOrderType());
        caseDataMap.put("isTheOrderByConsent", selectedOrder.getIsTheOrderByConsent());
        caseDataMap.put("dateOrderMade", selectedOrder.getDateOrderMade());
        caseDataMap.put("wasTheOrderApprovedAtHearing", selectedOrder.getWasTheOrderApprovedAtHearing());
        caseDataMap.put("judgeOrMagistrateTitle", selectedOrder.getJudgeOrMagistrateTitle());
        caseDataMap.put("judgeOrMagistratesLastName", selectedOrder.getJudgeOrMagistratesLastName());
        caseDataMap.put("justiceLegalAdviserFullName", selectedOrder.getJusticeLegalAdviserFullName());
        caseDataMap.put("magistrateLastName", selectedOrder.getMagistrateLastName());
        caseDataMap.put("isTheOrderAboutAllChildren", selectedOrder.getIsTheOrderAboutAllChildren());
        caseDataMap.put("isTheOrderAboutChildren", selectedOrder.getIsTheOrderAboutChildren());
        caseDataMap.put("childOption", (Yes.equals(selectedOrder.getIsTheOrderAboutChildren())
            || No.equals(selectedOrder.getIsTheOrderAboutAllChildren()))
            ? selectedOrder.getChildOption() : DynamicMultiSelectList.builder()
            .listItems(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).build());
        caseDataMap.put("recitalsOrPreamble", selectedOrder.getRecitalsOrPreamble());
        caseDataMap.put("orderDirections", selectedOrder.getOrderDirections());
        caseDataMap.put("furtherDirectionsIfRequired", selectedOrder.getFurtherDirectionsIfRequired());
        caseDataMap.put("furtherInformationIfRequired", selectedOrder.getFurtherInformationIfRequired());
        caseDataMap.put("childArrangementsOrdersToIssue", selectedOrder.getChildArrangementsOrdersToIssue());
        caseDataMap.put("selectChildArrangementsOrder", selectedOrder.getSelectChildArrangementsOrder());
        caseDataMap.put("cafcassOfficeDetails", selectedOrder.getCafcassOfficeDetails());
        caseDataMap.put("status", selectedOrder.getOtherDetails().getStatus());
        caseDataMap.put("reviewRequiredBy", selectedOrder.getOtherDetails().getReviewRequiredBy() != null
            ? selectedOrder.getOtherDetails().getReviewRequiredBy().getDisplayedValue() : null);

        populateOrderHearingDetails(authorization, caseData, caseDataMap,
                                    selectedOrder.getManageOrderHearingDetails(), caseEventId
        );

        caseDataMap.put(IS_ORDER_CREATED_BY_SOLICITOR, selectedOrder.getIsOrderCreatedBySolicitor());
        caseDataMap.put("hasJudgeProvidedHearingDetails", selectedOrder.getHasJudgeProvidedHearingDetails());
        caseDataMap.put(IS_HEARING_PAGE_NEEDED, isHearingPageNeeded(selectedOrder) ? Yes : No);
        caseDataMap.put("doYouWantToEditTheOrder", caseData.getDoYouWantToEditTheOrder());

        //Set existing hearingsType from draft order
        ManageOrders manageOrders = null != caseData.getManageOrders()
            ? caseData.getManageOrders().toBuilder().hearingsType(selectedOrder.getHearingsType()).build()
            : ManageOrders.builder().hearingsType(selectedOrder.getHearingsType()).build();
        caseData = caseData.toBuilder()
            .manageOrders(manageOrders)
            .build();
        //PRL-3319 - Fetch hearings dropdown
        DynamicList hearingsDynamicList = manageOrderService.populateHearingsDropdown(authorization, caseData);
        caseDataMap.put("hearingsType", hearingsDynamicList);
        log.info("inside populateCommonDraftOrderFields ==>" + caseDataMap);
        return caseDataMap;
    }

    public void populateOrderHearingDetails(String authorization, CaseData caseData, Map<String, Object> caseDataMap,
                                            List<Element<HearingData>> manageOrderHearingDetail,
                                            String caseEventId) {
        String caseReferenceNumber = String.valueOf(caseData.getId());
        if (CollectionUtils.isEmpty(manageOrderHearingDetail)) {
            Hearings hearings = hearingService.getHearings(authorization, caseReferenceNumber);
            HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
                hearingDataService.populateHearingDynamicLists(authorization, caseReferenceNumber, caseData, hearings);
            HearingData hearingData = hearingDataService.generateHearingData(
                hearingDataPrePopulatedDynamicLists, caseData);
            manageOrderHearingDetail = ElementUtils.wrapElements(hearingData);
        }
        if (!Event.DRAFT_AN_ORDER.getId().equalsIgnoreCase(caseEventId)) {
            caseDataMap.put(SOLICITOR_ORDERS_HEARING_DETAILS, manageOrderHearingDetail);
        }
        caseDataMap.put(ORDERS_HEARING_DETAILS, manageOrderHearingDetail);
    }

    public boolean isHearingPageNeeded(DraftOrder selectedOrder) {
        if (null != selectedOrder && !StringUtils.isEmpty(String.valueOf(selectedOrder.getOrderType()))) {
            if (CreateSelectOrderOptionsEnum.blankOrderOrDirections.equals(selectedOrder.getOrderType())) {
                return C21OrderOptionsEnum.c21other.equals(selectedOrder.getC21OrderOptions());
            }
            return Arrays.stream(HEARING_PAGE_NEEDED_ORDER_IDS)
                .anyMatch(orderId -> orderId.equalsIgnoreCase(String.valueOf(selectedOrder.getOrderType())));
        }
        return false;
    }

    public DraftOrder getSelectedDraftOrderDetails(CaseData caseData) {
        UUID orderId = elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper);
        return caseData.getDraftOrderCollection().stream()
            .filter(element -> element.getId().equals(orderId))
            .map(Element::getValue)
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException("Could not find order"));
    }

    public Map<String, Object> updateDraftOrderCollection(CaseData caseData, String authorisation, String eventId) {

        List<Element<DraftOrder>> draftOrderCollection = caseData.getDraftOrderCollection();
        String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
        UUID selectedOrderId = elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper);
        for (Element<DraftOrder> e : caseData.getDraftOrderCollection()) {
            DraftOrder draftOrder = e.getValue();
            if (e.getId().equals(selectedOrderId)) {
                if (YesOrNo.Yes.equals(caseData.getDoYouWantToEditTheOrder()) || (caseData.getManageOrders() != null
                    && Yes.equals(caseData.getManageOrders().getMakeChangesToUploadedOrder()))) {
                    if (caseData.getManageOrders().getOrdersHearingDetails() != null
                        && !Yes.equals(draftOrder.getIsOrderCreatedBySolicitor())) {
                        Hearings hearings = hearingService.getHearings(authorisation, String.valueOf(caseData.getId()));
                        caseData.getManageOrders().setOrdersHearingDetails(hearingDataService
                                                                               .getHearingDataForSelectedHearing(
                                                                                   caseData,
                                                                                   hearings
                                                                               ));
                        log.info("Updated order hearing details for docmosis");
                    }
                    draftOrder = getUpdatedDraftOrder(draftOrder, caseData, loggedInUserType, eventId);
                } else {
                    draftOrder = getDraftOrderWithUpdatedStatus(caseData, eventId, loggedInUserType, draftOrder);
                }
                draftOrderCollection.set(
                    draftOrderCollection.indexOf(e),
                    element(draftOrder)
                );
                break;
            }
        }
        draftOrderCollection.sort(Comparator.comparing(
            m -> m.getValue().getOtherDetails().getDateCreated(),
            Comparator.reverseOrder()
        ));
        return Map.of(DRAFT_ORDER_COLLECTION, draftOrderCollection
        );
    }

    private DraftOrder getDraftOrderWithUpdatedStatus(CaseData caseData, String eventId, String loggedInUserType, DraftOrder draftOrder) {
        return draftOrder.toBuilder()
            .judgeNotes(caseData.getJudgeDirectionsToAdmin())
            .adminNotes(caseData.getCourtAdminNotes())
            .otherDetails(draftOrder.getOtherDetails().toBuilder()
                              .status(manageOrderService.getOrderStatus(
                                  draftOrder.getOrderSelectionType(),
                                  loggedInUserType,
                                  eventId,
                                  draftOrder.getOtherDetails() != null ? draftOrder.getOtherDetails().getStatus() : null
                              ))
                              .build())
            .build();
    }

    private DraftOrder getUpdatedDraftOrder(DraftOrder draftOrder, CaseData caseData, String loggedInUserType, String eventId) {
        Document orderDocumentEng;
        Document orderDocumentWelsh = null;
        if (YesOrNo.Yes.equals(caseData.getManageOrders().getMakeChangesToUploadedOrder())) {
            orderDocumentEng = caseData.getManageOrders().getEditedUploadOrderDoc();
        } else {
            if (YesOrNo.Yes.equals(caseData.getDoYouWantToEditTheOrder())) {
                orderDocumentEng = caseData.getPreviewOrderDoc();
                orderDocumentWelsh = caseData.getPreviewOrderDocWelsh();
            } else {
                orderDocumentEng = draftOrder.getOrderDocument();
                orderDocumentWelsh = draftOrder.getOrderDocumentWelsh();
            }
        }
        SelectTypeOfOrderEnum typeOfOrder = CaseUtils.getSelectTypeOfOrder(caseData);
        return DraftOrder.builder().orderType(draftOrder.getOrderType())
            .typeOfOrder(typeOfOrder != null ? typeOfOrder.getDisplayedValue() : null)
            .orderTypeId(null != draftOrder.getOrderTypeId()
                             ? draftOrder.getOrderTypeId() : manageOrderService.getSelectedOrderInfoForUpload(caseData))
            .orderDocument(orderDocumentEng)
            .orderDocumentWelsh(orderDocumentWelsh)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy(caseData.getJudgeOrMagistratesLastName())
                              .dateCreated(draftOrder.getOtherDetails() != null ? draftOrder.getOtherDetails().getDateCreated() : dateTime.now())
                              .status(manageOrderService.getOrderStatus(
                                  draftOrder.getOrderSelectionType(),
                                  loggedInUserType,
                                  eventId,
                                  draftOrder.getOtherDetails() != null ? draftOrder.getOtherDetails().getStatus() : null
                              )).build())
            .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
            .wasTheOrderApprovedAtHearing(caseData.getWasTheOrderApprovedAtHearing())
            .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle())
            .judgeOrMagistratesLastName(caseData.getJudgeOrMagistratesLastName())
            .justiceLegalAdviserFullName(caseData.getJusticeLegalAdviserFullName())
            .magistrateLastName(caseData.getMagistrateLastName())
            .recitalsOrPreamble(caseData.getManageOrders().getRecitalsOrPreamble())
            .isTheOrderAboutAllChildren(caseData.getManageOrders().getIsTheOrderAboutAllChildren())
            .isTheOrderAboutChildren(caseData.getManageOrders().getIsTheOrderAboutChildren())
            .childOption(manageOrderService.getChildOption(caseData))
            .orderDirections(caseData.getManageOrders().getOrderDirections())
            .furtherDirectionsIfRequired(caseData.getManageOrders().getFurtherDirectionsIfRequired())
            .furtherInformationIfRequired(caseData.getManageOrders().getFurtherInformationIfRequired())
            .fl404CustomFields(caseData.getManageOrders().getFl404CustomFields())
            .manageOrdersCourtName(caseData.getManageOrders().getManageOrdersCourtName())
            .manageOrdersCourtAddress(caseData.getManageOrders().getManageOrdersCourtAddress())
            .manageOrdersCaseNo(caseData.getManageOrders().getManageOrdersCaseNo())
            .manageOrdersApplicant(caseData.getManageOrders().getManageOrdersApplicant())
            .manageOrdersApplicantReference(caseData.getManageOrders().getManageOrdersApplicantReference())
            .manageOrdersRespondent(caseData.getManageOrders().getManageOrdersRespondent())
            .manageOrdersRespondentReference(caseData.getManageOrders().getManageOrdersRespondentReference())
            .manageOrdersRespondentDob(caseData.getManageOrders().getManageOrdersRespondentDob())
            .manageOrdersRespondentAddress(caseData.getManageOrders().getManageOrdersRespondentAddress())
            .manageOrdersUnderTakingRepr(caseData.getManageOrders().getManageOrdersUnderTakingRepr())
            .underTakingSolicitorCounsel(caseData.getManageOrders().getUnderTakingSolicitorCounsel())
            .manageOrdersUnderTakingPerson(caseData.getManageOrders().getManageOrdersUnderTakingPerson())
            .manageOrdersUnderTakingAddress(caseData.getManageOrders().getManageOrdersUnderTakingAddress())
            .manageOrdersUnderTakingTerms(caseData.getManageOrders().getManageOrdersUnderTakingTerms())
            .manageOrdersDateOfUnderTaking(caseData.getManageOrders().getManageOrdersDateOfUnderTaking())
            .underTakingDateExpiry(caseData.getManageOrders().getUnderTakingDateExpiry())
            .underTakingExpiryTime(caseData.getManageOrders().getUnderTakingExpiryTime())
            .underTakingFormSign(caseData.getManageOrders().getUnderTakingFormSign())
            .judgeNotes(caseData.getJudgeDirectionsToAdmin())
            .parentName(caseData.getManageOrders().getParentName())
            .dateOrderMade(caseData.getDateOrderMade() != null ? caseData.getDateOrderMade() : draftOrder.getDateOrderMade())
            .childArrangementsOrdersToIssue(caseData.getManageOrders().getChildArrangementsOrdersToIssue())
            .selectChildArrangementsOrder(caseData.getManageOrders().getSelectChildArrangementsOrder())
            .cafcassOfficeDetails(caseData.getManageOrders().getCafcassOfficeDetails())
            .appointedGuardianName(caseData.getAppointedGuardianName())
            .manageOrdersDateOfhearing(caseData.getManageOrders().getManageOrdersDateOfhearing())
            .manageOrdersFl402CaseNo(caseData.getManageOrders().getManageOrdersFl402CaseNo())
            .manageOrdersFl402ApplicantRef(caseData.getManageOrders().getManageOrdersFl402ApplicantRef())
            .manageOrdersFl402Applicant(caseData.getManageOrders().getManageOrdersFl402Applicant())
            .manageOrdersFl402CourtAddress(caseData.getManageOrders().getManageOrdersFl402CourtAddress())
            .manageOrdersFl402CourtName(caseData.getManageOrders().getManageOrdersFl402CourtName())
            .dateOfHearingTime(caseData.getManageOrders().getDateOfHearingTime())
            .dateOfHearingTimeEstimate(caseData.getManageOrders().getDateOfHearingTimeEstimate())
            .fl402HearingCourtname(caseData.getManageOrders().getFl402HearingCourtname())
            .fl402HearingCourtAddress(caseData.getManageOrders().getFl402HearingCourtAddress())
            .orderSelectionType(draftOrder.getOrderSelectionType())
            .orderCreatedBy(loggedInUserType)
            .isOrderUploadedByJudgeOrAdmin(draftOrder.getIsOrderUploadedByJudgeOrAdmin())
            .approvalDate(draftOrder.getApprovalDate())
            .manageOrderHearingDetails(YesOrNo.Yes.equals(draftOrder.getIsOrderCreatedBySolicitor())
                                           ? caseData.getManageOrders().getSolicitorOrdersHearingDetails()
                                           : caseData.getManageOrders().getOrdersHearingDetails())
            .childrenList(manageOrderService.getSelectedChildInfoFromMangeOrder(caseData))
            .sdoDetails(CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(draftOrder.getOrderType())
                            ? manageOrderService.copyPropertiesToSdoDetails(caseData) : null)
            .hasJudgeProvidedHearingDetails(caseData.getManageOrders().getHasJudgeProvidedHearingDetails())
            .isOrderCreatedBySolicitor(draftOrder.getIsOrderCreatedBySolicitor())
            .hearingsType(caseData.getManageOrders().getHearingsType())
            .c21OrderOptions(caseData.getManageOrders().getC21OrderOptions())
            .build();
    }

    public CaseData generateDocument(@RequestBody CallbackRequest callbackRequest, CaseData caseData) {

        if (callbackRequest
            .getCaseDetailsBefore() != null && callbackRequest
            .getCaseDetailsBefore().getData().get(COURT_NAME) != null) {
            caseData.setCourtName(callbackRequest
                                      .getCaseDetailsBefore().getData().get(COURT_NAME).toString());
        }
        if (!C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            FL404 fl404CustomFields = caseData.getManageOrders().getFl404CustomFields();
            if (fl404CustomFields != null) {
                fl404CustomFields = fl404CustomFields.toBuilder().fl404bApplicantName(String.format(
                        PrlAppsConstants.FORMAT,
                        caseData.getApplicantsFL401().getFirstName(),
                        caseData.getApplicantsFL401().getLastName()
                    ))
                    .fl404bCourtName(caseData.getCourtName())
                    .fl404bRespondentName(String.format(
                        PrlAppsConstants.FORMAT,
                        caseData.getRespondentsFL401().getFirstName(),
                        caseData.getRespondentsFL401().getLastName()
                    )).build();
                if (ofNullable(caseData.getRespondentsFL401().getAddress()).isPresent()) {
                    fl404CustomFields = fl404CustomFields.toBuilder()
                        .fl404bRespondentAddress(caseData.getRespondentsFL401().getAddress()).build();
                }
                if (ofNullable(caseData.getRespondentsFL401().getDateOfBirth()).isPresent()) {
                    fl404CustomFields = fl404CustomFields.toBuilder()
                        .fl404bRespondentDob(caseData.getRespondentsFL401().getDateOfBirth()).build();
                }
            }
            caseData = caseData.toBuilder()
                .standardDirectionOrder(caseData.getStandardDirectionOrder())
                .manageOrders(ManageOrders.builder()
                                  .recitalsOrPreamble(caseData.getManageOrders().getRecitalsOrPreamble())
                                  .isCaseWithdrawn(caseData.getManageOrders().getIsCaseWithdrawn())
                                  .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
                                  .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle())
                                  .orderDirections(caseData.getManageOrders().getOrderDirections())
                                  .furtherDirectionsIfRequired(caseData.getManageOrders().getFurtherDirectionsIfRequired())
                                  .furtherInformationIfRequired(caseData.getManageOrders().getFurtherInformationIfRequired())
                                  .fl404CustomFields(fl404CustomFields)
                                  .manageOrdersFl402CourtName(caseData.getManageOrders().getManageOrdersFl402CourtName())
                                  .manageOrdersFl402CourtAddress(caseData.getManageOrders().getManageOrdersFl402CourtAddress())
                                  .manageOrdersFl402CaseNo(caseData.getManageOrders().getManageOrdersCaseNo())
                                  .manageOrdersFl402Applicant(caseData.getManageOrders().getManageOrdersFl402Applicant())
                                  .manageOrdersFl402ApplicantRef(caseData.getManageOrders().getManageOrdersFl402ApplicantRef())
                                  .fl402HearingCourtname(caseData.getManageOrders().getFl402HearingCourtname())
                                  .fl402HearingCourtAddress(caseData.getManageOrders().getFl402HearingCourtAddress())
                                  .manageOrdersDateOfhearing(caseData.getManageOrders().getManageOrdersDateOfhearing())
                                  .dateOfHearingTime(caseData.getManageOrders().getDateOfHearingTime())
                                  .dateOfHearingTimeEstimate(caseData.getManageOrders().getDateOfHearingTimeEstimate())
                                  .manageOrdersCourtName(caseData.getManageOrders().getManageOrdersCourtName())
                                  .manageOrdersCourtAddress(caseData.getManageOrders().getManageOrdersCourtAddress())
                                  .manageOrdersCaseNo(caseData.getManageOrders().getManageOrdersCaseNo())
                                  .manageOrdersApplicant(caseData.getManageOrders().getManageOrdersApplicant())
                                  .manageOrdersApplicantReference(caseData.getManageOrders().getManageOrdersApplicantReference())
                                  .manageOrdersRespondent(caseData.getManageOrders().getManageOrdersRespondent())
                                  .manageOrdersRespondentReference(caseData.getManageOrders().getManageOrdersRespondentReference())
                                  .manageOrdersRespondentDob(caseData.getManageOrders().getManageOrdersRespondentDob())
                                  .manageOrdersRespondentAddress(caseData.getManageOrders().getManageOrdersRespondentAddress())
                                  .manageOrdersUnderTakingRepr(caseData.getManageOrders().getManageOrdersUnderTakingRepr())
                                  .underTakingSolicitorCounsel(caseData.getManageOrders().getUnderTakingSolicitorCounsel())
                                  .manageOrdersUnderTakingPerson(caseData.getManageOrders().getManageOrdersUnderTakingPerson())
                                  .manageOrdersUnderTakingAddress(caseData.getManageOrders().getManageOrdersUnderTakingAddress())
                                  .manageOrdersUnderTakingTerms(caseData.getManageOrders().getManageOrdersUnderTakingTerms())
                                  .manageOrdersDateOfUnderTaking(caseData.getManageOrders().getManageOrdersDateOfUnderTaking())
                                  .underTakingDateExpiry(caseData.getManageOrders().getUnderTakingDateExpiry())
                                  .underTakingExpiryTime(caseData.getManageOrders().getUnderTakingExpiryTime())
                                  .underTakingFormSign(caseData.getManageOrders().getUnderTakingFormSign())
                                  .hearingsType(caseData.getManageOrders().getHearingsType())
                                  .c21OrderOptions(caseData.getManageOrders().getC21OrderOptions())
                                  .isTheOrderAboutChildren(caseData.getManageOrders().getIsTheOrderAboutChildren())
                                  .childOption(manageOrderService.getChildOption(caseData))
                                  .ordersHearingDetails(caseData.getManageOrders().getOrdersHearingDetails())
                                  .typeOfC21Order(null != caseData.getManageOrders().getC21OrderOptions()
                                                      ? caseData.getManageOrders().getC21OrderOptions().getDisplayedValue() : null)
                                  .build()).build();
        } else {
            caseData = caseData.toBuilder()
                .appointedGuardianName(caseData.getAppointedGuardianName())
                .dateOrderMade(caseData.getDateOrderMade())
                .standardDirectionOrder(caseData.getStandardDirectionOrder())
                .manageOrders(ManageOrders.builder()
                                  .parentName(caseData.getManageOrders().getParentName())
                                  .recitalsOrPreamble(caseData.getManageOrders().getRecitalsOrPreamble())
                                  .isCaseWithdrawn(caseData.getManageOrders().getIsCaseWithdrawn())
                                  .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
                                  .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle())
                                  .orderDirections(caseData.getManageOrders().getOrderDirections())
                                  .furtherDirectionsIfRequired(caseData.getManageOrders().getFurtherDirectionsIfRequired())
                                  .furtherInformationIfRequired(caseData.getManageOrders().getFurtherInformationIfRequired())
                                  .childArrangementsOrdersToIssue(caseData.getManageOrders().getChildArrangementsOrdersToIssue())
                                  .selectChildArrangementsOrder(caseData.getManageOrders().getSelectChildArrangementsOrder())
                                  .fl404CustomFields(caseData.getManageOrders().getFl404CustomFields())
                                  .hearingsType(caseData.getManageOrders().getHearingsType())
                                  .c21OrderOptions(caseData.getManageOrders().getC21OrderOptions())
                                  .typeOfC21Order(caseData.getManageOrders().getC21OrderOptions() != null
                                                      ? caseData.getManageOrders().getC21OrderOptions().getDisplayedValue() : null)
                                  .isTheOrderAboutAllChildren(caseData.getManageOrders().getIsTheOrderAboutAllChildren())
                                  .ordersHearingDetails(caseData.getManageOrders().getOrdersHearingDetails())
                                  .childOption(manageOrderService.getChildOption(caseData))
                                  .build()).build();
        }
        return caseData;
    }

    public static boolean checkStandingOrderOptionsSelected(CaseData caseData) {
        return !(caseData.getStandardDirectionOrder() != null
            && caseData.getStandardDirectionOrder().getSdoPreamblesList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoCafcassOrCymruList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoLocalAuthorityList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoCourtList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoOtherList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoFurtherList().isEmpty());
    }

    public void populateStandardDirectionOrderDefaultFields(String authorisation, CaseData caseData, Map<String, Object> caseDataUpdated) {
        populateRightToAskCourt(caseData, caseDataUpdated);
        populateHearingAndNextStepsText(caseData, caseDataUpdated);
        populateCafcassNextSteps(caseData, caseDataUpdated);
        populateCafcassCymruNextSteps(caseData, caseDataUpdated);
        populateSection7ChildImpactAnalysis(caseData, caseDataUpdated);
        populateCourtText(caseData, caseDataUpdated);
        populateDocumentAndEvidenceText(caseData, caseDataUpdated);
        populateCrossExaminationProhibition(caseData, caseDataUpdated);
        populateParentWithCare(caseData, caseDataUpdated);
        List<DynamicListElement> courtList = getCourtDynamicList(authorisation);
        populateCourtDynamicList(courtList, caseDataUpdated, caseData);
        populateLocalAuthorityDetails(caseData, caseDataUpdated);

        if (caseData.getStandardDirectionOrder().getSdoInstructionsFilingPartiesDynamicList() == null
            || CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoInstructionsFilingPartiesDynamicList().getListItems())) {
            DynamicList partiesList = partiesListGenerator.buildPartiesList(caseData, courtList);
            caseDataUpdated.put("sdoInstructionsFilingPartiesDynamicList", partiesList);
        }
        populateHearingDetails(authorisation, caseData, caseDataUpdated);
    }

    private void populateLocalAuthorityDetails(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (SWANSEA_COURT_NAME.equalsIgnoreCase(caseData.getCourtName())) {
            caseDataUpdated.put("sdoLocalAuthorityName", "City and County of Swansea");
        }
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoLocalAuthorityTextArea())) {
            caseDataUpdated.put(
                "sdoLocalAuthorityTextArea",
                LOCAL_AUTHORUTY_LETTER
            );
        }
    }

    private static void populateCrossExaminationProhibition(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoCrossExaminationEditContent())) {
            caseDataUpdated.put(
                "sdoCrossExaminationEditContent",
                CROSS_EXAMINATION_PROHIBITION
            );
        }
    }

    private static void populateSection7ChildImpactAnalysis(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoSection7EditContent())) {
            caseDataUpdated.put(
                "sdoSection7EditContent",
                SECTION7_EDIT_CONTENT
            );
        }
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoSection7FactsEditContent())) {
            caseDataUpdated.put(
                "sdoSection7FactsEditContent",
                SECTION7_INTERIM_ORDERS_FACTS
            );
        }
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoSection7daOccuredEditContent())) {
            caseDataUpdated.put(
                "sdoSection7daOccuredEditContent",
                SECTION7_DA_OCCURED
            );
        }
    }

    private static void populateCafcassNextSteps(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoCafcassNextStepEditContent())) {
            caseDataUpdated.put(
                "sdoCafcassNextStepEditContent",
                CAFCASS_NEXT_STEPS_CONTENT
            );
        }
    }

    private static void populateCafcassCymruNextSteps(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoCafcassCymruNextStepEditContent())) {
            caseDataUpdated.put(
                "sdoCafcassCymruNextStepEditContent",
                CAFCASS_CYMRU_NEXT_STEPS_CONTENT
            );
        }
    }

    private static void populateRightToAskCourt(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoRightToAskCourt())) {
            caseDataUpdated.put(
                "sdoRightToAskCourt",
                RIGHT_TO_ASK_COURT
            );
        }
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoAfterSecondGatekeeping())) {
            caseDataUpdated.put(
                "sdoAfterSecondGatekeeping",
                AFTER_SECOND_GATEKEEPING
            );
        }
    }

    private static void populateParentWithCare(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoParentWithCare())) {
            caseDataUpdated.put(
                "sdoParentWithCare",
                PARENT_WITHCARE
            );
        }
    }

    private void populateHearingDetails(String authorisation, CaseData caseData, Map<String, Object> caseDataUpdated) {
        Hearings hearings = hearingService.getHearings(authorisation, String.valueOf(caseData.getId()));
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            hearingDataService.populateHearingDynamicLists(
                authorisation,
                String.valueOf(caseData.getId()),
                caseData,
                hearings
            );
        HearingData hearingData = hearingDataService.generateHearingData(
            hearingDataPrePopulatedDynamicLists, caseData);
        populateHearingData(
            caseDataUpdated,
            hearingData,
            caseData.getStandardDirectionOrder().getSdoPermissionHearingDetails(),
            SDO_PERMISSION_HEARING_DETAILS
        );
        populateHearingData(
            caseDataUpdated,
            hearingData,
            caseData.getStandardDirectionOrder().getSdoSecondHearingDetails(),
            SDO_SECOND_HEARING_DETAILS
        );
        populateHearingData(
            caseDataUpdated,
            hearingData,
            caseData.getStandardDirectionOrder().getSdoUrgentHearingDetails(),
            SDO_URGENT_HEARING_DETAILS
        );
        populateHearingData(
            caseDataUpdated,
            hearingData,
            caseData.getStandardDirectionOrder().getSdoFhdraHearingDetails(),
            SDO_FHDRA_HEARING_DETAILS
        );
        populateHearingData(
            caseDataUpdated,
            hearingData,
            caseData.getStandardDirectionOrder().getSdoDraHearingDetails(),
            SDO_DRA_HEARING_DETAILS
        );
        populateHearingData(
            caseDataUpdated,
            hearingData,
            caseData.getStandardDirectionOrder().getSdoSettlementHearingDetails(),
            SDO_SETTLEMENT_HEARING_DETAILS
        );
    }

    private static void populateHearingData(Map<String, Object> caseDataUpdated, HearingData hearingData,
                                            HearingData existingHearingData, String hearingKey) {
        if (existingHearingData == null
            || existingHearingData.getHearingDateConfirmOptionEnum() == null) {
            caseDataUpdated.put(hearingKey, hearingData);
        } else {
            caseDataUpdated.put(SDO_PERMISSION_HEARING_DETAILS, existingHearingData);
        }
    }

    private static void populateDocumentAndEvidenceText(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoSpecifiedDocuments())) {
            caseDataUpdated.put(
                "sdoSpecifiedDocuments",
                SPECIFIED_DOCUMENTS
            );
        }
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoSpipAttendance())) {
            caseDataUpdated.put(
                "sdoSpipAttendance",
                SPIP_ATTENDANCE
            );
        }
    }

    private static void populateCourtText(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoCrossExaminationEx740())) {
            caseDataUpdated.put("sdoCrossExaminationEx740", CROSS_EXAMINATION_EX740);
        }
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoCrossExaminationQualifiedLegal())) {
            caseDataUpdated.put("sdoCrossExaminationQualifiedLegal", CROSS_EXAMINATION_QUALIFIED_LEGAL);
        }
    }

    private static void populateHearingAndNextStepsText(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoNextStepsAfterSecondGK())) {
            caseDataUpdated.put("sdoNextStepsAfterSecondGK", SAFE_GUARDING_LETTER);
        }
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoHearingNotNeeded())) {
            caseDataUpdated.put("sdoHearingNotNeeded", HEARING_NOT_NEEDED);
        }
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoParticipationDirections())) {
            caseDataUpdated.put("sdoParticipationDirections", PARTICIPATION_DIRECTIONS);
        }
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoJoiningInstructionsForRH())) {
            caseDataUpdated.put("sdoJoiningInstructionsForRH", JOINING_INSTRUCTIONS);
        }
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoUpdateContactDetails())) {
            caseDataUpdated.put("sdoUpdateContactDetails", UPDATE_CONTACT_DETAILS);
        }
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoPermissionHearingDirections())) {
            caseDataUpdated.put("sdoPermissionHearingDirections", SDO_PERMISSION_HEARING);
        }
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoCrossExaminationEx741())) {
            caseDataUpdated.put("sdoCrossExaminationEx741", SDO_CROSS_EXAMINATION_EX741);
        }
    }

    private void populateCourtDynamicList(List<DynamicListElement> courtList, Map<String, Object> caseDataUpdated, CaseData caseData) {
        DynamicList courtDynamicList = DynamicList.builder().value(DynamicListElement.EMPTY).listItems(courtList)
            .build();
        if (caseData.getStandardDirectionOrder().getSdoTransferApplicationCourtDynamicList() == null
            || CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoTransferApplicationCourtDynamicList().getListItems())) {
            caseDataUpdated.put(
                "sdoTransferApplicationCourtDynamicList", courtDynamicList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoDisclosureOfPapersCaseNumbers())) {
            List<Element<SdoDisclosureOfPapersCaseNumber>> elementList = new ArrayList<>();
            SdoDisclosureOfPapersCaseNumber sdoDisclosureOfPapersCaseNumbers = SdoDisclosureOfPapersCaseNumber.builder()
                .sdoDisclosureCourtList(courtDynamicList)
                .build();
            elementList.add(element(sdoDisclosureOfPapersCaseNumbers));
            caseDataUpdated.put(
                "sdoDisclosureOfPapersCaseNumbers", elementList);
        }
    }

    private List<DynamicListElement> getCourtDynamicList(String authorisation) {
        return locationRefDataService.getCourtLocations(authorisation);
    }

    public static boolean checkDirectionOnIssueOptionsSelected(CaseData caseData) {
        return !(caseData.getDirectionOnIssue().getDioPreamblesList().isEmpty()
            && caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().isEmpty()
            && caseData.getDirectionOnIssue().getDioCafcassOrCymruList().isEmpty()
            && caseData.getDirectionOnIssue().getDioLocalAuthorityList().isEmpty()
            && caseData.getDirectionOnIssue().getDioCourtList().isEmpty()
            && caseData.getDirectionOnIssue().getDioOtherList().isEmpty());
    }

    public void populateDirectionOnIssueFields(String authorisation, CaseData caseData, Map<String, Object> caseDataUpdated) {

        if (!caseData.getDirectionOnIssue().getDioPreamblesList().isEmpty()
            && caseData.getDirectionOnIssue().getDioPreamblesList().contains(DioPreamblesEnum.rightToAskCourt)) {
            caseDataUpdated.put("dioRightToAskCourt", DIO_RIGHT_TO_ASK);
        }
        if (!caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().isEmpty()
            && caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().contains(
            DioHearingsAndNextStepsEnum.caseReviewAtSecondGateKeeping)) {
            caseDataUpdated.put("dioCaseReviewAtSecondGateKeeping", DIO_CASE_REVIEW);
        }
        if (!caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().isEmpty()
            && caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().contains(
            DioHearingsAndNextStepsEnum.updateContactDetails)) {
            caseDataUpdated.put("dioUpdateContactDetails", DIO_UPDATE_CONTACT_DETAILS);
        }
        if (!caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().isEmpty()
            && caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().contains(
            DioHearingsAndNextStepsEnum.participationDirections)) {
            caseDataUpdated.put("dioParticipationDirections", DIO_PARTICIPATION_DIRECTION);
        }
        if (!caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().isEmpty()
            && caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().contains(
            DioHearingsAndNextStepsEnum.permissionHearing)) {
            caseDataUpdated.put("dioPermissionHearingDirections", DIO_PERMISSION_HEARING_DIRECTION);
        }
        if (!caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().isEmpty()
            && caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().contains(
            DioHearingsAndNextStepsEnum.positionStatement)) {
            caseDataUpdated.put("dioPositionStatementDetails", DIO_POSITION_STATEMENT_DIRECTION);
        }
        if (!caseData.getDirectionOnIssue().getDioCafcassOrCymruList().isEmpty()
            && caseData.getDirectionOnIssue().getDioCafcassOrCymruList().contains(
            DioCafcassOrCymruEnum.cafcassSafeguarding)) {
            caseDataUpdated.put("dioCafcassSafeguardingIssue", DIO_SAFEGUARDING_CAFCASS);
        }
        if (!caseData.getDirectionOnIssue().getDioCafcassOrCymruList().isEmpty()
            && caseData.getDirectionOnIssue().getDioCafcassOrCymruList().contains(
            DioCafcassOrCymruEnum.cafcassCymruSafeguarding)) {
            caseDataUpdated.put("dioCafcassCymruSafeguardingIssue", DIO_SAFEGUARING_CAFCASS_CYMRU);
        }
        if (!caseData.getDirectionOnIssue().getDioOtherList().isEmpty()
            && caseData.getDirectionOnIssue().getDioOtherList().contains(
            DioOtherEnum.parentWithCare)) {
            caseDataUpdated.put(
                "dioParentWithCare", DIO_PARENT_WITHCARE);
        }
        if (!caseData.getDirectionOnIssue().getDioOtherList().isEmpty()
            && caseData.getDirectionOnIssue().getDioOtherList().contains(
            DioOtherEnum.applicationToApplyPermission)) {

            List<Element<DioApplicationToApplyPermission>> dioApplicationToApplyPermissionList = new ArrayList<>();
            DioApplicationToApplyPermission dioApplicationToApplyPermission = DioApplicationToApplyPermission.builder()
                .applyPermissionToEditSection(DIO_APPLICATION_TO_APPLY_PERMISSION)
                .build();

            dioApplicationToApplyPermissionList.add(element(dioApplicationToApplyPermission));
            caseDataUpdated.put(
                "dioApplicationToApplyPermission", dioApplicationToApplyPermissionList);

        }

        List<DynamicListElement> courtList = getCourtDynamicList(authorisation);
        populateDioCourtDynamicList(courtList, caseDataUpdated);

    }

    private void populateDioCourtDynamicList(List<DynamicListElement> courtList, Map<String, Object> caseDataUpdated) {
        DynamicList courtDynamicList = DynamicList.builder().value(DynamicListElement.EMPTY).listItems(courtList)
            .build();
        caseDataUpdated.put(
            "dioFhdraCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "dioPermissionHearingCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "dioTransferApplicationCourtDynamicList", courtDynamicList);

        List<Element<SdoDisclosureOfPapersCaseNumber>> elementList = new ArrayList<>();
        SdoDisclosureOfPapersCaseNumber sdoDisclosureOfPapersCaseNumbers = SdoDisclosureOfPapersCaseNumber.builder()
            .sdoDisclosureCourtList(courtDynamicList)
            .build();
        elementList.add(element(sdoDisclosureOfPapersCaseNumbers));
        caseDataUpdated.put(
            "dioDisclosureOfPapersCaseNumbers", elementList);
    }

    public Map<String, Object> getDraftOrderInfo(String authorisation, CaseData caseData) throws Exception {
        DraftOrder draftOrder = getSelectedDraftOrderDetails(caseData);
        Map<String, Object> caseDataMap = getDraftOrderData(authorisation, caseData, draftOrder);
        caseDataMap.put(IS_ORDER_CREATED_BY_SOLICITOR, draftOrder.getIsOrderCreatedBySolicitor());
        caseDataMap.put(IS_HEARING_PAGE_NEEDED, isHearingPageNeeded(draftOrder) ? Yes : No);
        log.info(
            "is getDraftOrderInfo in isOrderCreatedBySolicitor:::{}::::::: isHearingPageNeeded :::::{}",
            caseDataMap.get(IS_ORDER_CREATED_BY_SOLICITOR), caseDataMap.get(IS_HEARING_PAGE_NEEDED)
        );
        return caseDataMap;
    }

    private Map<String, Object> getDraftOrderData(String authorisation, CaseData caseData, DraftOrder draftOrder) throws Exception {
        return manageOrderService.getCaseData(
            authorisation,
            caseData,
            draftOrder.getOrderType()
        );
    }

    public Map<String, Object> judgeOrAdminEditApproveDraftOrderMidEvent(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        String eventId = callbackRequest.getEventId();
        if (Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(eventId)
            && (WhatToDoWithOrderEnum.finalizeSaveToServeLater
            .equals(caseData.getServeOrderData().getWhatDoWithOrder())
            || YesOrNo.Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder()))) {
            CaseData updatedCaseData = objectMapper.convertValue(
                caseDataUpdated,
                CaseData.class
            );
            caseDataUpdated.putAll(removeDraftOrderAndAddToFinalOrder(authorisation, updatedCaseData, eventId));
            CaseData modifiedCaseData = objectMapper.convertValue(
                caseDataUpdated,
                CaseData.class
            );
            manageOrderService.populateServeOrderDetails(modifiedCaseData, caseDataUpdated);
        }
        log.info("order hearing details {}", caseDataUpdated.get(ORDERS_HEARING_DETAILS));
        log.info("order collection {}", caseDataUpdated.get("orderCollection"));

        return caseDataUpdated;
    }

    public Map<String, Object> judgeOrAdminEditApproveDraftOrderAboutToSubmit(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        caseData = manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        String eventId = callbackRequest.getEventId();
        if (Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(eventId)
            && (WhatToDoWithOrderEnum.finalizeSaveToServeLater
            .equals(caseData.getServeOrderData().getWhatDoWithOrder())
            || YesOrNo.Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder()))) {
            caseDataUpdated.putAll(removeDraftOrderAndAddToFinalOrder(
                authorisation,
                caseData, eventId
            ));
            if (YesOrNo.Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())) {
                CaseData modifiedCaseData = objectMapper.convertValue(
                    caseDataUpdated,
                    CaseData.class
                );
                List<Element<OrderDetails>> orderCollection = modifiedCaseData.getOrderCollection();
                caseDataUpdated.put(
                    "orderCollection",
                    manageOrderService.serveOrder(modifiedCaseData, orderCollection)
                );
            }
        } else {
            caseDataUpdated.putAll(updateDraftOrderCollection(caseData, authorisation, eventId));
        }
        return caseDataUpdated;
    }

    public static String checkIfOrderCanReviewed(CallbackRequest callbackRequest, Map<String, Object> response) {
        String orderStatus = (String) response.remove("status");
        String reviewRequiredBy = (String) response.remove("reviewRequiredBy");
        String errorMessage = null;
        if (Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(callbackRequest.getEventId())
            && ((OrderStatusEnum.createdByCA.getDisplayedValue().equalsIgnoreCase(orderStatus))
            && AmendOrderCheckEnum.judgeOrLegalAdvisorCheck.getDisplayedValue().equalsIgnoreCase(
            reviewRequiredBy) || OrderStatusEnum.draftedByLR.getDisplayedValue().equalsIgnoreCase(orderStatus))) {
            errorMessage = "Selected order is not reviewed by Judge.";
        } else if (Event.EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(callbackRequest.getEventId())
            && (!(((OrderStatusEnum.createdByCA.getDisplayedValue().equalsIgnoreCase(orderStatus))
            && AmendOrderCheckEnum.judgeOrLegalAdvisorCheck.getDisplayedValue().equalsIgnoreCase(
            reviewRequiredBy)) || OrderStatusEnum.reviewedByJudge.getDisplayedValue().equalsIgnoreCase(orderStatus)
            || OrderStatusEnum.draftedByLR.getDisplayedValue().equalsIgnoreCase(orderStatus)
            || OrderStatusEnum.createdByJudge.getDisplayedValue().equalsIgnoreCase(orderStatus)))) {
            errorMessage = "Selected order can not be reviewed by Judge.";
        }
        return errorMessage;
    }

    public Map<String, Object> generateOrderDocument(String authorisation, CallbackRequest callbackRequest, Hearings hearings) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseData = generateDocument(callbackRequest, caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (caseData.getCreateSelectOrderOptions() != null
            && CreateSelectOrderOptionsEnum.specialGuardianShip.equals(caseData.getCreateSelectOrderOptions())) {
            List<Element<AppointedGuardianFullName>> namesList = new ArrayList<>();
            manageOrderService.updateCaseDataWithAppointedGuardianNames(callbackRequest.getCaseDetails(), namesList);
            caseData.setAppointedGuardianName(namesList);
        }
        if (caseData.getManageOrders().getOrdersHearingDetails() != null) {
            caseData.getManageOrders()
                .setOrdersHearingDetails(hearingDataService.getHearingDataForSelectedHearing(caseData, hearings));
        }
        if (Event.EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(callbackRequest.getEventId())
            || Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
            caseDataUpdated.putAll(getDraftOrderInfo(authorisation, caseData));
        } else {
            caseDataUpdated.putAll(manageOrderService.getCaseData(
                authorisation,
                caseData,
                caseData.getCreateSelectOrderOptions()
            ));
        }
        //caseDataUpdated.put(ORDER_HEARING_DETAILS, caseData.getManageOrders().getOrdersHearingDetails());
        return caseDataUpdated;
    }

    public Map<String, Object> prepareDraftOrderCollection(String authorisation, CallbackRequest callbackRequest) {
        manageOrderService.resetChildOptions(callbackRequest);
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseData = manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.putAll(generateDraftOrderCollection(caseData, authorisation));
        ManageOrderService.cleanUpSelectedManageOrderOptions(caseDataUpdated);
        return caseDataUpdated;
    }

    public Map<String, Object> resetFields(CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        return caseDataUpdated;
    }
}

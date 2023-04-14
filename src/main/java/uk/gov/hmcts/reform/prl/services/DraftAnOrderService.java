package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoOtherEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio.DioApplicationToApplyPermission;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.PartiesListGenerator;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CROSS_EXAMINATION_EX740;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CROSS_EXAMINATION_QUALIFIED_LEGAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_APPLICATION_TO_APPLY_PERMISSION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_CASE_REVIEW;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_PARENT_WITHCARE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_RIGHT_TO_ASK;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_SAFEGUARDING_CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_SAFEGUARING_CAFCASS_CYMRU;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_NOT_NEEDED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JOINING_INSTRUCTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARENT_WITHCARE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTICIPATION_DIRECTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RIGHT_TO_ASK_COURT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SAFE_GUARDING_LETTER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SPECIFIED_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SPIP_ATTENDANCE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftAnOrderService {

    private final Time dateTime;
    private final ElementUtils elementUtils;
    private final ObjectMapper objectMapper;
    private final ManageOrderService manageOrderService;
    private final DgsService dgsService;
    private final DocumentLanguageService documentLanguageService;
    private final LocationRefDataService locationRefDataService;
    private final PartiesListGenerator partiesListGenerator;

    private static final String DRAFT_ORDER_COLLECTION = "draftOrderCollection";


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
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            caseData.getDraftOrderCollection(),
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));
        caseDataMap.put("caseTypeOfApplication", CaseUtils.getCaseTypeOfApplication(caseData));
        return caseDataMap;
    }

    public Map<String, Object> removeDraftOrderAndAddToFinalOrder(String authorisation, CaseData caseData, String eventId) {
        Map<String, Object> updatedCaseData = new HashMap<>();
        List<Element<DraftOrder>> draftOrderCollection = caseData.getDraftOrderCollection();
        UUID selectedOrderId = elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper);
        String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
        updatedCaseData.put("caseTypeOfApplication", CaseUtils.getCaseTypeOfApplication(caseData));
        for (Element<DraftOrder> e : caseData.getDraftOrderCollection()) {
            DraftOrder draftOrder = e.getValue();
            if (e.getId().equals(selectedOrderId)) {
                updatedCaseData.put("orderUploadedAsDraftFlag", draftOrder.getIsOrderUploadedByJudgeOrAdmin());
                if (YesOrNo.Yes.equals(caseData.getDoYouWantToEditTheOrder()) || (caseData.getManageOrders() != null
                    && Yes.equals(caseData.getManageOrders().getMakeChangesToUploadedOrder()))) {
                    draftOrder =  getUpdatedDraftOrder(draftOrder, caseData, loggedInUserType, eventId);
                } else {
                    draftOrder = getDraftOrderWithUpdatedStatus(caseData, eventId, loggedInUserType, draftOrder);
                }
                updatedCaseData.put("orderCollection", getFinalOrderCollection(authorisation, caseData, draftOrder, eventId));
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
            .serveOrderDetails(manageOrderService.buildServeOrderDetails(serveOrderData))
            .adminNotes(caseData.getCourtAdminNotes())
            .dateCreated(draftOrder.getOtherDetails().getDateCreated())
            .judgeNotes(draftOrder.getJudgeNotes())
            .otherDetails(
                OtherOrderDetails.builder().createdBy(draftOrder.getOtherDetails().getCreatedBy())
                    .orderCreatedDate(dateTime.now().format(DateTimeFormatter.ofPattern(
                        PrlAppsConstants.D_MMMM_YYYY,
                        Locale.UK
                    )))
                    .orderMadeDate(draftOrder.getDateOrderMade() != null ? draftOrder.getDateOrderMade().format(
                        DateTimeFormatter.ofPattern(
                            PrlAppsConstants.D_MMMM_YYYY,
                            Locale.UK
                        )) : null)
                    .approvalDate(draftOrder.getApprovalDate() != null ? draftOrder.getDateOrderMade().format(
                        DateTimeFormatter.ofPattern(
                            PrlAppsConstants.D_MMMM_YYYY,
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
            .childrenList(draftOrder.getChildrenList())
            .build();
        if (Yes.equals(draftOrder.getIsOrderUploadedByJudgeOrAdmin())) {
            orderDetails = orderDetails.toBuilder()
                .orderDocument(draftOrder.getOrderDocument())
                .build();
        } else {
            DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
            Map<String, String> fieldMap = manageOrderService.getOrderTemplateAndFile(draftOrder.getOrderType());
            try {
                if (documentLanguage.isGenEng()) {
                    generatedDocumentInfo = dgsService.generateDocument(
                        auth,
                        CaseDetails.builder().caseData(caseData).build(),
                        fieldMap.get(PrlAppsConstants.FINAL_TEMPLATE_NAME)
                    );
                }
                if (documentLanguage.isGenWelsh()) {
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

    private String getReportFiledDate(ServeOrderData serveOrderData) {
        if (serveOrderData.getWhenReportsMustBeFiled() != null) {
            return serveOrderData.getWhenReportsMustBeFiled().format(DateTimeFormatter.ofPattern(
                PrlAppsConstants.D_MMMM_YYYY,
                Locale.UK
            ));
        } else {
            return null;
        }

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
        log.info("orderUploadedAsDraftFlag value:: {} ", caseDataMap.get("orderUploadedAsDraftFlag"));
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
        caseDataMap.put("caseTypeOfApplication", caseData.getCaseTypeOfApplication());
        return caseDataMap;
    }

    public Map<String, Object> populateDraftOrderCustomFields(CaseData caseData) {
        Map<String, Object> caseDataMap = new HashMap<>();
        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData);
        caseDataMap.put("fl404CustomFields", selectedOrder.getFl404CustomFields());
        caseDataMap.put("parentName", selectedOrder.getParentName());
        caseDataMap.put("childArrangementsOrdersToIssue", selectedOrder.getChildArrangementsOrdersToIssue());
        caseDataMap.put("selectChildArrangementsOrder", selectedOrder.getSelectChildArrangementsOrder());
        caseDataMap.put("cafcassOfficeDetails", selectedOrder.getCafcassOfficeDetails());
        caseDataMap.put("appointedGuardianName", selectedOrder.getAppointedGuardianName());
        caseDataMap.put("manageOrdersFl402CourtName",selectedOrder.getManageOrdersFl402CourtName());
        caseDataMap.put("manageOrdersFl402CourtAddress",selectedOrder.getManageOrdersFl402CourtAddress());
        caseDataMap.put("manageOrdersFl402CaseNo",selectedOrder.getManageOrdersFl402CaseNo());
        caseDataMap.put("manageOrdersFl402Applicant",selectedOrder.getManageOrdersFl402Applicant());
        caseDataMap.put("manageOrdersFl402ApplicantRef",selectedOrder.getManageOrdersFl402ApplicantRef());
        caseDataMap.put("fl402HearingCourtname",selectedOrder.getFl402HearingCourtname());
        caseDataMap.put("fl402HearingCourtAddress",selectedOrder.getFl402HearingCourtAddress());
        caseDataMap.put("manageOrdersDateOfhearing",selectedOrder.getManageOrdersDateOfhearing());
        caseDataMap.put("dateOfHearingTime",selectedOrder.getDateOfHearingTime());
        caseDataMap.put("dateOfHearingTimeEstimate",selectedOrder.getDateOfHearingTimeEstimate());
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
        caseDataMap.put("caseTypeOfApplication", caseData.getCaseTypeOfApplication());
        return caseDataMap;
    }

    public Map<String, Object> populateCommonDraftOrderFields(CaseData caseData) {
        Map<String, Object> caseDataMap = new HashMap<>();
        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData);
        caseDataMap.put("orderType", selectedOrder.getOrderType());
        caseDataMap.put("isTheOrderByConsent", selectedOrder.getIsTheOrderByConsent());
        caseDataMap.put("dateOrderMade", selectedOrder.getDateOrderMade());
        caseDataMap.put("wasTheOrderApprovedAtHearing", selectedOrder.getWasTheOrderApprovedAtHearing());
        caseDataMap.put("judgeOrMagistrateTitle", selectedOrder.getJudgeOrMagistrateTitle());
        caseDataMap.put("judgeOrMagistratesLastName", selectedOrder.getJudgeOrMagistratesLastName());
        caseDataMap.put("justiceLegalAdviserFullName", selectedOrder.getJusticeLegalAdviserFullName());
        caseDataMap.put("magistrateLastName", selectedOrder.getMagistrateLastName());
        caseDataMap.put("isTheOrderAboutAllChildren", selectedOrder.getIsTheOrderAboutAllChildren());
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
        return caseDataMap;
    }

    public DraftOrder getSelectedDraftOrderDetails(CaseData caseData) {
        UUID orderId = elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper);
        return caseData.getDraftOrderCollection().stream()
            .filter(element -> {
                return element.getId().equals(orderId);
            })
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
            .isTheOrderAboutChildren(caseData.getManageOrders().getIsTheOrderAboutChildren())
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
            .childrenList(manageOrderService.getSelectedChildInfoFromMangeOrder(caseData.getManageOrders().getChildOption()))
            .build();
    }

    public CaseData generateDocument(@RequestBody CallbackRequest callbackRequest, CaseData caseData) {
        if (callbackRequest
            .getCaseDetailsBefore() != null && callbackRequest
            .getCaseDetailsBefore().getData().get(COURT_NAME) != null) {
            caseData.setCourtName(callbackRequest
                                      .getCaseDetailsBefore().getData().get(COURT_NAME).toString());
        }
        if (!C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            FL404 fl404CustomFields = caseData.getManageOrders().getFl404CustomFields();
            if (fl404CustomFields != null) {
                fl404CustomFields = fl404CustomFields.toBuilder().fl404bApplicantName(String.format(
                        PrlAppsConstants.FORMAT,
                        caseData.getApplicantsFL401().getFirstName(),
                        caseData.getApplicantsFL401().getLastName()
                    ))
                    .fl404bRespondentName(String.format(PrlAppsConstants.FORMAT,
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
                                  .c21OrderOptions(caseData.getManageOrders().getC21OrderOptions())
                                  .typeOfC21Order(caseData.getManageOrders().getC21OrderOptions() != null
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
                                  .c21OrderOptions(caseData.getManageOrders().getC21OrderOptions())
                                  .typeOfC21Order(caseData.getManageOrders().getC21OrderOptions() != null
                                                      ? caseData.getManageOrders().getC21OrderOptions().getDisplayedValue() : null)
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
            && caseData.getStandardDirectionOrder().getSdoOtherList().isEmpty());
    }

    public void populateStandardDirectionOrderFields(String authorisation, CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (!caseData.getStandardDirectionOrder().getSdoPreamblesList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoPreamblesList().contains(SdoPreamblesEnum.rightToAskCourt)) {
            caseDataUpdated.put(
                "sdoRightToAskCourt",
                RIGHT_TO_ASK_COURT
            );
        }
        populateHearingAndNextStepsText(caseData, caseDataUpdated);
        populateCourtText(caseData, caseDataUpdated);
        populateDocumentAndEvidenceText(caseData, caseDataUpdated);
        if (!caseData.getStandardDirectionOrder().getSdoOtherList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoOtherList().contains(
            SdoOtherEnum.parentWithCare)) {
            caseDataUpdated.put(
                "sdoParentWithCare",
                PARENT_WITHCARE
            );
        }
        List<DynamicListElement> courtList = getCourtDynamicList(authorisation);
        populateCourtDynamicList(courtList, caseDataUpdated);
        DynamicList partiesList = partiesListGenerator.buildPartiesList(caseData, courtList);
        caseDataUpdated.put("sdoInstructionsFilingPartiesDynamicList", partiesList);
    }

    private static void populateDocumentAndEvidenceText(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (!caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList().contains(
            SdoDocumentationAndEvidenceEnum.specifiedDocuments)) {
            caseDataUpdated.put(
                "sdoSpecifiedDocuments",
                SPECIFIED_DOCUMENTS
            );
        }
        if (!caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList().contains(
            SdoDocumentationAndEvidenceEnum.spipAttendance)) {
            caseDataUpdated.put(
                "sdoSpipAttendance",
                SPIP_ATTENDANCE
            );
        }
    }

    private static void populateCourtText(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (!caseData.getStandardDirectionOrder().getSdoCourtList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoCourtList().contains(
            SdoCourtEnum.crossExaminationEx740)) {
            caseDataUpdated.put(
                "sdoCrossExaminationEx740",
                CROSS_EXAMINATION_EX740
            );
        }
        if (!caseData.getStandardDirectionOrder().getSdoCourtList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoCourtList().contains(
            SdoCourtEnum.crossExaminationQualifiedLegal)) {
            caseDataUpdated.put(
                "sdoCrossExaminationQualifiedLegal",
                CROSS_EXAMINATION_QUALIFIED_LEGAL
            );
        }
    }

    private static void populateHearingAndNextStepsText(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
            SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping)) {
            caseDataUpdated.put(
                "sdoNextStepsAfterSecondGK",
                SAFE_GUARDING_LETTER
            );
        }
        if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
            SdoHearingsAndNextStepsEnum.hearingNotNeeded)) {
            caseDataUpdated.put(
                "sdoHearingNotNeeded",
                HEARING_NOT_NEEDED
            );
            caseDataUpdated.put(
                "sdoParticipationDirections",
                PARTICIPATION_DIRECTIONS
            );
        }
        if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
            SdoHearingsAndNextStepsEnum.joiningInstructions)) {
            caseDataUpdated.put(
                "sdoJoiningInstructionsForRH",
                JOINING_INSTRUCTIONS
            );
        }
        if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
            SdoHearingsAndNextStepsEnum.updateContactDetails)) {
            caseDataUpdated.put(
                "sdoUpdateContactDetails",
                UPDATE_CONTACT_DETAILS
            );
        }
    }

    private void populateCourtDynamicList(List<DynamicListElement> courtList, Map<String, Object> caseDataUpdated) {
        DynamicList courtDynamicList =  DynamicList.builder().value(DynamicListElement.EMPTY).listItems(courtList)
            .build();
        caseDataUpdated.put(
            "sdoUrgentHearingCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "sdoFhdraCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "sdoDirectionsDraCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "sdoSettlementConferenceCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "sdoTransferApplicationCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "sdoCrossExaminationCourtDynamicList", courtDynamicList);
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
        DynamicList courtDynamicList =  DynamicList.builder().value(DynamicListElement.EMPTY).listItems(courtList)
            .build();
        caseDataUpdated.put(
            "dioFhdraCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "dioPermissionHearingCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "dioTransferApplicationCourtDynamicList", courtDynamicList);
    }

    public Map<String,Object> getDraftOrderInfo(String authorisation, CaseData caseData)  throws Exception {
        DraftOrder draftOrder = getSelectedDraftOrderDetails(caseData);
        return  getDraftOrderData(authorisation, caseData, draftOrder);
    }

    private Map<String, Object> getDraftOrderData(String authorisation, CaseData caseData, DraftOrder draftOrder) throws Exception {
        Map<String, Object> caseDataUpdated = manageOrderService.getCaseData(
            authorisation,
            caseData,
            draftOrder.getOrderType()
        );
        return caseDataUpdated;
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
        return caseDataUpdated;
    }

    public  Map<String, Object> judgeOrAdminEditApproveDraftOrderAboutToSubmit(String authorisation, CallbackRequest callbackRequest) {
        String eventId = callbackRequest.getEventId();
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
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
            || OrderStatusEnum.draftedByLR.getDisplayedValue().equalsIgnoreCase(orderStatus)))) {
            errorMessage = "Selected order can not be reviewed by Judge.";
        }
        return errorMessage;
    }

    public Map<String, Object> generateOrderDocument(String authorisation, CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseData = generateDocument(callbackRequest, caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (caseData.getCreateSelectOrderOptions() != null
            && CreateSelectOrderOptionsEnum.specialGuardianShip.equals(caseData.getCreateSelectOrderOptions())) {
            List<Element<AppointedGuardianFullName>> namesList = new ArrayList<>();
            manageOrderService.updateCaseDataWithAppointedGuardianNames(callbackRequest.getCaseDetails(), namesList);
            caseData.setAppointedGuardianName(namesList);
        }
        if (Event.EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(callbackRequest.getEventId())
            || Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
            caseDataUpdated.putAll(getDraftOrderInfo(authorisation, caseData));
        } else {
            caseDataUpdated.putAll(manageOrderService.getCaseData(authorisation, caseData, caseData.getCreateSelectOrderOptions()));
        }
        return caseDataUpdated;
    }

    public Map<String, Object> prepareDraftOrderCollection(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.putAll(generateDraftOrderCollection(caseData, authorisation));
        manageOrderService.cleanUpSelectedManageOrderOptions(caseDataUpdated);
        return caseDataUpdated;
    }
}

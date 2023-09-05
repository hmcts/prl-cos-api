package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendOrderService {
    private static final String MEDIA_TYPE = "application/pdf";
    private static final String FILE_NAME_PREFIX = "amended_";

    private final AmendedOrderStamper stamper;
    private final  UploadDocumentService uploadService;
    private final Time time;
    private final ManageOrderService manageOrderService;

    public Map<String, Object> updateOrder(CaseData caseData, String authorisation) throws IOException {
        ManageOrders eventData = caseData.getManageOrders();

        byte[] stampedBinaries = stamper.amendDocument(eventData.getManageOrdersDocumentToAmend(), authorisation);
        String amendedFileName = updateFileName(eventData.getManageOrdersDocumentToAmend());
        Document stampedDocument = uploadService.uploadDocument(stampedBinaries, amendedFileName, MEDIA_TYPE, authorisation);

        String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);

        uk.gov.hmcts.reform.prl.models.documents.Document updatedDocument = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName(stampedDocument.originalDocumentName)
            .documentUrl(stampedDocument.links.self.href)
            .documentBinaryUrl(stampedDocument.links.binary.href)
            .build();

        return updateAmendedOrderDetails(caseData, updatedDocument, loggedInUserType);

    }

    private String updateFileName(uk.gov.hmcts.reform.prl.models.documents.Document original) {
        String filename = original.getDocumentFileName();
        return filename.startsWith(FILE_NAME_PREFIX) ? filename : FILE_NAME_PREFIX + filename;
    }

    private Map<String, Object> updateAmendedOrderDetails(CaseData caseData,
                                                          uk.gov.hmcts.reform.prl.models.documents.Document amendedDocument,
                                                          String loggedInUserType) {
        Map<String, Object> orderMap = new HashMap<>();
        UUID selectedOrderId = caseData.getManageOrders().getAmendOrderDynamicList().getValueCodeAsUuid();
        List<Element<OrderDetails>> orders = caseData.getOrderCollection();
        String orderSelectionType = CaseUtils.getOrderSelectionType(caseData);
        List<Element<OrderDetails>> updatedOrders;
        if (YesOrNo.Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())
            || WhatToDoWithOrderEnum.finalizeSaveToServeLater
                .equals(caseData.getServeOrderData().getWhatDoWithOrder())) {
            orders.stream()
                .filter(order -> Objects.equals(order.getId(), selectedOrderId))
                .findFirst()
                .ifPresent(order -> {
                    OrderDetails amended = order.getValue().toBuilder()
                        .orderDocument(amendedDocument)
                        .dateCreated(caseData.getManageOrders().getCurrentOrderCreatedDateTime() != null
                                         ? caseData.getManageOrders().getCurrentOrderCreatedDateTime() : LocalDateTime.now())
                        .orderType(order.getValue().getOrderType())
                        .typeOfOrder(order.getValue().getTypeOfOrder())
                        .serveOrderDetails(null)
                        .otherDetails(order.getValue().getOtherDetails().toBuilder()
                                          .orderServedDate(null)
                                          .orderCreatedDate(time.now().format(DateTimeFormatter.ofPattern(
                                              PrlAppsConstants.D_MMM_YYYY,
                                              Locale.UK
                                          )))
                                          .status(manageOrderService.getOrderStatus(
                                              orderSelectionType,
                                              loggedInUserType,
                                              null,
                                              null
                                          ))
                                          .build())
                        .build();

                    orders.addAll(List.of(element(amended)));
                    orders.sort(Comparator.comparing(
                        m -> m.getValue().getDateCreated(),
                        Comparator.reverseOrder()));
                    LocalDateTime currentOrderCreatedDateTime = amended.getDateCreated();
                    orderMap.put("currentOrderCreatedDateTime", currentOrderCreatedDateTime);
                });
            if (YesOrNo.Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())) {
                updatedOrders =  manageOrderService.serveOrder(caseData,orders);
            } else {
                updatedOrders = orders;
            }
            orderMap.put("orderCollection", updatedOrders);
            return orderMap;
        } else {
            return  setDraftOrderCollection(caseData, amendedDocument, loggedInUserType);
        }

    }

    public Map<String, Object> setDraftOrderCollection(CaseData caseData, uk.gov.hmcts.reform.prl.models.documents.Document amendedDocument,
                                                       String loggedInUserType) {
        List<Element<DraftOrder>> draftOrderList = new ArrayList<>();
        Element<DraftOrder> draftOrderElement = element(getCurrentDraftOrderDetails(caseData,amendedDocument, loggedInUserType));
        if (caseData.getDraftOrderCollection() != null) {
            draftOrderList.addAll(caseData.getDraftOrderCollection());
            draftOrderList.add(draftOrderElement);
        } else {
            draftOrderList.add(draftOrderElement);
        }
        draftOrderList.sort(Comparator.comparing(
            m -> m.getValue().getOtherDetails().getDateCreated(),
            Comparator.reverseOrder()
        ));
        return Map.of("draftOrderCollection", draftOrderList
        );
    }

    private DraftOrder getCurrentDraftOrderDetails(CaseData caseData,
                                                   uk.gov.hmcts.reform.prl.models.documents.Document amendedDocument,
                                                   String loggedInUserType) {
        UUID selectedOrderId = caseData.getManageOrders().getAmendOrderDynamicList().getValueCodeAsUuid();
        List<Element<OrderDetails>> orders = caseData.getOrderCollection();
        Optional<Element<OrderDetails>> orderDetails  = orders.stream()
            .filter(order -> Objects.equals(order.getId(), selectedOrderId))
            .findFirst();
        String orderType = orderDetails.isPresent() ? orderDetails.get().getValue().getOrderType() : null;

        String orderSelectionType = CaseUtils.getOrderSelectionType(caseData);
        return DraftOrder.builder()
            .typeOfOrder(orderType)
            .orderTypeId(orderType)
            .orderDocument(amendedDocument)
            .orderSelectionType(orderSelectionType)
            .isOrderUploadedByJudgeOrAdmin(YesOrNo.Yes)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy(caseData.getJudgeOrMagistratesLastName())
                              .dateCreated(time.now())
                              .status(manageOrderService.getOrderStatus(orderSelectionType, loggedInUserType, null, null))
                              .isJudgeApprovalNeeded(AmendOrderCheckEnum.noCheck.equals(
                                  caseData.getManageOrders().getAmendOrderSelectCheckOptions())
                                                            || AmendOrderCheckEnum.managerCheck.equals(
                                  caseData.getManageOrders().getAmendOrderSelectCheckOptions())
                                                            || UserRoles.JUDGE.name().equalsIgnoreCase(loggedInUserType) ? No : Yes)
                              .reviewRequiredBy(caseData.getManageOrders().getAmendOrderSelectCheckOptions())
                              .nameOfJudgeForReview(caseData.getManageOrders().getNameOfJudgeAmendOrder())
                              .nameOfLaForReview(caseData.getManageOrders().getNameOfLaAmendOrder())
                              .nameOfJudgeForReviewOrder(String.valueOf(caseData.getManageOrders().getNameOfJudgeToReviewOrder()))
                              .nameOfLaForReviewOrder(String.valueOf(caseData.getManageOrders().getNameOfLaToReviewOrder()))
                              .build())
            .dateOrderMade(caseData.getDateOrderMade())

            .manageOrderHearingDetails(caseData.getManageOrders().getOrdersHearingDetails())
            .build();
    }
}

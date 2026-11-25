package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.services.validators.LegalAdviserChecker;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

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

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendOrderService {

    private static final String FILE_NAME_PREFIX = "amended_";

    private final Time time;
    private final ManageOrderService manageOrderService;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final LegalAdviserChecker legalAdviserChecker;

    public Map<String, Object> updateOrder(CaseData caseData, String authorisation) {
        ManageOrders eventData = caseData.getManageOrders();
        //Currently unable to amend uploaded document unless the event is submitted due to XUI limitations,
        // Hence needs to revisit the logic, once XUI issue is resolved
        String amendedFileName = updateFileName(eventData.getManageOrdersDocumentToAmend());
        String loggedInUserType = userRoleService.getLoggedInUserType(authorisation);

        uk.gov.hmcts.reform.prl.models.documents.Document updatedDocument = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName(amendedFileName)
            .documentUrl(eventData.getManageOrdersAmendedOrder().getDocumentUrl())
            .documentBinaryUrl(eventData.getManageOrdersAmendedOrder().getDocumentBinaryUrl())
            .build();

        UserDetails userDetails = userService.getUserDetails(authorisation);
        String currentUserFullName = userDetails.getFullName();
        return updateAmendedOrderDetails(caseData, updatedDocument, loggedInUserType, currentUserFullName, authorisation);

    }

    private String updateFileName(uk.gov.hmcts.reform.prl.models.documents.Document original) {
        String filename = original.getDocumentFileName();
        return filename.startsWith(FILE_NAME_PREFIX) ? filename : FILE_NAME_PREFIX + filename;
    }

    private Map<String, Object> updateAmendedOrderDetails(CaseData caseData,
                                                          uk.gov.hmcts.reform.prl.models.documents.Document amendedDocument,
                                                          String loggedInUserType, String currentUserFullName, String authorisation) {
        Map<String, Object> orderMap = new HashMap<>();
        UUID selectedOrderId = caseData.getManageOrders().getAmendOrderDynamicList().getValueCodeAsUuid();
        List<Element<OrderDetails>> orders = caseData.getOrderCollection();

        UserRoles userRole = userRoleService.getLoggedInUserRole(authorisation);

        List<Element<OrderDetails>> updatedOrders;
        if (YesOrNo.Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())
            || WhatToDoWithOrderEnum.finalizeSaveToServeLater
                .equals(caseData.getServeOrderData().getWhatDoWithOrder())) {
            orders.stream()
                .filter(order -> Objects.equals(order.getId(), selectedOrderId))
                .findFirst()
                .ifPresent(order -> {
                    String orderSelectionType = CaseUtils.getOrderSelectionType(caseData);
                    String orderStatus = manageOrderService.getOrderStatus(orderSelectionType, userRole, null, null);
                    OrderDetails amended = order.getValue().toBuilder()
                        .orderDocument(amendedDocument)
                        .dateCreated(caseData.getManageOrders().getCurrentOrderCreatedDateTime() != null
                                         ? caseData.getManageOrders().getCurrentOrderCreatedDateTime() : LocalDateTime.now())
                        .otherDetails(order.getValue().getOtherDetails().toBuilder()
                                          .orderServedDate(null)
                                          .createdBy(order.getValue().getOtherDetails().getCreatedBy())
                                          .orderCreatedBy(currentUserFullName)
                                          .orderCreatedDate(time.now().format(DateTimeFormatter.ofPattern(
                                              PrlAppsConstants.D_MMM_YYYY,
                                              Locale.ENGLISH
                                          )))
                                          .status(orderStatus)
                                          .build())
                        .build();

                    orders.add(element(amended));
                    orders.sort(Comparator.comparing(
                        m -> m.getValue().getDateCreated(),
                        Comparator.reverseOrder()));
                    LocalDateTime currentOrderCreatedDateTime = amended.getDateCreated();
                    orderMap.put("currentOrderCreatedDateTime", currentOrderCreatedDateTime);
                });
            if (YesOrNo.Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())) {
                updatedOrders =  manageOrderService.serveOrder(caseData, orders);
            } else {
                updatedOrders = orders;
            }
            orderMap.put(ORDER_COLLECTION, updatedOrders);
            return orderMap;
        } else {
            return  setDraftOrderCollection(caseData, amendedDocument, loggedInUserType, currentUserFullName, userRole);
        }

    }

    public Map<String, Object> setDraftOrderCollection(CaseData caseData, uk.gov.hmcts.reform.prl.models.documents.Document amendedDocument,
                                                       String loggedInUserType, String currentUserFullName, UserRoles userRole) {
        List<Element<DraftOrder>> draftOrderList = new ArrayList<>();
        DraftOrder draftOrder = getCurrentDraftOrderDetails(caseData,amendedDocument, loggedInUserType,
                                                            currentUserFullName, userRole);
        Element<DraftOrder> draftOrderElement = element(draftOrder);
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
                                                   String loggedInUserType, String currentUserFullName,
                                                   UserRoles userRole) {
        UUID selectedOrderId = caseData.getManageOrders().getAmendOrderDynamicList().getValueCodeAsUuid();
        List<Element<OrderDetails>> orders = caseData.getOrderCollection();
        Optional<Element<OrderDetails>> orderDetails  = orders.stream()
            .filter(order -> Objects.equals(order.getId(), selectedOrderId))
            .findFirst();

        String orderType = orderDetails.map(orderDetailsElement -> orderDetailsElement.getValue().getOrderType()).orElse(
            null);

        Optional<String> legalAdviserName = legalAdviserChecker.validateLegalAdviserName(
            caseData.getManageOrders().getNameOfLaToReviewOrder(),
            caseData.getManageOrders().getLegalAdviserToReviewOrder()
        );
        String orderSelectionType = CaseUtils.getOrderSelectionType(caseData);
        String orderStatus = manageOrderService.getOrderStatus(orderSelectionType, userRole, null, null);
        return DraftOrder.builder()
            .typeOfOrder(orderType)
            .orderType(CreateSelectOrderOptionsEnum.getIdFromValue(orderType))
            .orderTypeId(orderType)
            .orderDocument(amendedDocument)
            .orderSelectionType(orderSelectionType)
            .isOrderUploadedByJudgeOrAdmin(YesOrNo.Yes)
            .miamForOrder(caseData.getMiamForOrder())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy(caseData.getJudgeOrMagistratesLastName())
                              .orderCreatedBy(currentUserFullName)
                              .dateCreated(time.now())
                              .status(orderStatus)
                              .isJudgeApprovalNeeded(AmendOrderCheckEnum.noCheck.equals(
                                  caseData.getManageOrders().getAmendOrderSelectCheckOptions())
                                                            || AmendOrderCheckEnum.managerCheck.equals(
                                  caseData.getManageOrders().getAmendOrderSelectCheckOptions())
                                                            || UserRoles.JUDGE.name().equalsIgnoreCase(loggedInUserType) ? No : Yes)
                              .reviewRequiredBy(caseData.getManageOrders().getAmendOrderSelectCheckOptions())
                              .nameOfJudgeForReview(caseData.getManageOrders().getNameOfJudgeAmendOrder())
                              .nameOfLaForReview(caseData.getManageOrders().getNameOfLaAmendOrder())
                              .nameOfJudgeForReviewOrder(String.valueOf(caseData.getManageOrders().getNameOfJudgeToReviewOrder()))
                              .nameOfLaForReviewOrder(legalAdviserName.orElse(null))
                              .build())
            .dateOrderMade(caseData.getDateOrderMade())

            .manageOrderHearingDetails(caseData.getManageOrders().getOrdersHearingDetails())
            .build();
    }
}

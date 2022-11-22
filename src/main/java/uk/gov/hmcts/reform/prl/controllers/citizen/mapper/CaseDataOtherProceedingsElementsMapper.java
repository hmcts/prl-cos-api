package uk.gov.hmcts.reform.prl.controllers.citizen.mapper;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherProceedingsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.Order;
import uk.gov.hmcts.reform.prl.models.c100rebuild.OrderDate;
import uk.gov.hmcts.reform.prl.models.c100rebuild.OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.ProceedingsEnum.ongoing;
import static uk.gov.hmcts.reform.prl.enums.ProceedingsEnum.previous;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.no;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

public class CaseDataOtherProceedingsElementsMapper {

    private CaseDataOtherProceedingsElementsMapper() {
    }

    public static void updateOtherProceedingsElementsForCaseData(CaseData.CaseDataBuilder caseDataBuilder,
                                                                 C100RebuildOtherProceedingsElements c100RebuildOtherProceedingsElements) {
        caseDataBuilder
                .previousOrOngoingProceedingsForChildren(buildPreviousOrOngoingProceedingsForChildren(c100RebuildOtherProceedingsElements))
                .existingProceedings(buildExistingProceedings(c100RebuildOtherProceedingsElements));
    }

    private static List<Element<ProceedingDetails>> buildExistingProceedings(C100RebuildOtherProceedingsElements
                                                                                     c100RebuildOtherProceedingsElements) {
        List<Element<ProceedingDetails>> ordersElements = new ArrayList<>();
        if (Arrays.stream(c100RebuildOtherProceedingsElements.getCourtProceedingsOrders()).findAny().isEmpty()) {
            return null;
        }
        OrderDetails orderDetails = c100RebuildOtherProceedingsElements.getOtherProceedings().getOrder();

        List<Order> childSupervisionOrders = nonNull(orderDetails.getSupervisionOrders()) ? orderDetails.getSupervisionOrders()
                .stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum.superviosionOrder).build())
                .collect(Collectors.toList()) : null;
        List<Order> careOrders = nonNull(orderDetails.getCareOrders()) ? orderDetails.getCareOrders()
                .stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum.careOrder).build())
                .collect(Collectors.toList()) : null;
        List<Order> emergencyProtectionOrders = nonNull(orderDetails.getEmergencyProtectionOrders()) ? orderDetails
                .getEmergencyProtectionOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .emergencyProtectionOrder).build()).collect(Collectors.toList()) : null;
        List<Order> childArrangementsOrders = nonNull(orderDetails.getChildArrangementOrders()) ? orderDetails
                .getChildArrangementOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .childArrangementsOrder).build()).collect(Collectors.toList()) : null;
        List<Order> childAbductionOrders = nonNull(orderDetails.getChildAbductionOrders()) ? orderDetails
                .getChildAbductionOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .childAbduction).build()).collect(Collectors.toList()) : null;
        List<Order> contactOrdersForDivorce = nonNull(orderDetails.getContactOrdersForDivorce()) ? orderDetails
                .getContactOrdersForDivorce().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .contactOrResidenceOrder).build()).collect(Collectors.toList()) : null;
        List<Order> contactOrdersForAdoption = nonNull(orderDetails.getContactOrdersForAdoption()) ? orderDetails
                .getContactOrdersForAdoption().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .contactOrResidenceOrderWithAdoption).build()).collect(Collectors.toList()) : null;
        List<Order> childMaintenanceOrders = nonNull(orderDetails.getChildMaintenanceOrders()) ? orderDetails
                .getChildMaintenanceOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .orderRelatingToChildMaintainance).build()).collect(Collectors.toList()) : null;
        List<Order> financialOrders = nonNull(orderDetails.getFinancialOrders()) ? orderDetails
                .getFinancialOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .childrenAct1989).build()).collect(Collectors.toList()) : null;
        List<Order> nonMolestationOrders = nonNull(orderDetails.getNonMolestationOrders()) ? orderDetails
                .getNonMolestationOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .nonMolestationOrder).build()).collect(Collectors.toList()) : null;
        List<Order> occupationOrders = nonNull(orderDetails.getOccupationOrders()) ? orderDetails
                .getOccupationOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .occupationOrder).build()).collect(Collectors.toList()) : null;
        List<Order> forcedMarriageProtectionOrders = nonNull(orderDetails.getForcedMarriageProtectionOrders()) ? orderDetails
                .getForcedMarriageProtectionOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .fmpo).build()).collect(Collectors.toList()) : null;
        List<Order> restrainingOrders = nonNull(orderDetails.getRestrainingOrders()) ? orderDetails
                .getRestrainingOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .restrainingOrder).build()).collect(Collectors.toList()) : null;
        List<Order> otherInjuctionOrders = nonNull(orderDetails.getOtherInjuctionOrders()) ? orderDetails
                .getOtherInjuctionOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .otherInjunctiveOrder).build()).collect(Collectors.toList()) : null;
        List<Order> undertakingOrders = nonNull(orderDetails.getUndertakingOrders()) ? orderDetails
                .getUndertakingOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .undertakingInPlaceOfAnOrder).build()).collect(Collectors.toList()) : null;
        List<Order> otherOrders = nonNull(orderDetails.getOtherOrders()) ? orderDetails
                .getOtherOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .otherOrder).build()).collect(Collectors.toList()) : null;

        List<List<Order>> ordersLists = Lists.newArrayList(childSupervisionOrders, careOrders, emergencyProtectionOrders,
                childArrangementsOrders, childAbductionOrders, contactOrdersForDivorce, contactOrdersForAdoption,
                childMaintenanceOrders, financialOrders, nonMolestationOrders, occupationOrders, forcedMarriageProtectionOrders,
                restrainingOrders, otherInjuctionOrders, undertakingOrders, otherOrders);
        CollectionUtils.filter(ordersLists, PredicateUtils.notNullPredicate());

        for (List<Order> orderList : ordersLists) {
            ordersElements.addAll(orderList.stream()
                    .map(order -> mapToProceedingDetails(order, order.getTypeOfOrderEnum())).collect(Collectors.toList()));
        }

        return ordersElements;
    }

    private static Element<ProceedingDetails> mapToProceedingDetails(Order order, TypeOfOrderEnum typeOfOrderEnum) {
        OrderDate startDate = order.getOrderDate();
        OrderDate endDate = order.getOrderEndDate();
        return Element.<ProceedingDetails>builder().value(ProceedingDetails.builder()
                .previousOrOngoingProceedings(Yes.name().equalsIgnoreCase(order.getCurrentOrder()) ? ongoing : previous)
                .caseNumber(order.getCaseNo())
                .dateStarted(buildDate(startDate))
                .dateEnded(buildDate(endDate))
                .typeOfOrder(List.of(typeOfOrderEnum))
                .nameOfCourt(order.getOrderDetail())
                .uploadRelevantOrder(buildDocument(order.getOrderDocument()))
                .build()).build();
    }

    private static LocalDate buildDate(OrderDate date) {
        if (isNotEmpty(date.getYear()) &&  isNotEmpty(date.getMonth()) && isNotEmpty(date.getDay())) {
            return LocalDate.of(Integer.parseInt(date.getYear()), Integer.parseInt(date.getMonth()),
                    Integer.parseInt(date.getDay()));
        }
        return null;
    }

    public static Document buildDocument(uk.gov.hmcts.reform.prl.models.c100rebuild.Document orderDocument) {
        if (isNotEmpty(orderDocument)) {
            return Document.builder()
                    .documentUrl(orderDocument.getUrl())
                    .documentBinaryUrl(orderDocument.getBinaryUrl())
                    .documentFileName(orderDocument.getFilename())
                    .build();
        }
        return null;
    }

    private static YesNoDontKnow buildPreviousOrOngoingProceedingsForChildren(C100RebuildOtherProceedingsElements
                                                                                      c100RebuildOtherProceedingsElements) {
        if (Yes.equals(c100RebuildOtherProceedingsElements.getChildrenInvolvedCourtCase())
                || Yes.equals(c100RebuildOtherProceedingsElements.getCourtOrderProtection())) {
            return yes;
        }
        return no;
    }
}

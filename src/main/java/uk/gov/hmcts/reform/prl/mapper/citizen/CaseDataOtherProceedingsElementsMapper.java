package uk.gov.hmcts.reform.prl.mapper.citizen;

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
import java.util.Collections;
import java.util.List;

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

    public static void updateOtherProceedingsElementsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                                 C100RebuildOtherProceedingsElements c100RebuildOtherProceedingsElements) {
        caseDataBuilder
                .previousOrOngoingProceedingsForChildren(buildPreviousOrOngoingProceedingsForChildren(c100RebuildOtherProceedingsElements))
                .existingProceedings(buildExistingProceedings(c100RebuildOtherProceedingsElements));
    }

    private static List<Element<ProceedingDetails>> buildExistingProceedings(C100RebuildOtherProceedingsElements
                                                                                     c100RebuildOtherProceedingsElements) {
        List<Element<ProceedingDetails>> ordersElements = new ArrayList<>();
        if (nonNull(c100RebuildOtherProceedingsElements.getCourtProceedingsOrders())
                && CollectionUtils.isNotEmpty(Arrays.asList(c100RebuildOtherProceedingsElements.getCourtProceedingsOrders()))) {

            OrderDetails orderDetails = c100RebuildOtherProceedingsElements.getOtherProceedings().getOrder();

            List<Order> childSupervisionOrders = getChildSupervisionOrders(orderDetails);
            List<Order> careOrders = getCareOrders(orderDetails);
            List<Order> emergencyProtectionOrders = getEmergencyProtectionOrders(orderDetails);
            List<Order> childArrangementsOrders = getChildArrangementsOrders(orderDetails);
            List<Order> childAbductionOrders = getChildAbductionOrders(orderDetails);
            List<Order> contactOrdersForDivorce = getContactOrdersForDivorce(orderDetails);
            List<Order> contactOrdersForAdoption = getContactOrdersForAdoption(orderDetails);
            List<Order> childMaintenanceOrders = getChildMaintenanceOrders(orderDetails);
            List<Order> financialOrders = getFinancialOrders(orderDetails);
            List<Order> nonMolestationOrders = getNonMolestationOrders(orderDetails);
            List<Order> occupationOrders = getOccupationOrders(orderDetails);
            List<Order> forcedMarriageProtectionOrders = getForcedMarriageProtectionOrders(orderDetails);
            List<Order> restrainingOrders = getRestrainingOrders(orderDetails);
            List<Order> otherInjuctionOrders = getOtherInjuctionOrders(orderDetails);
            List<Order> undertakingOrders = getUndertakingOrders(orderDetails);
            List<Order> otherOrders = getOtherOrders(orderDetails);

            List<List<Order>> ordersLists = Lists.newArrayList(childSupervisionOrders, careOrders, emergencyProtectionOrders,
                    childArrangementsOrders, childAbductionOrders, contactOrdersForDivorce, contactOrdersForAdoption,
                    childMaintenanceOrders, financialOrders, nonMolestationOrders, occupationOrders, forcedMarriageProtectionOrders,
                    restrainingOrders, otherInjuctionOrders, undertakingOrders, otherOrders);
            CollectionUtils.filter(ordersLists, PredicateUtils.notNullPredicate());

            for (List<Order> orderList : ordersLists) {
                ordersElements.addAll(orderList.stream()
                        .map(order -> mapToProceedingDetails(order, order.getTypeOfOrderEnum())).toList());
            }

            return ordersElements;
        }
        return Collections.emptyList();
    }

    private static List<Order> getOtherOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getOtherOrders()) ? orderDetails
                .getOtherOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .otherOrder).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getUndertakingOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getUndertakingOrders()) ? orderDetails
                .getUndertakingOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .undertakingInPlaceOfAnOrder).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getOtherInjuctionOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getOtherInjuctionOrders()) ? orderDetails
                .getOtherInjuctionOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .otherInjunctiveOrder).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getRestrainingOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getRestrainingOrders()) ? orderDetails
                .getRestrainingOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .restrainingOrder).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getForcedMarriageProtectionOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getForcedMarriageProtectionOrders()) ? orderDetails
                .getForcedMarriageProtectionOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .fmpo).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getOccupationOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getOccupationOrders()) ? orderDetails
                .getOccupationOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .occupationOrder).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getNonMolestationOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getNonMolestationOrders()) ? orderDetails
                .getNonMolestationOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .nonMolestationOrder).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getFinancialOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getFinancialOrders()) ? orderDetails
                .getFinancialOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .childrenAct1989).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getChildMaintenanceOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getChildMaintenanceOrders()) ? orderDetails
                .getChildMaintenanceOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .orderRelatingToChildMaintainance).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getContactOrdersForAdoption(OrderDetails orderDetails) {
        return nonNull(orderDetails.getContactOrdersForAdoption()) ? orderDetails
                .getContactOrdersForAdoption().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .contactOrResidenceOrderWithAdoption).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getContactOrdersForDivorce(OrderDetails orderDetails) {
        return nonNull(orderDetails.getContactOrdersForDivorce()) ? orderDetails
                .getContactOrdersForDivorce().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .contactOrResidenceOrder).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getChildAbductionOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getChildAbductionOrders()) ? orderDetails
                .getChildAbductionOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .childAbduction).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getChildArrangementsOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getChildArrangementOrders()) ? orderDetails
                .getChildArrangementOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .childArrangementsOrder).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getEmergencyProtectionOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getEmergencyProtectionOrders()) ? orderDetails
                .getEmergencyProtectionOrders().stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum
                        .emergencyProtectionOrder).build()).toList() : Collections.emptyList();
    }

    private static List<Order> getCareOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getCareOrders()) ? orderDetails.getCareOrders()
                .stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum.careOrder).build())
                .toList() : Collections.emptyList();
    }

    private static List<Order> getChildSupervisionOrders(OrderDetails orderDetails) {
        return nonNull(orderDetails.getSupervisionOrders()) ? orderDetails.getSupervisionOrders()
                .stream().map(order -> order.toBuilder().typeOfOrderEnum(TypeOfOrderEnum.supervisionOrder).build())
                .toList() : Collections.emptyList();
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

package uk.gov.hmcts.reform.prl.models.c100rebuild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetails {

    private List<Order> childArrangementOrders;
    private List<Order> emergencyProtectionOrders;
    private List<Order> supervisionOrders;
    private List<Order> careOrders;
    private List<Order> childAbductionOrders;
    private List<Order> contactOrdersForDivorce;
    private List<Order> contactOrdersForAdoption;
    private List<Order> childMaintenanceOrders;
    private List<Order> financialOrders;
    private List<Order> nonMolestationOrders;
    private List<Order> occupationOrders;
    private List<Order> forcedMarriageProtectionOrders;
    private List<Order> restrainingOrders;
    private List<Order> otherInjuctionOrders;
    private List<Order> undertakingOrders;
    private List<Order> otherOrders;
}
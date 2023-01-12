package uk.gov.hmcts.reform.prl.enums.manageorders;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ManageOrdersOptionsEnum {
    @JsonProperty("createAnOrder")
    createAnOrder("createAnOrder", "Create an order"),
    @JsonProperty("uploadAnOrder")
    uploadAnOrder("uploadAnOrder", "Upload an order"),
    @JsonProperty("amendOrderUnderSlipRule")
    amendOrderUnderSlipRule("amendOrderUnderSlipRule", "Amend an order under the slip rule"),
    @JsonProperty("servedSavedOrders")
    servedSavedOrders("servedSavedOrders", "Served saved orders"),
    @JsonProperty("adminProduceOrder")
    adminProduceOrder("adminProduceOrder", "Request that the admin produces an order"),
    @JsonProperty("approveDraftOrder")
    approveDraftOrder("approveDraftOrder", "Edit and approve a draft order");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ManageOrdersOptionsEnum getValue(String key) {
        return ManageOrdersOptionsEnum.valueOf(key);
    }
}

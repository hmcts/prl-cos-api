package uk.gov.hmcts.reform.prl.enums.manageorders;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ManageOrdersOptionsEnum {
    @CCD(label = "Create an order")
    @JsonProperty("createAnOrder")
    createAnOrder("createAnOrder", "Create an order"),
    @CCD(label = "Upload an order")
    @JsonProperty("uploadAnOrder")
    uploadAnOrder("uploadAnOrder", "Upload an order"),
    @CCD(label = "Amend an order")
    @JsonProperty("amendOrderUnderSlipRule")
    amendOrderUnderSlipRule("amendOrderUnderSlipRule", "Amend an order"),
    @CCD(label = "Serve saved orders")
    @JsonProperty("servedSavedOrders")
    servedSavedOrders("servedSavedOrders", "Served saved orders"),
    @CCD(label = "Create a custom order")
    @JsonProperty("createCustomOrder")
    createCustomOrder("createCustomOrder", "Create a custom order");

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

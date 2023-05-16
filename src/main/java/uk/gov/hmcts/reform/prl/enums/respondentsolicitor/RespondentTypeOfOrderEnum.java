package uk.gov.hmcts.reform.prl.enums.respondentsolicitor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum RespondentTypeOfOrderEnum {
    @JsonProperty("emergencyProtectionOrder")
    emergencyProtectionOrder("emergencyProtectionOrder", "Emergency Protection Order"),
    @JsonProperty("supervisionOrder")
    supervisionOrder("supervisionOrder", "Supervision Order"),
    @JsonProperty("careOrder")
    careOrder("careOrder", "Care Order"),
    @JsonProperty("childAbduction")
    childAbduction("childAbduction", "Child Abduction"),
    @JsonProperty("familyLaw1996Part4")
    familyLaw1996Part4("familyLaw1996Part4", "Family Law Act 1996 Part 4"),
    @JsonProperty("contactOrResidenceOrder")
    contactOrResidenceOrder(
        "contactOrResidenceOrder",
        "Contact or residence order made within proceedings for a divorce or dissolution of a civil partnership"
    ),
    @JsonProperty("contactOrResidenceOrderWithAdoption")
    contactOrResidenceOrderWithAdoption(
        "contactOrResidenceOrderWithAdoption",
        "Contact or residence order made in connection with an Adoption Order"
    ),
    @JsonProperty("orderRelatingToChildMaintainance")
    orderRelatingToChildMaintainance("orderRelatingToChildMaintainance", "Order relating to child maintenance"),
    @JsonProperty("childArrangementsOrder")
    childArrangementsOrder("childArrangementsOrder", "Child arrangements order"),
    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other orders");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static RespondentTypeOfOrderEnum getValue(String key) {
        return RespondentTypeOfOrderEnum.valueOf(key);
    }
}

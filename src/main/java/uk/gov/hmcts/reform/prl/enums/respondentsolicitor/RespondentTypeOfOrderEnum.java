package uk.gov.hmcts.reform.prl.enums.respondentsolicitor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum RespondentTypeOfOrderEnum {
    @CCD(label = "Emergency Protection Order")
    @JsonProperty("emergencyProtectionOrder")
    emergencyProtectionOrder("emergencyProtectionOrder", "Emergency Protection Order"),
    @CCD(label = "Supervision Order")
    @JsonProperty("supervisionOrder")
    supervisionOrder("supervisionOrder", "Supervision Order"),
    @CCD(label = "Care Order")
    @JsonProperty("careOrder")
    careOrder("careOrder", "Care Order"),
    @CCD(label = "Child abduction")
    @JsonProperty("childAbduction")
    childAbduction("childAbduction", "Child Abduction"),
    @CCD(label = "Family Law Act 1996 Part 4")
    @JsonProperty("familyLaw1996Part4")
    familyLaw1996Part4("familyLaw1996Part4", "Family Law Act 1996 Part 4"),
    @CCD(
            label = "Contact or residence order made within proceedings for a divorce or dissolution of a civil partnership"
    )
    @JsonProperty("contactOrResidenceOrder")
    contactOrResidenceOrder(
        "contactOrResidenceOrder",
        "Contact or residence order made within proceedings for a divorce or dissolution of a civil partnership"
    ),
    @CCD(label = "Contact or residence order made in connection with an Adoption Order")
    @JsonProperty("contactOrResidenceOrderWithAdoption")
    contactOrResidenceOrderWithAdoption(
        "contactOrResidenceOrderWithAdoption",
        "Contact or residence order made in connection with an Adoption Order"
    ),
    @CCD(label = "Order relating to child maintenance")
    @JsonProperty("orderRelatingToChildMaintainance")
    orderRelatingToChildMaintainance("orderRelatingToChildMaintainance", "Order relating to child maintenance"),
    @CCD(label = "Child arrangements order")
    @JsonProperty("childArrangementsOrder")
    childArrangementsOrder("childArrangementsOrder", "Child arrangements order"),
    @CCD(label = "Other order(s)")
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

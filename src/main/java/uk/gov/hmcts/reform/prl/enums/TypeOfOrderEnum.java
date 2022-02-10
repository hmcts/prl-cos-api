package uk.gov.hmcts.reform.prl.enums;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TypeOfOrderEnum {

    @JsonProperty("emergencyProtectionOrder")
    emergencyProtectionOrder("emergencyProtectionOrder", "Emergency Protection Order"),
    @JsonProperty("superviosionOrder")
    superviosionOrder("superviosionOrder", "Supervision Order"),
    @JsonProperty("careOrder")
    careOrder("careOrder", "Care Order"),
    @JsonProperty("childAbduction")
    childAbduction("childAbduction", "Child Abduction"),
    @JsonProperty("familyLaw1996Part4")
    familyLaw1996Part4("familyLaw1996Part4", "Family Law Act 1996 Part 4"),
    @JsonProperty("contactOrResidenceOrder")
    contactOrResidenceOrder("contactOrResidenceOrder",
                            "Contact or residence order made within proceedings for a divorce or dissolution of a civil partnership"),
    @JsonProperty("contactOrResidenceOrderWithAdoption")
    contactOrResidenceOrderWithAdoption("contactOrResidenceOrderWithAdoption",
                                        "Contact or residence order made in connection with an Adoption Order"),
    @JsonProperty("orderRelatingToChildMaintainance")
    orderRelatingToChildMaintainance("orderRelatingToChildMaintainance", "Order relating to child maintenance"),
    @JsonProperty("childArrangementsOrder")
    childArrangementsOrder("childArrangementsOrder", "Child arrangements order"),
    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other orders(s)");

    private final String id;
    private final String displayedValue;

}

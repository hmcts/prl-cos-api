package uk.gov.hmcts.reform.prl.enums;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TypeOfOrderEnum {

    @JsonProperty("emergencyProtectionOrder")
    EMERGENCY_PROTECTION_ORDER("emergencyProtectionOrder", "Emergency Protection Order"),
    @JsonProperty("superviosionOrder")
    SUPERVISION_ORDER("superviosionOrder", "Supervision Order"),
    @JsonProperty("careOrder")
    CARE_ORDER("careOrder", "Care Order"),
    @JsonProperty("childAbduction")
    CHILD_ABDUCTION("childAbduction", "Child Abduction"),
    @JsonProperty("familyLaw1996Part4")
    FAMILY_LAW_1996_PART_4("familyLaw1996Part4", "Family Law Act 1996 Part 4"),
    @JsonProperty("contactOrResidenceOrder")
    CONTACT_OR_RESIDENCE_ORDER("contactOrResidenceOrder",
                               "Contact or residence order made within proceedings for a divorce or dissolution of a civil partnership"),
    @JsonProperty("contactOrResidenceOrderWithAdoption")
    CONTACT_OR_RESIDENCE_ORDER_WITH_ADOPTION("contactOrResidenceOrderWithAdoption",
                                             "Contact or residence order made in connection with an Adoption Order"),
    @JsonProperty("orderRelatingToChildMaintainance")
    CHILD_MAINTENANCE_ORDER("orderRelatingToChildMaintainance", "Order relating to child maintenance"),
    @JsonProperty("childArrangementsOrder")
    CHILD_ARRANGEMENTS_ORDER("childArrangementsOrder", "Child arrangements order"),
    @JsonProperty("otherOrder")
    OTHER_ORDER("otherOrder", "Other orders(s)");



    private final String id;
    private final String displayedValue;
}

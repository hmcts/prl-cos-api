package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

/**
 * Enum for custom order name selection dropdown.
 * Used in the Create Custom Order flow to allow selection of standard order names
 * or "Other" for a custom name.
 */
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CustomOrderNameOptionsEnum {
    @JsonProperty("standardDirectionsOrder")
    standardDirectionsOrder("standardDirectionsOrder", "Standard directions order"),
    @JsonProperty("directionOnIssue")
    directionOnIssue("directionOnIssue", "Directions on issue"),
    @JsonProperty("blankOrderOrDirections")
    blankOrderOrDirections("blankOrderOrDirections", "Blank order or directions (C21)"),
    @JsonProperty("childArrangementsSpecificProhibitedOrder")
    childArrangementsSpecificProhibitedOrder(
        "childArrangementsSpecificProhibitedOrder",
        "Child arrangements, specific issue or prohibited steps order (C43)"),
    @JsonProperty("parentalResponsibility")
    parentalResponsibility("parentalResponsibility", "Parental responsibility order (C45A)"),
    @JsonProperty("specialGuardianShip")
    specialGuardianShip("specialGuardianShip", "Special guardianship order (C43A)"),
    @JsonProperty("noticeOfProceedingsParties")
    noticeOfProceedingsParties("noticeOfProceedingsParties",
        "Notice of proceedings (C6) (Notice to parties)"),
    @JsonProperty("noticeOfProceedingsNonParties")
    noticeOfProceedingsNonParties("noticeOfProceedingsNonParties",
        "Notice of proceedings (C6a) (Notice to non-parties)"),
    @JsonProperty("appointmentOfGuardian")
    appointmentOfGuardian("appointmentOfGuardian", "Appointment of a guardian (C47A)"),
    @JsonProperty("nonMolestation")
    nonMolestation("nonMolestation", "Non-molestation order (FL404A)"),
    @JsonProperty("occupation")
    occupation("occupation", "Occupation order (FL404)"),
    @JsonProperty("powerOfArrest")
    powerOfArrest("powerOfArrest", "Power of arrest (FL406)"),
    @JsonProperty("amendDischargedVaried")
    amendDischargedVaried("amendDischargedVaried", "Amended, discharged or varied order (FL404B)"),
    @JsonProperty("blank")
    blank("blank", "Blank order (FL404B)"),
    @JsonProperty("generalForm")
    generalForm("generalForm", "General form of undertaking (N117)"),
    @JsonProperty("noticeOfProceedings")
    noticeOfProceedings("noticeOfProceedings", "Notice of proceedings (FL402)"),
    @JsonProperty("other")
    other("other", "Other");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    public String getId() {
        return id;
    }

    @JsonCreator
    public static CustomOrderNameOptionsEnum getValue(String key) {
        return CustomOrderNameOptionsEnum.valueOf(key);
    }

    /**
     * Checks if this option represents the "Other" selection,
     * which means the user should provide a custom name.
     */
    public boolean isOther() {
        return this == other;
    }
}

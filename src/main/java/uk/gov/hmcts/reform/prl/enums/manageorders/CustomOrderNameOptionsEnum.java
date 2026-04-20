package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

/**
 * Enum for custom order name selection dropdown.
 * Used in the Create Custom Order flow to allow selection of standard order names.
 */
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CustomOrderNameOptionsEnum {
    standardDirectionsOrder("standardDirectionsOrder", "Standard directions order"),
    directionOnIssue("directionOnIssue", "Directions on issue"),
    blankOrderOrDirections("blankOrderOrDirections", "Blank order or directions (C21)"),
    childArrangementsSpecificProhibitedOrder(
        "childArrangementsSpecificProhibitedOrder",
        "Child arrangements, specific issue or prohibited steps order (C43)"),
    parentalResponsibility("parentalResponsibility", "Parental responsibility order (C45A)"),
    specialGuardianShip("specialGuardianShip", "Special guardianship order (C43A)"),
    noticeOfProceedingsParties("noticeOfProceedingsParties",
        "Notice of proceedings (C6) (Notice to parties)"),
    noticeOfProceedingsNonParties("noticeOfProceedingsNonParties",
        "Notice of proceedings (C6a) (Notice to non-parties)"),
    appointmentOfGuardian("appointmentOfGuardian", "Appointment of a guardian (C47A)"),
    nonMolestation("nonMolestation", "Non-molestation order (FL404A)"),
    occupation("occupation", "Occupation order (FL404)"),
    powerOfArrest("powerOfArrest", "Power of arrest (FL406)"),
    amendDischargedVaried("amendDischargedVaried", "Amended, discharged or varied order (FL404B)"),
    blank("blank", "Blank order (FL404B)"),
    generalForm("generalForm", "General form of undertaking (N117)"),
    noticeOfProceedings("noticeOfProceedings", "Notice of proceedings (FL402)");

    @Getter
    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CustomOrderNameOptionsEnum getValue(String key) {
        return CustomOrderNameOptionsEnum.valueOf(key);
    }
}

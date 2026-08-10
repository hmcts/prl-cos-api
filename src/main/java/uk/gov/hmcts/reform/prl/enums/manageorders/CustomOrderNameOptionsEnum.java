package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

/**
 * Enum for custom order name selection dropdown.
 * Used in the Create Custom Order flow to allow selection of standard order names.
 */
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CustomOrderNameOptionsEnum {
    @CCD(label = "Standard directions order")
    standardDirectionsOrder("standardDirectionsOrder", "Standard directions order"),
    @CCD(label = "Directions on issue")
    directionOnIssue("directionOnIssue", "Directions on issue"),
    @CCD(label = "Blank order or directions (C21)")
    blankOrderOrDirections("blankOrderOrDirections", "Blank order or directions (C21)"),
    @CCD(label = "Child arrangements, specific issue or prohibited steps order (C43)")
    childArrangementsSpecificProhibitedOrder(
        "childArrangementsSpecificProhibitedOrder",
        "Child arrangements, specific issue or prohibited steps order (C43)"),
    @CCD(label = "Parental responsibility order (C45A)")
    parentalResponsibility("parentalResponsibility", "Parental responsibility order (C45A)"),
    @CCD(label = "Special guardianship order (C43A)")
    specialGuardianShip("specialGuardianShip", "Special guardianship order (C43A)"),
    @CCD(label = "Notice of proceedings (C6) (Notice to parties)")
    noticeOfProceedingsParties("noticeOfProceedingsParties",
        "Notice of proceedings (C6) (Notice to parties)"),
    @CCD(label = "Notice of proceedings (C6a) (Notice to non-parties)")
    noticeOfProceedingsNonParties("noticeOfProceedingsNonParties",
        "Notice of proceedings (C6a) (Notice to non-parties)"),
    @CCD(label = "Appointment of a guardian (C47A)")
    appointmentOfGuardian("appointmentOfGuardian", "Appointment of a guardian (C47A)"),
    @CCD(label = "Non-molestation order (FL404A)")
    nonMolestation("nonMolestation", "Non-molestation order (FL404A)"),
    @CCD(label = "Occupation order (FL404)")
    occupation("occupation", "Occupation order (FL404)"),
    @CCD(label = "Power of arrest (FL406)")
    powerOfArrest("powerOfArrest", "Power of arrest (FL406)"),
    @CCD(label = "Amended, discharged or varied order (FL404B)")
    amendDischargedVaried("amendDischargedVaried", "Amended, discharged or varied order (FL404B)"),
    @CCD(label = "Blank order (FL404B)")
    blank("blank", "Blank order (FL404B)"),
    @CCD(label = "General form of undertaking (N117)")
    generalForm("generalForm", "General form of undertaking (N117)"),
    @CCD(label = "Notice of proceedings (FL402)")
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

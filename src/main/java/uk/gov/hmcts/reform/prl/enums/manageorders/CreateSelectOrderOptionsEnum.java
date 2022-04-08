package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CreateSelectOrderOptionsEnum {
    @JsonProperty("standardDirectionsOrder")
    standardDirectionsOrder("standardDirectionsOrder", "Standard directions order"),
    @JsonProperty("blankOrderOrDirections")
    blankOrderOrDirections("blankOrderOrDirections", "Blank order or directions(C21)"),
    @JsonProperty("childArrangementsSpecificProhibitedOrder")
    childArrangementsSpecificProhibitedOrder(
        "childArrangementsSpecificProhibitedOrder",
         "Child arrangements, specific issue or prohibited steps order(C43)"),
    @JsonProperty("parentalResponsibilities")
    parentalResponsibilities("parentalResponsibilities", "Parental responsibilities order(C45A)"),
    @JsonProperty("specialGuardianShip")
    specialGuardianShip("specialGuardianShip", "Special guardianship order(C43A)"),
    @JsonProperty("noticeOfProceedingsNonParties")
    noticeOfProceedingsNonParties("noticeOfProceedingsNonParties",
                                  "Notice of proceedings(C6a)(Notice to non-parties)"),
    @JsonProperty("appointmentOfGuardian")
    appointmentOfGuardian("appointmentOfGuardian", "Appointment of a guardian(C47A)"),
    @JsonProperty("nonMolestation")
    nonMolestation("nonMolestation", "Non-molestation order(FL404A)"),
    @JsonProperty("occupation")
    occupation("occupation", "Occupation order(FL404)"),
    @JsonProperty("powerOfArrest")
    powerOfArrest("powerOfArrest", "Power of arrest(FL406)"),
    @JsonProperty("amendDischargedVaried")
    amendDischargedVaried("amendDischargedVaried", "Amended, discharged or varied order(FL404B)"),
    @JsonProperty("blank")
    blank("blank", "Blank order(FL404B)"),
    @JsonProperty("generalForm")
    generalForm("generalForm", "General form of undertaking(N117)"),
    @JsonProperty("noticeOfProceedings")
    noticeOfProceedings("noticeOfProceedings", "Notice of proceedings(FL402)"),
    @JsonProperty("other")
    other("other", "Other(upload an order)");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CreateSelectOrderOptionsEnum getValue(String key) {
        return CreateSelectOrderOptionsEnum.valueOf(key);
    }
}

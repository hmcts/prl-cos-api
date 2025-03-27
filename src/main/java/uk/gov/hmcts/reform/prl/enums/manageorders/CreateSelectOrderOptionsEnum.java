package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CreateSelectOrderOptionsEnum {
    @JsonProperty("standardDirectionsOrder")
    standardDirectionsOrder("standardDirectionsOrder", "Standard directions order","1"),
    @JsonProperty("directionOnIssue")
    directionOnIssue("directionOnIssue", "Directions on issue","2"),
    @JsonProperty("blankOrderOrDirections")
    blankOrderOrDirections("blankOrderOrDirections", "Blank order or directions (C21)","3"),
    @JsonProperty("childArrangementsSpecificProhibitedOrder")
    childArrangementsSpecificProhibitedOrder(
        "childArrangementsSpecificProhibitedOrder",
         "Child arrangements, specific issue or prohibited steps order (C43)","4"),
    @JsonProperty("parentalResponsibility")
    parentalResponsibility("parentalResponsibility", "Parental responsibility order (C45A)","5"),
    @JsonProperty("specialGuardianShip")
    specialGuardianShip("specialGuardianShip", "Special guardianship order (C43A)","6"),
    @JsonProperty("noticeOfProceedingsParties")
    noticeOfProceedingsParties("noticeOfProceedingsParties",
                                  "Notice of proceedings (C6) (Notice to parties)","7"),
    @JsonProperty("noticeOfProceedingsNonParties")
    noticeOfProceedingsNonParties("noticeOfProceedingsNonParties",
                                  "Notice of proceedings (C6a) (Notice to non-parties)","8"),
    @JsonProperty("transferOfCaseToAnotherCourt")
    transferOfCaseToAnotherCourt("transferOfCaseToAnotherCourt",
                                  "Transfer of case to another court (C49)","9"),
    @JsonProperty("appointmentOfGuardian")
    appointmentOfGuardian("appointmentOfGuardian", "Appointment of a guardian (C47A)","10"),
    @JsonProperty("nonMolestation")
    nonMolestation("nonMolestation", "Non-molestation order (FL404A)","11"),
    @JsonProperty("occupation")
    occupation("occupation", "Occupation order (FL404)","12"),
    @JsonProperty("powerOfArrest")
    powerOfArrest("powerOfArrest", "Power of arrest (FL406)","13"),
    @JsonProperty("amendDischargedVaried")
    amendDischargedVaried("amendDischargedVaried", "Amended, discharged or varied order (FL404B)","14"),
    @JsonProperty("blank")
    blank("blank", "Blank order (FL404B)","15"),
    @JsonProperty("generalForm")
    generalForm("generalForm", "General form of undertaking (N117)","16"),
    @JsonProperty("noticeOfProceedings")
    noticeOfProceedings("noticeOfProceedings", "Notice of proceedings (FL402)","17"),
    @JsonProperty("other")
    other("other", "Other (upload an order)","18");

    private final String id;
    private final String displayedValue;
    private final String optionValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CreateSelectOrderOptionsEnum getValue(String key) {
        return CreateSelectOrderOptionsEnum.valueOf(key);
    }

    public static String mapOptionFromDisplayedValue(String enteredValue) {
        return Arrays.stream(CreateSelectOrderOptionsEnum.values())
            .filter(i -> i.getDisplayedValue().equals(enteredValue))
            .map(i -> "option" + i.optionValue)
            .findFirst().orElse("");
    }

    public static CreateSelectOrderOptionsEnum getIdFromValue(String value) {
        return Arrays.stream(CreateSelectOrderOptionsEnum.values())
            .filter(i -> i.getDisplayedValue().equals(value))
            .findFirst().orElse(null);
    }

    public static String getDisplayedValueFromEnumString(String enteredValue) {
        return Arrays.stream(CreateSelectOrderOptionsEnum.values())
            .map(i -> CreateSelectOrderOptionsEnum.valueOf(enteredValue))
            .map(i -> i.displayedValue)
            .findFirst().orElse("");
    }
}

package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CreateSelectOrderOptionsEnum {
    @CCD(label = "Standard directions order")
    @JsonProperty("standardDirectionsOrder")
    standardDirectionsOrder("standardDirectionsOrder", "Standard directions order","1",
        "Gorchymyn cyfarwyddo safonol"),
    @CCD(label = "Directions on issue")
    @JsonProperty("directionOnIssue")
    directionOnIssue("directionOnIssue", "Directions on issue","2",
        "Cyfarwyddyd ar gychwyn achos"),
    @CCD(label = "Blank order or directions (C21)")
    @JsonProperty("blankOrderOrDirections")
    blankOrderOrDirections("blankOrderOrDirections", "Blank order or directions (C21)","3",
        "Gorchymyn gwag neu gyfarwyddiadau (C21)"),
    @CCD(label = "Child arrangements, specific issue or prohibited steps order (C43)")
    @JsonProperty("childArrangementsSpecificProhibitedOrder")
    childArrangementsSpecificProhibitedOrder(
        "childArrangementsSpecificProhibitedOrder",
         "Child arrangements, specific issue or prohibited steps order (C43)","4",
        "Gorchymyn Trefniadau Plant, Mater Penodol neu Gamau Gwaharddedig (C43)"),
    @CCD(label = "Parental responsibility order (C45A)")
    @JsonProperty("parentalResponsibility")
    parentalResponsibility("parentalResponsibility", "Parental responsibility order (C45A)","5",
        "Gorchymyn cyfrifoldeb rhiant (C45A)"),
    @CCD(label = "Special guardianship order (C43A)")
    @JsonProperty("specialGuardianShip")
    specialGuardianShip("specialGuardianShip", "Special guardianship order (C43A)","6",
        "Gorchymyn Gwarcheidiaeth Arbennig (C43A)"),
    @CCD(label = "Notice of proceedings (C6) (Notice to parties)")
    @JsonProperty("noticeOfProceedingsParties")
    noticeOfProceedingsParties("noticeOfProceedingsParties",
                                  "Notice of proceedings (C6) (Notice to parties)","7",
                  "Hysbysiad o achosion (C6) (Hysbysiad i bartïon)"),
    @CCD(label = "Notice of proceedings (C6a) (Notice to non-parties)")
    @JsonProperty("noticeOfProceedingsNonParties")
    noticeOfProceedingsNonParties("noticeOfProceedingsNonParties",
                                  "Notice of proceedings (C6a) (Notice to non-parties)","8",
                  "Hysbysiad o achosion (C6a) (Hysbysiad i bobl nad ydynt yn bartïon)"),
    @JsonProperty("transferOfCaseToAnotherCourt")
    transferOfCaseToAnotherCourt("transferOfCaseToAnotherCourt",
                                  "Transfer of case to another court (C49)","9",
                  "TTrosglwyddo achos i lys arall (C49)"),
    @CCD(label = "Appointment of a guardian (C47A)")
    @JsonProperty("appointmentOfGuardian")
    appointmentOfGuardian("appointmentOfGuardian", "Appointment of a guardian (C47A)","10",
        "Penodi gwarcheidwad (C47A)"),
    @CCD(label = "Non-molestation order (FL404A)")
    @JsonProperty("nonMolestation")
    nonMolestation("nonMolestation", "Non-molestation order (FL404A)","11",
        "Gorchymyn rhag molestu (FL404A)"),
    @CCD(label = "Occupation order (FL404)")
    @JsonProperty("occupation")
    occupation("occupation", "Occupation order (FL404)","12",
        "Gorchymyn Anheddu (FL404)"),
    @CCD(label = "Power of arrest (FL406)")
    @JsonProperty("powerOfArrest")
    powerOfArrest("powerOfArrest", "Power of arrest (FL406)","13",
        "Pŵer i arestio (FL406)"),
    @CCD(label = "Amended, discharged or varied order (FL404B)")
    @JsonProperty("amendDischargedVaried")
    amendDischargedVaried("amendDischargedVaried", "Amended, discharged or varied order (FL404B)","14",
        "Gorchymyn sydd wedi ei ddiwygio, ei ryddhau neu ei amrywio (FL404B)"),
    @CCD(label = "Blank order (FL404B)")
    @JsonProperty("blank")
    blank("blank", "Blank order (FL404B)","15",
        "Gorchymyn gwag (FL404B)"),
    @CCD(label = "General form of undertaking (N117)")
    @JsonProperty("generalForm")
    generalForm("generalForm", "General form of undertaking (N117)","16",
        "Ffurfeln gyffredinol am ymgymeriad (N117)"),
    @CCD(label = "Notice of proceedings (FL402)")
    @JsonProperty("noticeOfProceedings")
    noticeOfProceedings("noticeOfProceedings", "Notice of proceedings (FL402)","17",
        "Rhybudd o achos (FL402)"),
    @JsonProperty("other")
    other("other", "Other (upload an order)","18",
        "Arall (uwchlwythwch orchymyn)");

    private final String id;
    private final String displayedValue;
    private final String optionValue;
    private final String displayedValueWelsh;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    public String getDisplayedValueWelsh() {
        return displayedValueWelsh;
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

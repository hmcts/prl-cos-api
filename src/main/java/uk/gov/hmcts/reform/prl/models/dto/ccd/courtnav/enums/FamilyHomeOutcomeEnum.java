package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum FamilyHomeOutcomeEnum {

    @JsonProperty("respondentToPayRepairsMaintenance")
    respondentToPayRepairsMaintenance("respondentToPayRepairsMaintenance",
                                      "The applicant needs the respondent to pay for or "
                                          + "contribute to repairs or maintenance to the home"),

    @JsonProperty("respondentToPayRentMortgage")
    respondentToPayRentMortgage("respondentToPayRentMortgage",
                                "The applicant needs the respondent to pay for or contribute to the rent "
                                    + "or the mortgage"),

    @JsonProperty("useOfFurnitureOrContents")
    useOfFurnitureOrContents("useOfFurnitureOrContents",
                             "The applicant needs the use of the furniture or other household contents");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static FamilyHomeOutcomeEnum getValue(String key) {
        return FamilyHomeOutcomeEnum.valueOf(key);
    }
}

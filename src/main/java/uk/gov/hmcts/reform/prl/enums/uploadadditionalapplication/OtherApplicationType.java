package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum OtherApplicationType {

    @JsonProperty("C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION")
    C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION(
        "C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION",
        "C1 - Change surname or remove from jurisdiction"
    ),

    @JsonProperty("C1_APPOINTMENT_OF_A_GUARDIAN")
    C1_APPOINTMENT_OF_A_GUARDIAN("C1_APPOINTMENT_OF_A_GUARDIAN", "C1 - Appointment of a guardian"),

    @JsonProperty("C1_TERMINATION_OF_APPOINTMENT_OF_A_GUARDIAN")
    C1_TERMINATION_OF_APPOINTMENT_OF_A_GUARDIAN(
        "C1_TERMINATION_OF_APPOINTMENT_OF_A_GUARDIAN",
        "C1 - Termination of appointment of a guardian"
    ),

    @JsonProperty("C1_PARENTAL_RESPONSIBILITY")
    C1_PARENTAL_RESPONSIBILITY("C1_PARENTAL_RESPONSIBILITY", "C1 - Parental responsibility"),

    @JsonProperty("C1_WITH_SUPPLEMENT")
    C1_WITH_SUPPLEMENT("C1_WITH_SUPPLEMENT", "C1 - With supplement"),

    @JsonProperty("C3_SEARCH_TAKE_CHARGE_AND_DELIVERY_OF_A_CHILD")
    C3_SEARCH_TAKE_CHARGE_AND_DELIVERY_OF_A_CHILD(
        "C3_SEARCH_TAKE_CHARGE_AND_DELIVERY_OF_A_CHILD",
        "C3 - Search, take charge and delivery of a child"
    ),

    @JsonProperty("C4_WHEREABOUTS_OF_A_MISSING_CHILD")
    C4_WHEREABOUTS_OF_A_MISSING_CHILD("C4_WHEREABOUTS_OF_A_MISSING_CHILD", "C4 - Whereabouts of a missing child"),

    @JsonProperty("C51_PARENTAL_ORDER_APPLICATION")
    C51_PARENTAL_ORDER_APPLICATION("C51_PARENTAL_ORDER_APPLICATION", "C51 - Parental Order application"),

    @JsonProperty("C63_DECLARATION_OF_PARENTAGE")
    C63_DECLARATION_OF_PARENTAGE("C63_DECLARATION_OF_PARENTAGE", "C63 - Declaration of parentage"),

    @JsonProperty("C79_ENFORCE_CHILD_ARRANGEMENTS_ORDER")
    C79_ENFORCE_CHILD_ARRANGEMENTS_ORDER("C79_ENFORCE_CHILD_ARRANGEMENTS_ORDER", "C79 -  Application to enforce a child arrangements order"),

    @JsonProperty("C100_CHILD_ARRANGEMENTS")
    C100_CHILD_ARRANGEMENTS("C100_CHILD_ARRANGEMENTS", "C100 - Child arrangements, prohibited steps or specific issue"),

    @JsonProperty("FL403_APPLICATION_EXTEND_OR_DISCHARGE_ORDER")
    FL403_APPLICATION_EXTEND_OR_DISCHARGE_ORDER("FL403_APPLICATION_EXTEND_OR_DISCHARGE_ORDER", "FL403 - Application to vary," +
        " extend or discharge an order"),

    @JsonProperty("FC600_CONTEMPT_APPLICATION")
    FC600_CONTEMPT_APPLICATION("FC600_CONTEMPT_APPLICATION", "FC600 - Contempt application");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static OtherApplicationType getValue(String key) {
        return OtherApplicationType.valueOf(key);
    }

}

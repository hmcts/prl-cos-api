package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum CombinedC2AdditionalOrdersRequested {

    @JsonProperty("CHANGE_SURNAME_OR_REMOVE_JURISDICTION")
    CHANGE_SURNAME_OR_REMOVE_JURISDICTION(
        "CHANGE_SURNAME_OR_REMOVE_JURISDICTION",
        "Change surname or remove from jurisdiction."
    ),
    @JsonProperty("APPOINTMENT_OF_GUARDIAN")
    APPOINTMENT_OF_GUARDIAN(
        "APPOINTMENT_OF_GUARDIAN",
        "Appointment of a guardian"
    ),
    @JsonProperty("TERMINATION_OF_APPOINTMENT_OF_GUARDIAN")
    TERMINATION_OF_APPOINTMENT_OF_GUARDIAN(
        "TERMINATION_OF_APPOINTMENT_OF_GUARDIAN",
            "Termination of appointment of a guardian"
    ),
    @JsonProperty("PARENTAL_RESPONSIBILITY")
    PARENTAL_RESPONSIBILITY(
        "PARENTAL_RESPONSIBILITY",
            "Parental responsibility"
    ),
    @JsonProperty("REQUESTING_ADJOURNMENT")
    REQUESTING_ADJOURNMENT(
        "REQUESTING_ADJOURNMENT",
            "Requesting an adjournment for a scheduled hearing"
    ),
    @JsonProperty("OTHER")
    OTHER(
        "OTHER",
        "Other"
    );


    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static CombinedC2AdditionalOrdersRequested getValue(String key) {
        return CombinedC2AdditionalOrdersRequested.valueOf(key);
    }
}

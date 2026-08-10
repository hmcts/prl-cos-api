package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum C2AdditionalOrdersRequestedCa {

    @CCD(label = "Change surname or remove from jurisdiction.")
    @JsonProperty("CHANGE_SURNAME_OR_REMOVE_JURISDICTION")
    CHANGE_SURNAME_OR_REMOVE_JURISDICTION(
        "CHANGE_SURNAME_OR_REMOVE_JURISDICTION",
        "Change surname or remove from jurisdiction."
    ),
    @CCD(label = "Appointment of a guardian")
    @JsonProperty("APPOINTMENT_OF_GUARDIAN")
    APPOINTMENT_OF_GUARDIAN(
        "APPOINTMENT_OF_GUARDIAN",
        "Appointment of a guardian"
    ),
    @CCD(label = "Termination of appointment of a guardian")
    @JsonProperty("TERMINATION_OF_APPOINTMENT_OF_GUARDIAN")
    TERMINATION_OF_APPOINTMENT_OF_GUARDIAN(
        "TERMINATION_OF_APPOINTMENT_OF_GUARDIAN",
            "Termination of appointment of a guardian"
    ),
    @CCD(label = "Parental responsibility")
    @JsonProperty("PARENTAL_RESPONSIBILITY")
    PARENTAL_RESPONSIBILITY(
        "PARENTAL_RESPONSIBILITY",
            "Parental responsibility"
    ),
    @CCD(label = "Requesting an adjournment for a scheduled hearing")
    @JsonProperty("REQUESTING_ADJOURNMENT")
    REQUESTING_ADJOURNMENT(
        "REQUESTING_ADJOURNMENT",
            "Requesting an adjournment for a scheduled hearing"
    ),
    @CCD(label = "Other")
    @JsonProperty("OTHER")
    OTHER(
        "OTHER",
        "Other"
    );


    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static C2AdditionalOrdersRequestedCa getValue(String key) {
        return C2AdditionalOrdersRequestedCa.valueOf(key);
    }
}

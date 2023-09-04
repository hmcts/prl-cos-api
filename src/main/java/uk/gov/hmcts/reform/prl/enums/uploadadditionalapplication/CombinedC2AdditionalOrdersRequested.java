package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
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
    ),

    //Citizen AWP reasons
    @JsonProperty("C2_DELAY_CANCEL_HEARING_DATE")
    C2_DELAY_CANCEL_HEARING_DATE("DELAY_CANCEL_HEARING_DATE",
                                 "C2 - Delay or cancel a hearing date"),
    @JsonProperty("C2_REQUEST_MORE_TIME")
    C2_REQUEST_MORE_TIME("C2_REQUEST_MORE_TIME",
                         "C2 - Request for more time"),
    @JsonProperty("C2_CHILD_ARRANGEMENTS_ORDER_TO_LIVE_SPEND_TIME")
    C2_CHILD_ARRANGEMENTS_ORDER_TO_LIVE_SPEND_TIME("C2_CHILD_ARRANGEMENTS_ORDER_TO_LIVE_SPEND_TIME",
                                                   "C2 - Child arrangments order to live with or spend time with"),
    @JsonProperty("C2_PROHIBITED_STEPS_ORDER")
    C2_PROHIBITED_STEPS_ORDER("C2_PROHIBITED_STEPS_ORDER",
                              "C2 - Prohibited steps order"),
    @JsonProperty("C2_SPECIFIC_ISSUE_ORDER")
    C2_SPECIFIC_ISSUE_ORDER("C2_SPECIFIC_ISSUE_ORDER",
                            "C2 - Specific issue order"),
    @JsonProperty("C2_SUBMIT_EVIDENCE_COURT_NOT_REQUESTED")
    C2_SUBMIT_EVIDENCE_COURT_NOT_REQUESTED("C2_SUBMIT_EVIDENCE_COURT_NOT_REQUESTED",
                                           "C2 - Submit the evidence the court has not requested"),
    @JsonProperty("C2_SHARE_DOCUMENTS_WITH_SOMEONE_ELSE")
    C2_SHARE_DOCUMENTS_WITH_SOMEONE_ELSE("C2_SHARE_DOCUMENTS_WITH_SOMEONE_ELSE",
                                         "C2 - Share documents with someone else"),
    @JsonProperty("C2_ASK_TO_JOIN_OR_LEAVE_A_CASE")
    C2_ASK_TO_JOIN_OR_LEAVE_A_CASE("C2_ASK_TO_JOIN_OR_LEAVE_A_CASE",
                          "C2 - Ask to join or leave a case"),
    @JsonProperty("C2_REQUEST_TO_WITHDRAW_APPLICATION")
    C2_REQUEST_TO_WITHDRAW_APPLICATION("C2_REQUEST_TO_WITHDRAW_APPLICATION",
                                       "C2 - Request to withdraw an application"),
    @JsonProperty("C2_ASK_COURT_FOR_APPOINTING_EXPERT")
    C2_ASK_COURT_FOR_APPOINTING_EXPERT("C2_ASK_COURT_FOR_APPOINTING_EXPERT",
                                       "C2 - Request to appoint an expert"),
    @JsonProperty("C2_PERMISSION_FOR_APPLICATION")
    C2_PERMISSION_FOR_APPLICATION("C2_PERMISSION_FOR_APPLICATION",
                                  "C2 - Permission for an application");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CombinedC2AdditionalOrdersRequested getValue(String key) {
        return CombinedC2AdditionalOrdersRequested.valueOf(key);
    }
}

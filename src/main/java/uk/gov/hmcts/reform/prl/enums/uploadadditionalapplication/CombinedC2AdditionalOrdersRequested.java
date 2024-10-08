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
    ),

    //Citizen AWP reasons
    @JsonProperty("C2_DELAY_OR_CANCEL_HEARING_DATE")
    C2_DELAY_OR_CANCEL_HEARING_DATE(
        "C2_DELAY_OR_CANCEL_HEARING_DATE",
        "C2 - Ask to delay or cancel a hearing date"
    ),
    @JsonProperty("C2_REQUEST_MORE_TIME")
    C2_REQUEST_MORE_TIME(
        "C2_REQUEST_MORE_TIME",
        "C2 - Request more time to do what is required by a court order"
    ),
    @JsonProperty("C2_CHILD_ARRANGEMENTS_ORDER_TO_LIVE_WITH_OR_SPEND_TIME")
    C2_CHILD_ARRANGEMENTS_ORDER_TO_LIVE_WITH_OR_SPEND_TIME(
        "C2_CHILD_ARRANGEMENTS_ORDER_TO_LIVE_WITH_OR_SPEND_TIME",
        "C2 - Request an order relating to a child - "
            + "Child arrangements live with, or spend time with order"
    ),
    @JsonProperty("C2_PROHIBITED_STEPS_ORDER")
    C2_PROHIBITED_STEPS_ORDER(
        "C2_PROHIBITED_STEPS_ORDER",
        "C2 - Request an order relating to a child - Prohibited steps order"
    ),
    @JsonProperty("C2_SPECIFIC_ISSUE_ORDER")
    C2_SPECIFIC_ISSUE_ORDER(
        "C2_SPECIFIC_ISSUE_ORDER",
        "C2 - Request an order relating to a child - Specific issue order"
    ),
    @JsonProperty("C2_SUBMIT_EVIDENCE_THE_COURT_HAS_NOT_REQUESTED")
    C2_SUBMIT_EVIDENCE_THE_COURT_HAS_NOT_REQUESTED(
        "C2_SUBMIT_EVIDENCE_THE_COURT_HAS_NOT_REQUESTED",
        "C2 - Ask to submit evidence the court has not requested"
    ),
    @JsonProperty("C2_SHARE_DOCUMENTS_WITH_SOMEONE_ELSE")
    C2_SHARE_DOCUMENTS_WITH_SOMEONE_ELSE(
        "C2_SHARE_DOCUMENTS_WITH_SOMEONE_ELSE",
        "C2 - Ask to share documents with someone else"
    ),
    @JsonProperty("C2_ASK_TO_JOIN_OR_LEAVE_A_CASE")
    C2_ASK_TO_JOIN_OR_LEAVE_A_CASE(
        "C2_ASK_TO_JOIN_OR_LEAVE_A_CASE",
        "C2 - Ask to join or leave a case"
    ),
    @JsonProperty("C2_REQUEST_TO_WITHDRAW_AN_APPLICATION")
    C2_REQUEST_TO_WITHDRAW_AN_APPLICATION(
        "C2_REQUEST_TO_WITHDRAW_AN_APPLICATION",
        "C2 - Request to withdraw an application"
    ),
    @JsonProperty("C2_REQUEST_TO_APPOINT_AN_EXPERT")
    C2_REQUEST_TO_APPOINT_AN_EXPERT(
        "C2_REQUEST_TO_APPOINT_AN_EXPERT",
        "C2 - Ask the court to appoint an expert"
    ),
    @JsonProperty("C2_PERMISSION_FOR_AN_APPLICATION_IF_COURT_PREVIOUSLY_STOPPED_YOU")
    C2_PERMISSION_FOR_AN_APPLICATION_IF_COURT_PREVIOUSLY_STOPPED_YOU(
            "C2_PERMISSION_FOR_AN_APPLICATION_IF_COURT_PREVIOUSLY_STOPPED_YOU",
            "C2 - Get permission for an application if the court previously stopped you"
        );

    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static CombinedC2AdditionalOrdersRequested getValue(String key) {
        return CombinedC2AdditionalOrdersRequested.valueOf(key);
    }
}

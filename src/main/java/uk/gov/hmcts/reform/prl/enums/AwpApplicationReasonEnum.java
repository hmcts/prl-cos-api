package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum AwpApplicationReasonEnum {

    @JsonProperty("REQUEST_PARENTAL_RESPONSIBILITY")
    REQUEST_PARENTAL_RESPONSIBILITY(
        "request-grant-for-parental-responsibility",
        "request-grant-for-parental-responsibility"
    ),
    @JsonProperty("REQUEST_GUARDIAN_FOR_CHILD")
    REQUEST_GUARDIAN_FOR_CHILD(
        "request-appoint-a-guardian-for-child",
        "request-appoint-a-guardian-for-child"
    ),
    @JsonProperty("DELAY_CANCEL_HEARING_DATE")
    DELAY_CANCEL_HEARING_DATE(
        "delay-or-cancel-hearing-date",
        "delay-or-cancel-hearing-date"
    ),
    @JsonProperty("REQUEST_MORE_TIME")
    REQUEST_MORE_TIME(
        "request-more-time",
        "request-more-time"
    ),
    @JsonProperty("CHILD_ARRANGEMENTS_ORDER_TO_LIVE_SPEND_TIME")
    CHILD_ARRANGEMENTS_ORDER_TO_LIVE_SPEND_TIME(
        "child-arrangements-order-to-live-with-or-spend-time",
        "child-arrangements-order-to-live-with-or-spend-time"
    ),
    @JsonProperty("PROHIBITED_STEPS_ORDER")
    PROHIBITED_STEPS_ORDER(
        "prohibited-steps-order",
        "prohibited-steps-order"
    ),
    @JsonProperty("SPECIFIC_ISSUE_ORCDER")
    SPECIFIC_ISSUE_ORCDER(
        "specfic-issue-order",
        "specfic-issue-order"
    ),
    @JsonProperty("SUBMIT_EVIDENCE_COURT_NOT_REQUESTED")
    SUBMIT_EVIDENCE_COURT_NOT_REQUESTED(
        "submit-evidence-the-court-has-not-requested",
        "submit-evidence-the-court-has-not-requested"
    ),
    @JsonProperty("SHARE_DOCUMENTS_WITH_SOMEONE_ELSE")
    SHARE_DOCUMENTS_WITH_SOMEONE_ELSE(
        "share-documents-with-someone-else",
        "share-documents-with-someone-else"
    ),
    @JsonProperty("JOIN_OR_LEAVE_CASE")
    JOIN_OR_LEAVE_CASE(
        "ask-to-join-or-leave-a-case",
        "ask-to-join-or-leave-a-case"
    ),
    @JsonProperty("REQUEST_TO_WITHDRAW_APPLICATION")
    REQUEST_TO_WITHDRAW_APPLICATION(
        "request-to-withdraw-an-application",
        "request-to-withdraw-an-application"
    ),
    @JsonProperty("ASK_COURT_FOR_APPOINTING_EXPERT")
    ASK_COURT_FOR_APPOINTING_EXPERT(
        "request-to-appoint-an-expert",
        "request-to-appoint-an-expert"
    ),
    @JsonProperty("PERMISSION_FOR_APPLICATION")
    PERMISSION_FOR_APPLICATION(
        "permission-for-an-application-if-court-previously-stopped-you",
        "permission-for-an-application-if-court-previously-stopped-you"
    ),
    @JsonProperty("ORDER_AUTHORISING_SEARCH")
    ORDER_AUTHORISING_SEARCH(
        "order-authorising-search-for-taking-charge-of-and-delivery-of-a-child",
        "order-authorising-search-for-taking-charge-of-and-delivery-of-a-child"
    ),
    @JsonProperty("ORDER_TO_KNOW_ABOUT_CHILD")
    ORDER_TO_KNOW_ABOUT_CHILD(
        "ask-court-to-order-someone-to-provide-child-information",
        "ask-court-to-order-someone-to-provide-child-information"
    ),
    @JsonProperty("ENFORCE_CHILD_ARRANGEMENTS_ORDER")
    ENFORCE_CHILD_ARRANGEMENTS_ORDER(
        "enforce-a-child-arrangements-order",
        "enforce-a-child-arrangements-order"
    ),
    @JsonProperty("DELIVER_PAPER_TO_OTHER_PARTY")
    DELIVER_PAPER_TO_OTHER_PARTY(
        "ask-to-deliver-paper-to-other-party",
        "ask-to-deliver-paper-to-other-party"
    ),
    @JsonProperty("YOU_ACCUSED_SOMEONE")
    YOU_ACCUSED_SOMEONE(
        "prevent-questioning-in-person-accusing-someone",
        "prevent-questioning-in-person-accusing-someone"
    ),
    @JsonProperty("ACCUSED_BY_SOMEONE")
    ACCUSED_BY_SOMEONE(
        "prevent-questioning-in-person-someone-accusing-you",
        "prevent-questioning-in-person-someone-accusing-you"
    ),
    @JsonProperty("REQUEST_FOR_ORDER_WITNESS")
    REQUEST_FOR_ORDER_WITNESS(
        "request-to-order-a-witness-to-attend-court",
        "request-to-order-a-witness-to-attend-court"
    ),
    @JsonProperty("REQUEST_COURT_TO_ACT_DURING_DISOBEY")
    REQUEST_COURT_TO_ACT_DURING_DISOBEY(
        "request-court-to-act-when-someone-in-the-case-is-disobeying-court-order",
        "request-court-to-act-when-someone-in-the-case-is-disobeying-court-order"
    ),
    @JsonProperty("APPEAL_COURT_ORDER")
    APPEAL_COURT_ORDER(
        "appeal-a-order-or-ask-permission-to-appeal",
        "appeal-a-order-or-ask-permission-to-appeal"
    ),
    @JsonProperty("CHANGE_EXTEND_CANCEL_NON_MOLESTATION_OR_OCCUPATION_ORDER")
    CHANGE_EXTEND_CANCEL_NON_MOLESTATION_OR_OCCUPATION_ORDER(
        "change-extend-or-cancel-non-molestation-order-or-occupation-order",
        "change-extend-or-cancel-non-molestation-order-or-occupation-order"
    ),
    @JsonProperty("REQUEST_FOR_ARREST_WARRENT")
    REQUEST_FOR_ARREST_WARRENT(
        "request-the-court-issues-an-arrest-warrant",
        "request-the-court-issues-an-arrest-warrant"
    );


    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static AwpApplicationReasonEnum getValue(String key) {
        return AwpApplicationReasonEnum.valueOf(key);
    }


}

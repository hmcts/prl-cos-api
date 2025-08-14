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
public enum OtherApplicationType {

    @JsonProperty("C1_CHILD_ORDER")
    C1_CHILD_ORDER(
        "C1_CHILD_ORDER",
        "C1 - Apply for certain orders under the Children Act"
    ),
    @JsonProperty("C3_CHILD_ORDER")
    C3_CHILD_ORDER(
        "C3_CHILD_ORDER",
        "C3 - Application for an order authorizing search and taking charge of a child"
    ),
    @JsonProperty("C4_CHILD_ORDER")
    C4_CHILD_ORDER(
        "C4_CHILD_ORDER",
        "C4 - Application for an order for disclosure of a child’s whereabouts"
    ),
    @JsonProperty("C79_CHILD_ORDER")
    C79_CHILD_ORDER(
        "C79_CHILD_ORDER",
        "C79 - Application to enforce a child arrangements order"
    ),
    @JsonProperty("EX740_CROSS_EXAMINATION_VICTIM")
    EX740_CROSS_EXAMINATION_VICTIM(
        "EX740_CROSS_EXAMINATION_VICTIM",
        "EX740 - Application to prohibit cross examination (victim)"
    ),
    @JsonProperty("EX741_CROSS_EXAMINATION_PERPETRATOR")
    EX741_CROSS_EXAMINATION_PERPETRATOR(
        "EX741_CROSS_EXAMINATION_PERPETRATOR",
        "EX741 - Application to prohibit cross examination (perpetrator)"
    ),
    @JsonProperty("FP25_WITNESS_SUMMONS")
    FP25_WITNESS_SUMMONS(
        "FP25_WITNESS_SUMMONS",
        "FP25 - Witness summons"
    ),
    @JsonProperty("FC600_COMMITTAL_APPLICATION")
    FC600_COMMITTAL_APPLICATION(
        "FC600_COMMITTAL_APPLICATION",
        "FC600 - Committal application"
    ),
    @JsonProperty("N161_APPELLANT_NOTICE")
    N161_APPELLANT_NOTICE(
        "N161_APPELLANT_NOTICE",
        "N161 - Appellant’s notice"
    ),
    @JsonProperty("FL403_EXTEND_AN_ORDER")
    FL403_EXTEND_AN_ORDER(
        "FL403_EXTEND_AN_ORDER",
                "FL403 - Application to vary, discharge or extend an order"
    ),
    @JsonProperty("FL407_ARREST_WARRANT")
    FL407_ARREST_WARRANT(
        "FL407_ARREST_WARRANT",
                "FL407 - Application for a warrant of arrest"
    ),
    @JsonProperty("D89_COURT_BAILIFF")
    D89_COURT_BAILIFF(
        "D89_COURT_BAILIFF",
        "D89 - Request for personal service by a court bailiff"
    ),

    //Citizen AWP reasons
    @JsonProperty("C1_REQUEST_GRANT_FOR_PARENTAL_RESPONSIBILITY")
    C1_REQUEST_GRANT_FOR_PARENTAL_RESPONSIBILITY(
        "C1_REQUEST_GRANT_FOR_PARENTAL_RESPONSIBILITY",
        "C1 - Request the court grants you parental responsibility"
    ),
    @JsonProperty("C1_REQUEST_APPOINT_A_GUARDIAN_FOR_CHILD")
    C1_REQUEST_APPOINT_A_GUARDIAN_FOR_CHILD(
        "C1_REQUEST_APPOINT_A_GUARDIAN_FOR_CHILD",
        "C1 - Request the court appoints a guardian for child"
    ),
    @JsonProperty("C3_ORDER_AUTHORISING_SEARCH_FOR_TAKING_CHARGE_OF_AND_DELIVERY_OF_A_CHILD")
    C3_ORDER_AUTHORISING_SEARCH_FOR_TAKING_CHARGE_OF_AND_DELIVERY_OF_A_CHILD(
        "C3_ORDER_AUTHORISING_SEARCH_FOR_TAKING_CHARGE_OF_AND_DELIVERY_OF_A_CHILD",
        "C3 - Ask for an order authorizing search for, taking charge of and delivery of a child"
    ),
    @JsonProperty("C4_ASK_COURT_TO_ORDER_SOMEONE_TO_PROVIDE_CHILD_INFORMATION")
    C4_ASK_COURT_TO_ORDER_SOMEONE_TO_PROVIDE_CHILD_INFORMATION(
        "C4_ASK_COURT_TO_ORDER_SOMEONE_TO_PROVIDE_CHILD_INFORMATION",
        "C4 - Ask the court to order someone to provide information on where child is"
    ),
    @JsonProperty("C79_ENFORCE_A_CHILD_ARRANGEMENTS_ORDER")
    C79_ENFORCE_A_CHILD_ARRANGEMENTS_ORDER(
        "C79_ENFORCE_A_CHILD_ARRANGEMENTS_ORDER",
        "C79 - Enforce a Child Arrangements Order"
    ),
    @JsonProperty("D89_ASK_TO_DELIVER_PAPER_TO_OTHER_PARTY")
    D89_ASK_TO_DELIVER_PAPER_TO_OTHER_PARTY(
        "D89_ASK_TO_DELIVER_PAPER_TO_OTHER_PARTY",
        "D89 - Ask the court to deliver papers to the other party"
    ),
    @JsonProperty("EX740_PREVENT_QUESTIONING_IN_PERSON_ACCUSING_SOMEONE")
    EX740_PREVENT_QUESTIONING_IN_PERSON_ACCUSING_SOMEONE(
        "EX740_PREVENT_QUESTIONING_IN_PERSON_ACCUSING_SOMEONE",
        "EX740 - Ask the court to prevent questioning in person - you accused someone"
    ),
    @JsonProperty("EX741_PREVENT_QUESTIONING_IN_PERSON_SOMEONE_ACCUSING_YOU")
    EX741_PREVENT_QUESTIONING_IN_PERSON_SOMEONE_ACCUSING_YOU(
        "EX741_PREVENT_QUESTIONING_IN_PERSON_SOMEONE_ACCUSING_YOU",
        "EX741 - Ask the court to prevent questioning in person - someone accused you"
    ),
    @JsonProperty("FP25_REQUEST_TO_ORDER_A_WITNESS_TO_ATTEND_COURT")
    FP25_REQUEST_TO_ORDER_A_WITNESS_TO_ATTEND_COURT(
        "FP25_REQUEST_TO_ORDER_A_WITNESS_TO_ATTEND_COURT",
        "FP25 - Make a request to order a witness to attend court"
    ),
    @JsonProperty("FC600_REQUEST_COURT_TO_ACT_WHEN_SOMEONE_IN_THE_CASE_IS_DISOBEYING_COURT_ORDER")
    FC600_REQUEST_COURT_TO_ACT_WHEN_SOMEONE_IN_THE_CASE_IS_DISOBEYING_COURT_ORDER(
            "FC600_REQUEST_COURT_TO_ACT_WHEN_SOMEONE_IN_THE_CASE_IS_DISOBEYING_COURT_ORDER",
            "FC600 - Request the court acts when someone in the case is disobeying a court order"
        ),
    @JsonProperty("N161_APPEAL_A_ORDER_OR_ASK_PERMISSION_TO_APPEAL")
    N161_APPEAL_A_ORDER_OR_ASK_PERMISSION_TO_APPEAL(
        "N161_APPEAL_A_ORDER_OR_ASK_PERMISSION_TO_APPEAL",
        "N161 - Appeal a court order or ask for permission to appeal"
    ),
    @JsonProperty("FL403_CHANGE_EXTEND_OR_CANCEL_NON_MOLESTATION_ORDER_OR_OCCUPATION_ORDER")
    FL403_CHANGE_EXTEND_OR_CANCEL_NON_MOLESTATION_ORDER_OR_OCCUPATION_ORDER(
        "FL403_CHANGE_EXTEND_OR_CANCEL_NON_MOLESTATION_ORDER_OR_OCCUPATION_ORDER",
        "FL403 - Apply to change, extend or cancel a non-molestation order or occupation order"
    ),
    @JsonProperty("FL407_REQUEST_THE_COURT_ISSUES_AN_ARREST_WARRANT")
    FL407_REQUEST_THE_COURT_ISSUES_AN_ARREST_WARRANT(
        "FL407_REQUEST_THE_COURT_ISSUES_AN_ARREST_WARRANT",
        "FL407 - Request the court issues an arrest warrant"
    );


    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static OtherApplicationType getValue(String key) {
        return OtherApplicationType.valueOf(key);
    }

}

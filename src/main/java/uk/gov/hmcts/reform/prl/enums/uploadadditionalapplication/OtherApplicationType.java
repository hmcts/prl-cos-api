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
    @JsonProperty("C63_DECLARATION_OF_PARENTAGE")
    C63_DECLARATION_OF_PARENTAGE(
        "C63_DECLARATION_OF_PARENTAGE",
        "C63 - Declaration of parentage"
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
    @JsonProperty("C1_REQUEST_PARENTAL_RESPONSIBILITY")
    C1_REQUEST_PARENTAL_RESPONSIBILITY("C1_REQUEST_PARENTAL_RESPONSIBILITY",
                                       "C1 - Request to grant parental responsibility"),
    @JsonProperty("C1_REQUEST_GUARDIAN_FOR_CHILD")
    C1_REQUEST_GUARDIAN_FOR_CHILD("C1_REQUEST_GUARDIAN_FOR_CHILD",
                                  "C1 - Request to appoint a guardian for the child"),
    @JsonProperty("C3_ORDER_AUTHORISING_SEARCH")
    C3_ORDER_AUTHORISING_SEARCH("C3_ORDER_AUTHORISING_SEARCH",
                                "C3 - Request for an order authorizing search for, taking charge of and delivery of a child"),
    @JsonProperty("C4_ORDER_TO_KNOW_ABOUT_CHILD")
    C4_ORDER_TO_KNOW_ABOUT_CHILD("C4_ORDER_TO_KNOW_ABOUT_CHILD",
                                 "C4 - Request the court to order someone to provide information on where a child is"),
    @JsonProperty("C79_ENFORCE_CHILD_ARRANGEMENTS_ORDER")
    C79_ENFORCE_CHILD_ARRANGEMENTS_ORDER("C79_ENFORCE_CHILD_ARRANGEMENTS_ORDER",
                                         "C79 - Enforce a Child Arrangements Order"),
    @JsonProperty("D89_DELIVER_PAPER_TO_OTHER_PARTY")
    D89_DELIVER_PAPER_TO_OTHER_PARTY("D89_DELIVER_PAPER_TO_OTHER_PARTY",
                                     "D89 - Request the court to deliver papers to the other party"),
    @JsonProperty("EX740_YOU_ACCUSED_SOMEONE")
    EX740_YOU_ACCUSED_SOMEONE("EX740_YOU_ACCUSED_SOMEONE",
                              "EX740 - Request the court to prevent questioning in person with accused"),
    @JsonProperty("EX741_ACCUSED_BY_SOMEONE")
    EX741_ACCUSED_BY_SOMEONE("EX741_ACCUSED_BY_SOMEONE",
                             "EX741 - Request the court to prevent questioning in person if someone has accused you"),
    @JsonProperty("FP25_REQUEST_FOR_ORDER_WITNESS")
    FP25_REQUEST_FOR_ORDER_WITNESS("FP25_REQUEST_FOR_ORDER_WITNESS",
                                   "FP25 - Request the court to order a witness to attend the court"),
    @JsonProperty("FC600_REQUEST_COURT_TO_ACT_DURING_DISOBEY")
    FC600_REQUEST_COURT_TO_ACT_DURING_DISOBEY("FC600_REQUEST_COURT_TO_ACT_DURING_DISOBEY",
                                              "FC600 - Request the court to act when someone is disobeying a court order"),
    @JsonProperty("N161_APPEAL_COURT_ORDER")
    N161_APPEAL_COURT_ORDER("N161_APPEAL_COURT_ORDER",
                            "N161 - Appeal a court order or ask for permission to appeal"),
    @JsonProperty("FL403_CHANGE_EXTEND_CANCEL_NON_MOLESTATION_OR_OCCUPATION_ORDER")
    FL403_CHANGE_EXTEND_CANCEL_NON_MOLESTATION_OR_OCCUPATION_ORDER("FL403_CHANGE_EXTEND_CANCEL_NON_MOLESTATION_OR_OCCUPATION_ORDER",
                                                                   "FL403 - Apply to change, extend or cancel a non-molestation or occupation order"),
    @JsonProperty("FL407_REQUEST_FOR_ARREST_WARRANT")
    FL407_REQUEST_FOR_ARREST_WARRANT("FL407_REQUEST_FOR_ARREST_WARRANT",
                                     "FL407 - Request the court to issue an arrest warrant");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    @JsonCreator
    public static OtherApplicationType getValue(String key) {
        return OtherApplicationType.valueOf(key);
    }

}

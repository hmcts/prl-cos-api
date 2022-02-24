package uk.gov.hmcts.reform.prl.enums;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EventErrorsEnum {

    CHILD_DETAILS_ERROR("Add child details"),
    APPLICANTS_DETAILS_ERROR("Add applicant details"),
    TYPE_OF_APPLICATION_ERROR("Add details about the type of application"),
    RESPONDENT_DETAILS_ERROR("Add respondent details"),
    LITIGATION_CAPACITY_ERROR("Add the litigation capacity details"),
    INTERNATIONAL_ELEMENT_ERROR("Add the international element details"),
    HEARING_URGENCY_ERROR("Add hearing urgency details"),
    MIAM_ERROR("Add MIAM details"),
    OTHER_PEOPLE_ERROR("Add the details about other people in the case"),
    ATTENDING_THE_HEARING_ERROR("Add details about attending the hearing"),
    WELSH_LANGUAGE_ERROR("Add details about welsh language requirements"),
    OTHER_PROCEEDINGS_ERROR("Add details about other proceedings"),
    ALLEGATIONS_OF_HARM_ERROR("Add details about allegations of harm"),

    //FL401 ERRORS
    WITHOUT_NOTICE_ORDER_ERROR("Add Without Notice Order details"),
    RELATIONSHIP_TO_RESPONDENT_ERROR("Add details about relationship to respondent"),
    FL401_APPLICANT_FAMILY_ERROR("Add details about applicant's family"),
    FL401_TYPE_OF_APPLICATION_ERROR("Ensure you have added all relevant information"),
    RESPONDENT_BEHAVIOUR_ERROR("Add details about respondent behaviour"),

    //FL401_TYPE_OF_APPLICATION_ERRORS
    FL401_TYPE_OF_APPLICATION_TYPE_OF_ORDER_ERROR("Add type of order"),
    FL401_TYPE_OF_APPLICATION_CHILD_ARRANGEMENTS_ERROR("Add child arrangement case number"),

    //WITHOUT_NOTICE_ORDER_ERRORS
    WITHOUT_NOTICE_ORDER_STATUS_ERROR("Add information on 'without notice' order status"),
    WITHOUT_NOTICE_ORDER_DETAILS_ERROR("Add information on 'without notice' order details"),
    WITHOUT_NOTICE_ORDER_BAIL_ERROR("Add information on bail conditions"),

    //RELATIONSHIP_TO_RESPONDENT_ERRORS
    RELATIONSHIP_TO_RESPONDENT_DETAILS_ERROR("Add relationship details"),

    //RESPONDENT_BEHAVIOUR_ERRORS
    RESPONDENT_BEHAVIOUR_DETAILS_ERROR("Add what the applicant wants the respond to stop doing");

    private final String error;

    @JsonValue
    public String getError() {
        return error;
    }

    @JsonCreator
    public static EventErrorsEnum getValue(String key) {
        return EventErrorsEnum.valueOf(key);
    }

}

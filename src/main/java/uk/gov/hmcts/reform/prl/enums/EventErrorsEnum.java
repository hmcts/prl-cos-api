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
    FL401_APPLICANT_FAMILY_ERROR("Add details about applicant's family");

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

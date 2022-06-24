package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
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
    WITHOUT_NOTICE_ORDER_ERROR("Ensure all relevant information has been added"),
    RELATIONSHIP_TO_RESPONDENT_ERROR("Ensure you have added all relevant information"),
    FL401_APPLICANT_FAMILY_ERROR("Ensure you have completed all relevant information"),
    FL401_TYPE_OF_APPLICATION_ERROR("Ensure you have added all relevant information"),
    FL401_OTHER_PROCEEDINGS_ERROR("Ensure that all the relevant information has been added"),
    RESPONDENT_BEHAVIOUR_ERROR("Ensure you have added relevant information"),
    HOME_ERROR("Ensure you have added all relevant details");

    private final String error;

}

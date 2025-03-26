package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventErrorsEnum { 

    CHILD_DETAILS_ERROR("Add people to the case details to / Ychwanegu pobl at fanylion yr achos ar gyfer"),
    APPLICANTS_DETAILS_ERROR("Add people to the case details / Ychwanegu pobl at fanylion yr achos"),
    TYPE_OF_APPLICATION_ERROR("Add details about the type of application / Ychwanegu manylion am y math o gais"),
    RESPONDENT_DETAILS_ERROR("Add people to the case details / Ychwanegu pobl at fanylion yr achos ar gyfer"),
    LITIGATION_CAPACITY_ERROR("Add the litigation capacity details / Ychwanegu manylion am gapasiti cyfreitha"),
    INTERNATIONAL_ELEMENT_ERROR("Add the international element details / Ychwanegu'r manylion elfen ryngwladol"),
    HEARING_URGENCY_ERROR("Add hearing urgency details / Ychwanegu manylion gwrandawiad brys"),
    MIAM_ERROR("Add MIAM details / Ychwanegu manylion MIAM"),
    MIAM_POLICY_UPGRADE_ERROR("Add MIAM details / Ychwanegu manylion MIAM"),
    OTHER_PEOPLE_ERROR("Add the details about other people in the case / Ychwanegu manylion am bobl eraill yn yr achos"),
    OTHER_PEOPLE_REVISED_ERROR(
        "Add the details about other people in the case / Ychwanegu manylion am bobl eraill yn yr achos"),
    ATTENDING_THE_HEARING_ERROR("Add details about attending the hearing / Ychwanegu manylion am fynychu'r gwrandawiad"),
    WELSH_LANGUAGE_ERROR(
        "Add details about welsh language requirements / Ychwanegu manylion am ofynion yr iaith Gymraeg"),
    OTHER_PROCEEDINGS_ERROR("Add details about other proceedings / Ychwanegu manylion am achosion eraill"),
    ALLEGATIONS_OF_HARM_ERROR("Add details about allegations of harm / Ychwanegu manylion am honiadau o niwed"),
    OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION_ERROR(
        "Add people to the case details / Ychwanegu pobl at fanylion yr achos"),
    CHILDREN_AND_APPLICANTS_ERROR("Add people to the case details / Ychwanegu pobl at fanylion yr achos"),
    CHILDREN_AND_RESPONDENTS_ERROR("Add people to the case details / Ychwanegu pobl at fanylion yr achos"),
    CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION_ERROR(
        "Add people to the case details to / Ychwanegu pobl at fanylion yr achos ar gyfer"),
    ALLEGATIONS_OF_HARM_REVISED_ERROR("Add details about Allegations of harm / Ychwanegu manylion am honiadau o niwed"),

    CHILD_DETAILS_REVISED_ERROR("Add child details / Ychwanegu manylion y plentyn"),
    //FL401 ERRORS
    WITHOUT_NOTICE_ORDER_ERROR(
        "Ensure all relevant information has been added / Sicrhau bod yr holl wybodaeth berthnasol wedi'i hychwanegu"),
    RELATIONSHIP_TO_RESPONDENT_ERROR(
        "Ensure you have added all relevant information / Sicrhau eich bod wedi ychwanegu'r holl wybodaeth berthnasol"),
    FL401_APPLICANT_FAMILY_ERROR(
        "Ensure you have completed all relevant information / Sicrhau eich bod wedi darparu'r holl wybodaeth berthnasol"),
    FL401_TYPE_OF_APPLICATION_ERROR(
        "Ensure you have added all relevant information / Sicrhau eich bod wedi ychwanegu'r holl wybodaeth berthnasol"),
    FL401_OTHER_PROCEEDINGS_ERROR(
        "Ensure that all the relevant information has been added / Sicrhau bod yr holl wybodaeth berthnasol wedi'i hychwanegu"),
    RESPONDENT_BEHAVIOUR_ERROR(
        "Ensure you have added relevant information / Sicrhau eich bod wedi ychwanegu'r wybodaeth berthnasol"),
    HOME_ERROR("Ensure you have added all relevant details / Sicrhau eich bod wedi ychwanegu'r holl fanylion perthnasol");

    private final String error;

}

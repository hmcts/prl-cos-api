package uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RespondentEventErrorsEnum {

    CONSENT_ERROR("Ensure all mandatory (marked with *) information has been added"),
    KEEP_DETAILS_PRIVATE_ERROR("Ensure all mandatory (marked with *) information has been added"),
    CONFIRM_EDIT_CONTACT_DETAILS_ERROR("Ensure all mandatory (marked with *) information has been added"),
    ATTENDING_THE_COURT_ERROR("Ensure all mandatory (marked with *) information has been added"),
    MIAM_ERROR("Ensure all mandatory (marked with *) information has been added"),
    OTHER_PROCEEDINGS_ERROR("Ensure all mandatory (marked with *) information has been added"),
    ALLEGATION_OF_HARM_ERROR("Ensure all mandatory (marked with *) information has been added"),
    RESPONSE_TO_ALLEGATION_OF_HARM_ERROR("Ensure all mandatory (marked with *) information has been added"),
    INTERNATIONAL_ELEMENT_ERROR("Ensure all mandatory (marked with *) information has been added"),
    ABILITY_TO_PARTICIPATE_ERROR("Ensure all mandatory (marked with *) information has been added");

    private final String error;

}

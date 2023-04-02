package uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RespondentEventErrorsEnum {

    CONSENT("Ensure all relevant information has been added"),
    KEEP_DETAILS_PRIVATE("Ensure all relevant information has been added"),
    CONFIRM_EDIT_CONTACT_DETAILS("Ensure all relevant information has been added"),
    ATTENDING_THE_COURT("Ensure all relevant information has been added"),
    MIAM("Ensure all relevant information has been added"),
    CURRENT_OR_PREVIOUS_PROCEEDINGS("Ensure all relevant information has been added"),
    ALLEGATION_OF_HARM("Ensure all relevant information has been added"),
    INTERNATIONAL_ELEMENT("Ensure all relevant information has been added"),
    ABILITY_TO_PARTICIPATE("Ensure all relevant information has been added");

    private final String error;

}

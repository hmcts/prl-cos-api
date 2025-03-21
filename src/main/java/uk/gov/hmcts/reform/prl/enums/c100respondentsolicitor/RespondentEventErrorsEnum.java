package uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RespondentEventErrorsEnum {

    CONSENT_ERROR("Ensure all mandatory (marked with *) information has been added "
        + "/ Sicrhewch fod yr holl wybodaeth orfodol (wedi'i marcio â *) wedi'i hychwanegu"),
    KEEP_DETAILS_PRIVATE_ERROR("Ensure all mandatory (marked with *) information has been added "
        + "/ Sicrhewch fod yr holl wybodaeth orfodol (wedi'i marcio â *) wedi'i hychwanegu"),
    CONFIRM_EDIT_CONTACT_DETAILS_ERROR("Ensure all mandatory (marked with *) information has been added "
        + "/ Sicrhewch fod yr holl wybodaeth orfodol (wedi'i marcio â *) wedi'i hychwanegu"),
    ATTENDING_THE_COURT_ERROR("Ensure all mandatory (marked with *) information has been added "
        + "/ Sicrhewch fod yr holl wybodaeth orfodol (wedi'i marcio â *) wedi'i hychwanegu"),
    MIAM_ERROR("Ensure all mandatory (marked with *) information has been added "
        + "/ Sicrhewch fod yr holl wybodaeth orfodol (wedi'i marcio â *) wedi'i hychwanegu"),
    OTHER_PROCEEDINGS_ERROR("Ensure all mandatory (marked with *) information has been added "
        + "/ Sicrhewch fod yr holl wybodaeth orfodol (wedi'i marcio â *) wedi'i hychwanegu"),
    ALLEGATION_OF_HARM_ERROR("Ensure all mandatory (marked with *) information has been added "
        + "/ Sicrhewch fod yr holl wybodaeth orfodol (wedi'i marcio â *) wedi'i hychwanegu"),
    RESPONSE_TO_ALLEGATION_OF_HARM_ERROR("Ensure all mandatory (marked with *) information has been added "
        + "/ Sicrhewch fod yr holl wybodaeth orfodol (wedi'i marcio â *) wedi'i hychwanegu"),
    INTERNATIONAL_ELEMENT_ERROR("Ensure all mandatory (marked with *) information has been added "
        + "/ Sicrhewch fod yr holl wybodaeth orfodol (wedi'i marcio â *) wedi'i hychwanegu"),
    ABILITY_TO_PARTICIPATE_ERROR("Ensure all mandatory (marked with *) information has been added "
        + "/ Sicrhewch fod yr holl wybodaeth orfodol (wedi'i marcio â *) wedi'i hychwanegu");

    private final String error;

}

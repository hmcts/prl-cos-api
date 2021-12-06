package uk.gov.hmcts.reform.prl.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventErrorsEnum {

    LITIGATION_CAPACITY_ERROR("Add the litigation capacity details"),
    INTERNATIONAL_ELEMENT_ERROR("Add the international element details"),
    OTHER_PEOPLE_ERROR("Add the details about other people in the case"),
    ATTENDING_THE_HEARING_ERROR("Add details about attending the hearing"),
    WELSH_LANGUAGE_ERROR("Add details about welsh language requirements");



    private final String error;



}

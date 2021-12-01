package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Event {

    TYPE_OF_APPLICATION("selectApplicationType", "Type of application"),
    ATTENDING_THE_HEARING("attendingTheHearing", "Attending the hearing"),
    HEARING_URGENCY("hearingUrgency",  "Hearing urgency"),
    APPLICANT_DETAILS("applicantsDetails", "Applicant details"),
    CHILD_DETAILS("childDetails", "Child details"),
    RESPONDENT_DETAILS("respondentsDetails", "Respondent details"),
    MIAM("miam", "MIAM"),
    CASE_NAME("caseName", "Case name"),
    ALLEGATIONS_OF_HARM("allegationsOfHarm", "Allegations of harm");

    private final String id;
    private final String name;

}

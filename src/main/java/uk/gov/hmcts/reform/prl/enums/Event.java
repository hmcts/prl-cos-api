package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@RequiredArgsConstructor
public enum Event {


    CASE_NAME("caseName", "Case name"),
    TYPE_OF_APPLICATION("selectApplicationType", "Type of application"),
    HEARING_URGENCY("hearingUrgency",  "Hearing urgency"),
    APPLICANT_DETAILS("applicantsDetails", "Applicant details"),
    CHILD_DETAILS("childDetails", "Child details"),
    RESPONDENT_DETAILS("respondentsDetails", "Respondent details"),
    MIAM("miam", "MIAM"),
    ALLEGATIONS_OF_HARM("allegationsOfHarm", "Allegations of harm"),
    OTHER_PEOPLE_IN_THE_CASE("otherPeopleInTheCase", "Other people in the case"),
    OTHER_PROCEEDINGS("otherProceedings", "Other proceedings"),
    ATTENDING_THE_HEARING("attendingTheHearing", "Attending the hearing"),
    INTERNATIONAL_ELEMENT("internationalElement", "International element"),
    LITIGATION_CAPACITY("litigationCapacity", "Litigation capacity"),
    WELSH_LANGUAGE_REQUIREMENTS("welshLanguageRequirements", "Welsh language requirements"),
    VIEW_PDF_DOCUMENT("viewPdfDocument", "View PDF application"),
    SUBMIT_AND_PAY("submitAndPay", "Submit and pay"),
    // FL401 Events
    FL401_CASE_NAME("fl401CaseName", "Case name"),
    RESPONDENT_BEHAVIOUR("respondentBehaviour", "Respondent's Behaviour"),
    WITHOUT_NOTICE_ORDER("withoutNoticeOrderDetails", "Without notice order"),
    FL401_HOME("fl401Home", "The Home"),
    RELATIONSHIP_TO_RESPONDENT("respondentRelationship","Relationship to respondent"),
    FL401_TYPE_OF_APPLICATION("fl401TypeOfApplication", "Type of application"),
    FL401_APPLICANT_FAMILY_DETAILS("fl401ApplicantFamilyDetails", "Applicant's Family"),
    FL401_OTHER_PROCEEDINGS("fl401OtherProceedings", "Other proceedings");


    private final String id;
    private final String name;


    public static List<Event> getEventOrder(String caseType) {
        List<Event> c100 = List.of(
            TYPE_OF_APPLICATION,
            HEARING_URGENCY,
            APPLICANT_DETAILS,
            CHILD_DETAILS,
            RESPONDENT_DETAILS,
            MIAM,
            ALLEGATIONS_OF_HARM,
            OTHER_PEOPLE_IN_THE_CASE,
            OTHER_PROCEEDINGS,
            ATTENDING_THE_HEARING,
            INTERNATIONAL_ELEMENT,
            LITIGATION_CAPACITY,
            WELSH_LANGUAGE_REQUIREMENTS
        );
        List<Event> fl401 = List.of(
            FL401_TYPE_OF_APPLICATION,
            WITHOUT_NOTICE_ORDER,
            APPLICANT_DETAILS,
            RESPONDENT_DETAILS,
            RELATIONSHIP_TO_RESPONDENT,
            FL401_APPLICANT_FAMILY_DETAILS,
            RESPONDENT_BEHAVIOUR,
            OTHER_PROCEEDINGS,
            ATTENDING_THE_HEARING,
            INTERNATIONAL_ELEMENT,
            WELSH_LANGUAGE_REQUIREMENTS
        );
        return caseType.equalsIgnoreCase(C100_CASE_TYPE) ? c100 : fl401;
    }


    @JsonValue
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @JsonCreator
    public static Event getValue(String key) {
        return Event.valueOf(key);
    }


}

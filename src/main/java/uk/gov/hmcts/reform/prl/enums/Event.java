package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

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
    STATEMENT_OF_TRUTH_AND_SUBMIT("statementOfTruthAndSubmit", "Statement of Truth and Submit"),
    UPLOAD_DOCUMENTS("uploadDocuments", "Upload Documents"),
    FL401_OTHER_PROCEEDINGS("fl401OtherProceedings", "Other proceedings");


    private final String id;
    private final String name;

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

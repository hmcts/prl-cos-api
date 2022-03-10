package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@Getter
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
    RESPONDENT_BEHAVIOUR("respondentBehaviour", "Respondent's behaviour"),
    WITHOUT_NOTICE_ORDER("withoutNoticeOrderDetails", "Without notice order"),
    FL401_HOME("fl401Home", "The home"),
    RELATIONSHIP_TO_RESPONDENT("respondentRelationship","Relationship to respondent"),
    FL401_TYPE_OF_APPLICATION("fl401TypeOfApplication", "Type of application"),
    FL401_APPLICANT_FAMILY_DETAILS("fl401ApplicantFamilyDetails", "Applicant's family"),
    UPLOAD_DOCUMENTS("uploadDocuments", "Upload documents"),
    FL401_OTHER_PROCEEDINGS("fl401OtherProceedings", "Other proceedings"),
    FL401_STATEMENT_OF_TRUTH("fl401StatementOfTruthAndSubmit", "Statement of truth and submit");


    private final String id;
    private final String name;

    public static List<Event> getEventOrder(String caseType) {
        List<Event> c100 = List.of(
            CASE_NAME,
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
            FL401_CASE_NAME,
            FL401_TYPE_OF_APPLICATION,
            WITHOUT_NOTICE_ORDER,
            APPLICANT_DETAILS,
            RESPONDENT_DETAILS,
            FL401_APPLICANT_FAMILY_DETAILS,
            RELATIONSHIP_TO_RESPONDENT,
            RESPONDENT_BEHAVIOUR,
            FL401_HOME,
            OTHER_PROCEEDINGS,
            ATTENDING_THE_HEARING,
            INTERNATIONAL_ELEMENT,
            WELSH_LANGUAGE_REQUIREMENTS
        );
        return caseType.equalsIgnoreCase(C100_CASE_TYPE) ? c100 : fl401;
    }

}

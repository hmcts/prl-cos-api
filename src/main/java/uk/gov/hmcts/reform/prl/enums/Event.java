package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;

@Getter
@RequiredArgsConstructor
public enum Event {

    CASE_NAME("caseName", "Case name"),
    TYPE_OF_APPLICATION("selectApplicationType", "Type of application"),
    HEARING_URGENCY("hearingUrgency",  "Hearing urgency"),
    APPLICANT_DETAILS("applicantsDetails", "Applicant details"),
    CHILD_DETAILS("childDetails", "Child details"),
    CHILD_DETAILS_REVISED("childDetailsRevised", "Child details"),
    RESPONDENT_DETAILS("respondentsDetails", "Respondent details"),
    MIAM("miam", "MIAM"),
    MIAM_POLICY_UPGRADE("miamPolicyUpgrade", "MIAM"),
    ALLEGATIONS_OF_HARM("allegationsOfHarm", "Allegations of harm"),
    ALLEGATIONS_OF_HARM_REVISED("allegationsOfHarmRevised", "Allegations of harm"),
    OTHER_PEOPLE_IN_THE_CASE("otherPeopleInTheCase", "Other people in the case"),
    OTHER_PEOPLE_IN_THE_CASE_REVISED("otherPeopleInTheCaseRevised", "Other people in the case"),
    OTHER_PROCEEDINGS("otherProceedings", "Other proceedings"),
    ATTENDING_THE_HEARING("attendingTheHearing", "Attending the hearing"),
    INTERNATIONAL_ELEMENT("internationalElement", "International element"),
    LITIGATION_CAPACITY("litigationCapacity", "Litigation capacity"),
    WELSH_LANGUAGE_REQUIREMENTS("welshLanguageRequirements", "Welsh language requirements"),
    VIEW_PDF_DOCUMENT("viewPdfDocument", "View PDF application"),
    SUBMIT_AND_PAY("submitAndPay", "Submit and pay"),
    SUBMIT("submit", "Submit"),
    MANAGE_ORDERS("manageOrders", "Manage orders"),
    OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION("otherChildNotInTheCase","Other children not in the case"),
    CHILDREN_AND_APPLICANTS("childrenAndApplicants","Children and applicants"),
    CHILDREN_AND_RESPONDENTS("childrenAndRespondents","Children and respondents"),
    CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION("childrenAndOtherPeople","Children and other people"),
    // FL401 Events
    FL401_CASE_NAME("fl401CaseName", "Case name"),
    RESPONDENT_BEHAVIOUR("respondentBehaviour", "Respondent's behaviour"),
    WITHOUT_NOTICE_ORDER("withoutNoticeOrderDetails", "Without notice order"),
    FL401_HOME("fl401Home", "The home"),
    RELATIONSHIP_TO_RESPONDENT("respondentRelationship","Relationship to respondent"),
    FL401_TYPE_OF_APPLICATION("fl401TypeOfApplication", "Type of application"),
    FL401_APPLICANT_FAMILY_DETAILS("fl401ApplicantFamilyDetails", "Applicant's family"),
    FL401_SOT_AND_SUBMIT("fl401StatementOfTruthAndSubmit", "Statement of truth and submit"),
    FL401_RESUBMIT("fl401resubmit", "Statement of Truth and submit"),
    FL401_OTHER_PROCEEDINGS("fl401OtherProceedings", "Other proceedings"),
    FL401_UPLOAD_DOCUMENTS("fl401UploadDocuments","Upload documents"),
    TS_SOLICITOR_APPLICATION("testingSupportDummySolicitorCreate", "TS-Solicitor application"),
    TS_ADMIN_APPLICATION_NOC("testingSupportDummyAdminCreateNoc", "TS-Admin application-Noc"),
    ADMIN_EDIT_AND_APPROVE_ORDER("adminEditAndApproveAnOrder", "Edit and serve an order"),
    REMOVE_DRAFT_ORDER("removeDraftOrder", "Remove draft order"),
    DRAFT_AN_ORDER("draftAnOrder", "Draft an order"),
    EDIT_AND_APPROVE_ORDER("editAndApproveAnOrder", "Edit and approve a draft order"),
    UPLOAD_ADDITIONAL_APPLICATIONS("uploadAdditionalApplications", "Upload additional applications"),
    TRANSFER_TO_ANOTHER_COURT("transferToAnotherCourt", "Transfer to another court"),
    EDIT_RETURNED_ORDER("editReturnedOrder", "Edit a returned order"),
    SOA("serviceOfApplication", "Service of application"),
    CONFIDENTIAL_CHECK("confidentialityCheck", "Confidentiality check"),
    ALLOCATED_JUDGE("allocatedJudge", "Allocated Judge"),
    SEND_TO_GATEKEEPER("sendToGateKeeper", "Send to Gatekeeper"),
    AMEND_MIAM_POLICY_UPGRADE("amendMiamPolicyUpgrade", "Amend MIAM"),
    SOLICITOR_CREATE("solicitorCreate", "Solicitor application");

    private final String id;
    private final String name;

    public static List<Event> getEventOrder(CaseData caseData) {
        List<Event> c100 = getC100Events(caseData);
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
            FL401_OTHER_PROCEEDINGS,
            ATTENDING_THE_HEARING,
            INTERNATIONAL_ELEMENT,
            WELSH_LANGUAGE_REQUIREMENTS
        );
        return C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? c100 : fl401;
    }

    private static List<Event> getC100Events(CaseData caseData) {
        if (TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
            return List.of(
                CASE_NAME,
                TYPE_OF_APPLICATION,
                HEARING_URGENCY,
                CHILD_DETAILS_REVISED,
                APPLICANT_DETAILS,
                RESPONDENT_DETAILS,
                OTHER_PEOPLE_IN_THE_CASE_REVISED,
                OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION,
                CHILDREN_AND_APPLICANTS,
                CHILDREN_AND_RESPONDENTS,
                CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION,
                ALLEGATIONS_OF_HARM_REVISED,
                MIAM_POLICY_UPGRADE,
                OTHER_PROCEEDINGS,
                ATTENDING_THE_HEARING,
                INTERNATIONAL_ELEMENT,
                LITIGATION_CAPACITY,
                WELSH_LANGUAGE_REQUIREMENTS
            );
        } else if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())) {
            return List.of(
                CASE_NAME,
                TYPE_OF_APPLICATION,
                HEARING_URGENCY,
                CHILD_DETAILS_REVISED,
                APPLICANT_DETAILS,
                RESPONDENT_DETAILS,
                OTHER_PEOPLE_IN_THE_CASE_REVISED,
                OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION,
                CHILDREN_AND_APPLICANTS,
                CHILDREN_AND_RESPONDENTS,
                CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION,
                ALLEGATIONS_OF_HARM_REVISED,
                MIAM,
                OTHER_PROCEEDINGS,
                ATTENDING_THE_HEARING,
                INTERNATIONAL_ELEMENT,
                LITIGATION_CAPACITY,
                WELSH_LANGUAGE_REQUIREMENTS
            );
        }
        return List.of(
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
    }

}

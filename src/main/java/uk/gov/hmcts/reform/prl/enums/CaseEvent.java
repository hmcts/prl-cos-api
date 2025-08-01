package uk.gov.hmcts.reform.prl.enums;

import java.util.Arrays;

public enum CaseEvent {
    LINK_CITIZEN("linkCitizenAccount"),
    CONFIRM_YOUR_DETAILS("confirmYourDetails"),
    SUPPORT_YOU_DURING_CASE("hearingNeeds"),
    CITIZEN_INTERNAL_CASE_UPDATE("citizen-internal-case-update"),
    CITIZEN_CASE_CREATE("citizenCreate"),
    CITIZEN_CASE_UPDATE("citizen-case-update"),
    CITIZEN_UPLOADED_DOCUMENT("citizenUploadedDocument"),
    CITIZEN_CASE_SUBMIT("citizen-case-submit"),
    CITIZEN_SAVE_C100_DRAFT_INTERNAL("citizenSaveC100DraftInternal"),
    DELETE_APPLICATION("deleteApplication"),
    CITIZEN_INTERNAL_FLAG_UPDATES("citizenInternalFlagUpdates"),
    CITIZEN_CURRENT_OR_PREVIOUS_PROCCEDINGS("citizenCurrentOrPreviousProceeding"),
    EVENT_RESPONDENT_MIAM("respondentMiam"),
    EVENT_RESPONDENT_AOH("citizenRespondentAoH"),
    KEEP_DETAILS_PRIVATE("keepYourDetailsPrivate"),
    CONSENT_TO_APPLICATION("consentToTheApplication"),
    EVENT_INTERNATIONAL_ELEMENT("citizenInternationalElement"),
    LEGAL_REPRESENTATION("legalRepresentation"),
    SUPPORT_YOU_NEED("support-you-need"),
    REVIEW_AND_SUBMIT("reviewAndSubmit"),
    PAYMENT_SUCCESS_CALLBACK("paymentSuccessCallback"),
    PAYMENT_FAILURE_CALLBACK("paymentFailureCallback"),
    AWP_PAYMENT_SUCCESS_CALLBACK("awpPaymentSuccessCallback"),
    AWP_PAYMENT_FAILURE_CALLBACK("awpPaymentFailureCallback"),
    UPDATE_ALL_TABS("internal-update-all-tabs"),
    COURTNAV_CASE_CREATION("courtnav-case-creation"),
    COURTNAV_DOCUMENT_UPLOAD_EVENT_ID("courtnav-document-upload"),
    HEARING_STATE_CHANGE_SUCCESS("hmcCaseUpdateSuccess"),
    HEARING_STATE_CHANGE_FAILURE("hmcCaseUpdateFailure"),
    INTERNAL_UPDATE_TASK_LIST("internal-update-task-list"),
    CITIZEN_CASE_SUBMIT_WITH_HWF("citizenCaseSubmitWithHWF"),
    CITIZEN_CASE_WITHDRAW("citizenCaseWithdraw"),
    UPDATE_NEXT_HEARING_DATE_IN_CCD("UpdateNextHearingInfo"),
    HMC_CASE_STATUS_UPDATE_TO_PREP_FOR_HEARING("hmcCaseUpdPrepForHearing"),
    HMC_CASE_STATUS_UPDATE_TO_DECISION_OUTCOME("hmcCaseUpdDecOutcome"),
    CITIZEN_REMOVE_LEGAL_REPRESENTATIVE("citizenRemoveLegalRepresentative"),
    C100_REQUEST_SUPPORT("c100RequestSupport"),
    FL401_REQUEST_SUPPORT("fl401RequestSupport"),
    C100_MANAGE_SUPPORT("c100ManageSupport"),
    C100_MANAGE_FLAGS("c100ManageFlags"),
    FL401_MANAGE_SUPPORT("fl401ManageSupport"),
    TS_ADMIN_APPLICATION_NOC("testingSupportDummyCase"),
    CITIZEN_STATEMENT_OF_SERVICE("citizenStatementOfService"),
    CITIZEN_CONTACT_PREFERENCE("citizenContactPreference"),
    C100_ALL_DOCS_REVIEWED("c100-all-docs-reviewed"),
    FL401_ALL_DOCS_REVIEWED("fl401-all-docs-reviewed"),
    CAFCASS_ENGLAND_DOCUMENT_UPLOAD("cafcass-document-upload"),
    ALL_AWP_IN_REVIEW("allAwPInReview"),
    CREATE_WA_TASK_FOR_CTSC_CASE_FLAGS("createWaTaskForCtscCaseFlags"),
    MARK_CASE_AS_RESTRICTED("restrictedCaseAccess"),
    MARK_CASE_AS_PRIVATE("privateCaseAccess"),
    MARK_CASE_AS_PUBLIC("publicCaseAccess"),
    CHANGE_CASE_ACCESS_AS_SYSUSER("changeCaseAccess"),
    CITIZEN_LANG_SUPPORT_NOTES("citizenLanguageSupportNotes"),
    CAFCASS_DOCUMENT_UPLOAD("cafcass-document-upload"),
    CITIZEN_PCQ_UPDATE("pcqUpdateForCitizen"),
    CITIZEN_RESPONSE_TO_AOH("citizenResponseToAoH"),
    FM5_NOTIFICATION_CASE_UPDATE("fm5NotificationCaseUpdate"),
    FM5_NOTIFICATION_NOT_REQUIRED_CASE_UPDATE("fm5NotificationNotRequiredCaseUpdate"),

    HWF_PROCESS_CASE_UPDATE("hwfProcessCaseUpdate"),
    HWF_PROCESS_AWP_STATUS_UPDATE("processHwfUpdateAwpStatus"),

    CITIZEN_AWP_CREATE("citizenAwpCreate"),
    CITIZEN_AWP_HWF_CREATE("citizenAwpHwfCreate"),
    ENABLE_UPDATE_HEARING_ACTUAL_TASK("enableUpdateHearingActualTask"),
    ENABLE_REQUEST_SOLICITOR_ORDER_TASK("enableRequestSolicitorOrderTask"),
    AMEND_APPLICANTS_DETAILS("amendApplicantsDetails"),
    AMEND_RESPONDENTS_DETAILS("amendRespondentsDetails"),
    AMEND_OTHER_PEOPLE_IN_THE_CASE_REVISED("amendOtherPeopleInTheCaseRevised"),
    APPLICANT_DETAILS("applicantsDetails"),
    REVIEW_ADDITIONAL_APPLICATION("reviewAdditionalApplication"),
    CLOSE_REVIEW_RA_REQUEST_TASK("closeReviewRARequestTask");

    private final String value;

    CaseEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CaseEvent fromValue(String value) {
        return Arrays.stream(values())
            .filter(event -> event.value.equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown event name: " + value));
    }
}

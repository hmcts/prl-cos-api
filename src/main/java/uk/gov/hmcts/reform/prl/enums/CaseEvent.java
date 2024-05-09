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
    MARK_CASE_AS_RESTRICTED("restrictedCaseAccess"),
    TS_ADMIN_APPLICATION_NOC("testingSupportDummyCase"),
    CITIZEN_STATEMENT_OF_SERVICE("citizenStatementOfService"),
    CITIZEN_CONTACT_PREFERENCE("citizenContactPreference"),
    C100_ALL_DOCS_REVIEWED("c100-all-docs-reviewed"),
    FL401_ALL_DOCS_REVIEWED("fl401-all-docs-reviewed"),
    CAFCASS_ENGLAND_DOCUMENT_UPLOAD("cafcass-document-upload"),
    ALL_AWP_IN_REVIEW("allAwPInReview"),
    CREATE_WA_TASK_FOR_CTSC_CASE_FLAGS("createWaTaskForCtscCaseFlags");

    private final String value;

    CaseEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static uk.gov.hmcts.reform.prl.enums.CaseEvent fromValue(String value) {
        return Arrays.stream(values())
            .filter(event -> event.value.equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown event name: " + value));
    }
}

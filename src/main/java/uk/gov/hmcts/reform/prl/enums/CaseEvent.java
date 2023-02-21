package uk.gov.hmcts.reform.prl.enums;

import java.util.Arrays;

public enum CaseEvent {
    LINK_CITIZEN("linkCitizenAccount"),
    CITIZEN_CASE_CREATE("citizenCreate"),
    CITIZEN_CASE_UPDATE("citizen-case-update"),
    CITIZEN_UPLOADED_DOCUMENT("citizenUploadedDocument"),
    CITIZEN_CASE_SUBMIT("citizen-case-submit"),
    DELETE_CASE("deleteApplication"),
    EVENT_RESPONDENT_MIAM("respondentMiam"),
    KEEP_DETAILS_PRIVATE("keepYourDetailsPrivate"),
    CONSENT_TO_APPLICATION("consentToTheApplication"),
    EVENT_INTERNATIONAL_ELEMENT("citizenInternationalElement"),
    LEGAL_REPRESENTATION("legalRepresentation"),
    SUPPORT_YOU_NEED("support-you-need"),
    REVIEW_AND_SUBMIT("reviewAndSubmit"),
    PAYMENT_SUCCESS_CALLBACK("paymentSuccessCallback"),
    PAYMENT_FAILURE_CALLBACK("paymentFailureCallback"),
    UPDATE_ALL_TABS("internal-update-all-tabs"),

    COURTNAV_CASE_CREATION("courtnav-case-creation"),
    COURTNAV_DOCUMENT_UPLOAD_EVENT_ID("courtnav-document-upload"),
    HEARING_STATE_CHANGE_SUCCESS("hmcCaseUpdateSuccess"),
    HEARING_STATE_CHANGE_FAILURE("hmcCaseUpdateFailure");

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

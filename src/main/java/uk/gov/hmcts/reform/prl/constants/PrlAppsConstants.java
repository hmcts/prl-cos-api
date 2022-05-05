package uk.gov.hmcts.reform.prl.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PrlAppsConstants {
    public static final String JURISDICTION = "PRIVATELAW";
    public static final String CASE_TYPE = "PRLAPPS";

    public static final String C100_CASE_TYPE = "C100";
    public static final String FL401_CASE_TYPE = "FL401";

    public static final String YES = "Yes";
    public static final String NO = "No";

    public static final String APPLICATION_NOTICE_EFFORTS = "ApplicationNoticeEfforts";
    public static final String IS_APPLICATION_URGENT = "IsApplicationUrgent";
    public static final String APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS = "ApplicationConsideredInDaysAndHours";
    public static final String DAYS = "days";
    public static final String HOURS = "hours";

    public static final String APPLICANT_ATTENDED_MIAM = "applicantAttendedMiam";
    public static final String CLAIMING_EXEMPTION_MIAM = "claimingExemptionMiam";

    // Fee and Pay related constants
    public static final String FEE_VERSION = "1";
    public static final Integer FEE_VOLUME = 1;
    public static final String PAYMENT_ACTION = "payment";

    public static final String BAIL_DETAILS = "bailDetails";
    public static final String APPLICANT_HAS_BAIL_END_DATE = "isRespondentAlreadyInBailCondition";
    public static final String APPLICANT_BAIL_END_DATE = "bailConditionEndDate";
    public static final String NOT_KNOWN_BAIL_END_DATE = "dontKnowBailEndDate";

    public static final String YES_SMALL = "yes";
    public static final String NO_SMALL = "no";

    public static final String DOCUMENT_FIELD_C1A = "c1ADocument";
    public static final String DOCUMENT_FIELD_C8 = "c8Document";
    public static final String DOCUMENT_FIELD_FINAL = "finalDocument";
    public static final String DRAFT_DOCUMENT_FIELD = "draftOrderDoc";
    public static final String DRAFT_DOCUMENT_WELSH_FIELD = "draftOrderDocWelsh";
    public static final String COURT_NAME_FIELD = "courtName";
    public static final String COURT_ID_FIELD = "courtId";
    public static final String COURT_EMAIL_ADDRESS_FIELD = "courtEmailAddress";
    public static final String FINAL_DOCUMENT_FIELD = "finalDocument";
    public static final String ISSUE_DATE_FIELD = "issueDate";
    public static final String DATE_SUBMITTED_FIELD = "dateSubmitted";
    public static final String DATE_AND_TIME_SUBMITTED_FIELD = "dateAndTimeSubmitted";
    public static final String STATE_FIELD = "state";

    public static final String THIS_INFORMATION_IS_CONFIDENTIAL = "This information is to be kept confidential";

    public static final String SEARCH_RESULTS_POSTCODE_POSTCODE_SERVICE_AREA = "search/results?postcode={postcode}&serviceArea=";
    public static final String CHILD_ARRANGEMENTS_POSTCODE_URL = SEARCH_RESULTS_POSTCODE_POSTCODE_SERVICE_AREA + "childcare-arrangements";
    public static final String COURT_DETAILS_URL = "courts/{court-slug}";
    public static final String DOMESTIC_ABUSE_POSTCODE_URL = SEARCH_RESULTS_POSTCODE_POSTCODE_SERVICE_AREA + "domestic-abuse";

    public static final String DOCUMENT_FIELD_C1A_WELSH = "c1AWelshDocument";
    public static final String DOCUMENT_FIELD_C8_WELSH = "c8WelshDocument";
    public static final String DOCUMENT_FIELD_FINAL_WELSH = "finalWelshDocument";

    public static final String CHILD_ARRANGEMENT_CASE = "CHILD ARRANGEMENT CASE";
    public static final String ISSUE_EVENT_CODE = "001";
    public static final String ISSUE_EVENT_SEQUENCE = "1";


    public static final String TEMPLATE = "template";
    public static final String FILE_NAME = "fileName";

    public static final String FINAL_TEMPLATE_NAME = "finalTemplateName";
    public static final String GENERATE_FILE_NAME = "generateFileName";

    public static final String URL_STRING = "/";



    public static final String C8_HINT = "C8";
    public static final String C1A_HINT = "C1A";
    public static final String FINAL_HINT = "FINAL";
    public static final String DRAFT_HINT = "DRAFT";

}

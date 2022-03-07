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

    public static final String DOCUMENT_FIELD_C1A_WELSH = "c1AWelshDocument";
    public static final String DOCUMENT_FIELD_C8_WELSH = "c8WelshDocument";
    public static final String DOCUMENT_FIELD_FINAL_WELSH = "finalWelshDocument";

    public static final String THIS_INFORMATION_IS_CONFIDENTIAL = "This information is to be kept confidential";

    public static final String CHILD_ARRANGEMENT_CASE = "CHILD ARRANGEMENT CASE";
    public static final String ISSUE_EVENT_CODE = "001";
    public static final String ISSUE_EVENT_SEQUENCE = "1";


}

package uk.gov.hmcts.reform.prl.models;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrchestrationConstants {

    public static final String YES = "Yes";
    public static final String NO = "No";

    public static final String APPLICATION_NOTICE_EFFORTS = "ApplicationNoticeEfforts";
    public static final String IS_APPLICATION_URGENT = "IsApplicationUrgent";
    public static final String APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS = "ApplicationConsideredInDaysAndHours";
    public static final String DAYS = "days";
    public static final String HOURS = "hours";

    public static final String APPLICANT_ATTENDED_MIAM = "ApplicantAttendedMIAM";
    public static final String CLAIMING_EXEMPTION_MIAM = "ClaimingExemptionMIAM";


}

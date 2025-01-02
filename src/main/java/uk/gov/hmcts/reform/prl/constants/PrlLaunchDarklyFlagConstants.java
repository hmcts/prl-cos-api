package uk.gov.hmcts.reform.prl.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PrlLaunchDarklyFlagConstants {
    public static final String TASK_LIST_V2_FLAG = "task-list-v2";
    public static final String TASK_LIST_V3_FLAG = "task-list-v3";
    public static final String ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY = "role-assignment-api-in-orders-journey";

    public static final String CREATE_URGENT_CASES_FLAG = "create-urgent-cases";
    public static final String COURTNAV_SWANSEA_COURT_MAPPING = "courtnav-swansea-court-mapping";
    public static final String ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER = "enable-citizen-access-code-in-cover-letter";

}

package uk.gov.hmcts.reform.prl.models;

import java.util.Map;

public enum FeeType {
    C100_SUBMISSION_FEE,
    C2_WITH_NOTICE_AND_FC600_FL403,
    C2_WITHOUT_NOTICE_AND_FP25,
    CHILD_ARRANGEMENTS_ORDER,
    N161_APPELLANT_NOTICE_CA,
    N161_APPELLANT_NOTICE_DA,
    D89_BAILIFF_CA,
    C3_SEARCH_AND_TAKING_CHARGE_OF_A_CHILD,
    C4_WHEREABOUTS_OF_A_MISSING_CHILD;

    public static final Map<String, FeeType> applicationToFeeMap = Map.of(
        "FP25_WITNESS_SUMMONS", C2_WITHOUT_NOTICE_AND_FP25,
        "N161_APPELLANT_NOTICE_CA", N161_APPELLANT_NOTICE_CA,
        "N161_APPELLANT_NOTICE_DA", N161_APPELLANT_NOTICE_DA,
        "D89_BAILIFF_CA", D89_BAILIFF_CA,
        "C79_ENFORCE_CHILD_ARRANGEMENTS_ORDER", CHILD_ARRANGEMENTS_ORDER,
        "FC600_COMMITTAL_APPLICATION", C2_WITH_NOTICE_AND_FC600_FL403,
        "C1_APPLY_FOR_CERTAIN_ORDERS_UNDER_THE_CHILDREN_ACT", CHILD_ARRANGEMENTS_ORDER,
        "C3_SEARCH_AND_TAKING_CHARGE_OF_A_CHILD", C3_SEARCH_AND_TAKING_CHARGE_OF_A_CHILD,
        "C4_WHEREABOUTS_OF_A_MISSING_CHILD", C4_WHEREABOUTS_OF_A_MISSING_CHILD);
}

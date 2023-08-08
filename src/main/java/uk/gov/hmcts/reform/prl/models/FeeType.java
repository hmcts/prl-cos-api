package uk.gov.hmcts.reform.prl.models;

import java.util.Map;

public enum FeeType {
    C100_SUBMISSION_FEE,
    C2_WITH_NOTICE,
    FC600_COMMITTAL_APPLICATION,
    FL403_EXTEND_AN_ORDER,
    C2_WITHOUT_NOTICE,
    FP25_WITNESS_SUMMONS,
    CHILD_ARRANGEMENTS_ORDER,
    N161_APPELLANT_NOTICE_CA,
    N161_APPELLANT_NOTICE_DA,

    D89_BAILIFF_CA,
    C3_SEARCH_AND_TAKING_CHARGE_OF_A_CHILD,
    C4_WHEREABOUTS_OF_A_MISSING_CHILD,
    FL407_ARREST_WARRANT;

    public static final Map<String, FeeType> applicationToFeeMap = Map.of(
        "FP25_WITNESS_SUMMONS", FP25_WITNESS_SUMMONS,
        "C79_CHILD_ORDER", CHILD_ARRANGEMENTS_ORDER,
        "FC600_COMMITTAL_APPLICATION", FC600_COMMITTAL_APPLICATION,
        "C1_CHILD_ORDER", CHILD_ARRANGEMENTS_ORDER,
        "C3_CHILD_ORDER", C3_SEARCH_AND_TAKING_CHARGE_OF_A_CHILD,
        "C4_CHILD_ORDER", C4_WHEREABOUTS_OF_A_MISSING_CHILD,
        "FL407_ARREST_WARRANT", FL407_ARREST_WARRANT);
}

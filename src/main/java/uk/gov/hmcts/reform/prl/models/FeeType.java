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
    FL407_ARREST_WARRANT,
    NO_FEE;

    public static final Map<String, FeeType> applicationToFeeMap = Map.of(
        "FP25_WITNESS_SUMMONS", FP25_WITNESS_SUMMONS,
        "C1_CHILD_ORDER", CHILD_ARRANGEMENTS_ORDER,
        "C3_CHILD_ORDER", C3_SEARCH_AND_TAKING_CHARGE_OF_A_CHILD,
        "C4_CHILD_ORDER", C4_WHEREABOUTS_OF_A_MISSING_CHILD
    );

    public static final Map<String, FeeType> applicationToFeeMapForCitizen = Map.ofEntries(
        Map.entry("C100_FP25_APPLICANT", FP25_WITNESS_SUMMONS),
        Map.entry("C100_FP25_RESPONDENT", FP25_WITNESS_SUMMONS),
        Map.entry("FL401_FP25_APPLICANT", FP25_WITNESS_SUMMONS),
        Map.entry("FL401_FP25_RESPONDENT", FP25_WITNESS_SUMMONS),

        Map.entry("C100_N161_APPLICANT", N161_APPELLANT_NOTICE_CA),
        Map.entry("C100_N161_RESPONDENT", N161_APPELLANT_NOTICE_CA),
        Map.entry("FL401_N161_APPLICANT", N161_APPELLANT_NOTICE_DA),
        Map.entry("FL401_N161_RESPONDENT", N161_APPELLANT_NOTICE_DA),

        Map.entry("C100_D89_APPLICANT", D89_BAILIFF_CA),
        Map.entry("C100_D89_RESPONDENT", D89_BAILIFF_CA),

        Map.entry("FL401_D89_APPLICANT", NO_FEE),
        Map.entry("FL401_D89_RESPONDENT", NO_FEE),

        Map.entry("FL401_FL407_APPLICANT", NO_FEE),

        Map.entry("FL401_FL403_APPLICANT", NO_FEE),
        Map.entry("FL401_FL403_RESPONDENT", NO_FEE),

        Map.entry("C100_EX740_APPLICANT", NO_FEE),
        Map.entry("C100_EX740_RESPONDENT", NO_FEE),
        Map.entry("FL401_EX740_APPLICANT", NO_FEE),
        Map.entry("FL401_EX740_RESPONDENT", NO_FEE),

        Map.entry("C100_EX741_APPLICANT", NO_FEE),
        Map.entry("C100_EX741_RESPONDENT", NO_FEE),
        Map.entry("FL401_EX741_APPLICANT", NO_FEE),
        Map.entry("FL401_EX741_RESPONDENT", NO_FEE),


        Map.entry("C100_C79_APPLICANT", CHILD_ARRANGEMENTS_ORDER),

        Map.entry("C100_FC600_APPLICANT", FC600_COMMITTAL_APPLICATION),
        Map.entry("FL401_FC600_APPLICANT", FC600_COMMITTAL_APPLICATION),

        Map.entry("C100_C1_APPLICANT", CHILD_ARRANGEMENTS_ORDER),
        Map.entry("C100_C1_RESPONDENT", CHILD_ARRANGEMENTS_ORDER),

        Map.entry("C100_C3_APPLICANT", C3_SEARCH_AND_TAKING_CHARGE_OF_A_CHILD),
        Map.entry("C100_C3_RESPONDENT", C3_SEARCH_AND_TAKING_CHARGE_OF_A_CHILD),

        Map.entry("C100_C4_APPLICANT", C4_WHEREABOUTS_OF_A_MISSING_CHILD),
        Map.entry("C100_C4_RESPONDENT", C4_WHEREABOUTS_OF_A_MISSING_CHILD));
}

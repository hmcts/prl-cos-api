package uk.gov.hmcts.reform.prl.enums.noticeofchange;

import java.util.List;

public enum CaseRole {
    CREATOR,
    APPLICANTSOLICITOR,
    C100APPLICANTSOLICITOR1,
    C100APPLICANTSOLICITOR2,
    C100APPLICANTSOLICITOR3,
    C100APPLICANTSOLICITOR4,
    C100APPLICANTSOLICITOR5,
    C100RESPONDENTSOLICITOR1,
    C100RESPONDENTSOLICITOR2,
    C100RESPONDENTSOLICITOR3,
    C100RESPONDENTSOLICITOR4,
    C100RESPONDENTSOLICITOR5;

    private final String formattedName;

    CaseRole() {
        this.formattedName = formatName(name());
    }

    public String formattedName() {
        return formattedName;
    }

    public static CaseRole from(String name) {
        return CaseRole.valueOf(name.replaceAll("[\\[\\]]", ""));
    }

    public static List<CaseRole> representativeSolicitors() {
        return List.of(C100APPLICANTSOLICITOR1,
                       C100APPLICANTSOLICITOR2,
                       C100APPLICANTSOLICITOR3,
                       C100APPLICANTSOLICITOR4,
                       C100APPLICANTSOLICITOR5
        );
    }

    public static List<CaseRole> respondentSolicitors() {
        return List.of(C100RESPONDENTSOLICITOR1,
                       C100RESPONDENTSOLICITOR2,
                       C100RESPONDENTSOLICITOR3,
                       C100RESPONDENTSOLICITOR4,
                       C100RESPONDENTSOLICITOR5
        );
    }

    private static String formatName(String name) {
        return String.format("[%s]", name);
    }
}

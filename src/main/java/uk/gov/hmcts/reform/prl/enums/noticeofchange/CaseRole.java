package uk.gov.hmcts.reform.prl.enums.noticeofchange;

import java.util.List;

public enum CaseRole {
    CREATOR,
    APPLICANTSOLICITOR,
    RESPONDENTSOLICITORA,
    RESPONDENTSOLICITORB,
    RESPONDENTSOLICITORC,
    RESPONDENTSOLICITORD,
    RESPONDENTSOLICITORE,
    RESPONDENTSOLICITORF,
    RESPONDENTSOLICITORG,
    RESPONDENTSOLICITORH,
    RESPONDENTSOLICITORI,
    RESPONDENTSOLICITORJ;

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
        return List.of(RESPONDENTSOLICITORA,
                       RESPONDENTSOLICITORB,
                       RESPONDENTSOLICITORC,
                       RESPONDENTSOLICITORD,
                       RESPONDENTSOLICITORE,
                       RESPONDENTSOLICITORF,
                       RESPONDENTSOLICITORG,
                       RESPONDENTSOLICITORH,
                       RESPONDENTSOLICITORI,
                       RESPONDENTSOLICITORJ
        );
    }

    public static List<CaseRole> respondentSolicitors() {
        return List.of(RESPONDENTSOLICITORA,
                       RESPONDENTSOLICITORB,
                       RESPONDENTSOLICITORC,
                       RESPONDENTSOLICITORD,
                       RESPONDENTSOLICITORE,
                       RESPONDENTSOLICITORF,
                       RESPONDENTSOLICITORG,
                       RESPONDENTSOLICITORH,
                       RESPONDENTSOLICITORI,
                       RESPONDENTSOLICITORJ
        );
    }

    private static String formatName(String name) {
        return String.format("[%s]", name);
    }
}

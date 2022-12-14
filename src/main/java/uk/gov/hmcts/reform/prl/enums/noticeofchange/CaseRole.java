package uk.gov.hmcts.reform.prl.enums.noticeofchange;

import java.util.List;

public enum CaseRole {
    CREATOR,
    APPLICANTSOLICITOR,
    SOLICITORA,
    SOLICITORB,
    SOLICITORC,
    SOLICITORD,
    SOLICITORE,
    SOLICITORF,
    SOLICITORG,
    SOLICITORH,
    SOLICITORI,
    SOLICITORJ;

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
        return List.of(SOLICITORA,
                       SOLICITORB,
                       SOLICITORC,
                       SOLICITORD,
                       SOLICITORE,
                       SOLICITORF,
                       SOLICITORG,
                       SOLICITORH,
                       SOLICITORI,
                       SOLICITORJ
        );
    }

    public static List<CaseRole> respondentSolicitors() {
        return List.of(SOLICITORA,
                       SOLICITORB,
                       SOLICITORC,
                       SOLICITORD,
                       SOLICITORE,
                       SOLICITORF,
                       SOLICITORG,
                       SOLICITORH,
                       SOLICITORI,
                       SOLICITORJ
        );
    }

    private static String formatName(String name) {
        return String.format("[%s]", name);
    }
}

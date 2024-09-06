package uk.gov.hmcts.reform.prl.enums;

public enum Roles {

    JUDGE("JUDGE","caseworker-privatelaw-judge"),
    LEGAL_ADVISER("LEGAL_ADVISER","caseworker-privatelaw-la"),
    SOLICITOR("SOLICITOR", "caseworker-privatelaw-solicitor"),
    COURT_ADMIN("COURT_ADMIN","caseworker-privatelaw-courtadmin"),
    CITIZEN("CITIZEN","citizen"),
    BULK_SCAN("BULK_SCAN","caseworker-privatelaw-bulkscan"),
    SYSTEM_UPDATE("SYSTEM_UPDATE","caseworker-privatelaw-systemupdate"),
    COURTNAV("COURTNAV","courtnav"),;

    private final String id;
    private final String value;

    Roles(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }
}

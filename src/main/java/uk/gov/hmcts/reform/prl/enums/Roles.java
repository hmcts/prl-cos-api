package uk.gov.hmcts.reform.prl.enums;

public enum Roles {

    JUDGE("caseworker-privatelaw-judge"),
    LEGAL_ADVISER("caseworker-privatelaw-la"),
    SOLICITOR("caseworker-privatelaw-solicitor"),
    COURT_ADMIN("caseworker-privatelaw-courtadmin"),
    CITIZEN("citizen"),
    SYSTEM_UPDATE("caseworker-privatelaw-systemupdate");

    private final String value;

    Roles(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

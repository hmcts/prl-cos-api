package uk.gov.hmcts.reform.prl.enums.amroles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum InternalCaseworkerAmRolesEnum {
    JUDGE("JUDGE", List.of("circuit-judge", "allocated-magistrate","allocated-judge","leadership-judge","judge")),
    LEGAL_ADVISER("LEGAL_ADVISER", List.of("tribunal-caseworker", "senior-tribunal-caseworker")),
    COURT_ADMIN("COURT_ADMIN", List.of("hearing-centre-admin", "ctsc-team-leader","ctsc")),
    CAFCASS_CYMRU("CAFCASS_CYMRU", List.of("listed-hearing-viewer", "caseworker-privatelaw-externaluser-viewonly"));

    private final String user;
    private final List<String> roles;

    public String getUser() {
        return user;
    }

    public List<String> getRoles() {
        return roles;
    }

}

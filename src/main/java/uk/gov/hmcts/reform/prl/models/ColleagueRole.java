package uk.gov.hmcts.reform.prl.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ColleagueRole {
    SOLICITOR("Solicitor"), SOCIAL_WORKER("Social worker"), OTHER("Other");

    private final String label;
}

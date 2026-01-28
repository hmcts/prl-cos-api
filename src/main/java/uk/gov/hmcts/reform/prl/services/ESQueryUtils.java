package uk.gov.hmcts.reform.prl.services;

import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Match;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Should;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;

import java.util.List;

public class ESQueryUtils {

    public static StateFilter getFilterForStates(List<State> states) {
        return StateFilter.builder()
            .should(states.stream()
                        .map(state -> Should.builder().match(Match.builder().state(state.getValue()).build()).build())
                        .toList())
            .build();
    }
}

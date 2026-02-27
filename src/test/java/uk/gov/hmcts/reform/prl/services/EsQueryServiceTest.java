package uk.gov.hmcts.reform.prl.services;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EsQueryServiceTest {

    @InjectMocks
    private EsQueryService esQueryService;

    @Test
    void shouldGetFilterForStates() {
        List<State> states = List.of(State.SUBMITTED_PAID, State.DECISION_OUTCOME);
        StateFilter stateFilter = esQueryService.getFilterForStates(states);

        assertThat(stateFilter.getShould()).hasSize(2);
    }

    @Test
    void shouldReturnObjectMapper() {
        assertThat(esQueryService.getObjectMapper()).isNotNull();
    }
}

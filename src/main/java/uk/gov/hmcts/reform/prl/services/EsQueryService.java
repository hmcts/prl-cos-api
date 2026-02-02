package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Match;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Should;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;

import java.util.List;

@Service
public class EsQueryService {

    public StateFilter getFilterForStates(List<State> states) {
        return StateFilter.builder()
            .should(states.stream()
                        .map(state -> Should.builder().match(Match.builder().state(state.getValue()).build()).build())
                        .toList())
            .build();
    }

    public ObjectMapper getObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        om.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        return om;
    }

}

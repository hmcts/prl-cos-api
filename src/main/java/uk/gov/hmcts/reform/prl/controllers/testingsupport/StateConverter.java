package uk.gov.hmcts.reform.prl.controllers.testingsupport;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.State;

import java.util.Arrays;

import static java.util.Arrays.stream;

@Component
@ConditionalOnProperty(name = "hearing.hack.enabled", havingValue = "true")
public class StateConverter implements Converter<String, State> {

    @Override
    public State convert(String source) {
        return stream(State.values())
            .filter(state -> state.getLabel()
                .equalsIgnoreCase(source))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid state: " + source));
    }
}

package uk.gov.hmcts.reform.prl.controllers.testingsupport;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.State;

@Component
@ConditionalOnProperty(name = "hearing.hack.enabled", havingValue = "true")
public class StateConverter implements Converter<String, State> {
    @Override
    public State convert(String source) {
        return State.tryFromValue(source)
            .orElseThrow(() -> new IllegalArgumentException("Invalid state: " + source));
    }
}

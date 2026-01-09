package uk.gov.hmcts.reform.prl.controllers.testingsupport;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.List;

public class TestLogAppender extends AppenderBase<ILoggingEvent> {
    private final List<ILoggingEvent> events = new ArrayList<>();

    @Override
    protected void append(ILoggingEvent eventObject) {
        events.add(eventObject);
    }

    public List<ILoggingEvent> getEvents() {
        return events;
    }
}

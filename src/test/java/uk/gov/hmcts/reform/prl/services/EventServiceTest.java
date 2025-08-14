package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class EventServiceTest {

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    EventService eventService;

    @Test
    public void testPublishEvent() {
        Object event = new Object();

        eventService.publishEvent(event);
        verify(applicationEventPublisher).publishEvent(event);

    }


}

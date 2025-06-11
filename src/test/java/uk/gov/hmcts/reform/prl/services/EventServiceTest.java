package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
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

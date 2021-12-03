package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskErrorService {

    List<EventValidationErrors> eventErrors = new ArrayList<>();

    public List<EventValidationErrors> getEventErrors() {
        return eventErrors;
    }

    public void addEventError(Event event, String error) {
        eventErrors.add(EventValidationErrors
                            .builder()
                            .event(event)
                            .errors(Collections.singletonList(error))
                            .build());
    }

    public void addMultipleEventErrors(Event event, List<String> errors) {
        eventErrors.add(EventValidationErrors
                            .builder()
                            .event(event)
                            .errors(errors)
                            .build());
    }







}

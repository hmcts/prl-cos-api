package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.EventErrorsEnum;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskErrorService {

    Map<EventErrorsEnum, EventValidationErrors> eventErrors = new HashMap<>();

    public List<EventValidationErrors> getEventErrors() {

        List<EventValidationErrors> eventErrorList = new ArrayList<>();

        for (Map.Entry<EventErrorsEnum, EventValidationErrors> entry : eventErrors.entrySet()) {
            eventErrorList.add(entry.getValue());
        }

        return eventErrorList;
    }

    public void addEventError(Event event, EventErrorsEnum errorType, String error) {
        eventErrors.put(errorType, EventValidationErrors
                                    .builder()
                                    .event(event)
                                    .errors(Collections.singletonList(error))
                                    .build());
    }

    public void addNestedEventError(Event event, EventErrorsEnum parentError, EventErrorsEnum errorType) {
        if (eventErrors.containsKey(parentError)) {

            List<String> updatedNestedErrors = new ArrayList<>(Collections.singleton(errorType.getError()));

            EventValidationErrors eventValidationErrors = eventErrors.get(parentError);
            if (ofNullable(eventValidationErrors.getNestedErrors()).isPresent()) {
                updatedNestedErrors.addAll(eventValidationErrors.getNestedErrors());
            }
            EventValidationErrors updatedErrors = EventValidationErrors.builder()
                .event(event)
                .errors(eventValidationErrors.getErrors())
                .nestedErrors(updatedNestedErrors)
                .build();

            eventErrors.put(parentError, updatedErrors);
        } else {
            EventValidationErrors updatedErrors = EventValidationErrors.builder()
                .event(event)
                .nestedErrors(Collections.singletonList(errorType.getError()))
                .build();

            eventErrors.put(parentError, updatedErrors);
        }
    }


    public void removeError(EventErrorsEnum errorType) {
        eventErrors.remove(errorType);
    }

}

package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.c100respondentsolicitor.RespondentEventValidationErrors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentTaskErrorService {

    Map<RespondentEventErrorsEnum, RespondentEventValidationErrors> eventErrors = new EnumMap<>(
        RespondentEventErrorsEnum.class);

    public List<RespondentEventValidationErrors> getEventErrors() {
        log.info("finding errors");
        List<RespondentEventValidationErrors> eventErrorList = new ArrayList<>();
        log.info("eventErrors size :: " + eventErrors.size());
        for (Map.Entry<RespondentEventErrorsEnum, RespondentEventValidationErrors> entry : eventErrors.entrySet()) {
            log.info("entry key :: " + entry.getKey());
            log.info("entry value :: " + entry.getValue());
            eventErrorList.add(entry.getValue());
        }
        log.info("eventErrorList size :: " + eventErrorList.size());
        eventErrorList.sort(Comparator.comparingInt(x -> RespondentSolicitorEvents.getEventOrder()
            .indexOf(x.getEvent())));
        log.info("eventErrorList size after :: " + eventErrorList.size());
        return eventErrorList;
    }

    public void addEventError(RespondentSolicitorEvents event, RespondentEventErrorsEnum errorType, String error) {
        eventErrors.put(errorType, RespondentEventValidationErrors
            .builder()
            .event(event)
            .errors(Collections.singletonList(error))
            .build());
    }

    public void removeError(RespondentEventErrorsEnum errorType) {
        eventErrors.remove(errorType);
    }

}

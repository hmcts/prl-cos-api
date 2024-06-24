package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.EventErrorsEnum;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TaskErrorService {

    Map<EventErrorsEnum, EventValidationErrors> eventErrors = new EnumMap<>(EventErrorsEnum.class);

    public List<EventValidationErrors> getEventErrors(CaseData caseData) {

        List<EventValidationErrors> eventErrorList = new ArrayList<>();

        for (Map.Entry<EventErrorsEnum, EventValidationErrors> entry : eventErrors.entrySet()) {
            eventErrorList.add(entry.getValue());
            log.info("Error added to tasklist");
        }
        log.info("eventErrorList is {}", eventErrorList);
        eventErrorList.sort(Comparator.comparingInt(x -> Event.getEventOrder(caseData)
            .indexOf(x.getEvent())));
        log.info("eventErrorList sorted is {}", eventErrorList);
        return eventErrorList;
    }

    public void addEventError(Event event, EventErrorsEnum errorType, String error) {
        eventErrors.put(errorType, EventValidationErrors
                                    .builder()
                                    .event(event)
                                    .errors(Collections.singletonList(error))
                                    .build());
    }


    public void removeError(EventErrorsEnum errorType) {
        eventErrors.remove(errorType);
    }

    public void clearErrors() {
        eventErrors.clear();
    }

}

package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.EventErrorsEnum;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskErrorService {

    @Autowired
    TaskListService taskListService;

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


    public void removeError(EventErrorsEnum errorType) {
        eventErrors.remove(errorType);
    }

}

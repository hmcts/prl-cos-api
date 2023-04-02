package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.EventErrorsEnum;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.c100respondentsolicitor.RespondentEventValidationErrors;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentTaskErrorService {

    Map<RespondentEventErrorsEnum, RespondentEventValidationErrors> eventErrors = new EnumMap<>(
        RespondentEventErrorsEnum.class);

    public List<RespondentEventValidationErrors> getEventErrors(CaseData caseData) {

        List<RespondentEventValidationErrors> eventErrorList = new ArrayList<>();

        for (Map.Entry<RespondentEventErrorsEnum, RespondentEventValidationErrors> entry : eventErrors.entrySet()) {
            eventErrorList.add(entry.getValue());
        }
        eventErrorList.sort(Comparator.comparingInt(x -> RespondentSolicitorEvents.getEventOrder()
            .indexOf(x.getEvent())));
        return eventErrorList;
    }

    public void addEventError(RespondentSolicitorEvents event, RespondentEventErrorsEnum errorType, String error) {
        eventErrors.put(errorType, RespondentEventValidationErrors
            .builder()
            .event(event)
            .errors(Collections.singletonList(error))
            .build());
    }


    public void removeError(EventErrorsEnum errorType) {
        eventErrors.remove(errorType);
    }

}

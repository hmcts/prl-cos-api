package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;

import java.util.List;

public abstract class AbstractCallbackController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventService eventPublisher;

    protected CaseData getCaseData(CaseDetails caseDetails) {

        return objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder()
            .id(caseDetails.getId())
            .build();
    }


    protected void publishEvent(Object event) {
        eventPublisher.publishEvent(event);
    }

    protected void publishEvents(List<Object> events) {
        events.forEach(this::publishEvent);
    }

}

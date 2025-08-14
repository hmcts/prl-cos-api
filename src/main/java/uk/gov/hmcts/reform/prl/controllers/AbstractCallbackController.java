package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;

public abstract class AbstractCallbackController {
    protected final ObjectMapper objectMapper;
    protected final EventService eventPublisher;

    protected AbstractCallbackController(ObjectMapper objectMapper, EventService eventPublisher) {
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    protected CaseData getCaseData(CaseDetails caseDetails) {
        return CaseUtils.getCaseData(caseDetails, objectMapper);
    }

    protected Map<String, Object> toMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }


    protected void publishEvent(Object event) {
        eventPublisher.publishEvent(event);
    }

    protected void publishEvents(List<Object> events) {
        events.forEach(this::publishEvent);
    }

}

package uk.gov.hmcts.reform.prl.models.wa;

import lombok.Builder;

@Builder
public class SearchEventAndCaseRequest {

    private final String caseId;
    private final String eventId;
    private final String caseJurisdiction;
    private final String caseType;

    public SearchEventAndCaseRequest(String caseId, String eventId,
                                     String caseJurisdiction, String caseType) {
        this.caseId = caseId;
        this.eventId = eventId;
        this.caseJurisdiction = caseJurisdiction;
        this.caseType = caseType;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getCaseJurisdiction() {
        return caseJurisdiction;
    }

    public String getCaseType() {
        return caseType;
    }
}

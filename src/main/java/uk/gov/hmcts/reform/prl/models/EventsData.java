package uk.gov.hmcts.reform.prl.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventsData {
    private final String eventSequence;
    private final String eventCode;
    private final String dateReceived;
    private final String eventDetails;
}

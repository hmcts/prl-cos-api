package uk.gov.hmcts.reform.prl.models.hearings;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Attendee {
    private String partyID;

    private String hearingSubChannel;
}

package uk.gov.hmcts.reform.prl.models.cafcass.hearing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(builderMethodName = "attendeeWith")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Attendee {

    private String partyID;

    private String hearingSubChannel;
}

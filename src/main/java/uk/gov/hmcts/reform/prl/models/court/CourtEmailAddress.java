package uk.gov.hmcts.reform.prl.models.court;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CourtEmailAddress {

    private final String address;
    private final String description;
    private final String explanation;

}

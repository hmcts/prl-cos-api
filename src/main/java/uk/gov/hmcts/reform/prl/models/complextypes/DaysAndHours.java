package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DaysAndHours {

    int days;
    int hours;
}

package uk.gov.hmcts.reform.prl.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DaysAndHours {

    int days;
    int hours;
}

package uk.gov.hmcts.reform.prl.models.c100.rebuild;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class C100RebuildReasonableAdjustments {

    @JsonProperty
    private final String typeOfHearingApplicantWant;
}

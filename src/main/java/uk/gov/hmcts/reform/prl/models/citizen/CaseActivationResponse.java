package uk.gov.hmcts.reform.prl.models.citizen;

import io.swagger.v3.oas.annotations.media.Schema;

public record CaseActivationResponse(
    @Schema(allowableValues = {"Valid", "Invalid", "Linked", "Duplicate", "NotReady"}) String status
) {
}

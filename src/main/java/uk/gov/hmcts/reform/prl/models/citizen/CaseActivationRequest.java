package uk.gov.hmcts.reform.prl.models.citizen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CaseActivationRequest(
    @NotBlank @Pattern(regexp = "\\d{16}") String caseId,
    @NotBlank @Pattern(regexp = "[a-zA-Z0-9]{8}") String accessCode
) {
}

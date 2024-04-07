package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Element<T> {
    @JsonProperty("id")
    private final UUID id;
    @NotNull
    @Valid
    @JsonProperty("value")
    private final T value;
}

package uk.gov.hmcts.reform.prl.models.common.dynamic;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Optional;
import java.util.UUID;



@Data
@Jacksonized
@Builder(toBuilder = true)
public class DynamicRadioList {

    /**
     * The selected value for the dropdown.
     */
    private DynamicListElement value;

    /**
     * List of options for the dropdown.
     */
    @JsonProperty("list_items")
    private List<DynamicListElement> listItems;

    @JsonIgnore
    public String getValueLabel() {
        return value == null ? null : value.getLabel();
    }

    @JsonIgnore
    public UUID getValueCodeAsUuid() {
        return Optional.ofNullable(getValueCode()).map(UUID::fromString).orElse(null);
    }

    @JsonIgnore
    public String getValueCode() {
        return value == null ? null : value.getCode();
    }
}

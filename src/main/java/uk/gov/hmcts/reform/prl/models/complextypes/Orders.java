package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;

import java.util.List;

@Data
@Builder
public class Orders {

    @JsonProperty("orderType")
    private final List<FL401OrderTypeEnum> orderType;
}

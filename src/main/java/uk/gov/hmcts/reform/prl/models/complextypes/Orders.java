package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@Jacksonized
public class Orders {

    @NotNull(message = "Select at least one type of order")
    @Size(min = 1, message = "Select at least one type of order")
    private final List<FL401OrderTypeEnum> orderType;
}

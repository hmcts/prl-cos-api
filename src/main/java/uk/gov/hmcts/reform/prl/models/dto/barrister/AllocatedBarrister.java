package uk.gov.hmcts.reform.prl.models.dto.barrister;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Builder
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllocatedBarrister {
    @JsonProperty("partyList")
    private final DynamicList partyList;

    @JsonProperty("barristerName")
    private final String barristerFirstName;

    @JsonProperty("barristerName")
    private final String barristerLastName;

    @JsonProperty("barristerEmail")
    private final String barristerEmail;

    @JsonProperty("barristerOrg")
    private final Organisation barristerOrg;
}

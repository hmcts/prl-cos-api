package uk.gov.hmcts.reform.prl.models.dto.barrister;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllocatedBarrister {
    public static final String FULL_NAME_FORMAT = "%s %s";

    @JsonProperty("partyList")
    private final DynamicList partyList;

    @JsonProperty("barristerFirstName")
    private final String barristerFirstName;

    @JsonProperty("barristerLastName")
    private final String barristerLastName;

    @JsonProperty("barristerEmail")
    private final String barristerEmail;

    @JsonProperty("barristerOrg")
    private final Organisation barristerOrg;

    @JsonIgnore
    public String getBarristerFullName() {
        return String.format(
            FULL_NAME_FORMAT,
            this.barristerFirstName,
            this.barristerLastName
        );
    }
}

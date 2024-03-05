package uk.gov.hmcts.reform.prl.models.roleassignment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;

@Data
@AllArgsConstructor
@Builder
public class RoleAssignmentDto {
    @JsonProperty("judicialUser")
    private final JudicialUser judicialUser;

    @JsonProperty("judgeEmail")
    private final String judgeEmail;

    @JsonProperty("legalAdviserList")
    private final DynamicList legalAdviserList;
}

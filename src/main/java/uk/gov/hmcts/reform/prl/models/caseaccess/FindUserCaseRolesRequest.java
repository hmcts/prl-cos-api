package uk.gov.hmcts.reform.prl.models.caseaccess;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class FindUserCaseRolesRequest {
    @JsonProperty("case_ids")
    private List<String> caseIds;
    @JsonProperty("user_ids")
    private List<String> userIds;
}

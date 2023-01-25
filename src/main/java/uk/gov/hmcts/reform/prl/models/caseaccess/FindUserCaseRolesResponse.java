package uk.gov.hmcts.reform.prl.models.caseaccess;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FindUserCaseRolesResponse {
    @JsonProperty("case_users")
    private List<CaseUser> caseUsers;
}

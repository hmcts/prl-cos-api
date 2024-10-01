package uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryAttributes {

    private List<String> jurisdiction;
    private List<String> caseType;
    private List<String> caseId;
}

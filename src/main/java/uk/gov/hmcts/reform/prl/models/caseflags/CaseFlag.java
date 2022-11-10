package uk.gov.hmcts.reform.prl.models.caseflags;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CaseFlag {

    String partyName;
    String roleOnCase;

    List<String> details;
}

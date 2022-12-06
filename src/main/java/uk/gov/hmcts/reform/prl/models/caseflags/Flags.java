package uk.gov.hmcts.reform.prl.models.caseflags;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class Flags {

    String partyName;
    String roleOnCase;

    List<Element<String>> details;
}

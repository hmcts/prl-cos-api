package uk.gov.hmcts.reform.prl.models.caseflags;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Element;

import java.util.Collections;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Flags {

    String partyName;
    String roleOnCase;

    List<Element<String>> details = Collections.emptyList();
}

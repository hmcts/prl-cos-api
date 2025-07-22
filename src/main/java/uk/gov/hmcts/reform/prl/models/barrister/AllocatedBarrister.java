package uk.gov.hmcts.reform.prl.models.barrister;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Jacksonized
@Builder(toBuilder = true)
public class AllocatedBarrister {
    DynamicList partyList;
    String barristerName;
    String barristerEmail;
    Organisation barristerOrg;
}

package uk.gov.hmcts.reform.prl.models.sendandreply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SendAndReplyDynamicDoc {

    private DynamicList submittedDocsRefList;
}

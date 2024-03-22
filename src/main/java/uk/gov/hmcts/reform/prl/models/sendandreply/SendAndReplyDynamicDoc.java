package uk.gov.hmcts.reform.prl.models.sendandreply;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;


@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class SendAndReplyDynamicDoc {

    private DynamicList submittedDocsRefList;
}

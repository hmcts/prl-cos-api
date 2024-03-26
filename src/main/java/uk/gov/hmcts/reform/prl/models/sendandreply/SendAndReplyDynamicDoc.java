package uk.gov.hmcts.reform.prl.models.sendandreply;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;


@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
public class SendAndReplyDynamicDoc {

    private DynamicList submittedDocsRefList;
}

package uk.gov.hmcts.reform.prl.models.sendandreply;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class SendAndReplyDynamicDoc {

    private DynamicList submittedDocsRefList;
}

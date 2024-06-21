package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.sendandreply.AllocatedJudgeForSendAndReply;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class SendAndReplyDto {
    @JsonProperty("allocatedJudgeForSendAndReply")
    private final List<Element<AllocatedJudgeForSendAndReply>> allocatedJudgeForSendAndReply;

    //Amend Draft order
    private String removeDraftOrderText;

    private Object removeDraftOrdersDynamicList;
}

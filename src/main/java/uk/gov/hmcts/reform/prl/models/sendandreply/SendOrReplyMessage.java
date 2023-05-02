package uk.gov.hmcts.reform.prl.models.sendandreply;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;

import java.util.List;

@Data
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class SendOrReplyMessage {

    //PRL-3454 - send & reply messages enhancements
    @JsonProperty("openMessagesList")
    private final List<Element<Message>> openMessagesList;

    @JsonProperty("closedMessagesList")
    private final List<Element<Message>> closedMessagesList;

    @JsonUnwrapped
    private DynamicList judicialOrMagistrateTierList;

    @JsonUnwrapped
    private DynamicList linkedApplicationsList;

    @JsonUnwrapped
    private DynamicList futureHearingsList;

    @JsonUnwrapped
    private DynamicList submittedDocumentsList;

    @JsonUnwrapped
    private DynamicMultiSelectList externalPartiesList;

}

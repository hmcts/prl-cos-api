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
    @JsonUnwrapped(enabled = false)
    @JsonProperty("openMessagesList")
    private final List<Element<Message>> openMessagesList;

    @JsonUnwrapped(enabled = false)
    @JsonProperty("closedMessagesList")
    private final List<Element<Message>> closedMessagesList;

    private DynamicList judicialOrMagistrateTierList;
    private DynamicList linkedApplicationsList;
    private DynamicList futureHearingsList;
    private DynamicList submittedDocumentsList;
    private DynamicMultiSelectList externalPartiesList;

}


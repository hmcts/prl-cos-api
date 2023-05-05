package uk.gov.hmcts.reform.prl.models.sendandreply;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalExternalMessageEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageAboutEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.ExternalPartyDocument;

import java.util.List;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class SendOrReplyMessage {

    //PRL-3454 - send & reply messages enhancements
    @JsonProperty("openMessagesList")
    private final List<Element<Message>> openMessagesList;

    @JsonProperty("closedMessagesList")
    private final List<Element<Message>> closedMessagesList;

    private DynamicList judicialOrMagistrateTierList;
    private DynamicList linkedApplicationsList;
    private DynamicList futureHearingsList;
    private DynamicList submittedDocumentsList;
    private DynamicList ctscEmailList;
    private DynamicMultiSelectList externalPartiesList;

    private InternalExternalMessageEnum internalOrExternalMessage;
    private InternalMessageWhoToSendToEnum internalMessageWhoToSendTo;
    private MessageAboutEnum messageAbout;
    private YesOrNo internalMessageUrgent;
    private String recipientEmailAddresses;
    private String messageSubject;
    private JudicialUser sendReplyJudgeName;

    @JsonProperty("externalPartyDocuments")
    private final List<Element<ExternalPartyDocument>> externalPartyDocuments;
}


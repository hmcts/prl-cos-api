package uk.gov.hmcts.reform.prl.services.sendandreply.messagehandler;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply;
import uk.gov.hmcts.reform.prl.mapper.dynamiclistelement.LegalAdviserDynamicListElementBiConverter;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.services.sendandreply.roleallocation.AssignRoleRequest;
import uk.gov.hmcts.reform.prl.services.sendandreply.roleallocation.LegalAdviserRoleAllocator;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum.COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum.JUDICIARY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum.LEGAL_ADVISER;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum.OTHER;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;

class LegalAdviserMessageHandlerTest {

    private static final String SELECTED_LEGAL_ADVISER_IDAM_ID = UUID.randomUUID().toString();

    @ParameterizedTest
    @MethodSource
    void testCanHandle(MessageRequest messageRequest, boolean expectedCanHandle) {
        LegalAdviserMessageHandler handler = new LegalAdviserMessageHandler(null, null);
        assertThat(handler.canHandle(messageRequest)).isEqualTo(expectedCanHandle);
    }

    private static Stream<Arguments> testCanHandle() {
        return Stream.of(
            Arguments.of(messageRequest(SEND, LEGAL_ADVISER, false, false), false),
            Arguments.of(messageRequest(SEND, LEGAL_ADVISER, true, false), false),
            Arguments.of(messageRequest(SEND, LEGAL_ADVISER, true, true), true),
            Arguments.of(messageRequest(REPLY, LEGAL_ADVISER, false, false), false),
            Arguments.of(messageRequest(REPLY, LEGAL_ADVISER, true, false), false),
            Arguments.of(messageRequest(REPLY, LEGAL_ADVISER, true, true), true),
            Arguments.of(messageRequest(SEND, COURT_ADMIN, false, false), false),
            Arguments.of(messageRequest(SEND, COURT_ADMIN, true, false), false),
            Arguments.of(messageRequest(SEND, COURT_ADMIN, true, true), false),
            Arguments.of(messageRequest(REPLY, COURT_ADMIN, false, false), false),
            Arguments.of(messageRequest(REPLY, COURT_ADMIN, true, false), false),
            Arguments.of(messageRequest(REPLY, COURT_ADMIN, true, true), false),
            Arguments.of(messageRequest(SEND, JUDICIARY, false, false), false),
            Arguments.of(messageRequest(SEND, JUDICIARY, true, false), false),
            Arguments.of(messageRequest(SEND, JUDICIARY, true, true), false),
            Arguments.of(messageRequest(REPLY, JUDICIARY, false, false), false),
            Arguments.of(messageRequest(REPLY, JUDICIARY, true, false), false),
            Arguments.of(messageRequest(REPLY, JUDICIARY, true, true), false),
            Arguments.of(messageRequest(SEND, OTHER, false, false), false),
            Arguments.of(messageRequest(SEND, OTHER, true, false), false),
            Arguments.of(messageRequest(SEND, OTHER, true, true), false),
            Arguments.of(messageRequest(REPLY, OTHER, false, false), false),
            Arguments.of(messageRequest(REPLY, OTHER, true, false), false),
            Arguments.of(messageRequest(REPLY, OTHER, true, true), false)
        );
    }

    @ParameterizedTest
    @EnumSource(value = SendOrReply.class)
    void testHandle(SendOrReply sendOrReply) {
        LegalAdviserRoleAllocator roleAllocator = mock(LegalAdviserRoleAllocator.class);
        LegalAdviserDynamicListElementBiConverter converter = new LegalAdviserDynamicListElementBiConverter();
        LegalAdviserMessageHandler handler = new LegalAdviserMessageHandler(roleAllocator, converter);

        MessageRequest messageRequest = messageRequest(sendOrReply, LEGAL_ADVISER, true, true);
        handler.handle(messageRequest);

        Message message = messageRequest.getMessage();
        assertThat(message.getLegalAdviserName()).isEqualTo("Legal Advisor Name");
        assertThat(message.getLegalAdviserEmail()).isEqualTo("legaladviser@justice.gov.uk");
        verify(roleAllocator).handleRequest(any(AssignRoleRequest.class));
    }

    private static MessageRequest messageRequest(SendOrReply sendOrReply, InternalMessageWhoToSendToEnum whoToSendTo,
                                                 boolean legalAdvisorList, boolean selectedLegalAdviser ) {
        DynamicList legalAdviserList = legalAdviserList(legalAdvisorList, selectedLegalAdviser);

        return MessageRequest.builder()
            .caseData(caseData(sendOrReply, legalAdviserList))
            .caseDataMap(new HashMap<>())
            .message(message(whoToSendTo, legalAdviserList))
            .build();
    }

    private static DynamicList legalAdviserList(boolean legalAdvisorList, boolean selectedLegalAdviser) {
        DynamicList legalAdviserList = null;
        if (legalAdvisorList) {
            legalAdviserList = DynamicList.builder().build();
            if (selectedLegalAdviser) {
                DynamicListElement value = DynamicListElement.builder()
                    .code(SELECTED_LEGAL_ADVISER_IDAM_ID)
                    .label("Legal Advisor Name (legaladviser@justice.gov.uk)")
                    .build();
                legalAdviserList.setValue(value);
            }
        }
        return legalAdviserList;
    }

    private static Message message(InternalMessageWhoToSendToEnum whoToSendTo, DynamicList legalAdviserList) {
        return Message.builder()
            .internalMessageWhoToSendTo(whoToSendTo)
            .legalAdviserList(legalAdviserList)
            .build();
    }

    private static CaseData caseData(SendOrReply sendOrReply, DynamicList legalAdviserList) {
        Message message = Message.builder()
            .legalAdviserList(legalAdviserList)
            .build();

        SendOrReplyMessage sendOrReplyMessage = SendOrReplyMessage.builder()
            .replyMessageObject(sendOrReply == REPLY ? message : null)
            .sendMessageObject(sendOrReply == SEND ? message : null)
            .build();

        return CaseData.builder()
            .chooseSendOrReply(sendOrReply)
            .sendOrReplyMessage(sendOrReplyMessage)
            .build();
    }
}

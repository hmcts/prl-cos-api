package uk.gov.hmcts.reform.prl.services.sendandreply.messagehandler;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply;
import uk.gov.hmcts.reform.prl.models.common.staff.StaffUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffProfile;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_ASSIGNEE_IDAM_ID;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum.COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum.JUDICIARY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum.LEGAL_ADVISER;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum.OTHER;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;

@ExtendWith(MockitoExtension.class)
class LegalAdviserMessageHandlerTest {

    private static final String SELECTED_LEGAL_ADVISER_IDAM_ID = UUID.randomUUID().toString();

    @InjectMocks
    LegalAdviserMessageHandler legalAdviserMessageHandler;
    @Mock
    RefDataUserService refDataUserService;

    @ParameterizedTest
    @MethodSource
    void testCanHandle(MessageRequest messageRequest, boolean expectedCanHandle) {
        assertThat(legalAdviserMessageHandler.canHandle(messageRequest)).isEqualTo(expectedCanHandle);
    }

    private static Stream<Arguments> testCanHandle() {
        return Stream.of(
            Arguments.of(messageRequest(SEND, LEGAL_ADVISER, createStaffUser()), true),
            Arguments.of(messageRequest(SEND, LEGAL_ADVISER, null), false),
            Arguments.of(messageRequest(REPLY, LEGAL_ADVISER, createStaffUser()), true),
            Arguments.of(messageRequest(REPLY, LEGAL_ADVISER, null), false),
            Arguments.of(messageRequest(SEND, COURT_ADMIN, createStaffUser()), false),
            Arguments.of(messageRequest(SEND, COURT_ADMIN, null), false),
            Arguments.of(messageRequest(REPLY, COURT_ADMIN, createStaffUser()), false),
            Arguments.of(messageRequest(REPLY, COURT_ADMIN, null), false),
            Arguments.of(messageRequest(SEND, JUDICIARY, createStaffUser()), false),
            Arguments.of(messageRequest(SEND, JUDICIARY, null), false),
            Arguments.of(messageRequest(REPLY, JUDICIARY, createStaffUser()), false),
            Arguments.of(messageRequest(REPLY, JUDICIARY, null), false),
            Arguments.of(messageRequest(SEND, OTHER, createStaffUser()), false),
            Arguments.of(messageRequest(SEND, OTHER, null), false),
            Arguments.of(messageRequest(REPLY, OTHER, createStaffUser()), false),
            Arguments.of(messageRequest(REPLY, OTHER, null), false)
        );
    }

    @ParameterizedTest
    @EnumSource(value = SendOrReply.class)
    void testHandle(SendOrReply sendOrReply) {
        StaffProfile la = new StaffProfile(SELECTED_LEGAL_ADVISER_IDAM_ID, "John", "Smith", "userType", "legaladviser@justice.gov.uk");
        MessageRequest messageRequest = messageRequest(sendOrReply, LEGAL_ADVISER, createStaffUser());
        when(refDataUserService.getLegalAdviserUserDetails(any(StaffUser.class)))
            .thenReturn(java.util.Optional.of(la));
        legalAdviserMessageHandler.handle(messageRequest);

        Message message = messageRequest.getMessage();
        assertThat(message.getLegalAdviserName()).isEqualTo("John Smith");
        assertThat(message.getLegalAdviserEmail()).isEqualTo("legaladviser@justice.gov.uk");
        assertThat(messageRequest.getCaseDataMap()).containsEntry(TASK_ASSIGNEE_IDAM_ID, SELECTED_LEGAL_ADVISER_IDAM_ID);
    }

    private static MessageRequest messageRequest(SendOrReply sendOrReply, InternalMessageWhoToSendToEnum whoToSendTo,
                                                 StaffUser legalAdviser) {

        return MessageRequest.builder()
            .caseData(caseData(sendOrReply, legalAdviser))
            .caseDataMap(new HashMap<>())
            .message(message(whoToSendTo, legalAdviser))
            .build();
    }

    private static Message message(InternalMessageWhoToSendToEnum whoToSendTo, StaffUser legalAdviser) {
        return Message.builder()
            .internalMessageWhoToSendTo(whoToSendTo)
            .legalAdviser(legalAdviser)
            .build();
    }

    private static CaseData caseData(SendOrReply sendOrReply, StaffUser legalAdviser) {
        Message message = Message.builder()
            .legalAdviser(legalAdviser)
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

    private static StaffUser createStaffUser(){
        return new StaffUser(SELECTED_LEGAL_ADVISER_IDAM_ID);
    }
}

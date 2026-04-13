package uk.gov.hmcts.reform.prl.services.sendandreply;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageAboutEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestOrderHearingTracking;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;

/**
 * Covers the orchestration method extracted from the controller (FPVTL-2408/2409).
 * Per-hearing tracking: lastCompletedDate is set ONLY when the current message is about
 * a hearing and has a selected hearing code. Otherwise the tracking collection is untouched.
 */
@ExtendWith(MockitoExtension.class)
class SendAndReplyCommonServiceProcessAboutToSubmitTest {

    private static final String HEARING_ID = "42";
    private static final String FUTURE_CODE = HEARING_ID + " - FIRST_HEARING";

    @Mock SendAndReplyService sendAndReplyService;
    @Mock UploadAdditionalApplicationService uploadAdditionalApplicationService;

    @InjectMocks
    SendAndReplyCommonService commonService;

    @Test
    void setsCompletionDateForSelectedHearingOnSend() {
        CaseData caseData = sendCaseAboutHearing(FUTURE_CODE).build();
        Map<String, Object> caseDataMap = new HashMap<>();

        AboutToStartOrSubmitCallbackResponse response =
            commonService.processAboutToSubmit("auth", caseData, caseDataMap);

        @SuppressWarnings("unchecked")
        List<Element<RequestOrderHearingTracking>> tracking =
            (List<Element<RequestOrderHearingTracking>>) response.getData().get("requestOrderTaskTrackingByHearing");
        assertThat(tracking).hasSize(1);
        assertThat(tracking.get(0).getValue().getHearingId()).isEqualTo(HEARING_ID);
        assertThat(tracking.get(0).getValue().getLastCompletedDate())
            .isEqualTo(LocalDate.now(ZoneId.of("Europe/London")));
    }

    @Test
    void setsCompletionDateForSelectedHearingOnReply() {
        CaseData caseData = CaseData.builder()
            .chooseSendOrReply(REPLY)
            .caseTypeOfApplication("FL401")
            .sendOrReplyMessage(SendOrReplyMessage.builder()
                .respondToMessage(YesOrNo.No)
                .replyMessageObject(messageAboutHearing(FUTURE_CODE))
                .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        AboutToStartOrSubmitCallbackResponse response =
            commonService.processAboutToSubmit("auth", caseData, caseDataMap);

        @SuppressWarnings("unchecked")
        List<Element<RequestOrderHearingTracking>> tracking =
            (List<Element<RequestOrderHearingTracking>>) response.getData().get("requestOrderTaskTrackingByHearing");
        assertThat(tracking).hasSize(1);
        assertThat(tracking.get(0).getValue().getHearingId()).isEqualTo(HEARING_ID);
    }

    @Test
    void updatesExistingEntryWhenHearingAlreadyTracked() {
        Element<RequestOrderHearingTracking> existing = Element.<RequestOrderHearingTracking>builder()
            .id(UUID.randomUUID())
            .value(RequestOrderHearingTracking.builder()
                .hearingId(HEARING_ID)
                .lastFiredDate(LocalDate.of(2026, 4, 10))
                .lastCompletedDate(LocalDate.of(2026, 4, 11))
                .build())
            .build();
        CaseData caseData = sendCaseAboutHearing(FUTURE_CODE)
            .requestOrderTaskTrackingByHearing(List.of(existing))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            commonService.processAboutToSubmit("auth", caseData, new HashMap<>());

        @SuppressWarnings("unchecked")
        List<Element<RequestOrderHearingTracking>> tracking =
            (List<Element<RequestOrderHearingTracking>>) response.getData().get("requestOrderTaskTrackingByHearing");
        assertThat(tracking).hasSize(1);
        assertThat(tracking.get(0).getValue().getLastFiredDate()).isNull();
        assertThat(tracking.get(0).getValue().getLastCompletedDate())
            .isEqualTo(LocalDate.now(ZoneId.of("Europe/London")));
    }

    @Test
    void doesNotTouchTrackingWhenMessageIsNotAboutHearingAndNoExistingEntries() {
        CaseData caseData = CaseData.builder()
            .chooseSendOrReply(SEND)
            .caseTypeOfApplication("C100")
            .sendOrReplyMessage(SendOrReplyMessage.builder()
                .sendMessageObject(Message.builder()
                    .messageAbout(MessageAboutEnum.APPLICATION)
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            commonService.processAboutToSubmit("auth", caseData, new HashMap<>());

        assertThat(response.getData()).doesNotContainKey("requestOrderTaskTrackingByHearing");
    }

    @Test
    void doesNotTouchTrackingWhenNoSelectedHearingCodeAndNoExistingEntries() {
        CaseData caseData = CaseData.builder()
            .chooseSendOrReply(SEND)
            .caseTypeOfApplication("C100")
            .sendOrReplyMessage(SendOrReplyMessage.builder()
                .sendMessageObject(Message.builder()
                    .messageAbout(MessageAboutEnum.HEARING)
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            commonService.processAboutToSubmit("auth", caseData, new HashMap<>());

        assertThat(response.getData()).doesNotContainKey("requestOrderTaskTrackingByHearing");
    }

    @Test
    void clearsLastFiredDateOnAllEntriesEvenWhenMessageNotAboutHearing() {
        // WA completion DMN auto-completes requestSolicitorOrder on ANY send-and-reply,
        // so we must clear in-flight state regardless of messageAbout. This prevents the
        // cron skipping the case forever if the user's chase message was filed under
        // APPLICATION rather than HEARING.
        Element<RequestOrderHearingTracking> existing = Element.<RequestOrderHearingTracking>builder()
            .id(UUID.randomUUID())
            .value(RequestOrderHearingTracking.builder()
                .hearingId(HEARING_ID)
                .lastFiredDate(LocalDate.of(2026, 4, 10))
                .build())
            .build();
        CaseData caseData = CaseData.builder()
            .chooseSendOrReply(SEND)
            .caseTypeOfApplication("C100")
            .sendOrReplyMessage(SendOrReplyMessage.builder()
                .sendMessageObject(Message.builder()
                    .messageAbout(MessageAboutEnum.APPLICATION)
                    .build())
                .build())
            .requestOrderTaskTrackingByHearing(List.of(existing))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            commonService.processAboutToSubmit("auth", caseData, new HashMap<>());

        @SuppressWarnings("unchecked")
        List<Element<RequestOrderHearingTracking>> tracking =
            (List<Element<RequestOrderHearingTracking>>) response.getData().get("requestOrderTaskTrackingByHearing");
        assertThat(tracking).hasSize(1);
        assertThat(tracking.get(0).getValue().getLastFiredDate()).isNull();
        assertThat(tracking.get(0).getValue().getLastCompletedDate()).isNull();
    }

    @Test
    void clearsLastFiredDateOnOtherHearingsWhenOnlyOneAddressed() {
        // Multi-hearing scenario: cron fired for [A, B]. User chases B via send-and-reply.
        // B gets lastCompletedDate; A's lastFiredDate is cleared so the cron re-evaluates
        // A on the next run (preventing A from being stuck "in flight" indefinitely).
        String otherHearingId = "99";
        Element<RequestOrderHearingTracking> addressed = Element.<RequestOrderHearingTracking>builder()
            .id(UUID.randomUUID())
            .value(RequestOrderHearingTracking.builder()
                .hearingId(HEARING_ID)
                .lastFiredDate(LocalDate.of(2026, 4, 10))
                .build())
            .build();
        Element<RequestOrderHearingTracking> unaddressed = Element.<RequestOrderHearingTracking>builder()
            .id(UUID.randomUUID())
            .value(RequestOrderHearingTracking.builder()
                .hearingId(otherHearingId)
                .lastFiredDate(LocalDate.of(2026, 4, 10))
                .build())
            .build();
        CaseData caseData = sendCaseAboutHearing(FUTURE_CODE)
            .requestOrderTaskTrackingByHearing(List.of(addressed, unaddressed))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            commonService.processAboutToSubmit("auth", caseData, new HashMap<>());

        @SuppressWarnings("unchecked")
        List<Element<RequestOrderHearingTracking>> tracking =
            (List<Element<RequestOrderHearingTracking>>) response.getData().get("requestOrderTaskTrackingByHearing");
        assertThat(tracking).hasSize(2);
        RequestOrderHearingTracking addressedAfter = tracking.stream()
            .map(Element::getValue).filter(v -> HEARING_ID.equals(v.getHearingId())).findFirst().orElseThrow();
        RequestOrderHearingTracking unaddressedAfter = tracking.stream()
            .map(Element::getValue).filter(v -> otherHearingId.equals(v.getHearingId())).findFirst().orElseThrow();
        assertThat(addressedAfter.getLastFiredDate()).isNull();
        assertThat(addressedAfter.getLastCompletedDate())
            .isEqualTo(LocalDate.now(ZoneId.of("Europe/London")));
        assertThat(unaddressedAfter.getLastFiredDate()).isNull();
        assertThat(unaddressedAfter.getLastCompletedDate()).isNull();
    }

    @Test
    void stampsCaseAccessCategoryRegardless() {
        CaseData caseData = sendCaseAboutHearing(FUTURE_CODE).build();

        AboutToStartOrSubmitCallbackResponse response =
            commonService.processAboutToSubmit("auth", caseData, new HashMap<>());

        assertThat(response.getData()).containsEntry("CaseAccessCategory", "C100");
    }

    @Test
    void clearsTemporaryFieldsBeforeReturning() {
        CaseData caseData = sendCaseAboutHearing(FUTURE_CODE).build();

        commonService.processAboutToSubmit("auth", caseData, new HashMap<>());

        verify(sendAndReplyService, org.mockito.Mockito.atLeastOnce())
            .removeTemporaryFields(any(), any(String[].class));
    }

    @Test
    void doesNotCallCloseMessageWhenSending() {
        CaseData caseData = sendCaseAboutHearing(FUTURE_CODE).build();

        commonService.processAboutToSubmit("auth", caseData, new HashMap<>());

        verify(sendAndReplyService, never()).closeMessage(any(CaseData.class), any());
    }

    // -------- helpers --------

    private CaseData.CaseDataBuilder<?, ?> sendCaseAboutHearing(String futureCode) {
        return CaseData.builder()
            .chooseSendOrReply(SEND)
            .caseTypeOfApplication("C100")
            .sendOrReplyMessage(SendOrReplyMessage.builder()
                .sendMessageObject(messageAboutHearing(futureCode))
                .build());
    }

    private Message messageAboutHearing(String futureCode) {
        return Message.builder()
            .messageAbout(MessageAboutEnum.HEARING)
            .selectedFutureHearingCode(futureCode)
            .build();
    }
}
package uk.gov.hmcts.reform.prl.services.sendandreply;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;

@ExtendWith(MockitoExtension.class)
class SendAndReplyCommonServiceProcessAboutToSubmitTest {

    private static final String HEARING_ID = "42";

    @Mock SendAndReplyService sendAndReplyService;
    @Mock UploadAdditionalApplicationService uploadAdditionalApplicationService;

    @InjectMocks
    SendAndReplyCommonService commonService;

    @Test
    void recordsCompletionForChasedHearingWhenNoExistingEntries() {
        CaseData caseData = sendCase().build();

        AboutToStartOrSubmitCallbackResponse response =
            commonService.processAboutToSubmit("auth", caseData, new HashMap<>(), HEARING_ID);

        List<Element<RequestOrderHearingTracking>> tracking = trackingFrom(response);
        assertThat(tracking).hasSize(1);
        assertThat(tracking.get(0).getValue().getHearingId()).isEqualTo(HEARING_ID);
        assertThat(tracking.get(0).getValue().getLastCompletedDate())
            .isEqualTo(LocalDate.now(ZoneId.of("Europe/London")));
        assertThat(tracking.get(0).getValue().getLastFiredDate()).isNull();
    }

    @Test
    void updatesExistingEntryStampingCompletionAndClearingInFlightGuard() {
        Element<RequestOrderHearingTracking> existing = Element.<RequestOrderHearingTracking>builder()
            .id(UUID.randomUUID())
            .value(RequestOrderHearingTracking.builder()
                .hearingId(HEARING_ID)
                .lastFiredDate(LocalDate.of(2026, 4, 10))
                .build())
            .build();
        CaseData caseData = sendCase()
            .requestOrderTaskTrackingByHearing(List.of(existing))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            commonService.processAboutToSubmit("auth", caseData, new HashMap<>(), HEARING_ID);

        List<Element<RequestOrderHearingTracking>> tracking = trackingFrom(response);
        assertThat(tracking).hasSize(1);
        assertThat(tracking.get(0).getValue().getLastFiredDate()).isNull();
        assertThat(tracking.get(0).getValue().getLastCompletedDate())
            .isEqualTo(LocalDate.now(ZoneId.of("Europe/London")));
    }

    @Test
    void leavesOtherHearingsUntouchedWhenChasingOne() {
        String otherHearingId = "99";
        LocalDate otherFired = LocalDate.of(2026, 4, 10);
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
                .lastFiredDate(otherFired)
                .build())
            .build();
        CaseData caseData = sendCase()
            .requestOrderTaskTrackingByHearing(List.of(addressed, unaddressed))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            commonService.processAboutToSubmit("auth", caseData, new HashMap<>(), HEARING_ID);

        List<Element<RequestOrderHearingTracking>> tracking = trackingFrom(response);
        assertThat(tracking).hasSize(2);
        RequestOrderHearingTracking addressedAfter = tracking.stream()
            .map(Element::getValue).filter(v -> HEARING_ID.equals(v.getHearingId())).findFirst().orElseThrow();
        RequestOrderHearingTracking unaddressedAfter = tracking.stream()
            .map(Element::getValue).filter(v -> otherHearingId.equals(v.getHearingId())).findFirst().orElseThrow();
        assertThat(addressedAfter.getLastFiredDate()).isNull();
        assertThat(addressedAfter.getLastCompletedDate())
            .isEqualTo(LocalDate.now(ZoneId.of("Europe/London")));
        assertThat(unaddressedAfter.getLastFiredDate()).isEqualTo(otherFired);
        assertThat(unaddressedAfter.getLastCompletedDate()).isNull();
    }

    @Test
    void nullChasedHearingIdLeavesTrackingUntouched() {
        Element<RequestOrderHearingTracking> existing = Element.<RequestOrderHearingTracking>builder()
            .id(UUID.randomUUID())
            .value(RequestOrderHearingTracking.builder()
                .hearingId(HEARING_ID)
                .lastFiredDate(LocalDate.of(2026, 4, 10))
                .build())
            .build();
        CaseData caseData = sendCase()
            .requestOrderTaskTrackingByHearing(List.of(existing))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            commonService.processAboutToSubmit("auth", caseData, new HashMap<>(), null);

        assertThat(response.getData()).doesNotContainKey("requestOrderTaskTrackingByHearing");
    }

    @Test
    void stampsCaseAccessCategoryRegardless() {
        CaseData caseData = sendCase().build();

        AboutToStartOrSubmitCallbackResponse response =
            commonService.processAboutToSubmit("auth", caseData, new HashMap<>(), HEARING_ID);

        assertThat(response.getData()).containsEntry("CaseAccessCategory", "C100");
    }

    @Test
    void clearsTemporaryFieldsBeforeReturning() {
        CaseData caseData = sendCase().build();

        commonService.processAboutToSubmit("auth", caseData, new HashMap<>(), HEARING_ID);

        verify(sendAndReplyService, org.mockito.Mockito.atLeastOnce())
            .removeTemporaryFields(any(), any(String[].class));
    }

    @Test
    void doesNotCallCloseMessageWhenSending() {
        CaseData caseData = sendCase().build();

        commonService.processAboutToSubmit("auth", caseData, new HashMap<>(), HEARING_ID);

        verify(sendAndReplyService, never()).closeMessage(any(CaseData.class), any());
    }


    private CaseData.CaseDataBuilder<?, ?> sendCase() {
        return CaseData.builder()
            .chooseSendOrReply(SEND)
            .caseTypeOfApplication("C100")
            .sendOrReplyMessage(SendOrReplyMessage.builder()
                .sendMessageObject(Message.builder().build())
                .build());
    }

    @SuppressWarnings("unchecked")
    private static List<Element<RequestOrderHearingTracking>> trackingFrom(AboutToStartOrSubmitCallbackResponse response) {
        return (List<Element<RequestOrderHearingTracking>>)
            response.getData().get("requestOrderTaskTrackingByHearing");
    }
}

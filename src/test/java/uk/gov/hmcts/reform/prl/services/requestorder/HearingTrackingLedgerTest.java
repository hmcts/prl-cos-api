package uk.gov.hmcts.reform.prl.services.requestorder;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestOrderHearingTracking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HearingTrackingLedgerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 4, 24);
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);

    @Test
    void fromCaseDataWithNullCollectionProducesEmptyLedger() {
        CaseData caseData = CaseData.builder().requestOrderTaskTrackingByHearing(null).build();

        HearingTrackingLedger ledger = HearingTrackingLedger.from(caseData);

        assertThat(ledger.find("anything")).isEmpty();
        assertThat(ledger.asCollection()).isEmpty();
    }

    @Test
    void fromCaseDataWithEmptyCollectionProducesEmptyLedger() {
        CaseData caseData = CaseData.builder().requestOrderTaskTrackingByHearing(List.of()).build();

        HearingTrackingLedger ledger = HearingTrackingLedger.from(caseData);

        assertThat(ledger.find("anything")).isEmpty();
        assertThat(ledger.asCollection()).isEmpty();
    }

    @Test
    void findReturnsExistingTrackingByHearingId() {
        RequestOrderHearingTracking tracking = RequestOrderHearingTracking.builder()
            .hearingId("H1")
            .lastFiredDate(YESTERDAY)
            .build();
        HearingTrackingLedger ledger = HearingTrackingLedger.from(caseDataWith(tracking));

        Optional<RequestOrderHearingTracking> found = ledger.find("H1");

        assertThat(found).isPresent();
        assertThat(found.get().getLastFiredDate()).isEqualTo(YESTERDAY);
    }

    @Test
    void findReturnsEmptyWhenHearingIdNotPresent() {
        HearingTrackingLedger ledger = HearingTrackingLedger.from(
            caseDataWith(RequestOrderHearingTracking.builder().hearingId("H1").build()));

        assertThat(ledger.find("UNKNOWN")).isEmpty();
    }

    @Test
    void recordFiredUpsertsLastFiredDateOnExistingEntry() {
        RequestOrderHearingTracking tracking = RequestOrderHearingTracking.builder()
            .hearingId("H1")
            .lastCompletedDate(YESTERDAY)
            .build();
        HearingTrackingLedger ledger = HearingTrackingLedger.from(caseDataWith(tracking));

        ledger.recordFired("H1", TODAY);

        RequestOrderHearingTracking updated = ledger.find("H1").orElseThrow();
        assertThat(updated.getLastFiredDate()).isEqualTo(TODAY);
        // existing fields preserved
        assertThat(updated.getLastCompletedDate()).isEqualTo(YESTERDAY);
    }

    @Test
    void recordFiredCreatesNewEntryWhenHearingIdAbsent() {
        HearingTrackingLedger ledger = HearingTrackingLedger.from(CaseData.builder().build());

        ledger.recordFired("H99", TODAY);

        RequestOrderHearingTracking created = ledger.find("H99").orElseThrow();
        assertThat(created.getHearingId()).isEqualTo("H99");
        assertThat(created.getLastFiredDate()).isEqualTo(TODAY);
        assertThat(created.getLastCompletedDate()).isNull();
    }

    @Test
    void asCollectionPreservesInsertionOrder() {
        HearingTrackingLedger ledger = HearingTrackingLedger.from(caseDataWith(
            RequestOrderHearingTracking.builder().hearingId("A").build(),
            RequestOrderHearingTracking.builder().hearingId("B").build()));

        ledger.recordFired("C", TODAY);

        assertThat(ledger.asCollection())
            .extracting(e -> e.getValue().getHearingId())
            .containsExactly("A", "B", "C");
    }

    @Test
    void recordFiredOnExistingEntryDoesNotChangeOrderingOrAddDuplicate() {
        HearingTrackingLedger ledger = HearingTrackingLedger.from(caseDataWith(
            RequestOrderHearingTracking.builder().hearingId("A").build(),
            RequestOrderHearingTracking.builder().hearingId("B").build()));

        ledger.recordFired("A", TODAY);

        assertThat(ledger.asCollection())
            .extracting(e -> e.getValue().getHearingId())
            .containsExactly("A", "B");
    }

    @Test
    void recordCompletedStampsLastCompletedAndClearsLastFiredOnExistingEntry() {
        RequestOrderHearingTracking tracking = RequestOrderHearingTracking.builder()
            .hearingId("H1")
            .lastFiredDate(YESTERDAY)
            .build();
        HearingTrackingLedger ledger = HearingTrackingLedger.from(caseDataWith(tracking));

        ledger.recordCompleted("H1", TODAY);

        RequestOrderHearingTracking updated = ledger.find("H1").orElseThrow();
        assertThat(updated.getLastCompletedDate()).isEqualTo(TODAY);
        assertThat(updated.getLastFiredDate()).isNull();
    }

    @Test
    void recordCompletedCreatesEntryWhenHearingNotPreviouslyTracked() {
        HearingTrackingLedger ledger = HearingTrackingLedger.from(CaseData.builder().build());

        ledger.recordCompleted("H99", TODAY);

        RequestOrderHearingTracking created = ledger.find("H99").orElseThrow();
        assertThat(created.getHearingId()).isEqualTo("H99");
        assertThat(created.getLastCompletedDate()).isEqualTo(TODAY);
        assertThat(created.getLastFiredDate()).isNull();
    }

    @Test
    void recordCompletedTouchesOnlyTheNamedHearing() {
        HearingTrackingLedger ledger = HearingTrackingLedger.from(caseDataWith(
            RequestOrderHearingTracking.builder().hearingId("A").lastFiredDate(YESTERDAY).build(),
            RequestOrderHearingTracking.builder().hearingId("B").lastFiredDate(YESTERDAY).build()));

        ledger.recordCompleted("A", TODAY);

        assertThat(ledger.find("A").orElseThrow().getLastFiredDate()).isNull();
        assertThat(ledger.find("A").orElseThrow().getLastCompletedDate()).isEqualTo(TODAY);
        assertThat(ledger.find("B").orElseThrow().getLastFiredDate()).isEqualTo(YESTERDAY);
        assertThat(ledger.find("B").orElseThrow().getLastCompletedDate()).isNull();
    }

    private static CaseData caseDataWith(RequestOrderHearingTracking... trackings) {
        return CaseData.builder()
            .requestOrderTaskTrackingByHearing(java.util.Arrays.stream(trackings)
                .map(t -> Element.<RequestOrderHearingTracking>builder()
                    .id(UUID.randomUUID()).value(t).build())
                .toList())
            .build();
    }
}

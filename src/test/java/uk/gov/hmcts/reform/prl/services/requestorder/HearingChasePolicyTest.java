package uk.gov.hmcts.reform.prl.services.requestorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestOrderHearingTracking;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.judicial.FinalisationDetails;
import uk.gov.hmcts.reform.prl.services.workingdays.WorkingDayIndicator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingChasePolicyTest {

    private static final String HEARING_ID = "1";
    private static final LocalDate TODAY = LocalDate.of(2026, 4, 24);//Friday

    @Mock WorkingDayIndicator workingDayIndicator;

    HearingChasePolicy policy;

    @BeforeEach
    void setUp() {
        policy = new HearingChasePolicy(workingDayIndicator);
        ReflectionTestUtils.setField(policy, "c100CadenceWorkingDays", 3);
        ReflectionTestUtils.setField(policy, "fl401CadenceWorkingDays", 1);
        ReflectionTestUtils.setField(policy, "hearingStatusesToFilter",
            List.of("COMPLETED", "AWAITING_ACTUALS"));
    }

    @Test
    void hearingIdOfReturnsStringForNonNullId() {
        CaseHearing hearing = CaseHearing.caseHearingWith().hearingID(42L).build();

        assertThat(HearingChasePolicy.hearingIdOf(hearing)).isEqualTo("42");
    }

    @Test
    void hearingIdOfReturnsNullForNullId() {
        CaseHearing hearing = CaseHearing.caseHearingWith().hearingID(null).build();

        assertThat(HearingChasePolicy.hearingIdOf(hearing)).isNull();
    }

    @Test
    void decideSkipsWhenHearingIdMissing() {
        CaseHearing hearing = CaseHearing.caseHearingWith()
            .hearingID(null).hmcStatus("COMPLETED").build();

        ChaseDecision decision = policy.decide(hearing, fl401Case().build(), emptyLedger(), TODAY);

        assertThat(decision.shouldFire()).isFalse();
        assertThat(decision.description()).isEqualTo("skipped - hearingId missing (status=COMPLETED)");
    }

    @Test
    void decideSkipsWhenStatusNotInFilter() {
        CaseHearing hearing = hearing("LISTED", TODAY.minusDays(5));

        ChaseDecision decision = policy.decide(hearing, fl401Case().build(), emptyLedger(), TODAY);

        assertThat(decision.shouldFire()).isFalse();
        assertThat(decision.description()).isEqualTo("skipped - status=LISTED not in filter");
    }

    @Test
    void decideSkipsWhenHearingHasNotAtCadence() {
        CaseHearing hearing = hearing("COMPLETED", TODAY.plusDays(1));
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(2);

        ChaseDecision decision = policy.decide(hearing, fl401Case().build(), emptyLedger(), TODAY);

        assertThat(decision.shouldFire()).isFalse();
        assertThat(decision.description()).isEqualTo("skipped - hearingEndDate=2026-04-25 not 1 days away");
    }

    @Test
    void decideSkipsWhenHearingMappedToDraftOrder() {
        CaseData caseData = fl401Case()
            .draftOrderCollection(List.of(draftOrderForHearing(HEARING_ID)))
            .build();

        ChaseDecision decision = policy.decide(
            hearing("COMPLETED", TODAY.minusDays(5)), caseData, emptyLedger(), TODAY);

        assertThat(decision.shouldFire()).isFalse();
        assertThat(decision.description()).isEqualTo("skipped - linked order exists (cycle complete)");
    }

    @Test
    void decideSkipsWhenDraftOrderUsesHearingsTypeLinkage() {
        // Solicitor draft-an-order flow stores the hearing as the dropdown label, not the
        // HMC UUID. Format: "<hearingTypeValue> - dd/MM/yyyy hh:mm:ss".
        LocalDate hearingDate = TODAY.minusDays(2);
        String label = "Allocation - " + hearingDate.atTime(9, 0).format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss"));
        CaseHearing hearingWithType = CaseHearing.caseHearingWith()
            .hearingID(Long.valueOf(HEARING_ID))
            .hmcStatus("COMPLETED")
            .hearingTypeValue("Allocation")
            .hearingDaySchedule(List.of(HearingDaySchedule.hearingDayScheduleWith()
                .hearingStartDateTime(hearingDate.atTime(9, 0))
                .hearingEndDateTime(hearingDate.atTime(16, 0))
                .build()))
            .build();
        CaseData caseData = fl401Case()
            .draftOrderCollection(List.of(draftOrderForHearingsTypeLabel(label)))
            .build();

        ChaseDecision decision = policy.decide(hearingWithType, caseData, emptyLedger(), TODAY);

        assertThat(decision.shouldFire()).isFalse();
        assertThat(decision.description()).isEqualTo("skipped - linked order exists (cycle complete)");
    }

    @Test
    void decideSkipsWhenHearingsTypeMatchesByDateSuffixDespiteEmptyTypeValue() {
        // Cron-context: HearingService.getHearings ref-data lookup failed, so
        // hearing.getHearingTypeValue() is blank. The draft order was saved earlier
        // (when ref-data was working) with the full "Allocation - dd/MM/yyyy hh:mm:ss"
        // label. Full-label match misses; date-suffix fallback still recognises the link.
        LocalDate hearingDate = TODAY.minusDays(2);
        String savedLabel = "Allocation - " + hearingDate.atTime(9, 0).format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss"));
        CaseHearing hearingWithEmptyType = CaseHearing.caseHearingWith()
            .hearingID(Long.valueOf(HEARING_ID))
            .hmcStatus("COMPLETED")
            .hearingTypeValue("")
            .hearingDaySchedule(List.of(HearingDaySchedule.hearingDayScheduleWith()
                .hearingStartDateTime(hearingDate.atTime(9, 0))
                .hearingEndDateTime(hearingDate.atTime(16, 0))
                .build()))
            .build();
        CaseData caseData = fl401Case()
            .draftOrderCollection(List.of(draftOrderForHearingsTypeLabel(savedLabel)))
            .build();

        ChaseDecision decision = policy.decide(hearingWithEmptyType, caseData, emptyLedger(), TODAY);

        assertThat(decision.shouldFire()).isFalse();
        assertThat(decision.description()).isEqualTo("skipped - linked order exists (cycle complete)");
    }

    @Test
    void decideDoesNotSkipWhenHearingsTypeLabelDoesNotMatchAnyDaySchedule() {
        // A solicitor draft was created for a DIFFERENT hearing — the chase should still fire.
        LocalDate hearingDate = TODAY.minusDays(2);
        CaseHearing hearingWithType = CaseHearing.caseHearingWith()
            .hearingID(Long.valueOf(HEARING_ID))
            .hmcStatus("COMPLETED")
            .hearingTypeValue("Allocation")
            .hearingDaySchedule(List.of(HearingDaySchedule.hearingDayScheduleWith()
                .hearingStartDateTime(hearingDate.atTime(9, 0))
                .hearingEndDateTime(hearingDate.atTime(16, 0))
                .build()))
            .build();
        CaseData caseData = fl401Case()
            .draftOrderCollection(List.of(draftOrderForHearingsTypeLabel("Mention - 31/12/2026 14:00:00")))
            .build();
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(1);

        ChaseDecision decision = policy.decide(hearingWithType, caseData, emptyLedger(), TODAY);

        assertThat(decision.shouldFire()).isTrue();
        assertThat(decision.description()).isEqualTo("cadence met - firing");
    }

    @Test
    void decideSkipsWhenHearingMappedToSavedOrder() {
        CaseData caseData = fl401Case()
            .orderCollection(List.of(savedOrderForHearing(HEARING_ID)))
            .build();

        ChaseDecision decision = policy.decide(
            hearing("COMPLETED", TODAY.plusDays(1)), caseData, emptyLedger(), TODAY);

        assertThat(decision.shouldFire()).isFalse();
        assertThat(decision.description()).isEqualTo("skipped - linked order exists (cycle complete)");
    }

    @Test
    void shouldFireForMarkAsDoneWithNoLastCompleted() {
        HearingTrackingLedger ledger = ledgerWith(
            RequestOrderHearingTracking.builder()
                .hearingId(HEARING_ID)
                .lastFiredDate(TODAY.minusDays(1))
                .lastCompletedDate(null)
                .build());

        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(1);

        ChaseDecision decision = policy.decide(
            hearing("COMPLETED", TODAY.minusDays(5)), fl401Case().build(), ledger, TODAY);

        assertThat(decision.shouldFire()).isTrue();
    }

    @Test
    void decideSkipsWhenHearingCadenceNotMet() {
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(2);

        ChaseDecision decision = policy.decide(
            hearing("COMPLETED", TODAY.minusDays(1)), fl401Case().build(), emptyLedger(), TODAY);

        assertThat(decision.shouldFire()).isFalse();
        assertThat(decision.description()).isEqualTo("skipped - hearingEndDate=2026-04-23 not 1 days away");
    }

    @Test
    void decideFiresForFL401WhenAllConditionsMet() {
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(1);

        ChaseDecision decision = policy.decide(
            hearing("COMPLETED", TODAY.plusDays(1)), fl401Case().build(), emptyLedger(), TODAY);

        assertThat(decision.shouldFire()).isTrue();
        assertThat(decision.description()).isEqualTo("cadence met - firing");
    }

    @Test
    void decideFiresForC100WhenCadenceOfThreeMet() {
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(3);

        ChaseDecision decision = policy.decide(
            hearing("COMPLETED", TODAY.minusDays(3)), c100Case().build(), emptyLedger(), TODAY);

        assertThat(decision.shouldFire()).isTrue();
    }

    @Test
    void decideSkipsWhenHearingMappedToFinalisedOrder() {
        LocalDate hearingDate = TODAY.plusDays(3);
        CaseData caseData = fl401Case()
            .orderCollection(List.of(finalisedOrder(hearingDate)))
            .build();
        CaseHearing hearingWithType = CaseHearing.caseHearingWith()
            .hearingID(Long.valueOf(HEARING_ID))
            .hmcStatus("COMPLETED")
            .hearingTypeValue("Allocation")
            .hearingDaySchedule(List.of(HearingDaySchedule.hearingDayScheduleWith()
                                            .hearingStartDateTime(hearingDate.atTime(9, 0))
                                            .hearingEndDateTime(hearingDate.atTime(16, 0))
                                            .build()))
            .build();

        ChaseDecision decision = policy.decide(
            hearingWithType, caseData, emptyLedger(), TODAY);

        assertThat(decision.shouldFire()).isFalse();
        assertThat(decision.description()).isEqualTo("skipped - linked order exists (cycle complete)");
    }

    private static CaseData.CaseDataBuilder<?, ?> fl401Case() {
        return CaseData.builder().caseTypeOfApplication("FL401");
    }

    private static CaseData.CaseDataBuilder<?, ?> c100Case() {
        return CaseData.builder().caseTypeOfApplication("C100");
    }

    private static CaseHearing hearing(String status, LocalDate endDate) {
        return CaseHearing.caseHearingWith()
            .hearingID(Long.valueOf(HEARING_ID))
            .hmcStatus(status)
            .hearingDaySchedule(List.of(HearingDaySchedule.hearingDayScheduleWith()
                .hearingStartDateTime(endDate.atTime(9, 0))
                .hearingEndDateTime(endDate.atTime(16, 0))
                .build()))
            .build();
    }

    private static HearingTrackingLedger emptyLedger() {
        return HearingTrackingLedger.from(CaseData.builder().build());
    }

    private static HearingTrackingLedger ledgerWith(RequestOrderHearingTracking tracking) {
        CaseData caseData = CaseData.builder()
            .requestOrderTaskTrackingByHearing(List.of(
                Element.<RequestOrderHearingTracking>builder()
                    .id(UUID.randomUUID()).value(tracking).build()))
            .build();
        return HearingTrackingLedger.from(caseData);
    }

    private static Element<DraftOrder> draftOrderForHearing(String hearingId) {
        return Element.<DraftOrder>builder().value(
            DraftOrder.builder()
                .manageOrderHearingDetails(List.of(
                    Element.<HearingData>builder().value(
                        HearingData.builder()
                            .confirmedHearingDates(DynamicList.builder()
                                .value(DynamicListElement.builder().code(hearingId).build())
                                .build())
                            .build()
                    ).build()))
                .build()
        ).build();
    }

    private static Element<DraftOrder> draftOrderForHearingsTypeLabel(String label) {
        return Element.<DraftOrder>builder().value(
            DraftOrder.builder()
                .hearingsType(DynamicList.builder()
                    .value(DynamicListElement.builder().code(label).label(label).build())
                    .build())
                .build()
        ).build();
    }

    private static Element<OrderDetails> savedOrderForHearing(String hearingId) {
        return Element.<OrderDetails>builder().value(
            OrderDetails.builder()
                .manageOrderHearingDetails(List.of(
                    Element.<HearingData>builder().value(
                        HearingData.builder()
                            .confirmedHearingDates(DynamicList.builder()
                                .value(DynamicListElement.builder().code(hearingId).build())
                                .build())
                            .build()
                    ).build()))
                .build()
        ).build();
    }

    private Element<OrderDetails> finalisedOrder(LocalDate hearingDate) {
        String formattedDate = hearingDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return Element.<OrderDetails>builder().value(
            OrderDetails.builder()
                .selectedHearingType("Allocation - " + formattedDate + " 09:00:00")
                .finalisationDetails(FinalisationDetails.builder().build())
                .build()
        ).build();
    }
}

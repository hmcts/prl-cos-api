package uk.gov.hmcts.reform.prl.services.requestorder;

import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestOrderHearingTracking;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

/**
 * Indexed view of a case's per-hearing Request Order tracking entries.
 */
public final class HearingTrackingLedger {

    private final Map<String, Element<RequestOrderHearingTracking>> hearingIdToRequestOrderTaskTracking;

    private HearingTrackingLedger(Map<String, Element<RequestOrderHearingTracking>> hearingIdToRequestOrderTaskTracking) {
        this.hearingIdToRequestOrderTaskTracking = hearingIdToRequestOrderTaskTracking;
    }

    public static HearingTrackingLedger from(CaseData caseData) {
        Map<String, Element<RequestOrderHearingTracking>> indexedTaskTracking = new LinkedHashMap<>();
        nullSafeCollection(
            caseData.getRequestOrderTaskTrackingByHearing())
                .forEach(element -> indexedTaskTracking.put(
                    element.getValue().getHearingId(),
                    element));

        return new HearingTrackingLedger(indexedTaskTracking);
    }

    public Optional<RequestOrderHearingTracking> find(String hearingId) {
        Element<RequestOrderHearingTracking> element = hearingIdToRequestOrderTaskTracking.get(hearingId);
        return element == null ? Optional.empty() : Optional.of(element.getValue());
    }

    /**
     * Records that a Request Order task has been fired today for this hearing — stamping the
     * existing entry if there is one, or creating a new entry on first fire.
     */
    public void recordFired(String hearingId, LocalDate today) {
        Element<RequestOrderHearingTracking> existing = hearingIdToRequestOrderTaskTracking.get(hearingId);
        if (existing != null) {
            existing.getValue().setLastFiredDate(today);
            return;
        }
        hearingIdToRequestOrderTaskTracking.put(hearingId, Element.<RequestOrderHearingTracking>builder()
            .id(UUID.randomUUID())
            .value(RequestOrderHearingTracking.builder()
                .hearingId(hearingId)
                .lastFiredDate(today)
                .build())
            .build());
    }

    /**
     * Records that the Request Order chase for this hearing has been satisfied today.
     */
    public void recordCompleted(String hearingId, LocalDate today) {
        Element<RequestOrderHearingTracking> existing = hearingIdToRequestOrderTaskTracking.get(hearingId);
        if (existing != null) {
            existing.getValue().setLastCompletedDate(today);
            existing.getValue().setLastFiredDate(null);
            return;
        }
        hearingIdToRequestOrderTaskTracking.put(hearingId, Element.<RequestOrderHearingTracking>builder()
            .id(UUID.randomUUID())
            .value(RequestOrderHearingTracking.builder()
                .hearingId(hearingId)
                .lastCompletedDate(today)
                .build())
            .build());
    }

    public List<Element<RequestOrderHearingTracking>> asCollection() {
        return List.copyOf(hearingIdToRequestOrderTaskTracking.values());
    }
}

package uk.gov.hmcts.reform.prl.services.requestorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
import uk.gov.hmcts.reform.prl.services.workingdays.WorkingDayIndicator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

/**
 * Encapsulates the rules deciding whether a hearing currently warrants a Request Order
 * task fire (FPVTL-2408/2409).
 */
@Component
@RequiredArgsConstructor
class HearingChasePolicy {

    private static final String C100 = "C100";

    private final WorkingDayIndicator workingDayIndicator;

    @Value("${request-order-task.cadence-working-days.c100:3}")
    private int c100CadenceWorkingDays;

    @Value("${request-order-task.cadence-working-days.fl401:1}")
    private int fl401CadenceWorkingDays;

    @Value("#{'${hearing_component.hearingStatusesToFilter}'.split(',')}")
    private List<String> hearingStatusesToFilter;

    static String hearingIdOf(CaseHearing hearing) {
        return hearing.getHearingID() == null ? null : String.valueOf(hearing.getHearingID());
    }

    ChaseDecision decide(CaseHearing hearing, CaseData caseData, HearingTrackingLedger ledger, LocalDate today) {
        String hearingId = hearingIdOf(hearing);
        if (hearingId == null) {
            return ChaseDecision.skipUnknownHearingId(hearing.getHmcStatus());
        }
        if (!allowedStatuses().contains(hearing.getHmcStatus())) {
            return ChaseDecision.skipStatusNotInFilter(hearing.getHmcStatus());
        }
        LocalDate hearingEndDate = computeHearingEndDate(hearing);
        if (hearingEndDate == null || hearingEndDate.isAfter(today)) {
            return ChaseDecision.skipHearingNotEnded(hearingEndDate);
        }
        if (isHearingMappedToOrder(caseData, hearingId)) {
            return ChaseDecision.skipLinkedOrderExists();
        }

        Optional<RequestOrderHearingTracking> tracking = ledger.find(hearingId);
        if (tracking.map(t -> t.getLastFiredDate() != null).orElse(false)) {
            return ChaseDecision.skipInFlight();
        }

        LocalDate anchor = tracking
            .map(RequestOrderHearingTracking::getLastCompletedDate)
            .orElse(hearingEndDate);
        int cadence = cadenceFor(caseData.getCaseTypeOfApplication());
        int workingDaysSinceAnchor = workingDayIndicator.workingDaysBetween(anchor, today);
        if (workingDaysSinceAnchor < cadence) {
            return ChaseDecision.skipBeforeCadence(workingDaysSinceAnchor, anchor, cadence);
        }
        return ChaseDecision.fire();
    }

    private List<String> allowedStatuses() {
        return hearingStatusesToFilter.stream().map(String::trim).toList();
    }

    private int cadenceFor(String caseTypeOfApplication) {
        return C100.equals(caseTypeOfApplication) ? c100CadenceWorkingDays : fl401CadenceWorkingDays;
    }

    private static LocalDate computeHearingEndDate(CaseHearing hearing) {
        return nullSafeCollection(hearing.getHearingDaySchedule()).stream()
            .map(HearingDaySchedule::getHearingEndDateTime).filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .map(LocalDateTime::toLocalDate)
            .orElse(null);
    }

    /**
     * Returns true if the hearing is already referenced by any order on the case — draft or
     * saved, created by any user role. A match short-circuits the chase: once an order
     * exists for the hearing (from a solicitor, judge, legal adviser, etc.),
     * no further Request Order reminder is needed.
     */
    private static boolean isHearingMappedToOrder(CaseData caseData, String hearingId) {
        return isHearingReferencedBy(caseData.getDraftOrderCollection(),
                                     DraftOrder::getManageOrderHearingDetails, hearingId)
            || isHearingReferencedBy(caseData.getOrderCollection(),
                                     OrderDetails::getManageOrderHearingDetails, hearingId);
    }

    private static <T> boolean isHearingReferencedBy(List<Element<T>> orders,
                                                     Function<T, List<Element<HearingData>>> hearingDetailsExtractor,
                                                     String hearingId) {
        return nullSafeCollection(orders).stream()
            .map(Element::getValue)
            .anyMatch(order -> orderReferencesHearing(order, hearingDetailsExtractor, hearingId));
    }

    /**
     * Checks whether a single order references the given hearing ID in any of its confirmed hearing dates.
     */
    private static <T> boolean orderReferencesHearing(T order,
                                                      Function<T, List<Element<HearingData>>> hearingDetailsExtractor,
                                                      String hearingId) {
        return nullSafeCollection(hearingDetailsExtractor.apply(order)).stream()
            .map(Element::getValue)
            .map(HearingData::getConfirmedHearingDates).filter(Objects::nonNull)
            .map(DynamicList::getValue)
            .filter(Objects::nonNull).map(DynamicListElement::getCode)
            .anyMatch(hearingId::equals);
    }
}

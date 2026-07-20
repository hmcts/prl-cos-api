package uk.gov.hmcts.reform.prl.services.requestorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.prl.utils.HearingLabelUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

/**
 * Encapsulates the rules deciding whether a hearing currently warrants a Request Order
 * task fire (FPVTL-2408/2409).
 */
@Slf4j
@Component
@RequiredArgsConstructor
class HearingChasePolicy {

    private static final String C100 = "C100";

    private final WorkingDayIndicator workingDayIndicator;

    @Value("${request-order-task.cadence-working-days.c100}")
    private int c100CadenceWorkingDays;

    @Value("${request-order-task.cadence-working-days.fl401}")
    private int fl401CadenceWorkingDays;

    @Value("#{'${hearing_component.hearingStatusesToFilter}'.split(',')}")
    private List<String> hearingStatusesToFilter;

    static String hearingIdOf(CaseHearing hearing) {
        return hearing.getHearingID() == null ? null : String.valueOf(hearing.getHearingID());
    }

    ChaseDecision decide(CaseHearing hearing, CaseData caseData, HearingTrackingLedger ledger, LocalDate cronDate) {
        String hearingId = hearingIdOf(hearing);
        if (hearingId == null) {
            return ChaseDecision.skipUnknownHearingId(hearing.getHmcStatus());
        }
        if (!allowedStatuses().contains(hearing.getHmcStatus())) {
            return ChaseDecision.skipStatusNotInFilter(hearing.getHmcStatus());
        }
        if (isHearingMappedToOrder(caseData, hearing)) {
            return ChaseDecision.skipLinkedOrderExists();
        }

        LocalDate hearingEndDate = computeHearingEndDate(hearing);
        int cadence = cadenceFor(caseData.getCaseTypeOfApplication());

        int workingDaysSinceHearingEndDate = workingDayIndicator.workingDaysBetween(hearingEndDate, cronDate);
        if (hearingEndDate != null && workingDaysSinceHearingEndDate != cadence) {
            return ChaseDecision.skipHearingNotAtCadence(hearingEndDate, cadence);
        }

        Optional<RequestOrderHearingTracking> tracking = ledger.find(hearingId);
        LocalDate lastCompletedDate = tracking.map(RequestOrderHearingTracking::getLastCompletedDate)
            .orElse(null);
        LocalDate lastFiredDate = tracking.map(RequestOrderHearingTracking::getLastFiredDate)
            .orElse(null);
        if (cronDate.equals(lastFiredDate)) {
            return ChaseDecision.skipInFlight();
        }

        if (lastCompletedDate != null) {
            int workingDaysSinceLastCompletedDate = workingDayIndicator.workingDaysBetween(lastCompletedDate, cronDate);
            if (workingDaysSinceLastCompletedDate != cadence) {
                return ChaseDecision.skipInFlight();
            }
        }
        if (lastCompletedDate == null && lastFiredDate != null) {
            int workingDaysSinceLastFired = workingDayIndicator.workingDaysBetween(lastFiredDate, cronDate);
            if (workingDaysSinceLastFired < cadence) {
                return ChaseDecision.skipInFlight();
            } else if (workingDaysSinceLastFired == cadence) {
                return ChaseDecision.fireCadenceMetDone(); //every cadence days it will fire a new Task
            }
        }

        return ChaseDecision.fireCadenceMet();//order still not added
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
    private static boolean isHearingMappedToOrder(CaseData caseData, CaseHearing hearing) {
        String hearingId = hearingIdOf(hearing);
        Set<String> hearingLabels = HearingLabelUtils.buildHearingsTypeLabels(hearing);
        log.info("hearingLabels for caseId={}, hearingId={}: {}", caseData.getId(),
                 hearingId, hearingLabels.stream().collect(Collectors.toList()));
        boolean hearingTypeLookupFailed = hearing.getHearingTypeValue() == null
            || hearing.getHearingTypeValue().isBlank();
        Set<String> hearingDateSuffixes = hearingTypeLookupFailed
            ? HearingLabelUtils.buildHearingDateSuffixes(hearing)
            : Set.of();

        if (hearingTypeLookupFailed) {
            // Ref-data lookup that turns the HMC hearing type code into a human label
            // (e.g. "ABA5-ALL" -> "Allocation") returned empty for the cron call, so
            // any saved order's "<type> - <date>" code cannot match on the full label.
            // Falling back to date-suffix matching keeps the chase correct while the
            // upstream ref-data issue is investigated.
            log.warn("Request Order link-check: hearingTypeValue empty for caseId={}, hearingId={} — "
                    + "ref-data lookup likely failed; falling back to date-suffix match", caseData.getId(),
                hearingId);
        }

        Long caseId = caseData.getId();
        return isHearingReferencedByManageOrderHearingDetails(caseId, caseData.getDraftOrderCollection(),
                                                              DraftOrder::getManageOrderHearingDetails, hearingId)
            || isHearingReferencedByManageOrderHearingDetails(caseId, caseData.getOrderCollection(),
                                                              OrderDetails::getManageOrderHearingDetails, hearingId)
            || isDraftOrderReferencedByHearingsType(caseId, caseData.getDraftOrderCollection(),
                                                    hearingLabels, hearingDateSuffixes)

            || isFinalisedOrderReferencedByHearingsType(caseId, caseData.getOrderCollection(), hearingLabels)
            || isCustomOrderHearingsType(caseId, caseData.getCustomOrderHearingsType(), hearingLabels);
    }

    private static boolean isCustomOrderHearingsType(Long caseId, DynamicList customOrderHearingsType,
                                                     Set<String> hearingLabels) {
        log.info("Evaluating CustomOrderHearingsType for the caseId={}, hearings {}", caseId, hearingLabels);
        return Optional.ofNullable(customOrderHearingsType)
            .map(DynamicList::getValue)
            .map(DynamicListElement::getCode)
            .filter(hearingLabels::contains)
            .isPresent();
    }

    private static <T> boolean isHearingReferencedByManageOrderHearingDetails(Long caseId,
            List<Element<T>> orders,
            Function<T, List<Element<HearingData>>> hearingDetailsExtractor,
            String hearingId) {
        log.info("trying isHearingReferencedByManageOrderHearingDetails for caseId={}, hearingId={}",
                 caseId, hearingId);
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

    private static boolean isDraftOrderReferencedByHearingsType(Long caseId, List<Element<DraftOrder>> draftOrders,
                                                                 Set<String> hearingLabels,
                                                                 Set<String> hearingDateSuffixes) {
        log.info("trying isDraftOrderReferencedByHearingsType for caseId={}", caseId);
        if (hearingLabels.isEmpty() && hearingDateSuffixes.isEmpty()) {
            return false;
        }
        return nullSafeCollection(draftOrders).stream()
            .map(Element::getValue)
            .map(DraftOrder::getHearingsType)
            .filter(Objects::nonNull)
            .map(DynamicList::getValue)
            .filter(Objects::nonNull)
            .map(DynamicListElement::getCode)
            .filter(Objects::nonNull)
            .anyMatch(code -> hearingLabels.contains(code)
                || hearingDateSuffixes.contains(HearingLabelUtils.extractDateSuffix(code)));
    }

    private static boolean isFinalisedOrderReferencedByHearingsType(Long caseId, List<Element<OrderDetails>> orderDetails,
                                                                Set<String> hearingLabels) {
        log.info("trying isFinalisedOrderReferencedByHearingsType for caseId={}", caseId);
        if (hearingLabels.isEmpty()) {
            return false;
        }
        return nullSafeCollection(orderDetails).stream()
            .map(Element::getValue)
            .filter(order -> order.getFinalisationDetails() != null)
            .map(OrderDetails::getSelectedHearingType)
            .filter(Objects::nonNull)
            .anyMatch(code -> hearingLabels.contains(code));
    }
}

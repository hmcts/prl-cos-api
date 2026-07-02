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
import uk.gov.hmcts.reform.prl.services.taskmanagement.TaskManagementService;
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

import static uk.gov.hmcts.reform.prl.enums.CaseEvent.ENABLE_REQUEST_SOLICITOR_ORDER_TASK;
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
    private final TaskManagementService taskManagementService;

    @Value("${request-order-task.cadence-working-days.c100}")
    private int c100CadenceWorkingDays;

    @Value("${request-order-task.cadence-working-days.fl401}")
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
        if (isHearingMappedToOrder(caseData, hearing)) {
            return ChaseDecision.skipLinkedOrderExists();
        }

        LocalDate hearingEndDate = computeHearingEndDate(hearing);
        int cadence = cadenceFor(caseData.getCaseTypeOfApplication());
        Optional<RequestOrderHearingTracking> tracking = ledger.find(hearingId);

        int workingDaysSinceHearingEndDate = workingDayIndicator.workingDaysBetween(today, hearingEndDate);
        if (hearingEndDate != null && workingDaysSinceHearingEndDate != cadence) {
            return ChaseDecision.skipHearingNotAtCadence(hearingEndDate, cadence);
        }

        LocalDate lastCompletedDate = tracking.map(RequestOrderHearingTracking::getLastCompletedDate)
            .orElse(null);
        LocalDate lastFiredDate = tracking.map(RequestOrderHearingTracking::getLastFiredDate)
            .orElse(null);
        if (lastFiredDate != null && lastCompletedDate == null
            && noOutstandingRequestSolicitorOrderTasks(caseData.getId(), hearingId)) {
            return ChaseDecision.fire();
        } else if (lastCompletedDate != null) {
            int workingDaysSinceLastCompletedDate = workingDayIndicator.workingDaysBetween(lastCompletedDate, today);
            if (workingDaysSinceLastCompletedDate != cadence) {
                return ChaseDecision.skipInFlight();
            }
        }

        return ChaseDecision.fire();
    }

    private boolean noOutstandingRequestSolicitorOrderTasks(long caseId, String hearingId) {
        return taskManagementService.hasNoCompletableTasksForHearing(hearingId, String.valueOf(caseId),
                                                                      ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue());
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
            log.warn("Request Order link-check: hearingTypeValue empty for hearingId={} — "
                    + "ref-data lookup likely failed; falling back to date-suffix match",
                hearingId);
        }

        return isHearingReferencedByManageOrderHearingDetails(caseData.getDraftOrderCollection(),
                                                              DraftOrder::getManageOrderHearingDetails, hearingId)
            || isHearingReferencedByManageOrderHearingDetails(caseData.getOrderCollection(),
                                                              OrderDetails::getManageOrderHearingDetails, hearingId)
            || isDraftOrderReferencedByHearingsType(
                caseData.getDraftOrderCollection(), hearingLabels, hearingDateSuffixes)

            || isFinalisedOrderReferencedByHearingsType(caseData.getOrderCollection(), hearingLabels)
            || isCustomOrderHearingsType(caseData.getCustomOrderHearingsType(), hearingLabels);
    }

    private static boolean isCustomOrderHearingsType(DynamicList customOrderHearingsType,
                                                     Set<String> hearingLabels) {
        log.info("Evaluating CustomOrderHearingsType for the hearings {}", hearingLabels);
        return Optional.ofNullable(customOrderHearingsType)
            .map(DynamicList::getValue)
            .map(DynamicListElement::getCode)
            .filter(hearingLabels::contains)
            .isPresent();
    }

    private static <T> boolean isHearingReferencedByManageOrderHearingDetails(
            List<Element<T>> orders,
            Function<T, List<Element<HearingData>>> hearingDetailsExtractor,
            String hearingId) {
        log.info("trying isHearingReferencedByManageOrderHearingDetails for {}", hearingId);
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

    private static boolean isDraftOrderReferencedByHearingsType(List<Element<DraftOrder>> draftOrders,
                                                                 Set<String> hearingLabels,
                                                                 Set<String> hearingDateSuffixes) {
        log.info("trying isDraftOrderReferencedByHearingsType");
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

    private static boolean isFinalisedOrderReferencedByHearingsType(List<Element<OrderDetails>> orderDetails,
                                                                Set<String> hearingLabels) {
        log.info("trying isFinalisedOrderReferencedByHearingsType");
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

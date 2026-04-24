package uk.gov.hmcts.reform.prl.services.workingdays;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.BankHolidaysApi;
import uk.gov.hmcts.reform.prl.models.holidaydates.HolidayDate;
import uk.gov.hmcts.reform.prl.models.holidaydates.UkHolidayDates;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loads and caches England and Wales public holidays from the gov.uk bank-holidays API.
 */
@Service
@RequiredArgsConstructor
public class PublicHolidaysCollection {

    private final BankHolidaysApi bankHolidaysApi;
    private Set<LocalDate> cachedPublicHolidays;

    public Set<LocalDate> getPublicHolidays() {
        if (cachedPublicHolidays == null) {
            cachedPublicHolidays = retrieveAllPublicHolidays();
        }
        return cachedPublicHolidays;
    }

    private Set<LocalDate> retrieveAllPublicHolidays() {
        UkHolidayDates ukHolidayDates = bankHolidaysApi.retrieveAll();
        if (ukHolidayDates == null
            || ukHolidayDates.getEnglandAndWales() == null
            || ukHolidayDates.getEnglandAndWales().getEvents() == null) {
            return Collections.emptySet();
        }
        return ukHolidayDates.getEnglandAndWales().getEvents().stream()
            .map(HolidayDate::getDate)
            .collect(Collectors.toSet());
    }
}
package uk.gov.hmcts.reform.prl.services.workingdays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.clients.BankHolidaysApi;
import uk.gov.hmcts.reform.prl.models.holidaydates.CountryHolidayDates;
import uk.gov.hmcts.reform.prl.models.holidaydates.HolidayDate;
import uk.gov.hmcts.reform.prl.models.holidaydates.UkHolidayDates;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicHolidaysCollectionTest {

    @Mock
    private BankHolidaysApi bankHolidaysApi;

    @InjectMocks
    private PublicHolidaysCollection collection;

    @Test
    void getPublicHolidaysReturnsHolidaysFromApi() {
        LocalDate xmas = LocalDate.of(2026, 12, 25);
        when(bankHolidaysApi.retrieveAll()).thenReturn(new UkHolidayDates(
            new CountryHolidayDates(List.of(new HolidayDate(xmas, "Christmas Day")))
        ));

        Set<LocalDate> result = collection.getPublicHolidays();

        assertThat(result).containsExactly(xmas);
    }

    @Test
    void getPublicHolidaysCachesAfterFirstCall() {
        when(bankHolidaysApi.retrieveAll()).thenReturn(new UkHolidayDates(
            new CountryHolidayDates(List.of())
        ));

        collection.getPublicHolidays();
        collection.getPublicHolidays();

        verify(bankHolidaysApi, times(1)).retrieveAll();
    }

    @Test
    void getPublicHolidaysHandlesNullApiResponse() {
        when(bankHolidaysApi.retrieveAll()).thenReturn(null);
        assertThat(collection.getPublicHolidays()).isEmpty();
    }
}
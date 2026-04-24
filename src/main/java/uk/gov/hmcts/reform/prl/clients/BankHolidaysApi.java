package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmcts.reform.prl.models.holidaydates.UkHolidayDates;

@FeignClient(
    name = "bank-holidays-api",
    url = "${bankHolidays.api.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface BankHolidaysApi {

    @GetMapping("/bank-holidays.json")
    UkHolidayDates retrieveAll();
}
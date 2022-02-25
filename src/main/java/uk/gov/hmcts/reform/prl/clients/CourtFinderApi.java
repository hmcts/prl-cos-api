package uk.gov.hmcts.reform.prl.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.ServiceArea;

@FeignClient(name = "court-finder-api", primary = false, url = "${courtfinder.api.url}")
public interface CourtFinderApi {

    final String SEARCH_RESULTS_POSTCODE_POSTCODE_SERVICE_AREA = "search/results?postcode={postcode}&serviceArea=";
    final String CHILD_ARRANGEMENTS_POSTCODE_URL = SEARCH_RESULTS_POSTCODE_POSTCODE_SERVICE_AREA + "childcare-arrangements";
    final String COURT_DETAILS_URL = "courts/{court-slug}";
    final String DOMESTIC_ABUSE_POSTCODE_URL = SEARCH_RESULTS_POSTCODE_POSTCODE_SERVICE_AREA + "domestic-abuse";

    @GetMapping(value = CHILD_ARRANGEMENTS_POSTCODE_URL)
    ServiceArea findClosestChildArrangementsCourtByPostcode(@PathVariable("postcode") String postcode);

    @GetMapping(value = COURT_DETAILS_URL)
    Court getCourtDetails(@PathVariable("court-slug") String courtSlug);

    @GetMapping(value = DOMESTIC_ABUSE_POSTCODE_URL)
    ServiceArea findClosestDomesticAbuseCourtByPostCode(@PathVariable("postcode") String postcode);

}

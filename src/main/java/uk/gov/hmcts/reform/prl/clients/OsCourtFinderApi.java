package uk.gov.hmcts.reform.prl.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.prl.config.OsPlacesFeignConfig;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.OsPlacesResponse;

@FeignClient(name = "os-court-finder-api", primary = false, url = "${postcodelookup.api.url}",
    configuration = OsPlacesFeignConfig.class)
public interface OsCourtFinderApi {

    @GetMapping(value = "/postcode", consumes = "application/json")
    OsPlacesResponse findCouncilByPostcode(@RequestParam("postcode") String postcode);

}

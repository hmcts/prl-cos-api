package uk.gov.hmcts.reform.prl.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.prl.models.court.CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;

import java.util.List;

@FeignClient(name = "location-ref-data-api", primary = false, url = "${locationfinder.api.url}")
public interface LocationRefDataApi {
    @GetMapping(value = "/refdata/location/court-venues/services")
    CourtDetails getCourtDetailsByService(@RequestHeader("Authorization") String authorization,
                                          @RequestHeader("ServiceAuthorization") String serviceAuthorization,
                                          @RequestParam("service_code") String serviceCode);

    @GetMapping(value = "/refdata/location/court-venues")
    List<CourtVenue> getCourtDetailsById(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestParam("epimms_id") final String epimmsId);
}

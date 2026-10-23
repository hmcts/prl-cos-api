package uk.gov.hmcts.reform.prl.clients.factv2;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.prl.models.court.factv2.FactV2CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.factv2.FactV2CourtSearchResult;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "fact-v2-api", url = "${fact.v2.api.url}")
public interface FactV2Api {

    @GetMapping("/search/courts/v1/postcode")
    List<FactV2CourtSearchResult> getCourtsByPostcode(
        @RequestParam("postcode") String postcode,
        @RequestParam("serviceArea") String serviceArea,
        @RequestParam("action") String action,
        @RequestParam("limit") int limit,
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader("X-User-Id") String userId
    );

    @GetMapping("/courts/slug/{courtSlug}/v1")
    FactV2CourtDetails getCourtDetailsBySlug(
        @PathVariable("courtSlug") String courtSlug,
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader("X-User-Id") String userId
    );
}

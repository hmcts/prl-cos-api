package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.prl.models.Organisations;

@FeignClient(
    name = "rd-professional-api",
    url = "${rd_professional.api.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface OrganisationApi {

    @GetMapping("/refdata/internal/v1/organisations")
    Organisations findOrganisation(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestParam(value = "id") String organisationId
    );
}

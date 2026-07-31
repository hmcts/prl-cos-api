package uk.gov.hmcts.reform.prl.clients;

import jakarta.validation.constraints.NotBlank;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.models.OrgSolicitors;
import uk.gov.hmcts.reform.prl.models.OrganisationUser;
import uk.gov.hmcts.reform.prl.models.Organisations;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

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

    @GetMapping("/refdata/internal/v1/organisations/{orgId}/users")
    OrgSolicitors findOrganisationSolicitors(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestParam(value = "orgId") String organisationId
    );

    @GetMapping("/refdata/external/v1/organisations")
    Organisations findUserOrganisation(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization
    );

    @GetMapping("/refdata/internal/v1/organisations")
    Object findOrganisations(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestParam(value = "status") String status
    );

    @GetMapping("/refdata/external/v1/organisations/users/accountId")
    OrganisationUser findUserByEmail(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader("UserEmail") final String email
    );

    @GetMapping("/refdata/internal/v1/organisations/orgDetails/{userId}")
    Organisations getOrganisationByUserId(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(CoreCaseDataApi.SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable("userId") @NotBlank String userId
    );
}

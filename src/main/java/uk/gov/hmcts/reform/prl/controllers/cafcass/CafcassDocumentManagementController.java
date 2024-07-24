package uk.gov.hmcts.reform.prl.controllers.cafcass;

import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.prl.exception.cafcass.exceptionhandlers.ApiError;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassCdamService;

import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.prl.constants.cafcass.CafcassAppConstants.CAFCASS_USER_ROLE;


@Slf4j
@RestController
@RequestMapping("/cases")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassDocumentManagementController {
    private final CafcassCdamService cafcassCdamService;
    private final AuthorisationService authorisationService;
    private final SystemUserService systemUserService;

    @GetMapping(path = "/documents/{documentId}/binary")
    @Operation(description = "Call CDAM to download document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Downloaded Successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request while downloading the document"),
        @ApiResponse(responseCode = "401", description = "Provided Authorization token is missing or invalid"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public <T> ResponseEntity<T> downloadDocument(@RequestHeader(AUTHORIZATION) String authorisation,
                                                     @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                                                     @PathVariable UUID documentId) {
        try {
            if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation)) && Boolean.TRUE.equals(
                authorisationService.authoriseService(serviceAuthorisation))
                && authorisationService.getUserInfo().getRoles().contains(CAFCASS_USER_ROLE)) {
                log.info("processing  request after authorization");
                return (ResponseEntity<T>) cafcassCdamService.getDocument(systemUserService.getSysUserToken(), serviceAuthorisation, documentId);

            } else {
                throw new ResponseStatusException(UNAUTHORIZED);
            }
        } catch (ResponseStatusException e) {
            return (ResponseEntity<T>) status(UNAUTHORIZED).body(new ApiError(e.getMessage()));
        } catch (FeignException feignException) {
            return (ResponseEntity<T>) status(feignException.status()).body(new ApiError(feignException.getMessage()));
        } catch (Exception e) {
            return (ResponseEntity<T>) status(INTERNAL_SERVER_ERROR).body(new ApiError(e.getMessage()));
        }
    }
}

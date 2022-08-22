package uk.gov.hmcts.reform.prl.controllers.citizen;


import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.FeeService;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FeesAndPaymentCitizenController {


    private final String serviceAuth = "ServiceAuthorization";

    @Autowired
    private AuthorisationService authorisationService;

    @Autowired
    private FeeService feeService;

    public FeesAndPaymentCitizenController() {
    }

    @GetMapping(path = "getC100ApplicationFees", produces = APPLICATION_JSON)
    @ApiOperation(value = "Frontend to fetch the Fees data")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Fee amount returned"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public FeeResponseForCitizen fetchFeesAmount(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(serviceAuth) String serviceAuthorization
    ) {
        FeeResponse feeResponse = null;
        try {
            if (Boolean.TRUE.equals(authorisationService.authorise(serviceAuthorization))) {
                feeResponse = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
            }
        } catch (Exception e) {
            return FeeResponseForCitizen.builder()
                .errorRetrievingResponse(e.getMessage())
                .build();
        }
        return FeeResponseForCitizen.builder()
            .amount(feeResponse.getAmount().toString());
    }
}

package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeResponseForCitizen;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.FeeService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


@Slf4j
@RestController
@RequiredArgsConstructor
public class FeesAndPaymentCitizenController {

    @Autowired
    private AuthorisationService authorisationService;

    @Autowired
    private FeeService feeService;

    @GetMapping(path = "/getC100ApplicationFees", produces = APPLICATION_JSON)
    @Operation(description = "Frontend to fetch the Fees Details for C100 Application Submission")
    public ResponseEntity fetchFeesAmount(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader("serviceAuthorization") String serviceAuthorization
    ) {
        FeeResponse feeResponse = null;
        try {
            System.out.println("Inside try block");
            if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation)) && Boolean.TRUE.equals(
                authorisationService.authoriseService(serviceAuthorization))) {
                System.out.println("Inside if block");

                feeResponse = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
            } else {
                System.out.println("Inside else block");

                throw (new RuntimeException("Invalid Client"));
            }
        } catch (Exception e) {
            System.out.println("Inside exce;ption block");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FeeResponseForCitizen.builder()
                                                                          .errorRetrievingResponse(e.getMessage())
                                                                          .build());
        }
        System.out.println("Inside return response block");

        return ResponseEntity.status(HttpStatus.OK).body(FeeResponseForCitizen.builder()
                                                             .amount(feeResponse.getAmount().toString()).build());
    }
}

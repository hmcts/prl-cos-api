package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Objects;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FL401SubmitApplicationController {

    @Autowired
    private CourtFinderService courtFinderService;

    @Autowired
    private UserService userService;

    @Autowired
    private SolicitorEmailService solicitorEmailService;

    @Autowired
    private CaseWorkerEmailService caseWorkerEmailService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(path = "/fl401-generate-document-submit-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to generate FL401 final document and submit application. ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Application Submitted."),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CallbackResponse fl401GenerateDocumentSubmitApplication(@RequestHeader("Authorization") String authorisation,
                                        @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        Court closestDomesticAbuseCourt = courtFinderService
            .getClosestDomesticAbuseCourt(CaseUtils.getCaseData(caseDetails, objectMapper));

        Optional<CourtEmailAddress> matchingEmailAddress = courtFinderService.getEmailAddress(closestDomesticAbuseCourt);

        CaseData caseData = objectMapper.convertValue(
            CaseData.builder()
                .courtName((closestDomesticAbuseCourt != null)  ? closestDomesticAbuseCourt.getCourtName() : "No Court Fetched")
                .courtEmailAddress((closestDomesticAbuseCourt != null && matchingEmailAddress.isPresent())
                                       ? matchingEmailAddress.get().getAddress() :
                                       Objects.requireNonNull(closestDomesticAbuseCourt).getCourtEmailAddresses().get(0).getAddress())
                .build(),
            CaseData.class
        );

        //todo document generation
        solicitorEmailService.sendEmail(caseDetails);
        caseWorkerEmailService.sendEmailToLocalCourt(caseDetails, matchingEmailAddress);

        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }
}

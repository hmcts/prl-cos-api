package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;


@Slf4j
@RestController
@RequiredArgsConstructor
public class CourtFinderController {

    @Autowired
    private final CourtFinderService courtLocatorService;

    @Autowired
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/find-child-arrangements-court")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CallbackResponse getChildArrangementsCourtAndAddToCaseData(@RequestBody CallbackRequest callbackRequest) {

        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getCaseData(), CaseData.class);
        Court court = courtLocatorService.getClosestChildArrangementsCourt(caseData);
        CaseData updatedCaseData = courtLocatorService.setCourtUnlessCourtAlreadyPresent(caseData, court);

        return CallbackResponse.builder()
                    .data(updatedCaseData)
                    .build();

    }





}

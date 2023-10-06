package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.SendGridRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ResponseMessage;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.SendgridService;

import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sendgrid")
public class SendGridController {

    @Autowired
    private SendgridService sendgridService;

    @Autowired
    private AuthorisationService authorisationService;


    @PostMapping(path = "/send-email-with-attachments")
    public ResponseEntity<Object> sendEmail(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody final SendGridRequest sendGridRequest) throws Exception {
        Map<String,String> emailProps = sendGridRequest.getEmailProps();
        String toEmailAddress = sendGridRequest.getToEmailAddress();

        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            sendgridService.sendEmailWithAttachments(authorisation,emailProps,toEmailAddress,
                                                     sendGridRequest.getListOfAttachments(),sendGridRequest.getServedParty());
            return ResponseEntity.ok().body(new ResponseMessage("OK"));
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

}

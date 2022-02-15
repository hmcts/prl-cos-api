package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.OrganisationApi;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrganisationDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CcdPayment;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CcdPaymentServiceRequestUpdate;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.JURISDICTION;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RequestUpdateCallbackService {

    public static final String PAYMENT_SUCCESS_CALLBACK = "paymentSuccessCallback";
    public static final String PAYMENT_FAILURE_CALLBACK = "paymentFailureCallback";
    public static final String PAID = "Paid";
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final CoreCaseDataApi coreCaseDataApi;
    private final OrganisationApi organisationApi;
    private final SystemUserService systemUserService;
    private final SolicitorEmailService solicitorEmailService;
    private final CaseWorkerEmailService caseWorkerEmailService;
    private final UserService userService;
    private List<OrganisationDetails> organisationDetails;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto) throws Exception {

        log.info("Processing the callback for the caseId {} with status {}", serviceRequestUpdateDto.getCcdCaseNumber(),
                 serviceRequestUpdateDto.getServiceRequestStatus()
        );
        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);
        log.info("Fetching the Case details based on caseId {}", serviceRequestUpdateDto.getCcdCaseNumber()
        );
        CaseDetails caseDetails = coreCaseDataApi.getCase(
            userToken,
            authTokenGenerator.generate(),
            serviceRequestUpdateDto.getCcdCaseNumber()
        );

        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder()
            .id(caseDetails.getId())
            .build();

        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        //Map<String, Object> organisationDetailsMap = new HashMap<>();
        OrganisationDetails orgDetails = OrganisationDetails.builder().build();
        log.info("applicants length {}",applicants.stream().count());

        for (PartyDetails applicant : applicants) {

            log.info("*** Count **** ");
            if (applicant.getSolicitorOrg() != null) {
                String organisationID = applicant.getSolicitorOrg().getOrganisationID();
                if (organisationID != null) {
                    log.info("Organisation Id : {}",organisationID);
                    log.info("*** Before api call organisation **** ");
                    orgDetails = organisationApi.findOrganisation(userToken, authTokenGenerator.generate(), organisationID);
                    log.info("*** After api call organisation **** {}",orgDetails.toString());
                    organisationDetails.add(orgDetails);
                }
            }
            log.info("*** solicitor org null **** ");
            /*organisationDetails.add(OrganisationDetails.builder()
                .contactInformation((List<ContactInformation>) organisationDetailsMap.get("contactInformation"))
                .name(String.valueOf(organisationDetailsMap.get("name")))
                .organisationIdentifier(String.valueOf(organisationDetailsMap.get("organisationIdentifier")))
                .build());
             */
        }

        log.info("****** Organisation details refdata: ");

        if (!Objects.isNull(caseDetails.getId())) {
            log.info(
                "Updating the Case data with payment information for caseId {}",
                serviceRequestUpdateDto.getCcdCaseNumber()
            );
            log.info("Before entering case event *****");
            createEvent(serviceRequestUpdateDto, userToken, systemUpdateUserId, organisationDetails,
                        serviceRequestUpdateDto.getServiceRequestStatus().equalsIgnoreCase(PAID)
                            ? PAYMENT_SUCCESS_CALLBACK : PAYMENT_FAILURE_CALLBACK
            );
            log.info("After entering case event *****");
            solicitorEmailService.sendEmail(caseDetails);
            caseWorkerEmailService.sendEmail(caseDetails);

        } else {
            log.error("Case id {} not present", serviceRequestUpdateDto.getCcdCaseNumber());
            throw new Exception("Case not present");
        }
    }

    //todo This method will be deleted once we wipe out Fee and Pay Bypass
    public void processCallbackForBypass(ServiceRequestUpdateDto serviceRequestUpdateDto, String authorisation) throws Exception {

        log.info("Processing the callback for the caseId {} with status {}", serviceRequestUpdateDto.getCcdCaseNumber(),
                 serviceRequestUpdateDto.getServiceRequestStatus()
        );
        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);
        log.info("Fetching the Case details based on caseId {}", serviceRequestUpdateDto.getCcdCaseNumber()
        );
        CaseDetails caseDetails = coreCaseDataApi.getCase(
            userToken,
            authTokenGenerator.generate(),
            serviceRequestUpdateDto.getCcdCaseNumber()
        );

        if (!Objects.isNull(caseDetails.getId())) {
            log.info(
                "Updating the Case data with payment information for caseId {}",
                serviceRequestUpdateDto.getCcdCaseNumber()
            );

            solicitorEmailService.sendEmailBypss(caseDetails, authorisation);
            caseWorkerEmailService.sendEmail(caseDetails);

        } else {
            log.error("Case id {} not present", serviceRequestUpdateDto.getCcdCaseNumber());
            throw new Exception("Case not present");
        }
    }

    // todo this method will be deleted once we wipe out fee and pay bypass
    private void createEventForFeeAndPayBypass(ServiceRequestUpdateDto serviceRequestUpdateDto, String userToken,
                                               String systemUpdateUserId, String eventId, String authorisation) {
        CaseData caseData = setCaseDataFeeAndPayBypass(serviceRequestUpdateDto, authorisation);
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            serviceRequestUpdateDto.getCcdCaseNumber(),
            eventId
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(caseData)
            .build();

        coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            serviceRequestUpdateDto.getCcdCaseNumber(),
            true,
            caseDataContent
        );
    }

    private void createEvent(ServiceRequestUpdateDto serviceRequestUpdateDto, String userToken,
                             String systemUpdateUserId, List<OrganisationDetails> organisation, String eventId) throws JsonProcessingException {
        CaseData caseData = setCaseData(serviceRequestUpdateDto, organisation);

        String jsonCaseData = objectMapper.writeValueAsString(caseData);

        log.info("CaseData with Organisation details: {} ", jsonCaseData);

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            serviceRequestUpdateDto.getCcdCaseNumber(),
            eventId
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(caseData)
            .build();

        coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            serviceRequestUpdateDto.getCcdCaseNumber(),
            true,
            caseDataContent
        );
    }


    //todo this method will be deleted once we wipe out fee and pay bypass
    private CaseData setCaseDataFeeAndPayBypass(ServiceRequestUpdateDto serviceRequestUpdateDto, String authorisation) {
        return objectMapper.convertValue(
            CaseData.builder()
                .id(Long.parseLong(serviceRequestUpdateDto.getCcdCaseNumber()))
                .applicantSolicitorEmailAddress(userService.getUserDetails(authorisation).getEmail())
                .caseworkerEmailAddress("prl_caseworker_solicitor@mailinator.com")
                .paymentCallbackServiceRequestUpdate(CcdPaymentServiceRequestUpdate.builder()
                                                         .serviceRequestReference(serviceRequestUpdateDto.getServiceRequestReference())
                                                         .ccdCaseNumber(serviceRequestUpdateDto.getCcdCaseNumber())
                                                         .serviceRequestAmount(serviceRequestUpdateDto.getServiceRequestAmount())
                                                         .serviceRequestStatus(serviceRequestUpdateDto.getServiceRequestStatus())
                                                         .callBackUpdateTimestamp(LocalDateTime.now())
                                                         .payment(CcdPayment.builder().paymentAmount(
                                                             serviceRequestUpdateDto.getPayment().getPaymentAmount())
                                                                      .paymentReference(serviceRequestUpdateDto.getPayment().getPaymentReference())
                                                                      .paymentMethod(serviceRequestUpdateDto.getPayment().getPaymentMethod())
                                                                      .caseReference(serviceRequestUpdateDto.getPayment().getCaseReference())
                                                                      .accountNumber(serviceRequestUpdateDto.getPayment().getAccountNumber())
                                                                      .build()).build()).build(),
            CaseData.class
        );

    }

    private CaseData setCaseData(ServiceRequestUpdateDto serviceRequestUpdateDto, List<OrganisationDetails> organisation) {

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        return objectMapper.convertValue(
            CaseData.builder()
                .id(Long.parseLong(serviceRequestUpdateDto.getCcdCaseNumber()))
                .paymentCallbackServiceRequestUpdate(CcdPaymentServiceRequestUpdate.builder()
                                                         .serviceRequestReference(serviceRequestUpdateDto.getServiceRequestReference())
                                                         .ccdCaseNumber(serviceRequestUpdateDto.getCcdCaseNumber())
                                                         .serviceRequestAmount(serviceRequestUpdateDto.getServiceRequestAmount())
                                                         .serviceRequestStatus(serviceRequestUpdateDto.getServiceRequestStatus())
                                                         .callBackUpdateTimestamp(LocalDateTime.now())
                                                         .payment(CcdPayment.builder().paymentAmount(
                                                             serviceRequestUpdateDto.getPayment().getPaymentAmount())
                                                                      .paymentReference(serviceRequestUpdateDto.getPayment().getPaymentReference())
                                                                      .paymentMethod(serviceRequestUpdateDto.getPayment().getPaymentMethod())
                                                                      .caseReference(serviceRequestUpdateDto.getPayment().getCaseReference())
                                                                      .accountNumber(serviceRequestUpdateDto.getPayment().getAccountNumber())
                                                                      .build()).build())
                .organisationDetails(organisation)
                .issueDate(issueDate.format(dateTimeFormatter))
                .build(),
            CaseData.class
        );

    }
}

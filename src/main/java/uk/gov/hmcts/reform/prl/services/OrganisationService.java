package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.OrganisationApi;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrganisationService {

    @Autowired
    private OrganisationApi organisationApi;

    private Organisations organisations;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;
    private List<Element<PartyDetails>> applicantsWithOrganisationDetails = new ArrayList<>();
    private UUID id;
    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> getApplicantOrganisationDetails(Map<String, Object> caseData) throws NotFoundException {
        log.info("Case Data before unwrapping : {}", caseData);
        if (Optional.ofNullable(caseData.get("applicants")).isPresent()) {
            String userToken = systemUserService.getSysUserToken();

            List<Map<String, Object>> applicants = (List<Map<String, Object>>) caseData.get("applicants");

            applicants.forEach((applicant) -> {
                    Map<String, Object> applicantDetails = (Map<String, Object>) applicant.get("value");
                    if (applicantDetails.get("solicitorOrg") != null) {
                        Map<String, Object> solicitorOrg = (Map<String, Object>) applicantDetails.get("solicitorOrg");
                        String organizationId = (String) solicitorOrg.get("OrganisationID");
                        if (organizationId != null) {
                            applicantDetails = addOrganizationDetails(userToken,organizationId,applicantDetails);
                            applicant.put("value", applicantDetails);
                            log.info("********* Applicant Details after map: {}**********\n",applicant);
                            log.info("**********Organisation details from API {}*************", organisations);
                        }
                    }
                });

            caseData.put("applicants", applicants);
            log.info("Case Data after unwrapping : {}", caseData);
        }
        return caseData;
    }

    public Map<String,Object> addOrganizationDetails(String userToken, String organisationId, Map<String, Object> applicantDetails) {

//        Organisations organisations = Organisations.builder()
//                                .contactInformation(List.of(ContactInformation.builder()
//                                                                .addressLine1("hello")
//                                                                .addressLine3("hello").build()))
//                                .build();
        Organisations organisations = organisationApi.findOrganisation(userToken,
                                                                       authTokenGenerator.generate(),
                                                                       organisationId);
        if (Optional.ofNullable(organisations.getContactInformation()).isPresent()) {
            ContactInformation contactInformation = organisations.getContactInformation().get(0);
            applicantDetails.put("organisationAddress1",
                                 Optional.ofNullable(contactInformation.getAddressLine1()).isPresent()
                                     ? contactInformation.getAddressLine1() : null);
            applicantDetails.put("organisationAddress2",
                                 Optional.ofNullable(contactInformation.getAddressLine2()).isPresent()
                                     ? contactInformation.getAddressLine2() : null);
            applicantDetails.put("organisationAddress3",
                                 Optional.ofNullable(contactInformation.getAddressLine3()).isPresent()
                                     ? contactInformation.getAddressLine3() : null);
            applicantDetails.put("country",
                                 Optional.ofNullable(contactInformation.getCountry()).isPresent()
                                     ? contactInformation.getCountry() : null);
            applicantDetails.put("county",
                                 Optional.ofNullable(contactInformation.getCounty()).isPresent()
                                     ? contactInformation.getCounty() : null);
            applicantDetails.put("postCode",
                                 Optional.ofNullable(contactInformation.getPostCode()).isPresent()
                                     ? contactInformation.getPostCode() : null);
            log.info("********* Applicant Details before map: {}**********\n",applicantDetails);

        }
        return applicantDetails;
    }
    public Map<String, Object> getRespondentOrganisationDetails(Map<String, Object> caseData) throws NotFoundException {

        if (Optional.ofNullable(caseData.get("respondents")).isPresent()) {
            String userToken = systemUserService.getSysUserToken();
            List<Map<String, Object>> respondents = (List<Map<String, Object>>) caseData.get("respondents");
            respondents.forEach(respondent -> {
                    Map<String, Object> respondentDetails = (Map<String, Object>) respondent.get("value");
                    if (respondentDetails.get("solicitorOrg") != null
                        && respondentDetails.get("doTheyHaveLegalRepresentation").equals("yes")) {
                        Map<String, Object> solicitorOrg = (Map<String, Object>) respondentDetails.get("solicitorOrg");
                        String organisationID = (String) solicitorOrg.get("OrganisationID");
                        if (organisationID != null) {
                            respondentDetails = addOrganizationDetails(userToken,organisationID,respondentDetails);
                        }
                        respondent.put("value", respondentDetails);
                    }

                });
            log.info("Respondents length {}", respondents);

            caseData.put("respondents", respondents);
        }
        return caseData;
    }

    public CaseData getApplicantOrganisationDetailsOld(CaseData caseData) {
        if (Optional.ofNullable(caseData.getApplicants()).isPresent()) {
            String userToken = systemUserService.getSysUserToken();
            List<PartyDetails> applicants = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            log.info("applicants length {}",  applicants.size());
            for (PartyDetails applicant : applicants) {
                if (applicant.getSolicitorOrg() != null) {
                    String organisationID = applicant.getSolicitorOrg().getOrganisationID();
                    if (organisationID != null) {
                        log.info("Organisation Id : {}",organisationID);
                        log.info("*** Before api call organisation **** \n");
                        organisations = organisationApi.findOrganisation(userToken, authTokenGenerator.generate(), organisationID);
                        log.info("*** After api call organisation **** {} ============ \n",organisations);
                        log.info("*** After api call organisation contact information address line 1 {} ============ \n",
                                 organisations.getContactInformation().get(0));
                        log.info("Before mapping, Applicant with to builder address line1: {} \n", applicant.getOrganisationAddress1());
                        applicant = applicant.toBuilder()
                            .organisationAddress1(organisations.getContactInformation().get(0).getAddressLine1())
                            .organisationAddress2(organisations.getContactInformation().get(0).getAddressLine2())
                            .organisationAddress3(organisations.getContactInformation().get(0).getAddressLine3())
                            .organisationCountry(organisations.getContactInformation().get(0).getCountry())
                            .organisationCounty(organisations.getContactInformation().get(0).getCounty())
                            .organisationPostcode(organisations.getContactInformation().get(0).getPostCode())
                            .build();
                        log.info("After mapping, Applicant with to builder address line1: {} \n", applicant.getOrganisationAddress1());
                        applicantsWithOrganisationDetails.add(Element.<PartyDetails>builder().value(applicant).build());
                        log.info("***** Applicant with Organisation address **** {} \n", applicantsWithOrganisationDetails);
                    }
                }
            }
            caseData.toBuilder().applicants(applicantsWithOrganisationDetails).build();
            caseData = caseData.toBuilder().applicants(applicantsWithOrganisationDetails).build();
            log.info("*********Casedata after organisation details {}********",caseData);
        }

        return caseData;
    }
}

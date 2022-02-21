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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrganisationService1 {

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
        log.info("Case Data before unwrapping : {}",caseData);
        if (Optional.ofNullable(caseData.get("applicants")).isPresent()) {

            String userToken = systemUserService.getSysUserToken();

            List<Map<String,Object>> applicants = (List<Map<String, Object>>) caseData.get("applicants");
            applicants.stream()
                .map(applicant ->  {
                    Map<String,Object> applicantDetails = (Map<String, Object>) applicant.get("value");
                    if (applicantDetails.get("solicitorOrg") != null) {
                        applicantDetails.put("organisationAddress1","hello");
                        applicantDetails.put("organisationAddress2","hello");
                        applicantDetails.put("organisationAddress3","hello");
                        applicant.put("value",applicantDetails);
                    }

                    return applicant;
                })
                .collect(Collectors.toList());

            caseData.put("applicants", applicants);
            log.info("Case Data after unwrapping : {}",caseData);
        }
        return caseData;
    }

    public Map<String, Object> getRespondentOrganisationDetails(Map<String, Object> caseData) throws NotFoundException {

        if (Optional.ofNullable(caseData.get("respondents")).isPresent()) {
            String userToken = systemUserService.getSysUserToken();
            List<Map<String,Object>> respondents = (List<Map<String, Object>>) caseData.get("respondents");
            respondents
                .stream()
                .map(respondent ->  {
                    Map<String,Object> respondentDetails = (Map<String, Object>) respondent.get("value");
                    if (respondentDetails.get("solicitorOrg") != null
                        && respondentDetails.get("doTheyHaveLegalRepresentation").equals("yes")) {
                        Map<String,Object> solicitorOrg = (Map<String, Object>) respondentDetails.get("solicitorOrg");
                        if (solicitorOrg.get("organisationID") != null) {
                            Organisations organisations = organisationApi.findOrganisation(userToken, authTokenGenerator.generate(),
                                                                                           (String) solicitorOrg.get("organisationID")
                            );
                            if (Optional.ofNullable(organisations.getContactInformation()).isPresent()) {
                                ContactInformation contactInformation = organisations.getContactInformation().get(0);
                                respondentDetails.put("organisationAddress1",
                                                      Optional.ofNullable(contactInformation.getAddressLine1()).isPresent()
                                                          ? contactInformation.getAddressLine1() : null);
                                respondentDetails.put("organisationAddress2",
                                                      Optional.ofNullable(contactInformation.getAddressLine2()).isPresent()
                                                          ? contactInformation.getAddressLine2() : null);
                                respondentDetails.put("organisationAddress3",
                                                      Optional.ofNullable(contactInformation.getAddressLine3()).isPresent()
                                                          ? contactInformation.getAddressLine3() : null);
                                respondentDetails.put("country",
                                                      Optional.ofNullable(contactInformation.getCountry()).isPresent()
                                                          ? contactInformation.getCountry() : null);
                                respondentDetails.put("county",
                                                      Optional.ofNullable(contactInformation.getCounty()).isPresent()
                                                          ? contactInformation.getCounty() : null);
                                respondentDetails.put("postCode",
                                                      Optional.ofNullable(contactInformation.getPostCode()).isPresent()
                                                          ? contactInformation.getPostCode() : null);

                                respondent.put("value",respondentDetails);
                            }
                            respondent.put("value", respondentDetails);
                        }
                    }
                    return respondent;
                })
                .collect(Collectors.toList());

            log.info("Respondents length {}", respondents);

            caseData.put("respondents", respondents);
        }
        return caseData;
    }
}

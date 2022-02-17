package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.OrganisationApi;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
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

    public CaseData getApplicantOrganisationDetails(CaseData caseData) throws NotFoundException {
        log.info("Case Data before unwrapping : {}",caseData);
        if (Optional.ofNullable(caseData.getApplicants()).isPresent()) {
            String userToken = systemUserService.getSysUserToken();

            List<PartyDetails> applicants = caseData
                .getApplicants()
                .stream()
                .map(Element -> {
                    id = Element.getId();
                    return Element.getValue();
                })
                .collect(Collectors.toList());

            List<Element<PartyDetails>> applicants1 = caseData.getRespondents()
                .stream()
                .map(eachItem ->  Element.<PartyDetails>builder().value(getApplicantWithOrg(eachItem.getValue())).id(eachItem.getId()).build())
                .collect(Collectors.toList());
            log.info("applicants length {}",  applicants1);

            for (PartyDetails applicant : applicants) {

                if (applicant.getSolicitorOrg() != null) {
                    String organisationID = applicant.getSolicitorOrg().getOrganisationID();
                    if (organisationID != null) {
                        log.info("Organisation Id : {}",organisationID);
                        log.info("*** Before api call organisation **** \n");
                        //organisations = organisationApi.findOrganisation(userToken, authTokenGenerator.generate(), organisationID);
                        organisations = Organisations.builder()
                            .contactInformation(List.of(ContactInformation.builder()
                                                            .addressLine1("hello")
                                                            .build()))
                            .build();
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
            caseData = caseData.toBuilder().applicants(applicantsWithOrganisationDetails).build();
            log.info("Case Data after unwrapping : {}",caseData);
        }

        return caseData;
    }

    public CaseData getRespondentOrganisationDetails(CaseData caseData) throws NotFoundException {

        if (Optional.ofNullable(caseData.getRespondents()).isPresent()) {
            String userToken = systemUserService.getSysUserToken();
            applicantsWithOrganisationDetails.clear();
            List<PartyDetails> respondents = caseData
                .getRespondents()
                .stream()
                .map(Element -> {
                    id = Element.getId();
                    return Element.getValue();
                })
                .collect(Collectors.toList());



            log.info("Respondents length {}", respondents.size());

            for (PartyDetails respondent : respondents) {
                if (respondent.getDoTheyHaveLegalRepresentation().equals(YesNoDontKnow.yes)
                    && respondent.getSolicitorOrg() != null) {

                    String organisationID = respondent.getSolicitorOrg().getOrganisationID();
                    if (organisationID != null) {
                        log.info("Organisation Id : {}",organisationID);
                        log.info("*** Before api call organisation **** ");
                        //organisations = organisationApi.findOrganisation(userToken, authTokenGenerator.generate(), organisationID);
                        organisations = Organisations.builder()
                            .contactInformation(List.of(ContactInformation.builder()
                                                            .addressLine1("hello")
                                                            .build()))
                            .build();
                        log.info("*** After api call organisation **** {}",organisations);

                        respondent = respondent.toBuilder()
                            .organisationAddress1(Optional.ofNullable(organisations.getContactInformation().get(0).getAddressLine1()).isPresent()
                                                          ? organisations.getContactInformation().get(0).getAddressLine1()
                                                          : "")
                            .organisationAddress2(Optional.ofNullable(organisations.getContactInformation().get(0).getAddressLine2()).isPresent()
                                                          ? organisations.getContactInformation().get(0).getAddressLine2()
                                                          : "")
                            .organisationAddress3(Optional.ofNullable(organisations.getContactInformation().get(0).getAddressLine3()).isPresent()
                                                          ? organisations.getContactInformation().get(0).getAddressLine3()
                                                          : "")
                            .organisationCountry(Optional.ofNullable(organisations.getContactInformation().get(0).getCountry()).isPresent()
                                                         ? organisations.getContactInformation().get(0).getCountry()
                                                         : "")
                            .organisationCounty(Optional.ofNullable(organisations.getContactInformation().get(0).getCounty()).isPresent()
                                                        ? organisations.getContactInformation().get(0).getCounty()
                                                        : "")
                            .organisationPostcode(Optional.ofNullable(organisations.getContactInformation().get(0).getPostCode()).isPresent()
                                                          ? organisations.getContactInformation().get(0).getPostCode()
                                                          : "")
                            .build();
                        applicantsWithOrganisationDetails.add(Element.<PartyDetails>builder().id(id).value(respondent).build());
                        log.info("***** Respondent with Organisation address **** {} ", applicantsWithOrganisationDetails);
                    }
                }
            }
            caseData = caseData.toBuilder().respondents(applicantsWithOrganisationDetails).build();
        }
        return caseData;
    }
    private PartyDetails getApplicantWithOrg( PartyDetails applicant){



            if (applicant.getSolicitorOrg() != null) {
                String organisationID = applicant.getSolicitorOrg().getOrganisationID();
                if (organisationID != null) {
                    log.info("Organisation Id : {}",organisationID);
                    log.info("*** Before api call organisation **** \n");
                    //organisations = organisationApi.findOrganisation(userToken, authTokenGenerator.generate(), organisationID);
                    organisations = Organisations.builder()
                        .contactInformation(List.of(ContactInformation.builder()
                                                        .addressLine1("hello")
                                                        .build()))
                        .build();
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
                }
            }
            return applicant;
        }

}

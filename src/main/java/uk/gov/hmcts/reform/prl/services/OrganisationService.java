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
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import javax.servlet.http.Part;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private  PartyDetails partyDetails = PartyDetails.builder().build();
    @Autowired
    private ObjectMapper objectMapper;

    public CaseData getApplicantOrganisationDetails(CaseData caseData) throws NotFoundException {

        String userToken = systemUserService.getSysUserToken();

        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        log.info("applicants length {}",applicants.stream().count());

        for (PartyDetails applicant : applicants) {

            log.info("*** Count **** ");
            if (applicant.getSolicitorOrg() != null) {
                String organisationID = applicant.getSolicitorOrg().getOrganisationID();
                if (organisationID != null) {
                    log.info("Organisation Id : {}",organisationID);
                    log.info("*** Before api call organisation **** ");
                    organisations = organisationApi.findOrganisation(userToken, authTokenGenerator.generate(), organisationID);
                    log.info("*** After api call organisation **** {}",organisations);
                    applicant.toBuilder()
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

                    log.info("***** Applicant with Organisation address **** {}", applicantsWithOrganisationDetails);
                }
            }
        }
        return caseData;
    }

    public List<Element<PartyDetails>> getRespondentOrganisationDetails(CaseData caseData) throws NotFoundException {

        String userToken = systemUserService.getSysUserToken();

        List<PartyDetails> respondents = caseData
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        log.info("applicants length {}",respondents.stream().count());

        for (PartyDetails respondent : respondents) {

            log.info("*** Count **** ");
            if (respondent.getDoTheyHaveLegalRepresentation().equals(YesNoDontKnow.yes)) {
                if (respondent.getSolicitorOrg() != null) {
                    String organisationID = respondent.getSolicitorOrg().getOrganisationID();
                    if (organisationID != null) {
                        log.info("Organisation Id : {}",organisationID);
                        log.info("*** Before api call organisation **** ");
                        organisations = organisationApi.findOrganisation(userToken, authTokenGenerator.generate(), organisationID);
                        log.info("*** After api call organisation **** {}",organisations);
                        String addressLine1 = Optional.ofNullable(organisations.getContactInformation().get(0).getAddressLine1()).isPresent()
                            ? organisations.getContactInformation().get(0).getAddressLine1()
                            : "";
                        String addressLine2 = Optional.ofNullable(organisations.getContactInformation().get(0).getAddressLine2()).isPresent()
                            ? organisations.getContactInformation().get(0).getAddressLine2()
                            : "";
                        String addressLine3 = Optional.ofNullable(organisations.getContactInformation().get(0).getAddressLine3()).isPresent()
                            ? organisations.getContactInformation().get(0).getAddressLine3()
                            : "";
                        String country = Optional.ofNullable(organisations.getContactInformation().get(0).getCountry()).isPresent()
                            ? organisations.getContactInformation().get(0).getCountry()
                            : "";
                        String county = Optional.ofNullable(organisations.getContactInformation().get(0).getCounty()).isPresent()
                            ? organisations.getContactInformation().get(0).getCounty()
                            : "";
                        String postcode = Optional.ofNullable(organisations.getContactInformation().get(0).getPostCode()).isPresent()
                            ? organisations.getContactInformation().get(0).getPostCode()
                            : "";
                        partyDetails = objectMapper
                            .convertValue(PartyDetails.builder()
                                              .organisationAddress1(addressLine1)
                                              .organisationAddress2(addressLine2)
                                              .organisationAddress3(addressLine3)
                                              .organisationCountry(country)
                                              .organisationCounty(county)
                                              .organisationPostcode(postcode)
                                              .build(), PartyDetails.class);
                        applicantsWithOrganisationDetails
                            .add(Element
                                     .<PartyDetails>builder()
                                     .value(partyDetails).build());

                        log.info("***** Applicant with Organisation address **** {}", applicantsWithOrganisationDetails);
                    }
                }
            }
        }
        return applicantsWithOrganisationDetails;
    }
}

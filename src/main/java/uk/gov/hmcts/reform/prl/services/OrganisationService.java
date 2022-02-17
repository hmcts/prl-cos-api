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

    @Autowired
    private ObjectMapper objectMapper;

    public CaseData getApplicantOrganisationDetails(CaseData caseData) throws NotFoundException {

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
                        applicantsWithOrganisationDetails.add(Element.<PartyDetails>builder().value(applicant).build());
                        log.info("***** Applicant with Organisation address **** {}", applicantsWithOrganisationDetails);
                    }
                }
            }
            caseData.toBuilder().applicants(applicantsWithOrganisationDetails).build();
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
                .map(Element::getValue)
                .collect(Collectors.toList());

            log.info("Respondents length {}", respondents.size());

            for (PartyDetails respondent : respondents) {
                if (respondent.getDoTheyHaveLegalRepresentation().equals(YesNoDontKnow.yes)
                    && respondent.getSolicitorOrg() != null) {

                    String organisationID = respondent.getSolicitorOrg().getOrganisationID();
                    if (organisationID != null) {
                        log.info("Organisation Id : {}",organisationID);
                        log.info("*** Before api call organisation **** ");
                        organisations = organisationApi.findOrganisation(userToken, authTokenGenerator.generate(), organisationID);
                        log.info("*** After api call organisation **** {}",organisations);

                        respondent.toBuilder()
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
                                                          : "");
                        applicantsWithOrganisationDetails.add(Element.<PartyDetails>builder().value(respondent).build());
                        log.info("***** Respondent with Organisation address **** {} ", applicantsWithOrganisationDetails);
                    }
                }
            }
            caseData.toBuilder().respondents(applicantsWithOrganisationDetails).build();
        }
        return caseData;
    }
}

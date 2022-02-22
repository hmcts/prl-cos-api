package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.OrganisationApi;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrganisationService {

    @Autowired
    private final OrganisationApi organisationApi;

    private Organisations organisations;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;
    private List<Element<PartyDetails>> applicantsWithOrganisationDetails = new ArrayList<>();

    public CaseData getApplicantOrganisationDetails(CaseData caseData)  {
        log.info("Case Data before unwrapping : {}",caseData);
        if (Optional.ofNullable(caseData.getApplicants()).isPresent()) {
            String userToken = systemUserService.getSysUserToken();
            List<Element<PartyDetails>> applicants = caseData.getApplicants()
                .stream()
                .map(eachItem ->  Element.<PartyDetails>builder()
                    .value(getApplicantWithOrg(eachItem.getValue(),userToken))
                    .id(eachItem.getId()).build())
                .collect(Collectors.toList());
            caseData = caseData.toBuilder()
                .applicants(applicants)
                .issueDate(LocalDate.now())
                .build();
            log.info("Case Data after unwrapping : {}",caseData);
        }
        return caseData;
    }

    public CaseData getRespondentOrganisationDetails(CaseData caseData) throws NotFoundException {

        if (Optional.ofNullable(caseData.getRespondents()).isPresent()) {
            String userToken = systemUserService.getSysUserToken();
            applicantsWithOrganisationDetails.clear();

            List<Element<PartyDetails>> respondents = caseData.getRespondents()
                .stream()
                .map(eachItem ->  Element.<PartyDetails>builder()
                    .value(getRespondentWithOrg(eachItem.getValue(),userToken))
                    .id(eachItem.getId()).build())
                .collect(Collectors.toList());

            log.info("Respondents length {}", respondents);

            caseData = caseData.toBuilder().respondents(respondents).build();
        }
        return caseData;
    }

    private PartyDetails getRespondentWithOrg(PartyDetails respondent, String userToken) {

        if (respondent.getDoTheyHaveLegalRepresentation().equals(YesNoDontKnow.yes)
            && respondent.getSolicitorOrg() != null) {

            String organisationID = respondent.getSolicitorOrg().getOrganisationID();
            if (organisationID != null) {
                log.info("Organisation Id : {}",organisationID);
                log.info("*** Before api call organisation **** ");
                organisations = getOrganisationDetaiils(userToken, organisationID);
                //                organisations = organisationApi.findOrganisation(userToken, authTokenGenerator.generate(), organisationID);
                //                                organisations = Organisations.builder()
                //                                    .contactInformation(List.of(ContactInformation.builder()
                //                                                                    .addressLine1("hello")
                //                                                                    .build()))
                //                                    .build();
                log.info("*** After api call organisation **** {}",organisations);

                respondent = respondent.toBuilder()
                    .organisations(organisations)
                    .build();

                log.info("***** Respondent with Organisation address **** {} ", respondent);
            }
        }
        return respondent;
    }

    public Organisations getOrganisationDetaiils(String userToken, String organisationID) {
        return organisationApi.findOrganisation(userToken, authTokenGenerator.generate(), organisationID);
    }

    private PartyDetails getApplicantWithOrg(PartyDetails applicant, String userToken) {

        if (applicant.getSolicitorOrg() != null) {
            String organisationID = applicant.getSolicitorOrg().getOrganisationID();
            if (organisationID != null) {
                log.info("Organisation Id : {}",organisationID);
                log.info("*** Before api call organisation **** \n");
                organisations = getOrganisationDetaiils(userToken, organisationID);
                //                organisations = Organisations.builder()
                //                                    .contactInformation(List.of(ContactInformation.builder()
                //                                                                    .addressLine1("hello")
                //                                                                    .build()))
                //                                    .build();
                log.info("*** After api call organisation **** {} ============ \n",organisations);
                log.info("*** After api call organisation contact information address line 1 {} ============ \n",
                         organisations.getContactInformation().get(0));

                applicant = applicant.toBuilder()
                    .organisations(organisations)
                    .build();
                log.info("After mapping, Applicant with to builder address line1: {} \n", applicant.getOrganisations());
            }
        }
        return applicant;
    }
}

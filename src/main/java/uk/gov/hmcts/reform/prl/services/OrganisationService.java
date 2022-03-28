package uk.gov.hmcts.reform.prl.services;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.NotFoundException;

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

    public CaseData getApplicantOrganisationDetails(CaseData caseData) {
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
                .build();
        }
        return caseData;
    }

    public CaseData getRespondentOrganisationDetails(CaseData caseData) {

        if (Optional.ofNullable(caseData.getRespondents()).isPresent()) {
            String userToken = systemUserService.getSysUserToken();
            applicantsWithOrganisationDetails.clear();

            List<Element<PartyDetails>> respondents = caseData.getRespondents()
                .stream()
                .map(eachItem ->  Element.<PartyDetails>builder()
                    .value(getRespondentWithOrg(eachItem.getValue(),userToken))
                    .id(eachItem.getId()).build())
                .collect(Collectors.toList());

            caseData = caseData.toBuilder().respondents(respondents).build();
        }
        return caseData;
    }

    private PartyDetails getRespondentWithOrg(PartyDetails respondent, String userToken) {

        if (respondent.getDoTheyHaveLegalRepresentation().equals(YesNoDontKnow.yes)
            && respondent.getSolicitorOrg() != null) {

            String organisationID = respondent.getSolicitorOrg().getOrganisationID();
            if (organisationID != null) {
                try {
                    organisations = getOrganisationDetaiils(userToken, organisationID);
                    respondent = respondent.toBuilder()
                        .organisations(organisations)
                        .build();
                } catch (NotFoundException e) {
                    log.info(
                        "OrganisationsAPi return 404, organisation not present for {} {} ",
                        organisationID,
                        e.getMessage()
                    );
                } catch (Exception e) {
                    log.info(
                        "Error while fetching org details for orgid {} {} ",
                        organisationID,
                        e.getMessage()
                    );
                }
            }
        }
        return respondent;
    }

    public Organisations getOrganisationDetaiils(String userToken, String organisationID) {
        log.info("Fetching organisation details for organisation id: {}", organisationID);

        return organisationApi.findOrganisation(userToken, authTokenGenerator.generate(), organisationID);
    }

    private PartyDetails getApplicantWithOrg(PartyDetails applicant, String userToken) {

        if (null != applicant && applicant.getSolicitorOrg() != null) {

            String organisationID = applicant.getSolicitorOrg().getOrganisationID();
            if (organisationID != null) {
                try {
                    organisations = getOrganisationDetaiils(userToken, organisationID);

                    applicant = applicant.toBuilder()
                        .organisations(organisations)
                        .build();
                } catch (NotFoundException e) {
                    log.info(
                        "OrganisationsAPi return 404, organisation not present for {} {} ",
                        organisationID,
                        e.getMessage()
                    );
                } catch (Exception e) {
                    log.info(
                        "Error while fetching org details for orgid {} {} ",
                        organisationID,
                        e.getMessage()
                    );
                }
            }

        }

        return applicant;
    }

    public CaseData getApplicantOrganisationDetailsForFL401(CaseData caseData)  {
        if (Optional.ofNullable(caseData.getApplicantsFL401()).isPresent()) {
            String userToken = systemUserService.getSysUserToken();
            PartyDetails applicantWithOrg = getApplicantWithOrg(caseData.getApplicantsFL401(), userToken);
            caseData = caseData.toBuilder()
                .applicantsFL401(applicantWithOrg)
                .build();
        }
        return caseData;
    }
}

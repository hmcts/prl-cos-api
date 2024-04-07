package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.OrganisationApi;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrgSolicitors;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.NotFoundException;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrganisationService {
    public static final String ACTIVE = "Active";
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
                .map(eachItem -> Element.<PartyDetails>builder()
                    .value(getApplicantWithOrg(eachItem.getValue(), userToken))
                    .id(eachItem.getId()).build())
                .toList();
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
                .map(eachItem -> Element.<PartyDetails>builder()
                    .value(getRespondentWithOrg(eachItem.getValue(), userToken))
                    .id(eachItem.getId()).build())
                .toList();

            caseData = caseData.toBuilder().respondents(respondents).build();
        }
        return caseData;
    }

    private PartyDetails getRespondentWithOrg(PartyDetails respondent, String userToken) {

        if (YesNoDontKnow.yes.equals(respondent.getDoTheyHaveLegalRepresentation())
            && respondent.getSolicitorOrg() != null) {

            String organisationID = respondent.getSolicitorOrg().getOrganisationID();
            if (organisationID != null) {
                try {
                    organisations = getOrganisationDetails(userToken, organisationID);
                    respondent = respondent.toBuilder()
                        .organisations(organisations)
                        .build();
                } catch (NotFoundException e) {
                    log.error(
                        "OrganisationsAPi return 404, organisation not present for {} {} ",
                        organisationID,
                        e.getMessage(),
                        e
                    );
                } catch (Exception e) {
                    log.error(
                        "Error while fetching org details for orgid {} {} ",
                        organisationID,
                        e.getMessage(),
                        e
                    );
                }
            }
        }
        return respondent;
    }

    public Organisations getOrganisationDetails(String userToken, String organisationID) {
        log.trace("Fetching organisation details for organisation id: {}", organisationID);
        return organisationApi.findOrganisation(userToken, authTokenGenerator.generate(), organisationID);
    }

    public OrgSolicitors getOrganisationSolicitorDetails(String userToken, String organisationID) {
        log.trace("Fetching all solicitor details for organisation id: {}", organisationID);
        return organisationApi.findOrganisationSolicitors(
            userToken,
            authTokenGenerator.generate(),
            organisationID
        );
    }

    private PartyDetails getApplicantWithOrg(PartyDetails applicant, String userToken) {

        if (null != applicant && applicant.getSolicitorOrg() != null) {

            String organisationID = applicant.getSolicitorOrg().getOrganisationID();
            if (organisationID != null) {
                try {
                    organisations = getOrganisationDetails(userToken, organisationID);
                    applicant = applicant.toBuilder()
                        .organisations(organisations)
                        .build();
                } catch (NotFoundException e) {
                    log.error(
                        "OrganisationsAPi return 404, organisation not present for {} {} ",
                        organisationID,
                        e.getMessage(),
                        e
                    );
                } catch (Exception e) {
                    log.error(
                        "Error while fetching org details for orgid {} {} ",
                        organisationID,
                        e.getMessage(),
                        e
                    );
                }
            }

        }

        return applicant;
    }

    public CaseData getApplicantOrganisationDetailsForFL401(CaseData caseData) {
        if (Optional.ofNullable(caseData.getApplicantsFL401()).isPresent()) {
            String userToken = systemUserService.getSysUserToken();
            PartyDetails applicantWithOrg = getApplicantWithOrg(caseData.getApplicantsFL401(), userToken);
            caseData = caseData.toBuilder()
                .applicantsFL401(applicantWithOrg)
                .build();
        }
        return caseData;
    }

    public CaseData getRespondentOrganisationDetailsForFL401(CaseData caseData) {
        if (Optional.ofNullable(caseData.getRespondentsFL401()).isPresent()) {
            String userToken = systemUserService.getSysUserToken();
            PartyDetails respondentWithOrg = getRespondentWithOrg(caseData.getRespondentsFL401(), userToken);
            caseData = caseData.toBuilder()
                .respondentsFL401(respondentWithOrg)
                .build();
        }
        return caseData;
    }

    public Optional<Organisations> findUserOrganisation(String authorization) {
        try {
            return ofNullable(organisationApi.findUserOrganisation(authorization, authTokenGenerator.generate()));
        } catch (FeignException.NotFound | FeignException.Forbidden ex) {
            log.error("Exception while getting org details of the logged in users ", ex);
            return Optional.empty();
        }
    }

    public List<Organisations> getAllActiveOrganisations(String userToken) {
        log.trace("Fetching all active organisation details");
        Object orgObject = organisationApi.findOrganisations(userToken, authTokenGenerator.generate(), ACTIVE);
        log.info("Fetching all active organisation details ==> " + orgObject);
        return (List<Organisations>) orgObject;
    }

}

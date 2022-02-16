package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.OrganisationApi;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
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
    private List<Element<PartyDetails>> applicantsWithOrganisationDetails;
    @Autowired
    private ObjectMapper objectMapper;

    public List<Element<PartyDetails>> getOrganisationDetails(CaseData caseData) throws NotFoundException {

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
                    applicantsWithOrganisationDetails
                        .add(Element
                                 .<PartyDetails>builder()
                                 .value(objectMapper
                                            .convertValue(PartyDetails.builder()
                                                              .organisationAddress1(organisations.getContactInformation().get(0).getAddressLine1())
                                                              .organisationAddress2(organisations.getContactInformation().get(0).getAddressLine2())
                                                              .organisationAddress3(organisations.getContactInformation().get(0).getAddressLine3())
                                                              .organisationCountry(organisations.getContactInformation().get(0).getCountry())
                                                              .organisationCounty(organisations.getContactInformation().get(0).getCounty())
                                                              .organisationPostcode(organisations.getContactInformation().get(0).getPostCode())
                                                              .build(), PartyDetails.class)).build());

                    log.info("***** Applicant with Organisation address **** {}", applicantsWithOrganisationDetails);
                }
            }
        }

        return applicantsWithOrganisationDetails;

    }
}

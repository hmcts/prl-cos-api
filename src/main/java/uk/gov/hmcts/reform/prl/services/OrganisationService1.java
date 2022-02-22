package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.OrganisationApi;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public Organisations fetchOrgs(String userToken, String s2sToken, String orgId) {
        return organisationApi.findOrganisation(userToken, s2sToken, orgId);
    }


}

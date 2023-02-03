package uk.gov.hmcts.reform.prl.services;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.prl.clients.StaffResponseDetailsApi;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.concat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFORDERASC;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFSORTCOLUMN;

@Slf4j
@Service
public class RefDataUserService {

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    StaffResponseDetailsApi staffResponseDetailsApi;

    @Autowired
    JudicialUserDetailsApi judicialUserDetailsApi;

    @Autowired
    IdamClient idamClient;

    @Value("${prl.refdata.username}")
    private String refDataIdamUsername;

    @Value("${prl.refdata.password}")
    private String refDataIdamPassword;

    public List<DynamicListElement> getLegalAdvisorList() {
        try {
            List<StaffResponse> listOfStaffResponse = staffResponseDetailsApi.getAllStaffResponseDetails(
                idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword),
                authTokenGenerator.generate(),
                SERVICENAME,
                STAFFSORTCOLUMN,
                STAFFORDERASC
            );
            return onlyLegalAdvisor(listOfStaffResponse);
        } catch (Exception e) {
            log.error("Staff details Lookup Failed - " + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());
    }


    public List<JudicialUsersApiResponse> getAllJudicialUserDetails(JudicialUsersApiRequest judicialUsersApiRequest) {
        return judicialUserDetailsApi.getAllJudicialUserDetails(
            idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword),
            authTokenGenerator.generate(),
            judicialUsersApiRequest);
    }


    private List<DynamicListElement> onlyLegalAdvisor(List<StaffResponse> listOfStaffResponse) {
        if (null != listOfStaffResponse) {
            return listOfStaffResponse.stream()
                .filter(response -> response.getStaffProfile().getUserType().equalsIgnoreCase("Legal office"))
                .map(this::getDisplayEntry).collect(Collectors.toList());
        }
        return List.of(DynamicListElement.builder().build());
    }


    private DynamicListElement getDisplayEntry(StaffResponse staffResponse) {
        String value = concat(concat(staffResponse.getStaffProfile().getLastName()," - "),staffResponse.getStaffProfile().getEmailId());
        return DynamicListElement.builder().code(value).build();
    }
}

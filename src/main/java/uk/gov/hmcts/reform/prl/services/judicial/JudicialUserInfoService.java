package uk.gov.hmcts.reform.prl.services.judicial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;

@Slf4j
@Service
public class JudicialUserInfoService {

    @Autowired
    JudicialUserDetailsApi judicialUserDetailsApi;

    public JudicialUsersApiResponse getAllJudicialUserDetails(JudicialUsersApiRequest judicialUsersApiRequest, String serviceAuthorization,
                                                              String authorization) {
        return judicialUserDetailsApi.getAllJudicialUserDetails(authorization,serviceAuthorization,judicialUsersApiRequest);
    }

}

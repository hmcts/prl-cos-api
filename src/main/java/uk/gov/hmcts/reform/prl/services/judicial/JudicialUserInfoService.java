package uk.gov.hmcts.reform.prl.services.judicial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersAPIRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersAPIResponse;

@Slf4j
@Service
public class JudicialUserInfoService {

    @Autowired
    JudicialUserDetailsApi judicialUserDetailsApi;

    public JudicialUsersAPIResponse getAllJudicialUserDetails(JudicialUsersAPIRequest judicialUsersApiRequest, String serviceAuthorization,
                                                              String authorization) {
        return judicialUserDetailsApi.getAllJudicialUserDetails(authorization,serviceAuthorization,judicialUsersApiRequest);
    }

}

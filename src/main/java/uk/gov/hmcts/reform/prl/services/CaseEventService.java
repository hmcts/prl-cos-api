package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;

import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@Service
@RequiredArgsConstructor
public class CaseEventService {

    private final CaseEventsApi caseEventsApi;

    private final AuthTokenGenerator authTokenGenerator;

    private final SystemUserService systemUserService;

    public List<CaseEventDetail> findEventsForCase(String ccdCaseId) {
        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);

        return caseEventsApi.findEventDetailsForCase(userToken,
            authTokenGenerator.generate(), systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE, ccdCaseId);
    }
}

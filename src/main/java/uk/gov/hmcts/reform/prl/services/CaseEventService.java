package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.prl.enums.Event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.JURISDICTION;

@Service
@RequiredArgsConstructor
public class CaseEventService {

    private final CaseEventsApi caseEventsApi;

    private final AuthTokenGenerator authTokenGenerator;

    private final SystemUserService systemUserService;

    public List<CaseEventDetail> findEventsForCase(String ccdCaseId) {
        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);

        List<CaseEventDetail> caseEventDetails = caseEventsApi.findEventDetailsForCase(userToken,
            authTokenGenerator.generate(), systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE, ccdCaseId);

        return caseEventDetails;
    }
}

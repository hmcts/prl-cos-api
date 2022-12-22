package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;

@Slf4j
@Service
@RequiredArgsConstructor
public class C100RespondentSolicitorService {
    private final CcdDataStoreService ccdDataStoreService;

    public void prePopulateRespondentSolicitorCaseData(String caseId, String authorisation) {
        CaseUser caseUser = ccdDataStoreService.findRespondentSolicitorCaseRoles(caseId, authorisation);

        log.info("CaseId: {} assigned case access to user {}", caseId, caseUser.getCaseRole());
    }
}

package uk.gov.hmcts.reform.prl.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Slf4j
@Service("caseRepository")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CcdCaseRepository implements CaseRepository {
    @Autowired
    CcdCaseApi ccdCaseApi;

    @Override
    public void linkDefendant(String authorisation, String anonymousUserToken, String s2sToken, String caseId, CaseData caseData) {
        ccdCaseApi.linkCitizenToCase(authorisation, anonymousUserToken, s2sToken, caseId, caseData);
    }
}

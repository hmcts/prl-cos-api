package uk.gov.hmcts.reform.prl.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

@Slf4j
@Service("caseRepository")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CcdCaseRepository implements CaseRepository {
    @Autowired
    CcdCaseApi ccdCaseApi;

    @Override
    public void linkDefendant(String userAuthorisation, String systemUserToken, String caseId,
                              EventRequestData eventRequestData, StartEventResponse startEventResponse, Map<String, Object> caseDataUpdated) {
        ccdCaseApi.linkCitizenToCase(
            userAuthorisation,
            systemUserToken,
            caseId,
            eventRequestData,
            startEventResponse,
            caseDataUpdated
        );
    }

    @Override
    public CaseDetails updateCase(String authorisation, String caseId, CaseData caseData, CaseEvent caseEvent) {
        return ccdCaseApi.updateCase(authorisation, caseId, caseData, caseEvent);
    }

    @Override
    public CaseDetails createCase(String authorisation, CaseData caseData) {
        return ccdCaseApi.createCase(authorisation, caseData);
    }

    @Override
    public CaseDetails getCase(String authorisation, String caseId) {
        return ccdCaseApi.getCase(authorisation, caseId);
    }
}

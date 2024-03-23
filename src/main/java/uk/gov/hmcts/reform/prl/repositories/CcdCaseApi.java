package uk.gov.hmcts.reform.prl.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenCoreCaseDataService;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CcdCaseApi {
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseAccessApi caseAccessApi;
    private final CitizenCoreCaseDataService citizenCoreCaseDataService;
    private final IdamClient idamClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(CcdCaseApi.class);

    public CaseDetails updateCase(String authorisation, String caseId, CaseData caseData, CaseEvent caseEvent) {
        return citizenCoreCaseDataService.updateCase(
            authorisation,
            Long.valueOf(caseId),
            caseData,
            caseEvent
        );
    }

    public CaseDetails createCase(String authorisation, CaseData caseData) {
        return citizenCoreCaseDataService.createCase(
            authorisation,
            caseData
        );
    }

    public CaseDetails getCase(String authorisation, String caseId) {
        return citizenCoreCaseDataService.getCase(
            authorisation,
            caseId
        );
    }
}

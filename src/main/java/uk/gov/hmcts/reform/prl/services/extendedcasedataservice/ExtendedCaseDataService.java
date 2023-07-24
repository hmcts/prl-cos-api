package uk.gov.hmcts.reform.prl.services.extendedcasedataservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.ccd.ExtendedCaseDataApi;
import uk.gov.hmcts.reform.prl.models.extendedcasedetails.ExtendedCaseDetails;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
public class ExtendedCaseDataService {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    private SystemUserService systemUserService;

    @Autowired
    private ExtendedCaseDataApi caseDataApi;

    public Map<String, Object> getDataClassification(String caseId) {
        ExtendedCaseDetails caseDetails = caseDataApi.getExtendedCaseDetails(
            systemUserService.getSysUserToken(),
            authTokenGenerator.generate(), caseId
        );

        log.info("** Testing case data:: " + caseDetails.getCaseData());
        log.info("** Testing case data classification:: " + caseDetails.getDataClassification());
        return caseDetails.getDataClassification();
    }
}

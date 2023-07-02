package uk.gov.hmcts.reform.prl.services.extendedcasedataservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.ccd.ExtendedCaseDataApi;
import uk.gov.hmcts.reform.prl.models.extendedcasedetails.ExtendedCaseDetails;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

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
        return caseDetails.getDataClassification();
    }
}

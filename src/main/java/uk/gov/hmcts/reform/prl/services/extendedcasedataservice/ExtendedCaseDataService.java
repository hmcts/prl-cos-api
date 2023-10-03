package uk.gov.hmcts.reform.prl.services.extendedcasedataservice;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
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
        String sysUserToken = systemUserService.getSysUserToken();
        String authToken = authTokenGenerator.generate();
        ExtendedCaseDetails caseDetails = caseDataApi.getExtendedCaseDetails(
            sysUserToken,
            authToken, caseId
        );

        return caseDetails.getDataClassification();
    }
}

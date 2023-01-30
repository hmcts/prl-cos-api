package uk.gov.hmcts.reform.prl.services.legalofficer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.StaffResponseDetailsApi;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffApiResponse;

@Slf4j
@Service
public class LegalOfficerInfoService {

    @Autowired
    StaffResponseDetailsApi staffResponseDetailsApi;

    public StaffApiResponse getAllLegalAdvisorDetails(String serviceAuthorization,
                                                      String authorization, String ccdServiceNames) {
        return staffResponseDetailsApi.getAllStaffResponseDetails(authorization,serviceAuthorization,ccdServiceNames);
    }
}

package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.StaffResponseDetailsApi;
import uk.gov.hmcts.reform.prl.exception.NoStaffResponseException;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFORDERASC;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFSORTCOLUMN;
import static uk.gov.hmcts.reform.prl.services.RefDataUserService.STAFF_REF_DATA_CACHE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StaffRefDataService {

    private final AuthTokenGenerator authTokenGenerator;
    private final StaffResponseDetailsApi staffResponseDetailsApi;
    private final IdamClient idamClient;

    @Value("${prl.refdata.username}")
    private String refDataIdamUsername;
    @Value("${prl.refdata.password}")
    private String refDataIdamPassword;

    @Cacheable(cacheNames = STAFF_REF_DATA_CACHE)
    public List<StaffResponse> getAllStaffDetails() {
        log.info("Fetching all staff ref data (not cached)");
        try {
            ResponseEntity<List<StaffResponse>> response = staffResponseDetailsApi.getAllStaffResponseDetails(
                idamClient.getAccessToken(refDataIdamUsername, refDataIdamPassword),
                authTokenGenerator.generate(),
                SERVICENAME,
                STAFFSORTCOLUMN,
                STAFFORDERASC,
                Integer.MAX_VALUE,
                0
            );
            if (response == null) {
                log.info("Fetched 0 staff members");
                return Collections.emptyList();
            }
            List<StaffResponse> staffList = response.getBody();
            log.info("Fetched {} staff members", staffList != null ? staffList.size() : 0);
            return staffList != null ? staffList : Collections.emptyList();
        } catch (FeignException e) {
            throw new NoStaffResponseException("Failed to retrieve staff response", e);
        }
    }
}

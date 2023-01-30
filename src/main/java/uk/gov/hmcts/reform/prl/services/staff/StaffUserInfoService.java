package uk.gov.hmcts.reform.prl.services.staff;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.StaffResponseDetailsApi;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffApiResponse;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.concat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICENAME;

@Slf4j
@Service
public class StaffUserInfoService {

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    StaffResponseDetailsApi staffResponseDetailsApi;

    public List<DynamicListElement> getLegalAdvisorList(String authorization) {
        try {
            StaffApiResponse staffDetails = staffResponseDetailsApi.getAllStaffResponseDetails(
                authorization,
                authTokenGenerator.generate(),
                SERVICENAME);
            return onlyLegalAdvisor(staffDetails);
        } catch (Exception e) {
            log.error("Staff details Lookup Failed - " + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());
    }

    private List<DynamicListElement> onlyLegalAdvisor(StaffApiResponse staffDetails) {
        return (staffDetails == null
            ? new ArrayList<>()
            : staffDetails.getStaffProfile().stream()
            .filter(role -> "Legal office".equalsIgnoreCase(role.getUserType()))
            .map(this::getDisplayEntry).collect(Collectors.toList()));
    }

    private DynamicListElement getDisplayEntry(StaffProfile staffProfile) {
        String value = concat(concat(staffProfile.getLastName()," - "),staffProfile.getEmailId());
        return DynamicListElement.builder().code(value).build();
    }

}

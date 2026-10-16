package uk.gov.hmcts.reform.prl.services.validators;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.staff.StaffUser;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffProfile;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;

import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LegalAdviserChecker {

    private final RefDataUserService refDataUserService;

    public Optional<String> validateLegalAdviserName(DynamicList laList, StaffUser legalAdviser) {
        if (!isLegalAdviserListPresent(laList) && legalAdviser == null) {
            return Optional.empty();
        } else {
            if (legalAdviser != null && legalAdviser.getIdamId() != null
                && StringUtils.isNotBlank(legalAdviser.getIdamId())) {
                Optional<StaffProfile> staffProfile = refDataUserService.getLegalAdviserDetails(legalAdviser);
                return staffProfile.map(this::extractStaffProfileName);
            } else if (isLegalAdviserListPresent(laList)) {
                return Optional.ofNullable(laList.getValueLabel());
            } else {
                return Optional.empty();
            }
        }
    }

    private String extractStaffProfileName(StaffProfile staffProfile) {
        if (staffProfile.getFirstName() != null && staffProfile.getLastName() != null) {
            return String.format("%s %s", staffProfile.getFirstName(), staffProfile.getLastName());
        } else if (staffProfile.getFirstName() != null) {
            return staffProfile.getFirstName();
        } else {
            return staffProfile.getLastName();
        }
    }

    public static boolean isLegalAdviserListPresent(DynamicList legalAdviserList) {
        return legalAdviserList != null && StringUtils.isNotBlank(legalAdviserList.getValueLabel());
    }
}

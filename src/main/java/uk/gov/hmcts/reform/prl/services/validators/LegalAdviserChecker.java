package uk.gov.hmcts.reform.prl.services.validators;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.staff.StaffUser;

import java.util.Objects;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LegalAdviserChecker {

    public String returnLegalAdviserName(DynamicList laList, StaffUser legalAdviser) {
        if (!isLegalAdviserListPresent(laList) && legalAdviser == null) {
            return null;
        } else {
            return String.valueOf(Objects.requireNonNullElse(legalAdviser, laList));
        }
    }

    public static boolean isLegalAdviserListPresent(DynamicList legalAdviserList) {
        return legalAdviserList != null && StringUtils.isNotBlank(legalAdviserList.getValueLabel());
    }
}

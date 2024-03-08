package uk.gov.hmcts.reform.prl.models.dto.ccd;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;

import java.util.Arrays;
import java.util.List;

@Data
@Configuration
public class WelshCourtEmail {
    @Value("${welsh.court.email-mapping}")
    protected String welshCourtEmailMapping;

    private static final Logger LOGGER = LoggerFactory.getLogger(WelshCourtEmail.class);

    public String populateCafcassCymruEmailInManageOrders(CaseData caseData) {

        CaseManagementLocation caseManagementLocation = caseData.getCaseManagementLocation();
        final String[] courtEmail = {""};

        if (caseManagementLocation != null
            && caseManagementLocation.getRegionId() != null) {
            if (welshCourtEmailMapping.length() > 0) {
                LOGGER.info("welsh Court email retrieved from the vault");
            }
            Arrays.stream(welshCourtEmailMapping.split(",")).forEach(
                value -> {
                    List<String> courtMapping = Arrays.asList(value.split("--"));
                    if (caseManagementLocation.getBaseLocationId().equals(courtMapping.get(0))
                        && caseManagementLocation.getRegionId().equals(courtMapping.get(1))) {
                        courtEmail[0] = courtMapping.get(3);
                    }
                }
            );
        } else if (caseManagementLocation != null
            && caseManagementLocation.getRegion() != null) {
            Arrays.stream(welshCourtEmailMapping.split(",")).forEach(
                value -> {
                    List<String> courtMapping = Arrays.asList(value.split("--"));
                    if (caseManagementLocation.getBaseLocation().equals(courtMapping.get(0))
                        && caseManagementLocation.getRegion().equals(courtMapping.get(1))) {
                        courtEmail[0] = courtMapping.get(3);
                    }
                }
            );
        }
        return courtEmail[0] != null && courtEmail[0].length() > 1 ? courtEmail[0] : null;
    }

}

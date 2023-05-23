package uk.gov.hmcts.reform.prl.models.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;

import java.util.List;

@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class ServedApplicationDetails {
    private List<Element<BulkPrintDetails>> bulkPrintDetails;
    private List<Element<EmailNotificationDetails>> emailNotificationDetails;
}

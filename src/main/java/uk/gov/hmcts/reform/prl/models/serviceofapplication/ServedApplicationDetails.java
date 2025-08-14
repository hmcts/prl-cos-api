package uk.gov.hmcts.reform.prl.models.serviceofapplication;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.prl.services.citizen.CaseService.DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServedApplicationDetails {
    @JsonProperty("bulkPrintDetails")
    private List<Element<BulkPrintDetails>> bulkPrintDetails;
    @JsonProperty("emailNotificationDetails")
    private List<Element<EmailNotificationDetails>> emailNotificationDetails;
    @JsonProperty("servedBy")
    private String servedBy;
    @JsonProperty("servedAt")
    private String servedAt;
    @JsonProperty("modeOfService")
    private String modeOfService;
    @JsonProperty("whoIsResponsible")
    private String whoIsResponsible;

    @JsonIgnore
    public LocalDateTime getServedDateTime() {
        return LocalDateTime.parse(servedAt,
                                   DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS);
    }
}

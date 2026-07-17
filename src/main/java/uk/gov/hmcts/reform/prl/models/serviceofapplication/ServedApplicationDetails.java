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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServedApplicationDetails {
    @CCD(label = "Print details", searchable = false)
    @JsonProperty("bulkPrintDetails")
    private List<Element<BulkPrintDetails>> bulkPrintDetails;
    @CCD(label = "Email notification details", searchable = false)
    @JsonProperty("emailNotificationDetails")
    private List<Element<EmailNotificationDetails>> emailNotificationDetails;
    @CCD(label = "Served by", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("servedBy")
    private String servedBy;
    @CCD(label = "Served at", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("servedAt")
    private String servedAt;
    @CCD(label = "Mode of service", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("modeOfService")
    private String modeOfService;
    @CCD(label = "Who is responsible for serving respondent?", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("whoIsResponsible")
    private String whoIsResponsible;

    @JsonIgnore
    public LocalDateTime getServedDateTime() {
        return LocalDateTime.parse(servedAt,
                                   DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS);
    }
}

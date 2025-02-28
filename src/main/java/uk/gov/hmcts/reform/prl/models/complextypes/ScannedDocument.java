package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;

@Data
@Builder
public class ScannedDocument {

    public final String fileName;
    public final String controlNumber;
    public final String type;
    public final String subtype;

    @JsonProperty("exceptionRecordReference")
    public final String exceptionRecordReference;

    @JsonProperty("url")
    public final Document url;

    public final LocalDateTime scannedDate;
    public final LocalDateTime deliveryDate;

    @JsonProperty("control_number")
    public final String controlNumberbulkscan;

    @JsonProperty("file_name")
    public final String fileNamebulkscan;

    @JsonProperty("scanned_date")
    public final LocalDateTime scannedDatebulkscan;

    @JsonProperty("delivery_date")
    public final LocalDateTime deliveryDatebulkscan;

}

package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class ScannedDocument {

    @CCD(label = "File Name", searchable = false)
    public final String fileName;
    @CCD(label = "Document Control Number", searchable = false)
    public final String controlNumber;
    @CCD(
            label = "Document Type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ScannedDocumentType"
    )
    public final String type;
    @CCD(label = "Document Subtype", searchable = false)
    public final String subtype;

    @CCD(label = "Exception record reference", searchable = false)
    @JsonProperty("exceptionRecordReference")
    public final String exceptionRecordReference;

    @CCD(label = "Scanned document url", categoryID = "bulkScanQuarantine", searchable = false)
    @JsonProperty("url")
    public final Document url;

    @CCD(label = "Scanned Date", searchable = false)
    public final LocalDateTime scannedDate;
    @CCD(label = "Delivery Date", searchable = false)
    public final LocalDateTime deliveryDate;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @JsonProperty("# Record Meta data")
  @CCD(label = "Scanned Records", searchable = false, typeOverride = FieldType.Label)
  private String __Record_Meta_data;
  // ==== end synthesised definition-only fields ====
}

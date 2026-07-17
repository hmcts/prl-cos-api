package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.FL401Consent;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class StatementOfTruth {
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "Fl401Consent"
    )
    @JsonProperty("applicantConsent")
    private final List<FL401Consent> applicantConsent;
    @CCD(label = " ", searchable = false)
    @JsonProperty("signature")
    private final String signature;
    //private final SignatureEnum signatureType;
    @CCD(label = "Date ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate date;
    @CCD(label = "Your full name ", searchable = false)
    private final String fullname;
    @CCD(label = "Name of your firm ", searchable = false)
    private final String nameOfFirm;
    @CCD(label = "If signing on behalf of firm or company give position or office held ", searchable = false)
    private final String signOnBehalf;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "I understand that proceedings for contempt of court may be brought against anyone who makes, or causes to be made, a false statement in a document verified by a statement of truth without an honest belief in its truth.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String proceedingsLabel;
  // ==== end synthesised definition-only fields ====
}

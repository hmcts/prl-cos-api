package uk.gov.hmcts.reform.prl.models.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SosUploadedByEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class StmtOfServiceAddRecipient {

    private DynamicList respondentDynamicList;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final LocalDateTime servedDateTimeOption;
    private final Document stmtOfServiceDocument;
    private final String citizenPartiesServedList;
    private final String citizenPartiesServedDate;
    private final List<Document> citizenSosDocs;
    private final String selectedPartyId;
    private final String selectedPartyName;
    private SosUploadedByEnum uploadedBy;
}

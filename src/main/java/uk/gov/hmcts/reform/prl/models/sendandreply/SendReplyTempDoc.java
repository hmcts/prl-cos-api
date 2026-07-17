package uk.gov.hmcts.reform.prl.models.sendandreply;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class SendReplyTempDoc {

    @CCD(label = "Attached date & time", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime attachedTime;
    @CCD(label = "Document", searchable = false)
    private Document document;
}

package uk.gov.hmcts.reform.prl.models.complextypes.serviceofdocuments;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.ServeOrgDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SodPack {

    private final List<Element<Document>> uploadedDocuments;
    private final List<Element<Document>> cfvDocuments;
    private final List<Element<String>> applicantIds;
    private final List<Element<String>> respondentIds;
    private final List<Element<String>> otherPersonIds;
    private List<Element<ServeOrgDetails>> additionalRecipients;
    private final String servedBy;
    private final YesOrNo isPersonalService;
    private final String submittedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime submittedDateTime;
}

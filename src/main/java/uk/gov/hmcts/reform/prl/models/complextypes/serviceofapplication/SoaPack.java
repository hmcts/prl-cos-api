package uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SoaPack {

    private final List<Element<Document>> packDocument;

    private final List<Element<String>> partyIds;

    private final String servedBy;

    private final String packCreatedDate;

    private final String personalServiceBy;

    private final String servedPartyEmail;
}

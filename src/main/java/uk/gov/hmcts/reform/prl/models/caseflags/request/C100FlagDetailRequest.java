package uk.gov.hmcts.reform.prl.models.caseflags.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class C100FlagDetailRequest {
    public String name;
    @SuppressWarnings(value = "MemberName")
    public String name_cy;
    public String subTypeValue;
    @SuppressWarnings(value = "MemberName")
    public String subTypeValue_cy;
    public String subTypeKey;
    public String otherDescription;
    @SuppressWarnings(value = "MemberName")
    public String otherDescription_cy;
    public String flagComment;
    @SuppressWarnings(value = "MemberName")
    public String flagComment_cy;
    public String flagUpdateComment;
    public String dateTimeCreated;
    public String dateTimeModified;
    public List<String> path;
    public YesOrNo hearingRelevant;
    public String flagCode;
    public String status;
    public YesOrNo availableExternally;
}

package uk.gov.hmcts.reform.prl.models.caseflags.flagdetails;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;

import java.time.LocalDateTime;
import java.util.List;

@lombok.Data
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FlagDetail {
    public String name;
    public String name_cy;
    public String subTypeValue;
    public String subTypeValue_cy;
    public String subTypeKey;
    public String otherDescription;
    public String otherDescription_cy;
    public String flagComment;
    public String flagComment_cy;
    public String flagUpdateComment;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public LocalDateTime dateTimeCreated;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public LocalDateTime dateTimeModified;
    public List<Element<String>> path;
    public YesOrNo hearingRelevant;
    public String flagCode;
    public String status;
    public YesOrNo availableExternally;
}

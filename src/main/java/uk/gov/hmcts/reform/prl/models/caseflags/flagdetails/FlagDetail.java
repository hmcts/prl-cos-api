package uk.gov.hmcts.reform.prl.models.caseflags.flagdetails;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class FlagDetail {
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
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public LocalDateTime dateTimeCreated;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public LocalDateTime dateTimeModified;
    public List<Element<String>> path;
    public YesOrNo hearingRelevant;
    public String flagCode;
    public String status;
    public YesOrNo availableExternally;
}

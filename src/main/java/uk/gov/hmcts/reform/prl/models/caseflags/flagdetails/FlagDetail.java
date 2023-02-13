package uk.gov.hmcts.reform.prl.models.caseflags.flagdetails;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@lombok.Data
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FlagDetail {
    //  public List<Element<Path>> path;
    public String hearingRelevant;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public LocalDateTime dateTimeCreated;
    public String flagComment;
    // public Object subTypeKey;
    public String flagCode;
    public String name;
    // public Object subTypeValue;
    // public Object otherDescription;
    public String status;
}

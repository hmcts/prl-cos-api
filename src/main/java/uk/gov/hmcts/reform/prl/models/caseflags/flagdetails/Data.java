package uk.gov.hmcts.reform.prl.models.caseflags.flagdetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@lombok.Data
@Builder(toBuilder = true)
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class Data {
    public List<Path> path;
    public String hearingRelevant;
    public LocalDateTime dateTimeCreated;
    public String flagComment;
    public Object subTypeKey;
    public String flagCode;
    public String name;
    public Object subTypeValue;
    public Object otherDescription;
    public String status;
}

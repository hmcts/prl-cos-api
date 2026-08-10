package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "addCaseNoteType", generate = true)
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class CaseNoteDetails {
    @CCD(label = "Subject", searchable = false)
    private final String subject;
    @CCD(label = "Case note", searchable = false, typeOverride = FieldType.TextArea)
    private final String caseNote;
    @CCD(label = "User", searchable = false)
    private final String user;
    @CCD(label = "Date added", searchable = false, typeOverride = FieldType.Date)
    private final String dateAdded;
    @CCD(label = "Date created", showCondition = "dateCreated=\"DO_NOT_SHOW\"", searchable = false)
    private final LocalDateTime dateCreated;
}

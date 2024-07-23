package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDateTime;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@SuperBuilder(toBuilder = true)
public class BaseCaseData {

    private long id;

    private State state;

    private String taskListVersion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime lastModifiedDate;

    private String dateSubmitted;

    private String caseSubmittedTimeStamp;

    private String courtSeal;

    /**
     * Case Type Of Application.
     */
    private String selectedCaseTypeID;
    /**
     * Case Type Of Application.
     */
    @JsonProperty("caseTypeOfApplication")
    private String caseTypeOfApplication;
    /**
     * Case name.
     */
    @JsonAlias({"applicantCaseName", "applicantOrRespondentCaseName"})
    private String applicantCaseName;

    //FPET-567 - Added for hiding fields for SDO
    @JsonProperty("isSdoSelected")
    private YesOrNo isSdoSelected;

    @JsonUnwrapped
    private final DocumentsNotifications documentsNotifications;
}

package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)

public class DssCaseData {

    @JsonUnwrapped
    private String namedApplicant;
    @JsonUnwrapped
    private String caseTypeOfApplication;
    @JsonUnwrapped
    private String applicantFirstName;
    @JsonUnwrapped
    private String applicantLastName;
    @JsonUnwrapped
    private String applicantDateOfBirth;
    @JsonUnwrapped
    private String applicantContactPreference;
    @JsonUnwrapped
    private String applicantEmailAddress;
    @JsonUnwrapped
    private String applicantPhoneNumber;
    @JsonUnwrapped
    private String applicantHomeNumber;
    @JsonUnwrapped
    private String applicantAddress1;
    @JsonUnwrapped
    private String applicantAddress2;
    @JsonUnwrapped
    private String applicantAddressTown;
    @JsonUnwrapped
    private String applicantAddressPostCode;
    @JsonUnwrapped
    private String applicantStatementOfTruth;

    @JsonUnwrapped
    private String selectedCourt;

    @JsonUnwrapped
    private List<Element<EdgeCaseDocument>> applicantApplicationFormDocuments;

    @JsonUnwrapped
    private List<Element<EdgeCaseDocument>> applicantAdditionalDocuments;
}

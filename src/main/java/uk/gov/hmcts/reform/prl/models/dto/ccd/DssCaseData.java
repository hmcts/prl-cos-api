package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.ccd.client.model.Document;

import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)
public class DssCaseData {

    private String namedApplicant;
    private String caseTypeOfApplication;
    private String applicantFirstName;
    private String applicantLastName;
    private String applicantDateOfBirth;
    private String applicantContactPreference;
    private String applicantEmailAddress;
    private String applicantPhoneNumber;
    private String applicantHomeNumber;
    private String applicantAddress1;
    private String applicantAddress2;
    private String applicantAddressTown;
    private String applicantAddressPostCode;
    private String applicantStatementOfTruth;
    private String selectedCourt;

    private List<Document> applicantApplicationFormDocuments;
    private List<Document> applicantAdditionalDocuments;
}

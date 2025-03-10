package uk.gov.hmcts.reform.prl.models.edgecases;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.c100rebuild.DateofBirth;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DssCaseData {

    private String edgeCaseTypeOfApplication;
    private String whomYouAreApplying;
    private String applicantFirstName;
    private String applicantLastName;
    private DateofBirth applicantDateOfBirth;
    private String applicantEmailAddress;
    private String applicantPhoneNumber;
    private String applicantAddress1;
    private String applicantAddress2;
    private String applicantAddressTown;
    private String applicantAddressCounty;
    private String applicantAddressPostCode;
    private String applicantAddressCountry;
    private String selectedCourtId;
    private String applicantStatementOfTruth;
    private String hwfPaymentSelection;
    private String helpWithFeesReferenceNumber;

    private List<Document> applicantApplicationFormDocuments;
    private List<Document> applicantAdditionalDocuments;
}

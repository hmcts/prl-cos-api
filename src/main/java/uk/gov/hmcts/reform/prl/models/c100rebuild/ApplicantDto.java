package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicantDto {

    private String id;
    private String applicantFirstName;
    private String applicantLastName;
    private String[] contactDetailsPrivate;
    private String[] contactDetailsPrivateAlternative;

    private RelationshipDetails relationshipDetails;
    private PersonalDetails personalDetails;
    private ContactDetail applicantContactDetail;
    private String applicantAddressPostcode;
    private String applicantAddress1;
    private String applicantAddress2;
    private String applicantAddressTown;
    private String applicantAddressCounty;
    private YesOrNo applicantAddressHistory;
    private String applicantProvideDetailsOfPreviousAddresses;

    private String detailsKnown;
    private String start;
    private String startAlternative;
}

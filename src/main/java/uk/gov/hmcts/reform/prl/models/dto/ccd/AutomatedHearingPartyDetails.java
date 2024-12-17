package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonRelationshipToChild;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder(builderMethodName = "automatedHearingPartyDetailsWith")
@AllArgsConstructor
public class AutomatedHearingPartyDetails {

    private String firstName;
    private String lastName;
    private String previousName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private DontKnow isDateOfBirthUnknown;

    private String otherGender;
    private String placeOfBirth;
    private DontKnow isAddressUnknown;

    private String addressLivedLessThan5YearsDetails;
    private String landline;

    private String relationshipToChildren;
    private List<Element<OtherPersonRelationshipToChild>> otherPersonRelationshipToChildren;
    private Organisation solicitorOrg;
    private Address solicitorAddress;
    private String dxNumber;
    private String solicitorReference;
    private String representativeFirstName;
    private String representativeLastName;
    private String sendSignUpLink;
    private String solicitorEmail;
    private String phoneNumber;
    private String email;
    private Address address;
    private String solicitorTelephone;
    private String caseTypeOfApplication;

    private Flags partyLevelFlag;

    private UUID partyId;
    private UUID solicitorOrgUuid;
    private UUID solicitorPartyId;

}

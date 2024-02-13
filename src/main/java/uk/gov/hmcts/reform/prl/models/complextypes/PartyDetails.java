package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreferredContactEnum;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class PartyDetails {

    public static final String FULL_NAME_FORMAT = "%s %s";
    private final String firstName;
    private final String lastName;
    private final String previousName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    private final DontKnow isDateOfBirthUnknown;
    private final Gender gender;
    private final String otherGender;
    private final String placeOfBirth;
    private final DontKnow isAddressUnknown;
    private YesOrNo isAddressConfidential;
    private final YesOrNo isAtAddressLessThan5Years;
    private final String addressLivedLessThan5YearsDetails;
    private final YesOrNo canYouProvideEmailAddress;
    private YesOrNo isEmailAddressConfidential;
    private final String landline;
    private YesOrNo isPhoneNumberConfidential;
    private final String relationshipToChildren;
    private final YesOrNo isDateOfBirthKnown;
    private final YesOrNo isCurrentAddressKnown;
    private final YesOrNo canYouProvidePhoneNumber;
    private final YesOrNo isPlaceOfBirthKnown;
    private final List<Element<OtherPersonRelationshipToChild>> otherPersonRelationshipToChildren;
    private final Organisation solicitorOrg;
    private final Address solicitorAddress;
    private final String dxNumber;
    private final String solicitorReference;
    private final String representativeFirstName;
    private final String representativeLastName;
    private final YesNoDontKnow isAtAddressLessThan5YearsWithDontKnow;
    private final YesNoDontKnow doTheyHaveLegalRepresentation;
    private final String sendSignUpLink;
    private final String solicitorEmail;
    private String phoneNumber;
    private String email;
    private Address address;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Organisations organisations;
    private final String solicitorTelephone;
    @JsonIgnore
    private final String caseTypeOfApplication;
    private final YesOrNo respondentLivedWithApplicant;

    @JsonProperty("applicantPreferredContact")
    private final List<PreferredContactEnum> applicantPreferredContact;
    private final String applicantContactInstructions;
    private User user;
    private Response response;
    private YesOrNo currentRespondent;

    // it will hold either applicant flag or respondent flag
    // Deprecated. kept for backward compatibility
    private Flags partyLevelFlag;

    private ContactPreferences contactPreferences;

    private YesOrNo isRemoveLegalRepresentativeRequested;

    public boolean hasConfidentialInfo() {
        return this.isAddressConfidential.equals(YesOrNo.Yes)
            || this.isPhoneNumberConfidential.equals(YesOrNo.Yes);
    }

    public boolean isCanYouProvideEmailAddress() {
        return this.canYouProvideEmailAddress.equals(YesOrNo.No);
    }

    @JsonIgnore
    public boolean isEmailAddressNull() {
        if (isCanYouProvideEmailAddress()) {
            return this.isEmailAddressConfidential == YesOrNo.No;
        }
        return this.isEmailAddressConfidential == YesOrNo.Yes;
    }

    @JsonIgnore
    public String getLabelForDynamicList() {
        return String.format(
            FULL_NAME_FORMAT,
            this.firstName,
            this.lastName
        );
    }

    @JsonIgnore
    public String getRepresentativeFullName() {
        return String.format(
            FULL_NAME_FORMAT,
            this.representativeFirstName,
            this.representativeLastName
        );
    }

    @JsonIgnore
    public String getRepresentativeFullNameForCaseFlags() {
        if (!StringUtils.isEmpty(this.representativeFirstName)
            && !StringUtils.isEmpty(this.representativeLastName)) {
            return String.format(
                FULL_NAME_FORMAT,
                StringUtils.capitalize(this.representativeFirstName.trim()),
                StringUtils.capitalize(this.representativeLastName.trim())
            );
        } else if (!StringUtils.isEmpty(this.representativeFirstName)
            && StringUtils.isEmpty(this.representativeLastName)) {
            return String.format(
                "%s",
                StringUtils.capitalize(this.representativeFirstName.trim())
            );
        } else if (StringUtils.isEmpty(this.representativeFirstName)
            && !StringUtils.isEmpty(this.representativeLastName)) {
            return String.format(
                "%s",
                StringUtils.capitalize(this.representativeLastName.trim())
            );
        } else {
            return StringUtils.EMPTY;
        }
    }

    private UUID partyId;

    private UUID solicitorOrgUuid;

    private UUID solicitorPartyId;

    @JsonIgnore
    private CitizenSos citizenSosObject;
}

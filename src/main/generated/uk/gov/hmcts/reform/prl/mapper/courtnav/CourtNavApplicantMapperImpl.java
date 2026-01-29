package uk.gov.hmcts.reform.prl.mapper.courtnav;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavApplicant;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreferredContactEnum;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-21T11:01:27+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.9 (Amazon.com Inc.)"
)
@Component
public class CourtNavApplicantMapperImpl implements CourtNavApplicantMapper {

    @Autowired
    private CourtNavAddressMapper courtNavAddressMapper;

    @Override
    public PartyDetails map(CourtNavApplicant applicant) {
        if ( applicant == null ) {
            return null;
        }

        PartyDetails.PartyDetailsBuilder partyDetails = PartyDetails.builder();

        partyDetails.solicitorOrg( courtNavApplicantToOrganisation( applicant ) );
        partyDetails.address( courtNavAddressMapper.map( applicant.getAddress() ) );
        partyDetails.solicitorAddress( courtNavAddressMapper.map( applicant.getSolicitorAddress() ) );
        partyDetails.firstName( applicant.getFirstName() );
        partyDetails.lastName( applicant.getLastName() );
        partyDetails.previousName( applicant.getPreviousName() );
        partyDetails.gender( applicant.getGender() );
        partyDetails.otherGender( applicant.getOtherGender() );
        partyDetails.dxNumber( applicant.getDxNumber() );
        partyDetails.solicitorReference( applicant.getSolicitorReference() );
        partyDetails.representativeFirstName( applicant.getRepresentativeFirstName() );
        partyDetails.representativeLastName( applicant.getRepresentativeLastName() );
        partyDetails.solicitorEmail( applicant.getSolicitorEmail() );
        partyDetails.phoneNumber( applicant.getPhoneNumber() );
        partyDetails.email( applicant.getEmail() );
        partyDetails.solicitorTelephone( applicant.getSolicitorTelephone() );
        List<PreferredContactEnum> list = applicant.getApplicantPreferredContact();
        if ( list != null ) {
            partyDetails.applicantPreferredContact( new ArrayList<PreferredContactEnum>( list ) );
        }
        partyDetails.applicantContactInstructions( applicant.getApplicantContactInstructions() );

        partyDetails.dateOfBirth( parseDate(applicant.getDateOfBirth()) );
        partyDetails.isAddressConfidential( resolveConfidentialFlag(applicant.getAddress(), applicant.isShareContactDetailsWithRespondent()) );
        partyDetails.isEmailAddressConfidential( resolveConfidentialFlag(applicant.getEmail(), applicant.isShareContactDetailsWithRespondent()) );
        partyDetails.isPhoneNumberConfidential( resolveConfidentialFlag(applicant.getPhoneNumber(), applicant.isShareContactDetailsWithRespondent()) );
        partyDetails.canYouProvideEmailAddress( resolveYesNo(applicant.getEmail()) );
        partyDetails.contactPreferences( resolveContactPreferences(applicant) );
        partyDetails.partyId( java.util.UUID.randomUUID() );

        return partyDetails.build();
    }

    protected Organisation courtNavApplicantToOrganisation(CourtNavApplicant courtNavApplicant) {
        if ( courtNavApplicant == null ) {
            return null;
        }

        Organisation.OrganisationBuilder organisation = Organisation.builder();

        organisation.organisationName( courtNavApplicant.getSolicitorFirmName() );

        return organisation.build();
    }
}

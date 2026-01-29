package uk.gov.hmcts.reform.prl.mapper.courtnav;

import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRespondent;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-21T11:01:27+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.9 (Amazon.com Inc.)"
)
@Component
public class CourtNavRespondentMapperImpl implements CourtNavRespondentMapper {

    @Autowired
    private CourtNavAddressMapper courtNavAddressMapper;

    @Override
    public PartyDetails map(CourtNavRespondent respondent) {
        if ( respondent == null ) {
            return null;
        }

        PartyDetails.PartyDetailsBuilder partyDetails = PartyDetails.builder();

        partyDetails.dateOfBirth( mapCourtNavDate( respondent.getDateOfBirth() ) );
        partyDetails.isDateOfBirthKnown( isNotNull( respondent.getDateOfBirth() ) );
        partyDetails.canYouProvideEmailAddress( isNotNull( respondent.getEmail() ) );
        partyDetails.canYouProvidePhoneNumber( isNotNull( respondent.getPhoneNumber() ) );
        partyDetails.isCurrentAddressKnown( isNotNull( respondent.getAddress() ) );
        partyDetails.respondentLivedWithApplicant( booleanToYesOrNo( respondent.isRespondentLivesWithApplicant() ) );
        partyDetails.firstName( respondent.getFirstName() );
        partyDetails.lastName( respondent.getLastName() );
        partyDetails.previousName( respondent.getPreviousName() );
        partyDetails.phoneNumber( respondent.getPhoneNumber() );
        partyDetails.email( respondent.getEmail() );
        partyDetails.address( courtNavAddressMapper.map( respondent.getAddress() ) );

        partyDetails.partyId( java.util.UUID.randomUUID() );

        return partyDetails.build();
    }
}

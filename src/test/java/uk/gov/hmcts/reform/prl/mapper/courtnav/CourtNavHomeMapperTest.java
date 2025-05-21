package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ChildAtAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavHome;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ContractEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.CurrentResidentAtAddressEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.FamilyHomeOutcomeEnum.respondentToPayRentMortgage;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.LivingSituationOutcomeEnum.stayInHome;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreviousOrIntendedResidentAtAddressEnum.applicant;

class CourtNavHomeMapperTest {

    private CourtNavHomeMapper homeMapper;

    @BeforeEach
    void setUp() {
        CourtNavAddressMapper addressMapper = Mappers.getMapper(CourtNavAddressMapper.class);
        homeMapper = new CourtNavHomeMapper(addressMapper);
    }

    @Test
    void shouldMapFullCourtNavHomeDetails() {
        CourtNavAddress address = CourtNavAddress.builder()
            .addressLine1("1 Main St")
            .postTown("Town")
            .postCode("TS1 1AA")
            .build();

        CourtNavHome courtNavHome = CourtNavHome.builder()
            .occupationOrderAddress(address)
            .currentlyLivesAtAddress(List.of(CurrentResidentAtAddressEnum.other))
            .currentlyLivesAtAddressOther("Other")
            .previouslyLivedAtAddress(applicant)
            .intendedToLiveAtAddress(applicant)
            .childrenApplicantResponsibility(List.of(ChildAtAddress.builder().fullName("Child 1").age(5).build()))
            .propertySpeciallyAdapted(true)
            .propertySpeciallyAdaptedDetails("ramp access")
            .propertyHasMortgage(true)
            .namedOnMortgage(List.of(ContractEnum.applicant))
            .mortgageLenderName("Bank")
            .mortgageNumber("M12345")
            .mortgageLenderAddress(address)
            .propertyIsRented(true)
            .namedOnRentalAgreement(List.of(ContractEnum.applicant))
            .landlordName("Landlord")
            .landlordAddress(address)
            .haveHomeRights(true)
            .wantToHappenWithLivingSituation(List.of(stayInHome))
            .wantToHappenWithFamilyHome(List.of(respondentToPayRentMortgage))
            .anythingElseForCourtToConsider("Court notes")
            .build();

        Home result = homeMapper.mapHome(courtNavHome);

        assertEquals("1 Main St", result.getAddress().getAddressLine1());
        assertEquals("Other", result.getTextAreaSomethingElse());
        assertEquals(Yes, result.getIsPropertyAdapted());
        assertEquals("ramp access", result.getHowIsThePropertyAdapted());
        assertEquals("Bank", result.getMortgages().getMortgageLenderName());
        assertEquals("Landlord", result.getLandlords().getLandlordName());
        assertEquals(Yes, result.getDoAnyChildrenLiveAtAddress());
        assertEquals("Court notes", result.getFurtherInformation());
    }

    @Test
    void shouldHandleNullOptionalFieldsSafely() {
        CourtNavHome courtNavHome = CourtNavHome.builder()
            .propertyIsRented(false)
            .propertyHasMortgage(false)
            .childrenApplicantResponsibility(null)
            .childrenSharedResponsibility(null)
            .build();

        Home result = homeMapper.mapHome(courtNavHome);

        assertNull(result.getLandlords());
        assertNull(result.getMortgages());
        assertEquals(No, result.getDoAnyChildrenLiveAtAddress());
    }
}

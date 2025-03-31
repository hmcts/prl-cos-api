package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChildTest {

    @Test
    void shouldBuildChildWithTrimmedAndPopulatedFields() {
        WhoDoesTheChildLiveWith livesWith = WhoDoesTheChildLiveWith.builder()
            .partyId("  123 ")
            .partyFullName(" John Doe ")
            .partyType(PartyTypeEnum.APPLICANT)
            .build();

        LocalDate dob = LocalDate.of(2015, 6, 1);

        Child child = Child.builder()
            .firstName("  Alice  ")
            .lastName("  Smith  ")
            .gender(Gender.female)
            .dateOfBirth(dob)
            .otherGender("  Non-binary ")
            .orderAppliedFor(List.of(OrderTypeEnum.childArrangementsOrder))
            .otherApplicantsRelationshipToChild("  Parent  ")
            .otherRespondentsRelationshipToChild("  Aunt  ")
            .parentalResponsibilityDetails("  Some detailed info  ")
            .whoDoesTheChildLiveWith(livesWith)
            .build();

        assertThat(child.getFirstName()).isEqualTo("Alice");
        assertThat(child.getLastName()).isEqualTo("Smith");
        assertThat(child.getOtherGender()).isEqualTo("Non-binary");
        assertThat(child.getGender()).isEqualTo(Gender.female);
        assertThat(child.getDateOfBirth()).isEqualTo(dob);
        assertThat(child.getOrderAppliedFor()).containsExactly(OrderTypeEnum.childArrangementsOrder);
        assertThat(child.getOtherApplicantsRelationshipToChild()).isEqualTo("Parent");
        assertThat(child.getOtherRespondentsRelationshipToChild()).isEqualTo("Aunt");
        assertThat(child.getParentalResponsibilityDetails()).isEqualTo("Some detailed info");
        assertThat(child.getWhoDoesTheChildLiveWith()).isEqualTo(livesWith);
    }

    @Test
    void shouldIgnoreBlankStringsAndConvertThemToNull() {
        Child child = Child.builder()
            .firstName("   ")
            .lastName("")
            .otherGender("   ")
            .otherApplicantsRelationshipToChild(null)
            .parentalResponsibilityDetails(" \n\t ")
            .build();

        assertThat(child.getFirstName()).isNull();
        assertThat(child.getLastName()).isNull();
        assertThat(child.getOtherGender()).isNull();
        assertThat(child.getOtherApplicantsRelationshipToChild()).isNull();
        assertThat(child.getParentalResponsibilityDetails()).isNull();
    }
}

package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChildTest {

    @Test
    void shouldBuildChildWithCleanedStringValues() {
        Child child = Child.builder()
            .firstName("  Ella  ")
            .lastName("  Smith ")
            .otherGender("  Other  ") // this is a free-text field
            .otherApplicantsRelationshipToChild("  Aunt  ")
            .otherRespondentsRelationshipToChild("  Foster Parent  ")
            .parentalResponsibilityDetails("  Shared with grandmother  ")
            .gender(Gender.other)
            .dateOfBirth(LocalDate.of(2015, 5, 15))
            .orderAppliedFor(List.of(OrderTypeEnum.childArrangementsOrder))
            .build();

        assertThat(child.getFirstName()).isEqualTo("Ella");
        assertThat(child.getLastName()).isEqualTo("Smith");
        assertThat(child.getOtherGender()).isEqualTo("Other");
        assertThat(child.getOtherApplicantsRelationshipToChild()).isEqualTo("Aunt");
        assertThat(child.getOtherRespondentsRelationshipToChild()).isEqualTo("Foster Parent");
        assertThat(child.getParentalResponsibilityDetails()).isEqualTo("Shared with grandmother");
        assertThat(child.getGender()).isEqualTo(Gender.other);
        assertThat(child.getDateOfBirth()).isEqualTo(LocalDate.of(2015, 5, 15));
    }

    @Test
    void shouldCleanBlankAndNullFieldsToNull() {
        Child child = Child.builder()
            .firstName("   ")
            .lastName(null)
            .otherGender("")
            .otherApplicantsRelationshipToChild("  ")
            .otherRespondentsRelationshipToChild(null)
            .parentalResponsibilityDetails(" ")
            .build();

        assertThat(child.getFirstName()).isNull();
        assertThat(child.getLastName()).isNull();
        assertThat(child.getOtherGender()).isNull();
        assertThat(child.getOtherApplicantsRelationshipToChild()).isNull();
        assertThat(child.getOtherRespondentsRelationshipToChild()).isNull();
        assertThat(child.getParentalResponsibilityDetails()).isNull();
    }

    @Test
    void hasConfidentialInfoShouldReturnTrueIfAddressIsConfidential() {
        Child child = Child.builder()
            .isChildAddressConfidential(YesOrNo.Yes)
            .build();

        assertThat(child.hasConfidentialInfo()).isTrue();
    }

    @Test
    void hasConfidentialInfoShouldReturnFalseIfAddressIsNotConfidential() {
        Child child = Child.builder()
            .isChildAddressConfidential(YesOrNo.No)
            .build();

        assertThat(child.hasConfidentialInfo()).isFalse();
    }

    @Test
    void hasConfidentialInfoShouldReturnFalseIfValueIsNull() {
        Child child = Child.builder().build();

        assertThat(child.hasConfidentialInfo()).isFalse();
    }
}

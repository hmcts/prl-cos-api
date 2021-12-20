package uk.gov.hmcts.reform.prl.services.validators;


import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.Address;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allEmpty;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.isEmptyAddress;

class EventCheckerHelperTest {

    @Nested
    class AllNonEmpty {

        @Test
        void testEmptyArguments() {
            assertThat(allNonEmpty()).isTrue();
        }

        @Test
        void testOneEmpty() {
            assertThat(allNonEmpty("")).isFalse();
        }

        @Test
        void testOneNull() {
            assertThat(allNonEmpty(new Object[]{null})).isFalse();
        }

        @Test
        void testMultipleAndOneEmpty() {
            assertThat(allNonEmpty("", "x")).isFalse();
        }

        @Test
        void testNonEmpty() {
            assertThat(allNonEmpty("x")).isTrue();
        }

    }

    @Nested
    class EmptyAddress {

        @Test
        void testEmptyAddress() {

            Address address = Address.builder().build();
            assertThat(isEmptyAddress(address)).isTrue();
        }

        @Test
        void testPartialAddress() {
            Address address = Address.builder()
                .postTown("London")
                .build();

            assertThat(isEmptyAddress(address)).isFalse();
        }
    }

    @Nested
    class AllEmpty {

        @Test
        void testEmptyArguments() {
            assertThat(allEmpty()).isTrue();
        }

        @Test
        void testOneEmpty() {
            assertThat(allEmpty("")).isTrue();
        }

        @Test
        void testOneNull() {
            assertThat(allEmpty(new Object[]{null})).isTrue();
        }


    }

}

package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.court.PrlCourtMapping;
import uk.gov.hmcts.reform.prl.utils.csv.CsvReader;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class PrlCourtConfigServiceTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Spy
    private CsvReader csvReader = new CsvReader();

    @InjectMocks
    private PrlCourtConfigService prlCourtConfigService;


    @Nested
    class PathfinderConfig {
        @Test
        void shouldReturnTrueIfPathfinderCourt() {
            Optional<PrlCourtMapping> result = prlCourtConfigService.getPathFinderMappingByCourtField("234946");

            assertThat(result.isPresent(), is(true));
            assertThat(result.get().getCourtField(), is("swanseaDFJCourt"));
        }

        @Test
        void shouldReturnEmptyWhenCourtFieldDoesNotExist() {
            Optional<PrlCourtMapping> result = prlCourtConfigService.getPathFinderMappingByCourtField("nonexistent");

            assertThat(result.isPresent(), is(false));
        }

        @Test
        void shouldReturnEmptyWhenCourtFieldIsNull() {
            Optional<PrlCourtMapping> result = prlCourtConfigService.getPathFinderMappingByCourtField(null);

            assertThat(result.isPresent(), is(false));
        }
    }

    @Nested
    class DfjAreas {

        @Test
        void shouldGetDfjAreaFieldsToBeUpdatedIfExists() {
            Map<String, String> fields = prlCourtConfigService.getDfjAreaFieldsByCourtId("234946");
            Assertions.assertThat(fields)
                .containsAllEntriesOf(Map.of("dfjArea", "SWANSEA", "swanseaDFJCourt", "234946"));
        }

        @Test
        void shouldGetDfjAreaFieldsToBeUpdatedByNameIfExists() {
            Map<String, String> fields = prlCourtConfigService.getDfjAreaFieldsByCourtName("Chelmsford Justice Centre");
            Assertions.assertThat(fields).containsAllEntriesOf(Map.of("dfjArea", "ESSEX_AND_SUFFOLK",
                                                                      "essexAndSuffolkDFJCourt", "816875"));
        }

        @Test
        void shouldGetEmptyMapIfDfjAreaDoesNotExistById() {
            Map<String, String> fields = prlCourtConfigService.getDfjAreaFieldsByCourtId("000000");
            Assertions.assertThat(fields).isEmpty();
        }

        @Test
        void shouldGetEmptyMapIfDfjAreaDoesNotExistByName() {
            Map<String, String> fields = prlCourtConfigService.getDfjAreaFieldsByCourtName("???");
            Assertions.assertThat(fields).isEmpty();
        }

        @Test
        void shouldGetAllCourtFields() {
            Assertions.assertThat(prlCourtConfigService.getAllCourtFields())
                .containsExactlyInAnyOrder("swanseaDFJCourt", "essexAndSuffolkDFJCourt");
        }
    }
}

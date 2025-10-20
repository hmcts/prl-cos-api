package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DfjLookupServiceTest {

    private DfjLookupService dfjLookupService;

    @BeforeEach
    void setup() {
        dfjLookupService = new DfjLookupService(new ObjectMapper());
    }

    @Test
    void shouldGetDfjAreaFieldsToBeUpdatedIfExists() {
        Map<String, String> fields = dfjLookupService.getDfjAreaFieldsByCourtId("234946");
        assertThat(fields)
            .containsAllEntriesOf(Map.of("dfjArea", "SWANSEA", "swanseaDFJCourt", "234946"));
    }

    @Test
    void shouldGetDfjAreaFieldsToBeUpdatedByNameIfExists() {
        Map<String, String> fields = dfjLookupService.getDfjAreaFieldsByCourtName("Chelmsford Justice Centre");
        assertThat(fields).containsAllEntriesOf(Map.of("dfjArea", "ESSEX_AND_SUFFOLK",
                                         "essexAndSuffolkDFJCourt", "816875"));
    }

    @Test
    void shouldGetEmptyMapIfDfjAreaDoesNotExistById() {
        Map<String, String> fields = dfjLookupService.getDfjAreaFieldsByCourtId("000000");
        assertThat(fields).isEmpty();
    }

    @Test
    void shouldGetEmptyMapIfDfjAreaDoesNotExistByName() {
        Map<String, String> fields = dfjLookupService.getDfjAreaFieldsByCourtName("???");
        assertThat(fields).isEmpty();
    }

    @Test
    void shouldGetAllCourtFields() {
        assertThat(dfjLookupService.getAllCourtFields())
            .containsExactlyInAnyOrder("swanseaDFJCourt", "essexAndSuffolkDFJCourt");
    }
}

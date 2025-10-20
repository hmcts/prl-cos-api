package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.court.DfjAreaCourtMapping;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DfjLookupServiceTest {

    private DfjLookupService dfjLookupService;

    @BeforeEach
    void setup() {
        dfjLookupService = new DfjLookupService(new ObjectMapper());
    }

    @Test
    void shouldGetDfjAreaIfExistsInConfig() {
        DfjAreaCourtMapping mapping = dfjLookupService.getDfjArea("816875").orElseThrow();
        assertThat(mapping).isEqualTo(DfjAreaCourtMapping.builder()
            .courtCode("816875")
            .courtName("Chelmsford")
            .courtField("essexAndSuffolkDFJCourt")
            .dfjArea("ESSEX_AND_SUFFOLK")
            .build());
    }

    @Test
    void shouldGetEmptyIfDfjAreaDoesNotExistInConfig() {
        assertThat(dfjLookupService.getDfjArea("000000")).isEmpty();
    }

    @Test
    void shouldGetDfjAreaFieldsToBeUpdatedIfExists() {
        Map<String, String> fields = dfjLookupService.getDfjAreaFields("234946");
        assertThat(fields)
            .containsAllEntriesOf(Map.of("dfjArea", "SWANSEA", "swanseaDFJCourt", "234946"));
    }

    @Test
    void shouldGetEmptyMapIfDfjAreaDoesNotExist() {
        Map<String, String> fields = dfjLookupService.getDfjAreaFields("000000");
        assertThat(fields).isEmpty();
    }


    @Test
    void shouldGetAllCourtFields() {
        assertThat(dfjLookupService.getAllCourtFields())
            .containsExactlyInAnyOrder("swanseaDFJCourt", "essexAndSuffolkDFJCourt");
    }
}

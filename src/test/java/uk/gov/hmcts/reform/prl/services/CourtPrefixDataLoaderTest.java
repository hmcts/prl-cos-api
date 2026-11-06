package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.utils.csv.CsvReader;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtPrefixDataLoaderTest {

    @Mock
    private CsvReader csvReader;

    private CourtPrefixDataLoader dataLoader;

    @BeforeEach
    void setUp() {
        dataLoader = new CourtPrefixDataLoader(csvReader);

        when(csvReader.read(anyString())).thenReturn(List.of(Map.of("EpimmsId", "123",
                                                                    "Prefix", "WV")));

    }

    @Test
    void shouldReturnPrefixForGivenEpimmsId() {
        String prefix = dataLoader.getPrefixForGivenEpimmsId("123");
        assertEquals("WV", prefix);
    }

    @Test
    void shouldReturnNullForEpigramsIdNotPresentInCsv() {
        String prefix = dataLoader.getPrefixForGivenEpimmsId("234");
        assertNull(prefix);
    }
}

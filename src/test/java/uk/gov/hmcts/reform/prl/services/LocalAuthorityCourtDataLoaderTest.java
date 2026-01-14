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
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalAuthorityCourtDataLoaderTest {

    @Mock
    private CsvReader csvReader;

    private LocalAuthorityCourtDataLoader dataLoader;

    @BeforeEach
    void setUp() {
        dataLoader = new LocalAuthorityCourtDataLoader(csvReader);

        when(csvReader.read(anyString())).thenReturn(List.of(Map.of("Local Authorities", "Central Court",
                                                             "Local Custodian code", "123")));

    }

    @Test
    void shouldGetLocalAuthorityCourtList() {

        String localAuthority = dataLoader.getLocalAuthorityCourtList().getFirst().getLocalAuthority();
        String custodianCode = dataLoader.getLocalAuthorityCourtList().getFirst().getLocalCustodianCode();
        assertEquals("Central Court", localAuthority);
        assertEquals("123", custodianCode);
    }

}

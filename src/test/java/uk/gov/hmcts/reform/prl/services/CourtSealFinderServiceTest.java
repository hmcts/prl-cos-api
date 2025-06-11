package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
public class CourtSealFinderServiceTest {


    @InjectMocks
    CourtSealFinderService courtSealFinderService;

    @Test
    public void testSeal() {
        assertNotNull(courtSealFinderService.getCourtSeal("test"));
    }
}

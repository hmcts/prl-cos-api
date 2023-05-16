package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith(MockitoJUnitRunner.class)
public class CourtSealFinderServiceTest {


    @InjectMocks
    CourtSealFinderService courtSealFinderService;

    @Test
    public void testSeal() {
        assertNotNull(courtSealFinderService.getCourtSeal("test"));
    }
}
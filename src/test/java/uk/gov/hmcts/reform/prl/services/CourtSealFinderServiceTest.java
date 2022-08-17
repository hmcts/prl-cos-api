package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;



@RunWith(MockitoJUnitRunner.class)
public class CourtSealFinderServiceTest {


    @InjectMocks
    CourtSealFinderService courtSealFinderService;

    @Test
    public void isWelshSeal() {
        ReflectionTestUtils.setField(courtSealFinderService,"welshCourtCodes",new ArrayList<>());
        Assertions.assertFalse(courtSealFinderService.isWelshSeal("test"));
    }
}
package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormattedTitleDisplayServiceTest {
    @InjectMocks
    FormattedTitleDisplayService formattedTitleDisplayService;
    @Mock
    FeatureToggleService featureToggleService;

    @Test
    void shouldFormatTitleBeforeRelease() {
        when(featureToggleService.isCaseIssuedAfterReleaseDate(any())).thenReturn(false);
        assertEquals("## appName\n ## FamilyMan ID: familyManId\n ## Casenumber: 1234567890123456",
                     formattedTitleDisplayService.getFormattedTitleDisplay("appName", "internalName","familyManId",
                                                                           1234567890123456L, LocalDate.now()));
    }

    @Test
    void shouldFormatTitleAfterRelease() {
        when(featureToggleService.isCaseIssuedAfterReleaseDate(any())).thenReturn(true);
        assertEquals("## internalName\n ## Casenumber: 1234567890123456", formattedTitleDisplayService
            .getFormattedTitleDisplay("appName", "internalName","familyManId", 1234567890123456L, LocalDate.now()
            ));

    }

}

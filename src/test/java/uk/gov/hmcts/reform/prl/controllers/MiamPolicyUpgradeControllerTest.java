package uk.gov.hmcts.reform.prl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeService;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiamPolicyUpgradeControllerTest {

    @InjectMocks
    MiamPolicyUpgradeController miamPolicyUpgradeController;

    @Mock
    AuthorisationService authorisationService;

    @Mock
    MiamPolicyUpgradeService miamPolicyUpgradeService;

    @Test
    void testSubmitMiamPolicyUpgrade() {
        when(authorisationService.isAuthorized("test", "test")).thenReturn(true);
        when(miamPolicyUpgradeService
            .populateAmendedMiamPolicyUpgradeDetails(CallbackRequest.builder().build())).thenReturn(new HashMap<>());
        assertNotNull(miamPolicyUpgradeController
            .submitMiamPolicyUpgrade("test", "test", CallbackRequest.builder().build()));
    }

    @Test
    void testSubmitMiamPolicyUpgradeInvalidAuthorisation() {
        when(authorisationService.isAuthorized("test", "test")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            miamPolicyUpgradeController
                .submitMiamPolicyUpgrade("test", "test", CallbackRequest.builder().build());
        });

        assertEquals("Invalid Client", ex.getMessage());
    }
}

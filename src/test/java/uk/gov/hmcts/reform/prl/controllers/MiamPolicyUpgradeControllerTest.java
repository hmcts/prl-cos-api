package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeService;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MiamPolicyUpgradeControllerTest {

    @InjectMocks
    MiamPolicyUpgradeController miamPolicyUpgradeController;

    @Mock
    AuthorisationService authorisationService;

    @Mock
    MiamPolicyUpgradeService miamPolicyUpgradeService;

    @Test
    public void testSubmitMiamPolicyUpgrade() {
        when(authorisationService.isAuthorized("test", "test")).thenReturn(true);
        when(miamPolicyUpgradeService
            .populateAmendedMiamPolicyUpgradeDetails(CallbackRequest.builder().build())).thenReturn(new HashMap<>());
        Assert.assertNotNull(miamPolicyUpgradeController
            .submitMiamPolicyUpgrade("test", "test", CallbackRequest.builder().build()));
    }

    @Test
    public void testSubmitMiamPolicyUpgradeInvalidAuthorisation() {
        when(authorisationService.isAuthorized("test", "test")).thenReturn(false);
        assertExpectedException(() -> {
            miamPolicyUpgradeController
                .submitMiamPolicyUpgrade("test", "test", CallbackRequest.builder().build());
            },
                                RuntimeException.class, "Invalid Client"
        );
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}

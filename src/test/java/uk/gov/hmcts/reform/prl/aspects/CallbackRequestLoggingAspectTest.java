package uk.gov.hmcts.reform.prl.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallbackRequestLoggingAspectTest {

    @Mock
    private JoinPoint joinPoint;
    @Mock
    private MethodSignature methodSignature;
    @Mock
    private CallbackRequest callbackRequest;
    @Mock
    private CaseDetails caseDetails;

    private CallbackRequestLoggingAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new CallbackRequestLoggingAspect();
    }

    @Test
    void logCallbackRequest_logsCaseId_whenCallbackRequestPresent() throws NoSuchMethodException {
        // Arrange: use a real method with CallbackRequest as parameter
        Method method = this.getClass().getDeclaredMethod("dummyMethod", CallbackRequest.class, String.class);
        Object[] args = new Object[]{callbackRequest, "test"};

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(12345L);
        when(methodSignature.getDeclaringType()).thenReturn(this.getClass());
        when(methodSignature.getName()).thenReturn("dummyMethod");

        // Act & Assert
        assertDoesNotThrow(() -> aspect.logCallbackRequest(joinPoint));
    }

    @Test
    void logCallbackRequest_doesNothing_whenNoParameters() throws NoSuchMethodException {
        // Arrange: method with no parameters
        Method method = this.getClass().getDeclaredMethod("noParamMethod");
        Object[] args = new Object[]{};

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        // No further stubbing needed, as aspect will return before calling other methods

        // Act & Assert
        assertDoesNotThrow(() -> aspect.logCallbackRequest(joinPoint));
    }

    @Test
    void logCallbackRequest_doesNothing_whenNoCallbackRequestParameter() throws NoSuchMethodException {
        // Arrange: method with parameters, but none are CallbackRequest
        Method method = this.getClass().getDeclaredMethod("noCallbackRequestParamMethod", String.class, Integer.class);
        Object[] args = new Object[]{"test", 42};

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        // No further stubbing needed, as aspect will return before calling other methods

        // Act & Assert
        assertDoesNotThrow(() -> aspect.logCallbackRequest(joinPoint));
    }

    // Used for reflection in test
    private void dummyMethod(CallbackRequest callbackRequest, String other) {
    }

    private void noParamMethod() {
    }

    private void noCallbackRequestParamMethod(String a, Integer b) {
    }
}

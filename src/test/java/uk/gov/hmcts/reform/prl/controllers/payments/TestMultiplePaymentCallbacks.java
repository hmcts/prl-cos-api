package uk.gov.hmcts.reform.prl.controllers.payments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.controllers.ServiceRequestUpdateCallbackController;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.RequestUpdateCallbackService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class TestMultiplePaymentCallbacks {
    @InjectMocks
    private ServiceRequestUpdateCallbackController controller;

    @Mock
    private RequestUpdateCallbackService requestUpdateCallbackService;
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private LaunchDarklyClient launchDarklyClient;
    // AbstractCallbackController dependency
    @Mock
    private EventService eventService;

    private ServiceRequestUpdateDto body;

    @BeforeEach
    void setUp() {
        body = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber("1767185179654843")
            .serviceRequestReference("SR-REPRO-0001")
            .serviceRequestAmount("1000")
            .serviceRequestStatus("Paid")
            .build();

        // Keep S2S OFF here (simplifies test - controller won’t validate S2S token)
        lenient().when(launchDarklyClient.isFeatureEnabled(any())).thenReturn(false);
        lenient().when(authorisationService.authoriseService(any())).thenReturn(true);
    }

    @Test
    void preFix_threeConcurrentCalls_oneSuccess_twoWorkflowExceptions() throws Exception {
        // First invocation succeeds; subsequent ones simulate a downstream failure -> controller wraps as WorkflowException
        AtomicInteger call = new AtomicInteger(0);
        doAnswer(inv -> {
            if (call.incrementAndGet() == 1) {
                return null; // first request succeeds
            }
            throw new RuntimeException("simulated CCD conflict");
        }).when(requestUpdateCallbackService).processCallback(any(ServiceRequestUpdateDto.class));

        final int threads = 3;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        List<String> outcomes = Collections.synchronizedList(new ArrayList<>());

        Runnable task = () -> {
            try {
                start.await();
                try {
                    controller.serviceRequestUpdate("Bearer s2s", body);
                    outcomes.add("OK");
                } catch (WorkflowException wf) {
                    outcomes.add("WF");
                } catch (Exception ex) {
                    outcomes.add("EX");
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                outcomes.add("INT");
            } finally {
                done.countDown();
            }
        };

        new Thread(task, "req-1").start();
        new Thread(task, "req-2").start();
        new Thread(task, "req-3").start();

        start.countDown();
        boolean finished = done.await(7, TimeUnit.SECONDS);
        assertThat(finished).isTrue();

        long ok = outcomes.stream().filter("OK"::equals).count();
        long wf = outcomes.stream().filter("WF"::equals).count();
        long ex = outcomes.stream().filter("EX"::equals).count();

        // Pre-fix reproduction: exactly one success; two failures wrapped to WorkflowException
        assertThat(ok).isEqualTo(1);
        assertThat(wf + ex).isEqualTo(2);

        verify(requestUpdateCallbackService, times(3)).processCallback(any(ServiceRequestUpdateDto.class));
    }

    @Test
    void s2sHeaderWithoutBearer_isAccepted() throws Exception {
        // Mirrors existing behaviour: controller prepends "Bearer " if missing when the LD flag is ON;
        // here LD is OFF, so it passes through anyway and still calls the service.
        controller.serviceRequestUpdate("s2s", body);
        verify(requestUpdateCallbackService).processCallback(body);
    }
}

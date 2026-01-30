package uk.gov.hmcts.reform.prl.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.lang.reflect.Parameter;

@Aspect
@Component
@Slf4j
public class CallbackRequestLoggingAspect {

    @Before("within(@org.springframework.web.bind.annotation.RestController *) && execution(* *(..))")
    public void logCallbackRequest(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType().equals(CallbackRequest.class)) {
                CallbackRequest callbackRequest = (CallbackRequest) args[i];
                Long caseId = callbackRequest.getCaseDetails().getId();
                log.info("===> {}.{} callback request for case {}", signature.getDeclaringType().getSimpleName(),
                         signature.getName(), caseId);

                return;
            }
        }
    }
}

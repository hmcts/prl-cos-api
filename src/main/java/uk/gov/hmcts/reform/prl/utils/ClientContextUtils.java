package uk.gov.hmcts.reform.prl.utils;

public class ClientContextUtils {


    public static String extractTaskTriggeredByFromClientContext(String clientContext) {
        if (clientContext == null || clientContext.isBlank()) {
            return null;
        }
        return CaseUtils.getTaskTriggeredBy(CaseUtils.getWaMapper(clientContext));
    }


    public static String extractHearingIdFromClientContext(String clientContext) {
        if (clientContext == null || clientContext.isBlank()) {
            return null;
        }
        return CaseUtils.getHearingId(CaseUtils.getWaMapper(clientContext));
    }
}

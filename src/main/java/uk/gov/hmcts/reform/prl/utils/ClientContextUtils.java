package uk.gov.hmcts.reform.prl.utils;

public class ClientContextUtils {


    public static String extractHearingIdFromClientContext(String clientContext) {
        if (clientContext == null || clientContext.isBlank()) {
            return null;
        }
        return CaseUtils.getHearingId(CaseUtils.getWaMapper(clientContext));
    }
}

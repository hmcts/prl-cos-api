package uk.gov.hmcts.reform.prl.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SslVerificationDisabler {

    public static void turnOffSslVerification() throws KeyManagementException, NoSuchAlgorithmException {
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    /* no check actually performed to suppress errors for self-signed certs */
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    /* no check actually performed to suppress errors for self-signed certs */
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
        };

        SSLContext sc = SSLContext.getInstance("SSLv3");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
}

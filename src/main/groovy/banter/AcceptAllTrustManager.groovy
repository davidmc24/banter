package banter

import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

class AcceptAllTrustManager implements X509TrustManager {

    @Override
    void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
    }

    @Override
    void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
    }

    @Override
    X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0]
    }

}

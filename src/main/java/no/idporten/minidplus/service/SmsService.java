package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.minidplus.config.SmsProperties;
import no.idporten.minidplus.domain.SmsMessage;
import no.idporten.minidplus.exception.minid.MinIDSystemException;
import org.apache.http.HttpHost;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.springframework.stereotype.Service;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;

import static no.idporten.minidplus.exception.IDPortenExceptionID.SMS_PSWINCOM_GATEWAY_UNREACHABLE;
import static no.idporten.minidplus.exception.IDPortenExceptionID.SMS_PSWINCOM_SEND_FAILED;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private static final int HTTPS_PORT = 443;
    private static final int MINID_CONNECTION_TIMEOUT = 2000;
    private static final int MINID_READ_TIMEOUT = 10000;

    private final SmsProperties smsProperties;
    private final PSWinComUtils psWinComUtils;


    public void sendSms(final SmsMessage sms) throws MinIDSystemException, IOException {
        if (log.isInfoEnabled()) {
            log.info("Starting sending sms to " + sms.getTo() + "Using the PSWinCOM xml over http(s) interface");
            log.info("Sms-tekst: " + sms.getBody());
        }

        final HttpPost post = new HttpPost(smsProperties.getPswincom().getUrl());
        StringEntity se = new StringEntity(psWinComUtils.createPSWinComXML(sms), "ISO-8859-1");
        se.setContentType("text/xml");
        post.setEntity(se);

        String responseBody = null;
        CloseableHttpClient httpclient = null;
        try {
            httpclient = getTrustingHttpClient();
            final ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpHost httpHost = new HttpHost("MinID", HTTPS_PORT, "https");
            responseBody = httpclient.execute(httpHost, post, responseHandler);
            // Check that the error value is zero
            if (responseBody != null && responseBody.contains("<STATUS>OK</STATUS>")) {
                // OK: return
                if (log.isInfoEnabled()) {
                    log.info("Sms sent successfully to " + sms.getTo());
                }
                return;
            }
        } catch (IOException ioe) {
            log.error("Error occurred sending sms", ioe);
            throw new MinIDSystemException(SMS_PSWINCOM_GATEWAY_UNREACHABLE, "Error reaching sms gateway", ioe);
        } catch (Exception ex) {
            log.error("Error occurred sending sms", ex);
            throw new MinIDSystemException(SMS_PSWINCOM_SEND_FAILED, "Error sending sms", ex);
        } finally {
            if (!(httpclient == null)) {
                httpclient.close();
            }
        }
        log.error("Failed sending SMS:" + responseBody.replaceAll("\n", ""));
        throw new MinIDSystemException(SMS_PSWINCOM_SEND_FAILED, "SMS not sent");

    }

    private CloseableHttpClient getTrustingHttpClient() {
        try {
            // First create a trust manager that won't care.
            final TrustManager easyTrustManager = new X509TrustManager() {

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String string)
                        throws java.security.cert.CertificateException {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String string)
                        throws java.security.cert.CertificateException {
                }
            };
            //PSWINCOM failed if USE_EXPECT_CONTINUE is not set to false

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(MINID_CONNECTION_TIMEOUT)
                    .setSocketTimeout(MINID_READ_TIMEOUT)
                    .setExpectContinueEnabled(false)
                    .build();
            final CloseableHttpClient httpclient = HttpClientBuilder.create()
                    .setSchemePortResolver(new DefaultSchemePortResolver())
                    .setDefaultRequestConfig(requestConfig)
                    .build();
            return httpclient;
        } catch (Exception e) {
            log.error("Error getting httpclient" + e);


        }
        return null;
    }
}

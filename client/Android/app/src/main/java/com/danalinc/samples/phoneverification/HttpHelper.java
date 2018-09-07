package com.danalinc.samples.phoneverification;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpHelper extends AsyncTask<String, Void, HttpResponse> {
    private static final boolean ACCEPT_ALL_SERVER_CERTS = false;
    private static final Gson GSON = new Gson();
    private Map<String, String> parameters;
    private OnHttpCallCompleted listener;
    private Exception exception;
    private OkHttpClient client = new OkHttpClient.Builder().build();

    @Override
    protected HttpResponse doInBackground(String... strings) {
        String url = strings[0];
        String debug = strings[1];
        try {
            //enable this during testing against a server with a self-signed cert
            if (ACCEPT_ALL_SERVER_CERTS) {
                // Create a trust manager that does not validate certificate chains
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };

                // Install the all-trusting trust manager
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                builder.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                client = builder.build();
            }

            //append parameters to URL
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (!url.contains("?")) {
                    url += "?";
                } else {
                    url += "&";
                }
                url +=  entry.getKey() + '=' + URLEncoder.encode(entry.getValue(), "UTF-8");
            }

            //headers
            Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put("Accept", "application/json");
            requestProperties.put("Content-Type", "application/json");
            Headers headers = Headers.of(requestProperties);

            System.out.println("HttpHelper.doInBackground: " + debug + ", url=" + url + ", headers=" + headers);

            //make the call
            Request request = new Request.Builder()
                    .url(url)
                    .headers(headers)
                    .build();
            Response response = client.newCall(request).execute();

            //create the response
            Map<String, Object> responseMap = null;
            responseMap = GSON.fromJson(new String(response.body().bytes(), "UTF-8"), Map.class);
            return HttpResponse.builder()
                    .debug(debug)
                    .response(responseMap)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            this.exception = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(HttpResponse response) {
        if (listener != null) {
            if (response != null) {
                listener.onHttpCallCompleted(response);
            } else if (exception != null) {
                listener.onHttpCallCompleted(HttpResponse.builder()
                        .debug(exception.getMessage())
                        .build());
            } else {
                listener.onHttpCallCompleted(HttpResponse.builder()
                        .debug("Null response")
                        .build());
            }
        }
    }

    HttpHelper(Map<String, String> parameters, OnHttpCallCompleted listener) {
        this.parameters = parameters;
        this.listener = listener;
    }

    public static HttpHelperBuilder builder() {
        return new HttpHelperBuilder();
    }

    public static class HttpHelperBuilder {

        private Map<String, String> parameters;
        private OnHttpCallCompleted listener;

        HttpHelperBuilder() {
        }

        public HttpHelperBuilder parameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }

        public HttpHelperBuilder listener(OnHttpCallCompleted listener) {
            this.listener = listener;
            return this;
        }

        public HttpHelper build() {
            return new HttpHelper(parameters, listener);
        }

        @Override
        public String toString() {
            return "HttpHelperBuilder{" + "parameters=" + parameters + ", listener=" + listener + '}';
        }
    }
}


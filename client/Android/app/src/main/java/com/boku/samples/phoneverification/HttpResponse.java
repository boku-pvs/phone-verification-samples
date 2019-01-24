package com.boku.samples.phoneverification;

import java.util.Map;

class HttpResponse {
    String debug;
    Map<String, Object> response;


    HttpResponse(String debug, Map<String, Object> response) {
        this.debug = debug;
        this.response = response;
    }

    public static HttpResponseBuilder builder() {
        return new HttpResponseBuilder();
    }

    public static class HttpResponseBuilder {

        String debug;
        Map<String, Object> response;

        HttpResponseBuilder() {
        }

        public HttpResponseBuilder debug(String debug) {
            this.debug = debug;
            return this;
        }

        public HttpResponseBuilder response(Map<String, Object> response) {
            this.response = response;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(debug, response);
        }

        @Override
        public String toString() {
            return "HttpResponseBuilder{" + "debug=" + debug + ", response=" + response + '}';
        }
    }
}

package com.hyutils.core.utils.http;

import com.hyutils.core.utils.Json;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.protocol.Protocol;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class SendReq {


    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }

        return false;
    }

    private static HttpClient createHttpClient() {
        Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", easyhttps);
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(1000);
        connectionManager.getParams().setMaxTotalConnections(1000);
        connectionManager.getParams().setConnectionTimeout(10 * 1000);
        HttpClient httpclient = new HttpClient(connectionManager);
        return httpclient;
    }

    public static InputStream downloaddReq(String url, String method, Map<String, Object> params, Map<String, String> requestHeaders) {
        GetMethod httpget = null;
        HttpClient httpClient = null;
        int code = 0;
        String tmpUrl = url;
        if (isChinese(tmpUrl)) {
            url = "";
            for (int i = 0; i < tmpUrl.length(); i++) {
                String tmp = tmpUrl.charAt(i) + "";
                if (isChinese(tmp)) {
                    try {
                        tmp = URLEncoder.encode(tmp, "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                url = url + tmp;
            }
        }
        try {
            String line;
           if (method.equalsIgnoreCase("GET")) {
                httpget = new GetMethod(url);
                httpget.setFollowRedirects(true);
                for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
                    httpget.setRequestHeader(requestHeader.getKey(), requestHeader.getValue());
                }
                httpClient = createHttpClient();
                code = httpClient.executeMethod(httpget);
                if(code == 200){
                    return httpget.getResponseBodyAsStream();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static ResBody sendReq(String url, String method, Map<String, Object> params, Map<String, String> requestHeaders) {
        GetMethod httpget = null;
        HeadMethod httphead = null;
        PostMethod httpPost = null;
        DeleteMethod httpDelete = null;
        PutMethod httpPut = null;
        HttpClient httpClient = null;
        String rawHeader = "";
        String responce = "";
        String rawResponce = "";
        String responseHeader = "";
        String responseBody = "";
        int code = 0;
        String tmpUrl = url;
        if (isChinese(tmpUrl)) {
            url = "";
            for (int i = 0; i < tmpUrl.length(); i++) {
                String tmp = tmpUrl.charAt(i) + "";
                if (isChinese(tmp)) {
                    try {
                        tmp = URLEncoder.encode(tmp, "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                url = url + tmp;
            }
        }
        try {
            String line;
            if (method.equalsIgnoreCase("HEAD")) {
                httphead = new HeadMethod(url);
                for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
                    httphead.setRequestHeader(requestHeader.getKey(), requestHeader.getValue());
                }
                httphead.setFollowRedirects(true);
                httpClient = createHttpClient();
                code = httpClient.executeMethod(httphead);
                httphead.releaseConnection();
            } else if (method.equalsIgnoreCase("GET")) {
                httpget = new GetMethod(url);
                httpget.setFollowRedirects(true);
                for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
                    httpget.setRequestHeader(requestHeader.getKey(), requestHeader.getValue());
                }
                httpClient = createHttpClient();
                code = httpClient.executeMethod(httpget);
                BufferedReader input = new BufferedReader(new InputStreamReader(httpget.getResponseBodyAsStream(), StandardCharsets.UTF_8));
                rawHeader = httpget.getStatusLine() + "\r\n";
                Header[] headers = httpget.getResponseHeaders();
                StringBuffer buf = new StringBuffer();
                for (int a = 0; a < headers.length; ++a) {
                    buf.append(headers[a].getName() + ": " + headers[a].getValue() + "\r\n");
                }
                rawHeader = rawHeader + buf.toString();
                responseHeader = rawHeader;
                buf = new StringBuffer();

                while ((line = input.readLine()) != null) {
                    buf.append("\r\n" + line);
                }

                responce = buf.toString();
                responseBody = responce;
                input.close();
                rawResponce = rawHeader + responce;
                // TODO: 2021/3/17 responce 就是最后的值
                Header contentType = httpget.getResponseHeader("Content-Type");
                if (contentType != null && contentType.getValue().startsWith("text")) {
                    // TODO: 2021/3/17 解析页面，默认先不解析
                }
                Thread.sleep(10L);
                httpget.releaseConnection();
            } else if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("DELETE")) {
                BufferedReader input = null;
                Header[] headers = null;
                if (method.equalsIgnoreCase("POST")) {
                    httpPost = new PostMethod(url);
                    for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
                        httpPost.setRequestHeader(requestHeader.getKey(), requestHeader.getValue());
                    }
                    String toJson = Json.toJson(params);
                    RequestEntity se = new StringRequestEntity(toJson, "application/x-www-form-urlencoded", "UTF-8");

                    httpPost.setRequestEntity(se);
                    for (Map.Entry<String,Object>x:params.entrySet()){
                        httpPost.setParameter(x.getKey(),x.getValue().toString());
                    }
                    httpClient = createHttpClient();
                    code = httpClient.executeMethod(httpPost);
                    input = new BufferedReader(new InputStreamReader(httpPost.getResponseBodyAsStream()));
                    rawHeader = httpPost.getStatusLine() + "\r\n";
                    headers = httpPost.getResponseHeaders();
                } else if (method.equalsIgnoreCase("PUT")) {
                    httpPut = new PutMethod(url);
                    for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
                        httpPut.setRequestHeader(requestHeader.getKey(), requestHeader.getValue());
                    }
//                    httpPut.setFollowRedirects(true);
                    String toJson = Json.toJson(params);
                    RequestEntity se = new StringRequestEntity(toJson, "application/json", "UTF-8");
                    httpPut.setRequestEntity(se);
                    httpClient = createHttpClient();
                    code = httpClient.executeMethod(httpPut);
                    input = new BufferedReader(new InputStreamReader(httpPut.getResponseBodyAsStream()));
                    rawHeader = httpPut.getStatusLine() + "\r\n";
                    headers = httpPut.getResponseHeaders();
                } else if (method.equalsIgnoreCase("DELETE")) {
                    httpDelete = new DeleteMethod(url);
                    for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
                        httpDelete.setRequestHeader(requestHeader.getKey(), requestHeader.getValue());
                    }
//                    httpDelete.setFollowRedirects(true);
                    httpClient = createHttpClient();
                    code = httpClient.executeMethod(httpDelete);
                    input = new BufferedReader(new InputStreamReader(httpDelete.getResponseBodyAsStream()));
                    rawHeader = httpDelete.getStatusLine() + "\r\n";
                    headers = httpDelete.getResponseHeaders();
                }
                StringBuffer buf = new StringBuffer();
                for (int a = 0; a < headers.length; ++a) {
                    buf.append(headers[a].getName() + ": " + headers[a].getValue() + "\r\n");
                }
                rawHeader = rawHeader + buf.toString();
                responseHeader = rawHeader;
                buf = new StringBuffer();

                while ((line = input.readLine()) != null) {
                    buf.append("\r\n" + line);
                }
                responce = buf.toString();
                responseBody = responce;
                input.close();
                rawResponce = rawHeader + responce;
                Thread.sleep(10L);
                if (Objects.nonNull(httpDelete)) httpDelete.releaseConnection();
                if (Objects.nonNull(httpPost)) httpPost.releaseConnection();
                if (Objects.nonNull(httpPut)) httpPut.releaseConnection();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResBody(rawHeader, responce, rawResponce, responseHeader, responseBody, code);
    }
    public static ResBody sendReq2(String url, String method, Map<String, Object> params, Map<String, String> requestHeaders) {
        GetMethod httpget = null;
        HeadMethod httphead = null;
        PostMethod httpPost = null;
        DeleteMethod httpDelete = null;
        PutMethod httpPut = null;
        HttpClient httpClient = null;
        String rawHeader = "";
        String responce = "";
        String rawResponce = "";
        String responseHeader = "";
        String responseBody = "";
        int code = 0;
        String tmpUrl = url;
        if (isChinese(tmpUrl)) {
            url = "";
            for (int i = 0; i < tmpUrl.length(); i++) {
                String tmp = tmpUrl.charAt(i) + "";
                if (isChinese(tmp)) {
                    try {
                        tmp = URLEncoder.encode(tmp, "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                url = url + tmp;
            }
        }
        try {
            String line;
            if (method.equalsIgnoreCase("HEAD")) {
                httphead = new HeadMethod(url);
                for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
                    httphead.setRequestHeader(requestHeader.getKey(), requestHeader.getValue());
                }
                httphead.setFollowRedirects(true);
                httpClient = createHttpClient();
                code = httpClient.executeMethod(httphead);
                httphead.releaseConnection();
            } else if (method.equalsIgnoreCase("GET")) {
                httpget = new GetMethod(url);
                httpget.setFollowRedirects(true);
                for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
                    httpget.setRequestHeader(requestHeader.getKey(), requestHeader.getValue());
                }
                httpClient = createHttpClient();
                code = httpClient.executeMethod(httpget);
                BufferedReader input = new BufferedReader(new InputStreamReader(httpget.getResponseBodyAsStream(), StandardCharsets.UTF_8));
                rawHeader = httpget.getStatusLine() + "\r\n";
                Header[] headers = httpget.getResponseHeaders();
                StringBuffer buf = new StringBuffer();
                for (int a = 0; a < headers.length; ++a) {
                    buf.append(headers[a].getName() + ": " + headers[a].getValue() + "\r\n");
                }
                rawHeader = rawHeader + buf.toString();
                responseHeader = rawHeader;
                buf = new StringBuffer();

                while ((line = input.readLine()) != null) {
                    buf.append("\r\n" + line);
                }

                responce = buf.toString();
                responseBody = responce;
                input.close();
                rawResponce = rawHeader + responce;
                // TODO: 2021/3/17 responce 就是最后的值
                Header contentType = httpget.getResponseHeader("Content-Type");
                if (contentType != null && contentType.getValue().startsWith("text")) {
                    // TODO: 2021/3/17 解析页面，默认先不解析
                }
                Thread.sleep(10L);
                httpget.releaseConnection();
            } else if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("DELETE")) {
                BufferedReader input = null;
                Header[] headers = null;
                if (method.equalsIgnoreCase("POST")) {
                    httpPost = new PostMethod(url);
                    for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
                        httpPost.setRequestHeader(requestHeader.getKey(), requestHeader.getValue());
                    }
                    String toJson = Json.toJson(params);
                    RequestEntity se = new StringRequestEntity(toJson, "application/json", "UTF-8");

                    httpPost.setRequestEntity(se);
//                    for (Map.Entry<String,Object>x:params.entrySet()){
//                        httpPost.setParameter(x.getKey(),x.getValue().toString());
//                    }
                    httpClient = createHttpClient();
                    code = httpClient.executeMethod(httpPost);
                    input = new BufferedReader(new InputStreamReader(httpPost.getResponseBodyAsStream()));
                    rawHeader = httpPost.getStatusLine() + "\r\n";
                    headers = httpPost.getResponseHeaders();
                } else if (method.equalsIgnoreCase("PUT")) {
                    httpPut = new PutMethod(url);
                    for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
                        httpPut.setRequestHeader(requestHeader.getKey(), requestHeader.getValue());
                    }
//                    httpPut.setFollowRedirects(true);
                    String toJson = Json.toJson(params);
                    RequestEntity se = new StringRequestEntity(toJson, "application/json", "UTF-8");
                    httpPut.setRequestEntity(se);
                    httpClient = createHttpClient();
                    code = httpClient.executeMethod(httpPut);
                    input = new BufferedReader(new InputStreamReader(httpPut.getResponseBodyAsStream()));
                    rawHeader = httpPut.getStatusLine() + "\r\n";
                    headers = httpPut.getResponseHeaders();
                } else if (method.equalsIgnoreCase("DELETE")) {
                    httpDelete = new DeleteMethod(url);
                    for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
                        httpDelete.setRequestHeader(requestHeader.getKey(), requestHeader.getValue());
                    }
//                    httpDelete.setFollowRedirects(true);
                    httpClient = createHttpClient();
                    code = httpClient.executeMethod(httpDelete);
                    input = new BufferedReader(new InputStreamReader(httpDelete.getResponseBodyAsStream()));
                    rawHeader = httpDelete.getStatusLine() + "\r\n";
                    headers = httpDelete.getResponseHeaders();
                }
                StringBuffer buf = new StringBuffer();
                for (int a = 0; a < headers.length; ++a) {
                    buf.append(headers[a].getName() + ": " + headers[a].getValue() + "\r\n");
                }
                rawHeader = rawHeader + buf.toString();
                responseHeader = rawHeader;
                buf = new StringBuffer();

                while ((line = input.readLine()) != null) {
                    buf.append("\r\n" + line);
                }
                responce = buf.toString();
                responseBody = responce;
                input.close();
                rawResponce = rawHeader + responce;
                Thread.sleep(10L);
                if (Objects.nonNull(httpDelete)) httpDelete.releaseConnection();
                if (Objects.nonNull(httpPost)) httpPost.releaseConnection();
                if (Objects.nonNull(httpPut)) httpPut.releaseConnection();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResBody(rawHeader, responce, rawResponce, responseHeader, responseBody, code);
    }

    public static class ResBody {
        private String rawHeader;
        private String responce;
        private String rawResponce;
        private String responseHeader;
        private String responseBody;
        private Integer code;

        public ResBody(String rawHeader, String responce, String rawResponce, String responseHeader, String responseBody, Integer code) {
            this.rawHeader = rawHeader;
            this.responce = responce;
            this.rawResponce = rawResponce;
            this.responseHeader = responseHeader;
            this.responseBody = responseBody;
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getRawHeader() {
            return rawHeader;
        }

        public String getRawResponce() {
            return rawResponce;
        }

        public String getResponce() {
            return responce;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public String getResponseHeader() {
            return responseHeader;
        }

        public void setRawHeader(String rawHeader) {
            this.rawHeader = rawHeader;
        }

        public void setRawResponce(String rawResponce) {
            this.rawResponce = rawResponce;
        }

        public void setResponce(String responce) {
            this.responce = responce;
        }

        public void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }

        public void setResponseHeader(String responseHeader) {
            this.responseHeader = responseHeader;
        }

    }
}

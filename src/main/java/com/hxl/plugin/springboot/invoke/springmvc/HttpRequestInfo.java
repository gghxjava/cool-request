package com.hxl.plugin.springboot.invoke.springmvc;

import com.hxl.plugin.springboot.invoke.net.FormDataInfo;

import java.util.ArrayList;
import java.util.List;

public class HttpRequestInfo {
    private UrlInfo url;
    private String requestBody;
    private String contentType;
    private List<RequestParameterDescription> headers = new ArrayList<>();
    private List<RequestParameterDescription> urlParams = new ArrayList<>();
    private List<FormDataInfo> formDataInfos = new ArrayList<>();
    private List<RequestParameterDescription> urlencodedBody = new ArrayList<>();

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public List<RequestParameterDescription> getHeaders() {
        return headers;
    }

    public void setHeaders(List<RequestParameterDescription> headers) {
        this.headers = headers;
    }

    public List<RequestParameterDescription> getUrlParams() {
        return urlParams;
    }

    public void setUrlParams(List<RequestParameterDescription> urlParams) {
        this.urlParams = urlParams;
    }

    public List<FormDataInfo> getFormDataInfos() {
        return formDataInfos;
    }

    public void setFormDataInfos(List<FormDataInfo> formDataInfos) {
        this.formDataInfos = formDataInfos;
    }

    public List<RequestParameterDescription> getUrlencodedBody() {
        return urlencodedBody;
    }

    public void setUrlencodedBody(List<RequestParameterDescription> urlencodedBody) {
        this.urlencodedBody = urlencodedBody;
    }

    public UrlInfo getUrl() {
        return url;
    }

    public void setUrl(UrlInfo url) {
        this.url = url;
    }

    public static class UrlInfo {
        private String url;
        private List<String> pathVariable;

        public UrlInfo(String url) {
            this(url, new ArrayList<>());
        }

        public UrlInfo(String url, List<String> pathVariable) {
            this.url = url;
            this.pathVariable = pathVariable;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public List<String> getPathVariable() {
            return pathVariable;
        }

        public void setPathVariable(List<String> pathVariable) {
            this.pathVariable = pathVariable;
        }
    }
}

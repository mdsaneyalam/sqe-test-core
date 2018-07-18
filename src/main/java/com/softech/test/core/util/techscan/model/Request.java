package com.softech.test.core.util.techscan.model;

import net.lightbody.bmp.core.har.HarCookie;
import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.core.har.HarPostData;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Uladzimir_Kazimirchyk on 7/15/16.
 */
public class Request {
    private String method;
    private String url;
    private List<HarCookie> cookies = new CopyOnWriteArrayList<>();
    private List<HarNameValuePair> headers = new CopyOnWriteArrayList<>();
    private List<HarNameValuePair> queryString = new CopyOnWriteArrayList<>();
    private HarPostData postData;
    private long headersSize;
    private long bodySize;

    public Request() {
    }

    public Request(String method, String url, List<HarNameValuePair> queryString, HarPostData postData, long bodySize,
            List<HarNameValuePair> headers, List<HarCookie> cookies) {
        this.method = method;
        this.url = url;
        this.cookies = cookies;
        this.headers = headers;
        this.headersSize = headers.size();
        this.postData = postData;
        this.bodySize = bodySize;
        this.queryString = queryString;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<HarCookie> getCookies() {
        return cookies;
    }

    public List<HarNameValuePair> getHeaders() {
        return headers;
    }

    public List<HarNameValuePair> getQueryString() {
        return queryString;
    }

    public HarPostData getPostData() {
        return postData;
    }

    public long getHeadersSize() {
        return headersSize;
    }

    public void setHeadersSize(long headersSize) {
        this.headersSize = headersSize;
    }

    public long getBodySize() {
        return bodySize;
    }

    public void setBodySize(long bodySize) {
        this.bodySize = bodySize;
    }

}

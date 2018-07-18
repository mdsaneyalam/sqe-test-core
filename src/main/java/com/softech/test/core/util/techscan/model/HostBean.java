package com.softech.test.core.util.techscan.model;

import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.core.har.HarPostDataParam;
import net.lightbody.bmp.core.har.HarRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bean for storing and validation of calls for TechScan test
 *
 * @author Uladzimir_Kazimirchyk
 */
public class HostBean {

    private static final String HOST_PATTERN =
            "(?:https?://)?[a-zA-Z0-9_/\\-\\.]+\\.(?:[A-Za-z/]{2,5})[a-zA-Z0-9_/&\\?=\\-\\.~%]*";
    private static final String EMAIL_PATTERN =
            "[_A-Za-z0-9-\\+]+(?:\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9]+)*(?:\\.[A-Za-z]{2,})";
    private static final String PHONE_PATTERN =
            "\\D*(([2-9]\\d{2})(-|\\s)([2-9]\\d{2})(-|\\s)(\\d{4}))\\D*";
    private static final String METHOD_POST = "POST";

    private Map<String, String> foundHosts = new HashMap<>();
    private Map<String, String> foundEmails = new HashMap<>();
    private Map<String, String> foundPhones = new HashMap<>();

    private boolean isValid;
    private String name;
    private String callType;
    private String url;
    private String approvedBy;
    private String approvedUntil;
    private String notes;
    private String description;
    private String businessgroup;

    private List<HarNameValuePair> headers;

    public HostBean(Request request) {
        this.url = request.getUrl();
        this.headers = request.getHeaders();
        findRecognizableItems(request);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getApprovedUntil() {
        return approvedUntil;
    }

    public String getNotes() {
        return notes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setApprovedUntil(String approvedUntil) {
        this.approvedUntil = approvedUntil;
    }

    public String getBusinessgroup() {
        return businessgroup;
    }

    public void setBusinessgroup(String businessgroup) {
        this.businessgroup = businessgroup;
    }

    public Map<String, String> getFoundHosts() {
        return foundHosts;
    }

    public Map<String, String> getFoundEmails() {
        return foundEmails;
    }

    public Map<String, String> getFoundPhones() {
        return foundPhones;
    }

    /**
     * Returns request headers as string
     *
     * @return {@link String}
     */
    public String getHeaders() {
        StringBuilder headersAsString = new StringBuilder("Request headers: \n");
        for (HarNameValuePair header : headers) {
            headersAsString.append(header.toString()).append("\n");
        }
        return headersAsString.toString();
    }

    /**
     * Finds any urls, email address or phone number in request
     * (query param, header pair, request body) and populates
     * correspondent fields.
     *
     * @param request {@link HarRequest}
     */
    private void findRecognizableItems(Request request) {
        List<HarNameValuePair> queryParams = request.getQueryString();
        if (queryParams.size() > 0) {
            for (HarNameValuePair queryPair : queryParams) {
                matchPatterns(queryPair);
            }
        }

        if (headers.size() > 0) {
            for (HarNameValuePair headerPair : headers) {
                matchPatterns(headerPair);
            }
        }

        if (request.getMethod().equals(METHOD_POST)) {
            if (request.getBodySize() > 0) {
                if (request.getPostData().getText() != null) {
                    matchPatterns(request.getPostData().getText());
                } else {
                    for (HarPostDataParam postParam : request.getPostData().getParams()) {
                        if (postParam.getValue().isEmpty()) {
                            matchPatterns(postParam.getName());
                        } else {
                            matchPatterns(postParam.getValue());
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates and returns all found urls, email addresses and phone numbers
     * in request.
     *
     * @return {@link String}
     */

    public String getRecognizedItems() {
        StringBuilder sb = new StringBuilder();
        if (foundHosts.size() > 0) {
            Set<String> paramsWithHosts = foundHosts.keySet();
            sb.append("Hosts found in request:<br />");
            for (String param : paramsWithHosts) {
                sb.append(param).append(" = ").append(foundHosts.get(param)).append("<br />");
            }
        }
        if (foundEmails.size() > 0) {
            Set<String> paramsWithHosts = foundEmails.keySet();
            sb.append("Email addresses found in request:<br />");
            for (String param : paramsWithHosts) {
                sb.append(param).append(" = ").append(foundEmails.get(param)).append("<br />");
            }
        }
        if (foundPhones.size() > 0) {
            Set<String> paramsWithHosts = foundPhones.keySet();
            sb.append("Telephone numbers found in request:<br />");
            for (String param : paramsWithHosts) {
                sb.append(param).append(" = ").append(foundPhones.get(param)).append("<br />");
            }
        }

        return sb.toString();
    }

    /**
     * Matches any urls, emails or phone numbers in query params
     * or in header pairs
     *
     * @param pair {@link HarNameValuePair}
     */
    private void matchPatterns(HarNameValuePair pair) {
        if (pair.getValue().matches(HOST_PATTERN)) {
            foundHosts.put(pair.getName(), pair.getValue());
        }
        if (pair.getValue().matches(EMAIL_PATTERN)) {
            foundEmails.put(pair.getName(), pair.getValue());
        }
        if (pair.getValue().matches(PHONE_PATTERN)) {
            foundPhones.put(pair.getName(), pair.getValue());
        }
    }

    /**
     * Matches any urls, emails or phone numbers in
     * request body
     *
     * @param text {@link String}
     */
    private void matchPatterns(String text) {
        Matcher matcher = Pattern.compile(HOST_PATTERN).matcher(text);
        // Match urls in request body
        if (matcher.find()) {
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                sb.append(matcher.group()).append("\n");
            }
            foundHosts.put("BODY", sb.toString());
        }
        // Match emails in request body
        matcher = Pattern.compile(EMAIL_PATTERN).matcher(text);
        if (matcher.find()) {
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                sb.append(matcher.group()).append("\n");
            }
            foundEmails.put("BODY", sb.toString());
        }
        // Match phones in request body
        matcher = Pattern.compile(PHONE_PATTERN).matcher(text);
        if (matcher.find()) {
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                sb.append(matcher.group()).append("\n");
            }
            foundPhones.put("BODY", sb.toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HostBean hostBean = (HostBean) o;

        if (isValid != hostBean.isValid) {
            return false;
        }
        if (foundHosts != null ? !foundHosts.equals(hostBean.foundHosts)
                : hostBean.foundHosts != null) {
            return false;
        }
        if (foundEmails != null ? !foundEmails.equals(hostBean.foundEmails)
                : hostBean.foundEmails != null) {
            return false;
        }
        if (foundPhones != null ? !foundPhones.equals(hostBean.foundPhones)
                : hostBean.foundPhones != null) {
            return false;
        }
        if (!name.equals(hostBean.name)) {
            return false;
        }
        if (!callType.equals(hostBean.callType)) {
            return false;
        }
        if (!url.equals(hostBean.url)) {
            return false;
        }
        if (!approvedBy.equals(hostBean.approvedBy)) {
            return false;
        }
        if (!approvedUntil.equals(hostBean.approvedUntil)) {
            return false;
        }
        if (!notes.equals(hostBean.notes)) {
            return false;
        }
        if (!description.equals(hostBean.description)) {
            return false;
        }
        if (!businessgroup.equals(hostBean.businessgroup)) {
            return false;
        }
        return headers.equals(hostBean.headers);
    }

    @Override
    public int hashCode() {
        int result = foundHosts != null ? foundHosts.hashCode() : 0;
        result = 31 * result + (foundEmails != null ? foundEmails.hashCode() : 0);
        result = 31 * result + (foundPhones != null ? foundPhones.hashCode() : 0);
        result = 31 * result + (isValid ? 1 : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + callType.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + approvedBy.hashCode();
        result = 31 * result + approvedUntil.hashCode();
        result = 31 * result + notes.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + businessgroup.hashCode();
        result = 31 * result + headers.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "HostBean [name=" + name + ", callType=" + callType + ", url=" + url
                + ", isValid=" + isValid + ", approvedBy=" + approvedBy
                + ", approvedUntil=" + approvedUntil + ", foundURLS=" + foundHosts.size()
                + ", foundEmails=" + foundEmails.size() + ", foundPhones=" + foundPhones.size() + "]";
    }
}

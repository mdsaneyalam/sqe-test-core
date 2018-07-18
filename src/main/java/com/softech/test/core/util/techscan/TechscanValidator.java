package com.softech.test.core.util.techscan;

import com.softech.test.core.util.Logger;
import com.softech.test.core.util.techscan.model.HostBean;
import com.softech.test.core.util.techscan.model.Request;
import com.softech.test.core.util.techscan.model.TechscanItem;
import com.softech.test.core.util.techscan.model.WhiteListEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for validation of calls collected from proxy log
 * against specified whitelist.
 *
 * @author Uladzimir_Kazimirchyk
 */
public class TechscanValidator {

    private static final String CATEGORY_UNKNOWN = "UNKNOWN";
    private static final String PROTOCOL = "https?://";
    private static final String DATE_REGEX = "\\d\\d?/\\d\\d?/\\d{4}";

    private List<TechscanItem> knownTypes = new ArrayList<>();
    private List<WhiteListEntry> whiteList = new ArrayList<>();
    private List<String> validNames = new LinkedList<>();

    public TechscanValidator(List<WhiteListEntry> whiteList, List<TechscanItem> knownCategoryTypes) {
        this.whiteList = whiteList;
        setValidNames();
        knownTypes.addAll(knownCategoryTypes);
    }

    /**
     * Categorizes request using predefined categories list and creates HostBean object
     * for later validation.
     * @param request {@link Request}
     * @return {@link HostBean}
     */
    public HostBean categorizeRequest(Request request) {

        String requestString = request.getUrl();

        HostBean hb = new HostBean(request);
        for (TechscanItem item : knownTypes) {
            Pattern p = Pattern.compile(item.getPattern());
            Matcher matcher = p.matcher(requestString);
            if (matcher.find()) {
                hb.setCallType(item.getCategory());
                hb.setName(item.getName());
                return hb;
            }
        }
        hb.setName(CATEGORY_UNKNOWN);
        hb.setCallType(CATEGORY_UNKNOWN);
        return hb;
    }

    /**
     * Populates valid names found in whitelist
     */
    private void setValidNames() {
        for (WhiteListEntry whiteListEntry : whiteList) {
            String entry = whiteListEntry.getEntry();
            if (!entry.matches(PROTOCOL)) {
                validNames.add(entry.toLowerCase());
            }
        }
    }

    /**
     * Method validates HostBean against whitelist and returns updated HostBean object
     *
     * @param hostBean {@link HostBean} to be validated
     * @return Validated HostBean
     */
    public HostBean validate(HostBean hostBean) {
        boolean result = false;

        for (WhiteListEntry whiteListEntry : whiteList) {
            if (whiteListEntry.getEntry().toLowerCase().contains(hostBean.getName())) {
                result = validNames.contains(hostBean.getName());
                if (!result) {
                    result = Pattern.compile(whiteListEntry.getEntry())
                            .matcher(hostBean.getUrl()).find();
                }
                if (result) {
                    if (whiteListEntry.getApprovedUntil().matches(DATE_REGEX)) {
                        hostBean.setApprovedBy(whiteListEntry.getApprovedBy());
                        hostBean.setApprovedUntil(whiteListEntry.getApprovedUntil());
                        hostBean.setNotes(whiteListEntry.getNotes());
                        hostBean.setDescription(whiteListEntry.getDescription());
                        hostBean.setBusinessgroup(whiteListEntry.getBusinessgroup());

                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                        Date approvedDate = null;
                        try {
                            approvedDate = dateFormat.parse(whiteListEntry.getApprovedUntil());
                        } catch (ParseException e) {
                            Logger.logConsoleMessage("Error parsing Approval Expiration date");
                            e.printStackTrace();
                        }
                        Date current = new Date();
                        result = current.before(approvedDate);
                    }
                    if (hostBean.getName().equalsIgnoreCase(CATEGORY_UNKNOWN)) {
                        result = false;
                    }
                    break;
                }
            } else {
                hostBean.setApprovedBy("Not Approved");
                hostBean.setApprovedUntil("No Approval expiration date");
                hostBean.setNotes("");
                hostBean.setDescription("");
                hostBean.setBusinessgroup("");
            }
        }

        hostBean.setValid(result);

        return hostBean;
    }

    /**
     * Validates list of HostBean objects against whitelist
     *
     * @param hostBeans - {@link List<HostBean>}
     * @return Validated list
     */
    public List<HostBean> validate(List<HostBean> hostBeans) {
        List<HostBean> validatedBeans = new LinkedList<>();
        for (HostBean hb : hostBeans) {
            validatedBeans.add(validate(hb));
        }
        return validatedBeans;
    }
}

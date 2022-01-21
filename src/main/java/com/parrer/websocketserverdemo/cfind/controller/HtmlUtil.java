package com.parrer.websocketserverdemo.cfind.controller;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.lastIndexOf;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.startsWith;

public class HtmlUtil {
    private static Pattern SCRIPT_PATTERN = Pattern.compile("<script.*?></scripts>");
    private static Pattern LINK_PATTERN = Pattern.compile("<link.*?href=\"(.+?)\">");

    public static String getUrlProtocolHost(String url) {
        if (isBlank(url) || (!startsWith(url, "http://") && !startsWith(url, "https://"))) {
            return EMPTY;
        }
        boolean http = startsWith(url, "http://");
        String uri = http ? removeStart(url, "http://") : removeStart(url, "https://");
        if (isBlank(uri)) {
            return EMPTY;
        }
        String host = uri.substring(0, uri.indexOf("/"));
        return isBlank(host) ? EMPTY : ((http ? "http://" : "https://") + host);
    }

    public static String getUrlSourceName(String url) {
        if (isBlank(url) || (!startsWith(url, "http://") && !startsWith(url, "https://"))) {
            return EMPTY;
        }
        String replaced = url.replace("://", EMPTY);
        int lastIndexOf = lastIndexOf(replaced, "/");
        if (lastIndexOf == -1) {
            return EMPTY;
        }
        String substring = replaced.substring(lastIndexOf + 1);
        if (isBlank(substring)) {
            return EMPTY;
        }
        return substring.split("\\?")[0].split("#")[0];
    }
}

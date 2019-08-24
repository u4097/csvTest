package utils;

import org.apache.commons.lang3.StringUtils;

import java.net.IDN;

public class UrlUtils {

    private static final String ACE_PREFIX = "xn--";

    public static String trimBrowserUrl(String browserUrl) {
        browserUrl = StringUtils.stripToNull(browserUrl);
        if (browserUrl == null) {
            return null;
        }

        int pos = skipWyciwygProtocol(browserUrl);
        if (pos == -1) {
            pos = skipViewSourceProtocol(browserUrl);
        }

        return pos != -1 ? StringUtils.stripToNull(browserUrl.substring(pos)) : browserUrl;
    }

    public static boolean isFile(String url) {
        return getFileHost(url) != null;
    }

    private static String getFileHost(String url) {
        if (StringUtils.isEmpty(url) || url.length() < 3) {
            return null;
        }

        int begin, end;
        final String fileProtocol = "file://";
        if (url.startsWith(fileProtocol)) {
            begin = fileProtocol.length();

            if (begin < url.length() && url.charAt(begin) == '/') {
                ++begin;
            }

            String disk = tryGetDiskName(url, begin);
            if (disk != null) {
                return disk;
            }

            end = url.indexOf('/', begin);
        } else {
            if (url.charAt(0) == '\\' && url.charAt(1) == '\\' && Character.isLetterOrDigit(url.charAt(2))) {
                begin = 2;
                end = url.indexOf('\\', begin);
            } else {
                return tryGetDiskName(url, 0);
            }
        }

        String res = url.substring(begin, end != -1 ? end : url.length());
        if (res.contains(ACE_PREFIX)) {
            res = IDN.toUnicode(res);
        }
        return res;
    }

    private static String tryGetDiskName(String url, int begin) {
        if ((begin + 2) >= url.length()) {
            return null;
        }

        char firstChar = url.charAt(begin);
        if (Character.isLetter(firstChar) && url.charAt(begin + 1) == ':') {
            char ch = url.charAt(begin + 2);
            if (ch == '\\' || ch == '/') {
                return "Disk " + firstChar;
            }
        }

        return null;
    }

    public static String getDomainOrDefault(String url, String defaultDomain) {
        if (StringUtils.isEmpty(url)) {
            return defaultDomain;
        }

        String fileHost = getFileHost(url);
        if (fileHost != null) {
            return StringUtils.defaultIfEmpty(fileHost, defaultDomain);
        }

        final int len = url.length();

        boolean isIpV6 = false, protocolSkipped = false;
        int beginPos = 0, endPos = 0;
        for (; endPos < len; ++endPos) {
            final int ch = url.charAt(endPos);
            if (ch == ':') {
                if (isIpV6 || protocolSkipped) {
                    continue;
                } else if (len <= (endPos + 2) || url.charAt(endPos + 1) != '/' || url.charAt(endPos + 2) != '/') {
                    continue;
                }

                endPos += 2;
                beginPos = endPos + 1;
                protocolSkipped = true;
            } else if (ch == '[') {
                isIpV6 = true;
            } else if (ch == '@') {
                beginPos = endPos + 1;
            } else if (ch == '/') {
                break;
            }
        }

        if (url.startsWith("www.", beginPos)) {
            beginPos += 4;
        }

        String res = url.substring(beginPos, endPos);
        if (res.contains(ACE_PREFIX)) {
            int portPos = res.lastIndexOf(':');
            if (portPos != -1) {
                res = IDN.toUnicode(res.substring(0, portPos)) + res.substring(portPos);
            } else {
                res = IDN.toUnicode(res);
            }
        }

        return StringUtils.defaultIfEmpty(res, defaultDomain);
    }

    /**
     * wyciwyg://[number]/[url]
     *
     * @return start position of url
     */
    private static int skipWyciwygProtocol(String browserUrl) {
        final String wyciwygProtocol = "wyciwyg://";
        if (!browserUrl.startsWith(wyciwygProtocol)) {
            return -1;
        }

        int pos = browserUrl.indexOf('/', wyciwygProtocol.length());
        if (pos != -1) {
            ++pos;
        }

        return pos;
    }

    /**
     * view-source:
     *
     * @return start position of url
     */
    private static int skipViewSourceProtocol(String browserUrl) {
        final String viewSourcePrefix = "view-source:";
        return browserUrl.startsWith(viewSourcePrefix) ? viewSourcePrefix.length() : -1;
    }
}

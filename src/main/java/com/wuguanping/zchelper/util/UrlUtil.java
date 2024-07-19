package com.wuguanping.zchelper.util;

import com.google.common.base.CaseFormat;
import com.intellij.openapi.util.text.Strings;
import com.intellij.psi.PsiFile;
import com.intellij.util.Url;

import java.util.ArrayList;
import java.util.Arrays;

public class UrlUtil {
    public static String getUrlPathByPsiFile(PsiFile file) {
        String path = file.getVirtualFile().getPath();
        String[] pathParts = path.split("/");

        String fileName = pathParts[pathParts.length - 1];
        pathParts = Arrays.copyOfRange(pathParts, 0, pathParts.length - 1);

        boolean find = false;
        ArrayList<String> apiPathParts = new ArrayList<>();

        for (String pathPart : pathParts) {
            if (find) {
                apiPathParts.add(toUndline(pathPart));
            } else if (pathPart.equals("controller")) {
                find = true;
            }
        }

        String controller = getControllerNameByFileName(fileName);
        apiPathParts.add(controller);
        return Strings.join(apiPathParts, "/");

    }


    private static String getControllerNameByFileName(String fileName) {
        String[] fileNameParts = fileName.split("\\.");
        String className = fileNameParts[fileNameParts.length - 2];
        String replace = className.replace("Controller", "");
        return toUndline(replace);
    }

    public static String toUndline(String string) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string);
    }

    public static String removeRedundancyMarkup(String pattern) {
        String localhostRegex = "(http(s?)://)?(localhost)(:\\d+)?";
        String hostAndPortRegex =
                "(http(s?)://)?" +
                        "( " +
                        "([a-zA-Z0-9]([a-zA-Z0-9\\\\-]{0,61}[a-zA-Z0-9])?\\\\.)+[a-zA-Z]{2,6} |" + // domain
                        "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)" + // ip address
                        ")";

        String localhost = "localhost";

        if (pattern.contains(localhost)) {
            pattern = pattern.replaceFirst(localhostRegex, "");
        }
        // quick test if reg exp should be used
        System.out.println("contains https " + pattern.contains("https:"));
        System.out.println("contains http " + pattern.contains("http:"));
        if (pattern.contains("http:") || pattern.contains("https:")) {
            pattern = pattern.replaceFirst(hostAndPortRegex, "");
        }

        //TODO : resolve RequestMapping(params="method=someMethod")
        if (!pattern.contains("?")) {
            return pattern;
        }
        pattern = pattern.substring(0, pattern.indexOf("?"));
        return pattern;
    }

}

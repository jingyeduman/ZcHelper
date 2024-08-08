package com.wuguanping.zchelper.util;

import com.google.common.base.CaseFormat;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.Strings;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Url;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.ArrayList;
import java.util.Arrays;

public class UrlUtil {

    public static String getUrlPathByAnActionEvent(AnActionEvent e) {
        if (e == null) {
            return null;
        }

        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file == null) {
            return null;
        }

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return null;
        }

        int offset = editor.getCaretModel().getCurrentCaret().getOffset();
        PsiElement element = file.findElementAt(offset);
        Method containingMethod = PsiTreeUtil.getParentOfType(element, Method.class);
        if (containingMethod == null) {
            System.out.println("containingMethod is null");
            return null;
        }

        String methodName = containingMethod.getName();
        if (methodName.startsWith("__")) {
            return null;
        }

        if (!containingMethod.getAccess().isPublic()) {
            return null;
        }

        PhpClass containingClass = containingMethod.getContainingClass();
        if (containingClass == null) {
            System.out.println("containingClass is null");
            return null;
        }

        String className = containingClass.getName();
        String controllerKeywords = "Controller";
        if (!className.endsWith(controllerKeywords)) {
            System.out.println("controllerKeywords is not match");
            return null;
        }

        return getUrlPathByPsiFile(file) + "/" + toUndline(methodName);
    }

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

        if (!find) {
            return null;
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

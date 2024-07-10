package com.wuguanping.zchelper;

import com.google.common.base.CaseFormat;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.Strings;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.impl.ArrayHashElementImpl;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import kotlin.reflect.KFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class GetApiPathAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        String apiPath = getApiPath(e);
        if (apiPath == null) {
            return;
        }

        String apiHost = getApiHost(e);
        if (apiHost != null) {
            apiPath = apiHost + "/" + apiPath;
        }

        StringSelection stringSelection = new StringSelection(apiPath);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private String getApiHost(AnActionEvent e) {
        try {
            Project project = e.getProject();
            if (project == null) {
                return null;
            }

            String basePath = e.getProject().getBasePath();
            Properties properties = new Properties();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(basePath + "/api/.env.dev"));
            properties.load(bufferedReader);
            String apiHost = properties.getProperty("API_HOST");
            if (apiHost == null) {
                return null;
            }

            return apiHost.trim();
        } catch (IOException ex) {
            System.out.println(ex);
        }

        return null;
    }

    private String getApiPath(AnActionEvent e)  {
        if (e == null) {
            System.out.println("e is null");
            return null;
        }




        System.out.println("start GetApiPathAction");
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file == null) {
            System.out.println("file is null");
            return null;
        }

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            System.out.println("editor is null");
            return null;
        }

        int offset = editor.getCaretModel().getCurrentCaret().getOffset();
        PsiElement element = file.findElementAt(offset);
        Method containingMethod = PsiTreeUtil.getParentOfType(element, Method.class);
        if (containingMethod == null) {
            System.out.println("containingMethod is null");
            return null;
        }

        String methodMame = containingMethod.getName();
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

        String controller = className.substring(0, className.length() - controllerKeywords.length());
        controller = toUndline(controller);
        methodMame = toUndline(methodMame);

        String apiDir = getApiDir(e);
        if (apiDir == null) {
            return null;
        }

        return apiDir + "/" + controller + "/" + methodMame;
    }

    private String toUndline(String string) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string);
    }

    private String getApiDir(AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file == null) {
            return null;
        }

        String path = file.getVirtualFile().getPath();
        String[] pathParts = path.split("/");
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

        return Strings.join(apiPathParts, "/");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        String apiPath = getApiPath(e);
        Presentation presentation = e.getPresentation();
        if (apiPath == null) {
            presentation.setEnabled(false);
            return;
        }

        presentation.setEnabled(true);
    }
}

package com.wuguanping.zchelper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.wuguanping.zchelper.util.UrlUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class GetApiUrlAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        String apiPath = UrlUtil.getUrlPathByAnActionEvent(e);
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

            String filePath = basePath + "/api/.env.dev";
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("filePath" + filePath + " not exists");
                filePath = basePath + "/.env.dev";
                file = new File(filePath);
            }

            if (!file.exists()) {
                System.out.println("filePath" + filePath + " not exists");
                return null;
            }

            Properties properties = new Properties();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
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

    @Override
    public void update(@NotNull AnActionEvent e) {
        String apiPath = UrlUtil.getUrlPathByAnActionEvent(e);;
        String apiHost = getApiHost(e);
        Presentation presentation = e.getPresentation();
        if (apiPath == null || apiHost == null) {
            presentation.setEnabled(false);
            return;
        }

        presentation.setEnabled(true);
    }
}

package com.wuguanping.zchelper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.wuguanping.zchelper.util.UrlUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class GetApiPathAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        String apiPath = UrlUtil.getUrlPathByAnActionEvent(e);
        if (apiPath == null) {
            return;
        }

        StringSelection stringSelection = new StringSelection("/" + apiPath);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        String apiPath = UrlUtil.getUrlPathByAnActionEvent(e);
        Presentation presentation = e.getPresentation();
        if (apiPath == null) {
            presentation.setEnabled(false);
            return;
        }

        presentation.setEnabled(true);
    }
}

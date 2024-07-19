package com.wuguanping.zchelper;

import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.wuguanping.zchelper.search.*;
import org.jetbrains.annotations.NotNull;
import com.intellij.navigation.ChooseByNameContributor;

import java.awt.datatransfer.DataFlavor;

public class SearchMethodByUrlAction extends GotoActionBase implements DumbAware {
    @Override
    protected void gotoActionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        FilterByUrlContributor chooseByname = new FilterByUrlContributor(project);
        ChooseByNameContributor[] butor = new ChooseByNameContributor[]{chooseByname};
        FilterByUrlModel model = new FilterByUrlModel(project, butor);

        GotoActionCallback<Object> callback = new GotoActionCallback<>() {
            @Override
            public void elementChosen(ChooseByNamePopup chooseByNamePopup, Object o) {
                if (o instanceof FilterByUrlNavigationItem) {
                    ((FilterByUrlNavigationItem) o).navigate(true);
                }
            }
        };

        ChooseByNameItemProvider myChooseByNameItemProvider = new FilterByUrlProvider(getPsiContext(e));

        String predefinedText = tryFindCopiedURL();
        ChooseByNamePopup popup = ChooseByNamePopup.createPopup(project, model, myChooseByNameItemProvider, predefinedText);

        showNavigationPopup(callback, "title", popup);
    }

    private String tryFindCopiedURL() {
        String contents = CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor);
        if (contents == null) {
            return null;
        }

        contents = contents.trim();
        if (contents.length() > 120) {
            contents = contents.substring(0, 120);
        }
        return contents;
    }
}

package com.wuguanping.zchelper;

import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.wuguanping.zchelper.search.*;
import org.jetbrains.annotations.NotNull;
import com.intellij.navigation.ChooseByNameContributor;

public class SearchMethodByUrlAction extends GotoActionBase implements DumbAware {
    @Override
    protected void gotoActionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        FilterByUrlContributor chooseByname = new FilterByUrlContributor();
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
        showNavigationPopup(e, model, callback, "根据URL搜索函数", false, false, myChooseByNameItemProvider);
    }
}

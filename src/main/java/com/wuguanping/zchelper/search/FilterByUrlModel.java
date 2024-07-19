package com.wuguanping.zchelper.search;

import com.intellij.ide.util.gotoByName.CustomMatcherModel;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilterByUrlModel extends FilteringGotoByModel<Object>  implements DumbAware, CustomMatcherModel {

    public FilterByUrlModel(@NotNull Project project, @NotNull ChooseByNameContributor[] contributors) {
        super(project, contributors);
    }

    /**
     * @description: 命中选项
     *
     * @author: chenzhiwei
     * @create: 2020/5/16 22:32
     * @return java.lang.Object
     */
    @Override
    protected @Nullable Object filterValueFor(NavigationItem navigationItem) {
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getPromptText() {
        return "请输入URL";
    }

    @Override
    public @NotNull @NlsContexts.Label String getNotInMessage() {
        return "Not in Message";
    }

    @Override
    public @NotNull @NlsContexts.Label String getNotFoundMessage() {
        return "Not Found message";
    }

    @Override
    public @Nullable @NlsContexts.Label String getCheckBoxName() {
        return null;
    }

    @Override
    public boolean loadInitialCheckBoxState() {
        return false;
    }

    @Override
    public void saveInitialCheckBoxState(boolean b) {

    }

    @Override
    public String @NotNull [] getSeparators() {
        return new String[]{"/","?"};
    }

    /**
     * @description: 必须重写，返回数据项
     *
     * @author: chenzhiwei
     * @create: 2020/5/16 22:33
     * @return java.lang.String
     */
    @Override
    public @Nullable String getFullName(@NotNull Object element) {
        return ((FilterByUrlNavigationItem)element).getValue();
    }

    @Override
    public boolean willOpenEditor() {
        return true;
    }

    @Override
    public boolean matches(@NotNull String s, @NotNull String s1) {
        return true;
    }
}

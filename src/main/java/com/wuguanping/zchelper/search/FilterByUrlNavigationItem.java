package com.wuguanping.zchelper.search;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public class FilterByUrlNavigationItem implements NavigationItem {
    private NavigationItem navigationItem;
    private String name;

    public FilterByUrlNavigationItem(String name, PsiElement psiElement) {
        if (psiElement instanceof NavigationItem) {
            this.navigationItem = (NavigationItem) psiElement;
        }
        this.name = name;
    }

    @Override
    public @Nullable @NlsSafe String getName() {
        return name;
    }

    @Override
    public @Nullable ItemPresentation getPresentation() {
        return new FilterByUrlPresentation(name);
    }

    @Override
    public void navigate(boolean b) {
        if (null != navigationItem) {
            navigationItem.navigate(b);
        }
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    public String getValue() {
        return this.name;
    }
}

package com.wuguanping.zchelper.search;

import com.intellij.ide.util.gotoByName.ChooseByNameViewModel;
import com.intellij.ide.util.gotoByName.DefaultChooseByNameItemProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilterByUrlProvider extends DefaultChooseByNameItemProvider {

    public FilterByUrlProvider(@Nullable PsiElement context) {
        super(context);
    }

    @Override
    public boolean filterElements(@NotNull ChooseByNameViewModel base, @NotNull String pattern, boolean everywhere, @NotNull ProgressIndicator indicator, @NotNull Processor<Object> consumer) {
        pattern = pattern.replaceFirst("http(s?)://[^/]*/(api/)?", "");
        if (pattern.contains("?")) {
            pattern = pattern.substring(0, pattern.indexOf("?"));
        }
        pattern = pattern.replace("_", "");
        return super.filterElements(base, pattern, everywhere, indicator, consumer);
    }
}

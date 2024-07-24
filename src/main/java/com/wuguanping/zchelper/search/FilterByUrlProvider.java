package com.wuguanping.zchelper.search;

import com.intellij.ide.util.gotoByName.ChooseByNameViewModel;
import com.intellij.ide.util.gotoByName.DefaultChooseByNameItemProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.wuguanping.zchelper.util.UrlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class FilterByUrlProvider extends DefaultChooseByNameItemProvider {

    public FilterByUrlProvider(@Nullable PsiElement context) {
        super(context);
    }

    @Override
    public boolean filterElements(@NotNull ChooseByNameViewModel base, @NotNull String pattern, boolean everywhere, @NotNull ProgressIndicator indicator, @NotNull Processor<Object> consumer) {
        System.out.println("start filterElements " + pattern);
        pattern = pattern.replaceFirst("http(s?)://[^/]*/(api/)?", "");
        if (pattern.contains("?")) {
            pattern = pattern.substring(0, pattern.indexOf("?"));
        }

        pattern = UrlUtil.toUndline(pattern);
        ArrayList<String> patterns = new ArrayList<>();
        for (String string : pattern.split("/")) {
            if (!string.isEmpty()) {
                patterns.add(string);
            }
        }

        pattern = String.join("/", patterns);
        System.out.println("end filterElements " + pattern);
        return super.filterElements(base, String.join("/", patterns), everywhere, indicator, consumer);
    }

}

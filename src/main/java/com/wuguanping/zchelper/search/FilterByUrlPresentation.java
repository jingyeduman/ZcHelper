package com.wuguanping.zchelper.search;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FilterByUrlPresentation implements ItemPresentation {
    private String name;
    public FilterByUrlPresentation(String name) {
        this.name = name;
    }

    /**
     * @description: 搜索结果最终显示
     *
     * @author: chenzhiwei
     * @create: 2020/5/17 10:16
     * @return java.lang.String
     */
    @Nullable
    @Override
    public String getPresentableText() {
        return name;
    }

    /**
     * @description: 搜索结果的辅助说明
     *
     * @author: chenzhiwei
     * @create: 2020/5/17 10:16
     * @return java.lang.String
     */
    @Nullable
    @Override
    public String getLocationString() {
        return "";
    }

    /**
     * @description: 搜索结果的图标
     *
     * @author: chenzhiwei
     * @create: 2020/5/17 10:16
     * @return javax.swing.Icon
     */
    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return null;
    }

}

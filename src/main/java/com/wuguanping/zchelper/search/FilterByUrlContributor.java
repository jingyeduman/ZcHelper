package com.wuguanping.zchelper.search;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.wuguanping.zchelper.util.FileUtil;
import com.wuguanping.zchelper.util.UrlUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FilterByUrlContributor implements ChooseByNameContributor {
    List<FilterByUrlNavigationItem> list = new ArrayList<>();

    public FilterByUrlContributor() {

    }

    private void initList(Project project) {
        String dirPath = project.getBasePath() + "/app/libs/controller";
        ArrayList<String> filePaths = new ArrayList<>();
        FileUtil.findFiles(dirPath, "*Controller.php", filePaths);

        List<FilterByUrlNavigationItem> listTemp = new ArrayList<>();
        for (String filePath : filePaths) {
            VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(filePath);
            if (fileByPath == null) {
                continue;
            }

            PsiFile file = PsiManager.getInstance(project).findFile(fileByPath);
            if (file == null) {
                continue;
            }

            String urlPath = UrlUtil.getUrlPathByPsiFile(file);
            file.accept(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof Method) {
                        Method method = (Method) element;
                        if (method.getAccess().isPublic() && !method.getName().equals("__construct")) {
                            String url = "/" + urlPath + "/" + UrlUtil.toUndline(((Method) element).getName());
                            listTemp.add(new FilterByUrlNavigationItem(url, method));
                        }
                    }
                    super.visitElement(element);
                }
            });
        }

        list = listTemp;
    }

    /**
     * @description: 提供全部选项
     *
     * @author: chenzhiwei
     * @create: 2020/5/16 22:31
     * @return java.lang.String[]
     */
    @NotNull
    @Override
    public String[] getNames(Project project, boolean b) {
        this.initList(project);
        return this.list.parallelStream().map(FilterByUrlNavigationItem::getValue).toArray(String[]::new);
    }

    /**
     * @description: 匹配到符合的项
     *
     * @author: chenzhiwei
     * @create: 2020/5/16 22:31
     * @return com.intellij.navigation.NavigationItem[]
     */
    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String s, String s1, Project project, boolean b) {
        NavigationItem[] navigationItems = list.parallelStream().filter(
                        p -> p.getValue().equals(s)
                )
                .toArray(NavigationItem[]::new);
        return navigationItems;
    }
}

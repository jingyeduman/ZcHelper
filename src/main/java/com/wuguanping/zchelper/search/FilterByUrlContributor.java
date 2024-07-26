package com.wuguanping.zchelper.search;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpClassIndex;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpMethodIndex;
import com.wuguanping.zchelper.util.FileUtil;
import com.wuguanping.zchelper.util.UrlUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FilterByUrlContributor implements ChooseByNameContributor {


    List<FilterByUrlNavigationItem> list = new ArrayList<>();
    String[] names = new String[0];

    private static final Logger LOG = Logger.getInstance(FilterByUrlContributor.class);

    public FilterByUrlContributor(Project project) {
        //this.initList(project);
        this.initListV2(project);
    }

    private void initListV2(Project project) {
        if (!this.list.isEmpty()) {
            return;
        }

        if (project == null) {
            return;
        }

        System.out.println("start initListV2 ");
        long stime = System.currentTimeMillis();

        List<FilterByUrlNavigationItem> listTemp = new ArrayList<>();
        ArrayList<String> namesTemp = new ArrayList<>();

        GlobalSearchScope globalSearchScope = FindSymbolParameters.searchScopeFor(project, false);
        StubIndex index = StubIndex.getInstance();
        System.out.println("initListV2 start loop");
        try {
            ArrayList<String> controllerKeys = new ArrayList<>();
            index.processAllKeys(PhpClassIndex.KEY, key -> {
                if (key == null) {
                    return true;
                }

                if (!key.endsWith("controller")) {
                    return true;
                }


                controllerKeys.add(key);
                return true;
            }, globalSearchScope);

            for (String controllerKey : controllerKeys) {
                index.processElements(PhpClassIndex.KEY, controllerKey, project, globalSearchScope, PhpClass.class, file -> {
                    PsiFile containingFile = file.getContainingFile();
                    if (containingFile == null) {
                        return false;
                    }

                    if (file.isAbstract()) {
                        return false;
                    }

                    Collection<Method> methods = file.getMethods();
                    if (methods == null || methods.isEmpty()) {
                        return false;
                    }

                    String urlPath = UrlUtil.getUrlPathByPsiFile(containingFile);
                    for (Method method : methods) {
                        if (method.getAccess().isPublic() && !method.getName().equals("__construct")) {
                            String url = "/" + urlPath + "/" + UrlUtil.toUndline(method.getName());
                            listTemp.add(new FilterByUrlNavigationItem(url, method));
                            namesTemp.add(url);
                        }
                    }

                    return true;
                });
            }

        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            return;
        }

        

        System.out.println("initListV2 end loop");
        list = listTemp;
        names = namesTemp.toArray(String[]::new);
        long etime = System.currentTimeMillis();
        // 计算执行时间
        System.out.printf("initListV2 执行：%d 毫秒.", (etime - stime));
        System.out.println("end initListV2 ");
    }

    private void initList(Project project) {
        if (!this.list.isEmpty()) {
            return;
        }

        System.out.println("start initList ");
        long stime = System.currentTimeMillis();

        String dirPath = project.getBasePath() + "/app/libs/controller";
        if (!FileUtil.isDir(dirPath)) {
            dirPath = project.getBasePath() + "/api/app/libs/controller";
        }
        System.out.println("start initList dirPath " + dirPath);
        ArrayList<String> filePaths = new ArrayList<>();
        FileUtil.findFiles(dirPath, "Controller.php", filePaths);
        
        List<FilterByUrlNavigationItem> listTemp = new ArrayList<>();
        ArrayList<String> namesTemp = new ArrayList<>();

        System.out.println("start initList start loop  ");
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
                            namesTemp.add(url);
                        }
                    }
                    super.visitElement(element);
                }
            });
        }


        list = listTemp;
        names = namesTemp.toArray(String[]::new);

        long etime = System.currentTimeMillis();
        // 计算执行时间
        System.out.printf("initList 执行：%d 毫秒.", (etime - stime));
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
        this.initListV2(project);
        return names;
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

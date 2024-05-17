package com.wuguanping.zchelper;

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector;
import com.intellij.codeInsight.hints.InlayHintsSink;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.ArrayHashElementImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MultiLanguageHintCollector extends FactoryInlayHintsCollector {
    public MultiLanguageHintCollector(@NotNull Editor editor) {
        super(editor);
    }

    @Override
    public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
        if (!(psiElement instanceof PhpPsiElement)) {
            return false;
        }


        PsiManager psiManager = PsiManager.getInstance(editor.getProject());

        Project project = editor.getProject();
        VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(project.getBasePath() + "/zh-cn/watermark.php");

        Document document = FileDocumentManager.getInstance().getDocument(fileByPath);

        PsiFile file = PsiManager.getInstance(project).findFile(fileByPath);
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        file.accept(new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                PsiElement[] children = element.getChildren();
                for (int i = 0; i < children.length; i++) {
                    var next = children[i];
                    this.visitElement(next);
                }

                if (element instanceof ArrayHashElementImpl) {
                    ArrayHashElementImpl element1 = (ArrayHashElementImpl)element;
                    PhpPsiElement key = element1.getKey();
                    PhpPsiElement value = element1.getValue();
                    System.out.println("get value");
                    PsiElement[] children1 = value.getChildren();
                    for (PsiElement item:
                    children1) {
                        System.out.println("child");
                        System.out.println(item.getText());
                    }
                    System.out.println(value.getChildren());

                    objectObjectHashMap.put(key.getText(), value.getText());
                }
            }
        });

        psiElement.accept(new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                PsiElement[] children = element.getChildren();
                for (int i = 0; i < children.length; i++) {
                    var next = children[i];
                    this.visitElement(next);
                }

                if (element instanceof ParameterListOwner) {
                    PsiElement[] callArguments = ((ParameterListOwner) element).getParameters();
                    for(int i = 0; i < Math.min(callArguments.length, callArguments.length); ++i) {
                        PsiElement arg = callArguments[i];
                        String s = objectObjectHashMap.get(arg.getText());
                        System.out.println(arg.getText());
                        System.out.println(s);
                        System.out.println("==============");
                        if (s == null || s.isEmpty()) {
                            continue;
                        }


                        InlayPresentation text = getFactory().text(s);
                        InlayPresentation roundText = getFactory().roundWithBackground(text);

                        inlayHintsSink.addInlineElement(arg.getTextOffset(), true, roundText, false);
                    }

                }

            }
        });


        return false;

    }
    
}

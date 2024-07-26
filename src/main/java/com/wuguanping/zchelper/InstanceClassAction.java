package com.wuguanping.zchelper;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiUtilBase;
import com.jetbrains.php.config.PhpTreeClassChooserDialog;
import com.jetbrains.php.lang.psi.PhpCodeEditUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import com.intellij.ide.util.TreeChooser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstanceClassAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (e == null) {
            System.out.println("e is null");
            return;
        }
        System.out.println("start InstanceClassAction");
        Project project = e.getProject();
        if (project == null) {
            System.out.println("project is null");
            return;
        }

        PhpTreeClassChooserDialog phpTreeClassChooserDialog = new PhpTreeClassChooserDialog("选择要引入的类", project, null);
        phpTreeClassChooserDialog.showDialog();
        PhpClass selected = phpTreeClassChooserDialog.getSelected();
        if (selected == null) {
            return;
        }

        String selectedClassName = selected.getName();
        String varName = StringUtils.uncapitalize(selectedClassName);

        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        if (editor == null) {
            return;
        }

        PsiFile eidtorPsiFile  = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (eidtorPsiFile == null) {
            return;
        }

        PhpClass editorClass = PhpCodeEditUtil.findClassAtCaret(editor, eidtorPsiFile);
        if (editorClass == null) {
            return;
        }

        boolean isService = selectedClassName.endsWith("Service");
        boolean isDao = selectedClassName.endsWith("Dao");
        boolean isUtil = selectedClassName.endsWith("Util");

        Field[] fields = editorClass.getOwnFields();
        String preFieldName = "";
        int insertPos = 0;
        boolean hasMatch = false;
        for (Field field : fields) {
            ASTNode node = field.getNode();
            if (node == null) {
                continue;
            }

            if (field.getName().equals(varName)) {
                return;
            }
            boolean matchService = isService && field.getName().endsWith("Service");
            boolean matchDao = isDao && field.getName().endsWith("Dao");
            boolean matchUtil = isUtil && field.getName().endsWith("Util");
            if (matchService || matchDao || matchUtil || !hasMatch) {
                insertPos = node.getStartOffset() + node.getTextLength() + 1;
                preFieldName = node.getText().substring(1);
                hasMatch = true;
            }
        }

        Method ownConstructor = editorClass.getOwnConstructor();
        if (ownConstructor == null) {
            return;
        }

        if (insertPos == 0) {
            insertPos = ownConstructor.getNode().getTreePrev().getStartOffset();
        }

        int initInsertPos = ownConstructor.getLastChild().getTextOffset() + 1;
        PsiElement[] children = ownConstructor.getLastChild().getChildren();
        for (PsiElement child : children) {
            if (preFieldName.isBlank()) {
                initInsertPos = child.getTextOffset() + child.getTextLength();
                continue;
            }

            String regex = "\\$this->(\\S+)";
            Pattern compile = Pattern.compile(regex);
            Matcher matcher = compile.matcher(child.getText());
            if (matcher.find()) {
                String matchFieldName = matcher.group(1);
                if (matchFieldName.equals(preFieldName)) {
                    initInsertPos = child.getTextOffset() + child.getTextLength();
                    break;
                }
            }
        }

        int finalInsertPos = insertPos;
        int finalInitInsertPos = initInsertPos;
        WriteCommandAction.runWriteCommandAction(project, () -> ApplicationManager.getApplication().runWriteAction(() -> {
            StringBuffer textBuf = new StringBuffer();
            textBuf.append("\n");
            textBuf.append("\n");
            textBuf.append("/**\n*@var className\n*/".replace("className", selectedClassName));
            textBuf.append("private $varName;".replace("varName", varName));

            editor.getDocument().insertString(finalInsertPos, textBuf);
            int endPos1 = finalInsertPos + textBuf.length();

            StringBuffer textBuf1 = new StringBuffer();
            textBuf1.append("\n");
            textBuf1.append("$this->varName = \\Zc::singleton(className::class);".replace("className", selectedClassName).replace("varName", varName));

            int insertPos1 = finalInitInsertPos + textBuf.length();
            editor.getDocument().insertString(insertPos1, textBuf1);
            int endPos2 = insertPos1 + textBuf1.length();

            CodeStyleManager.getInstance(project).reformatText(eidtorPsiFile, finalInsertPos, endPos1);
            CodeStyleManager.getInstance(project).reformatText(eidtorPsiFile, insertPos1, endPos2);
        }));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();

        Project project = e.getProject();
        if (project == null) {
            presentation.setEnabled(false);
            return;
        }

        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        if (editor == null) {
            presentation.setEnabled(false);
            return;
        }

        final PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (file == null) {
            presentation.setEnabled(false);
            return;
        }

        presentation.setEnabled(true);
    }
}

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
            System.out.println("editor is null");
            return;
        }

        PhpFile phpFile  = (PhpFile)PsiUtilBase.getPsiFileInEditor(editor, project);
        if (phpFile == null) {
            System.out.println("phpFile is null");
            return;
        }

        PhpClass phpClass = PhpCodeEditUtil.findClassAtCaret(editor, phpFile);
        if (phpClass == null) {
            System.out.println("phpClass is null");
            return;
        }

        System.out.println("class name " + phpClass.getName());

        boolean isService = selectedClassName.endsWith("Service");
        boolean isDao = selectedClassName.endsWith("Dao");
        boolean isUtil = selectedClassName.endsWith("Util");
        System.out.println("selectedClassName" + selectedClassName);

        System.out.println("isService " + isService);
        System.out.println("isDao " + isDao);
        System.out.println("isUtil " + isUtil);

        Field[] fields = phpClass.getOwnFields();
        String preFieldName = null;
        int insertPos = 0;
        for (Field field : fields) {
            ASTNode node = field.getNode();
            if (node == null) {
                continue;
            }

            System.out.println("field name " + field.getName());
            if (field.getName().equals(varName)) {
                return;
            }
            boolean matchService = isService && field.getName().endsWith("Service");
            boolean matchDao = isDao && field.getName().endsWith("Dao");
            boolean matchUtil = isUtil && field.getName().endsWith("Util");

            System.out.println("matchService " + matchService);
            System.out.println("matchDao " + matchDao);
            System.out.println("matchUtil " + matchUtil);
            if (matchService || matchDao || matchUtil) {

                insertPos = node.getStartOffset();
                insertPos += node.getTextLength() + 1;
                preFieldName = node.getText().substring(1);
            }
        }

        System.out.println("start 103");

        if (insertPos == 0 && fields.length > 0) {
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                ASTNode node = field.getNode();
                if (node == null) {
                    continue;
                }

                insertPos = node.getStartOffset();
                insertPos += node.getTextLength() + 1;
                preFieldName = node.getText().substring(1);
                System.out.println("preFieldName" + preFieldName);
            }
        }

        if (insertPos == 0) {
            ASTNode nameNode = phpClass.getNameNode();
            while (nameNode != null) {
                if (nameNode.getText().equals("{")) {
                    insertPos = nameNode.getStartOffset() + 1;
                    break;
                }
                nameNode = nameNode.getTreeNext();
            }
        }

        Method ownConstructor = phpClass.getOwnConstructor();
        if (ownConstructor == null) {
            return;
        }

        PsiElement[] children = ownConstructor.getChildren();
        PsiElement[] children1 = children[2].getChildren();
        int initInsertPos = ownConstructor.getLastChild().getTextOffset() + 1;

        for (int i = 0; i < children1.length; i++) {
            var child = children1[i];
            if (!preFieldName.isEmpty()) {
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
            } else {
                initInsertPos = child.getTextOffset() + child.getTextLength();
            }
        }
        System.out.println("initInsertPos" + initInsertPos);

        int finalInsertPos = insertPos;
        int finalInitInsertPos = initInsertPos;
        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(() -> {
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

                    int insertPos = finalInitInsertPos + textBuf.length();
                    editor.getDocument().insertString(insertPos, textBuf1);
                    int endPos2 = insertPos + textBuf1.length();

                    CodeStyleManager.getInstance(project).reformatText(phpFile, finalInsertPos, endPos1);
                    CodeStyleManager.getInstance(project).reformatText(phpFile, insertPos, endPos2);
                });
            }
        });
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

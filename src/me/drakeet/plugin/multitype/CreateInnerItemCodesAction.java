package me.drakeet.plugin.multitype;

import com.google.common.base.CaseFormat;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;

/**
 * @author drakeet
 */
public class CreateInnerItemCodesAction extends BaseGenerateAction implements CreateInnerCodesDialog.OnOKListener {

    private boolean and;
    private CreateInnerCodesDialog dialog;
    private PsiFile psiFile;
    private Editor editor;


    public CreateInnerItemCodesAction() {
        super(null);
    }


    public CreateInnerItemCodesAction(CodeInsightActionHandler handler) {
        super(handler);
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        psiFile = event.getData(LangDataKeys.PSI_FILE);
        editor = event.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return;
        }
        if (dialog == null) {
            dialog = new CreateInnerCodesDialog();
            dialog.setOnOKListener(this);
        }
        dialog.setTitle("Generate MultiType Codes");
        dialog.pack();
        dialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(event.getProject()));
        dialog.setVisible(true);
    }


    @Override
    public void onOK(String typeName, boolean onlyBinder) {
        new CodeWriter(typeName, onlyBinder, psiFile, getTargetClass(editor, psiFile)).execute();
    }


    private class CodeWriter extends WriteCommandAction.Simple {

        private final Project project;
        private final PsiClass clazz;
        private final PsiElementFactory factory;
        private final String typeName;
        private final boolean onlyBinder;


        CodeWriter(String typeName, boolean onlyBinder, PsiFile psiFile, PsiClass clazz) {
            super(clazz.getProject(), "");
            this.typeName = typeName;
            this.onlyBinder = onlyBinder;
            this.clazz = clazz;
            this.project = clazz.getProject();
            this.factory = JavaPsiFacade.getElementFactory(project);
        }


        @Override
        protected void run() throws Throwable {
            if (!onlyBinder) {
                createTypeClass();
            }
            createItemViewBinderClass();
            // reformat class
            JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
            styleManager.optimizeImports(psiFile);
            styleManager.shortenClassReferences(clazz);
            new ReformatCodeProcessor(project, clazz.getContainingFile(), null, false).runWithoutProgress();
        }


        private void createTypeClass() {
            PsiClass innerTypeClass = factory.createClass(typeName);
            privateStatic(innerTypeClass);
            clazz.add(innerTypeClass);
        }


        private void createItemViewBinderClass() {
            clazz.add(createInnerClassFromText(BINDER_TEMPLATE
                .replace("${NAME}", typeName + "ViewBinder")
                .replace("MTI_CLASS", typeName)
                .replace("MTI_LOWER_NAME", CaseFormat.UPPER_CAMEL.to(LOWER_UNDERSCORE, typeName))
                .replace("MTI_NAME", CaseFormat.UPPER_CAMEL.to(LOWER_CAMEL, typeName)), clazz)
            );
        }


        private PsiClass createInnerClassFromText(String text, PsiElement element) {
            return factory.createClassFromText(text, element).getInnerClasses()[0];
        }


        private void privateStatic(PsiClass innerTypeClass) {
            PsiModifierList psiModifierList = innerTypeClass.getModifierList();
            assert psiModifierList != null;
            psiModifierList.setModifierProperty("static", true);
            psiModifierList.setModifierProperty("private", true);
        }


        private final String BINDER_TEMPLATE =
            "private static class ${NAME} extends ItemViewBinder<MTI_CLASS, ${NAME}.ViewHolder> {\n" +
                "\n" +
                "    @NonNull @Override\n" +
                "    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {\n" +
                "        View root = inflater.inflate(R.layout.item_MTI_LOWER_NAME, parent, false);\n" +
                "        return new ViewHolder(root);\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull MTI_CLASS MTI_NAME) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    static class ViewHolder extends RecyclerView.ViewHolder {\n" +
                "\n" +
                "        ViewHolder(View itemView) {\n" +
                "            super(itemView);\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }
}

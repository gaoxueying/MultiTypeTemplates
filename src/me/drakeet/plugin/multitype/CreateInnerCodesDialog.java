package me.drakeet.plugin.multitype;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class CreateInnerCodesDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField className;
    private JCheckBox onlyItemViewBinderCheckBox;


    public CreateInnerCodesDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {onOK();}
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {onCancel();}
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }


    private OnOKListener onOKListener;


    public interface OnOKListener {
        void onOK(String text, boolean and);
    }


    public void setOnOKListener(OnOKListener onOKListener) {
        this.onOKListener = onOKListener;
    }


    public OnOKListener getOnOKListener() {
        return onOKListener;
    }


    private void onOK() {
        if (onOKListener != null) {
            onOKListener.onOK(className.getText(), onlyItemViewBinderCheckBox.isSelected());
        }
        dispose();
    }


    private void onCancel() {
        dispose();
    }


    public static void main(String[] args) {
        CreateInnerCodesDialog dialog = new CreateInnerCodesDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}

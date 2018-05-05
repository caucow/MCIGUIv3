package com.caucraft.mciguiv3.pstor;

import java.awt.Window;
import javax.swing.JDialog;

/**
 *
 * @author caucow
 */
public class PasswordDialog extends JDialog {
    
    private PasswordDialogPanel passPanel;
    
    private PasswordDialog(Window owner, String title) {
        super(owner, title, ModalityType.DOCUMENT_MODAL);
        passPanel = new PasswordDialogPanel(this);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setAlwaysOnTop(true);
        getContentPane().add(passPanel);
        pack();
        setLocationRelativeTo(owner);
    }
    
    public static char[] getPassword(Window owner, String title) {
        PasswordDialog pd = new PasswordDialog(owner, title);
        pd.setVisible(true);
        return pd.passPanel.getPassword();
    }
    
    public static char[] getPassword(Window owner) {
        PasswordDialog pd = new PasswordDialog(owner, "Enter Password");
        pd.setVisible(true);
        return pd.passPanel.getPassword();
    }
    
}

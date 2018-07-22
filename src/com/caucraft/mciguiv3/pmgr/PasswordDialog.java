package com.caucraft.mciguiv3.pmgr;

import java.awt.Window;
import javax.swing.JDialog;

/**
 *
 * @author caucow
 */
public class PasswordDialog extends JDialog {
    
    private PasswordDialogPanel passPanel;
    
    private PasswordDialog(Window owner, String title, boolean canDecrypt) {
        super(owner, title, ModalityType.DOCUMENT_MODAL);
        passPanel = new PasswordDialogPanel(this, canDecrypt);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setAlwaysOnTop(true);
        getContentPane().add(passPanel);
        pack();
        setLocationRelativeTo(owner);
    }
    
    public static char[] getPassword(Window owner, String title, boolean canDecrypt) {
        PasswordDialog pd = new PasswordDialog(owner, title, canDecrypt);
        pd.setVisible(true);
        return pd.passPanel.getPassword();
    }
    
    public void setCanDecrypt(boolean canDecrypt) {
        passPanel.setCanDecrypt(canDecrypt);
    }
    
    public static char[] getPassword(Window owner, boolean canDecrypt) {
        return getPassword(owner, "Enter Password", canDecrypt);
    }
    
    public static PasswordDialog getPasswordDialog(Window owner, String title, boolean canDecrypt) {
        return new PasswordDialog(owner, title, canDecrypt);
    }
    
    public static PasswordDialog getPasswordDialog(Window owner, boolean canDecrypt) {
        return getPasswordDialog(owner, "Enter Password", canDecrypt);
    }
    
    public PasswordDialogPanel.Result getResult() {
        return passPanel.getResult();
    }
    
    public char[] getPassword() {
        return passPanel.getPassword();
    }
    
}

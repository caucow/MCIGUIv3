package com.caucraft.mciguiv3.pstor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;

/**
 *
 * @author caucow
 */
public class EditPasswordsDialogPanel extends javax.swing.JPanel {
    
    private final JDialog parent;
    private String[] unames;
    private byte[][] passes;
    private QuestionableEncryptionThing[] ciphers;
    private JPanel scrollPaneView;
    private JTextField[] nameFields;
    private int focus = -1;
    private JPasswordField[] passFields;
    private JCheckBox[] showPassChecks;
    private JButton[] remButtons;
    private boolean success;

    /**
     * Creates new form EditPasswordsDialogPanel
     * @param ciphers the ciphers needed to encrypt/decrypt the passwords, in
     * order from deepest/most nested encryption to shallowest/least nested.
     */
    public EditPasswordsDialogPanel(JDialog parent, String[] unames, byte[][] passes, QuestionableEncryptionThing... ciphers) {
        initComponents();
        this.parent = parent;
        parent.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.unames = Arrays.copyOf(unames, unames.length);
        this.passes = Arrays.copyOf(passes, passes.length);
        this.ciphers = ciphers;
        scrollPaneView = new JPanel(new MigLayout("wrap,flowx,fillx", "[left,200!,fill][left,0:0:max,grow,fill][left,min!,fill][left,min!,fill]"));
        passScrollPane.setViewportView(scrollPaneView);
        passScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        passScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        passScrollPane.getVerticalScrollBar().setUnitIncrement(30);
        populateScrollPane();
        parent.setMinimumSize(new Dimension(500, 300));
        parent.setMaximumSize(new Dimension(800, 600));
        parent.setPreferredSize(new Dimension(600, 400));
        parent.getContentPane().add(this);
        parent.revalidate();
    }
    
    private void populateScrollPane() {
        if (unames.length != passes.length) {
            throw new IllegalStateException("Username and password lists are different lengths!");
        }
        scrollPaneView.removeAll();
        final int len = unames.length;
        nameFields = new JTextField[len];
        passFields = new JPasswordField[len];
        showPassChecks = new JCheckBox[len];
        remButtons = new JButton[len];
        for (int i = 0; i < unames.length; i++) {
            final int pos = i;
            nameFields[i] = new JTextField(unames[i]);
            passFields[i] = new JPasswordField(passes[i] == null | passes[i].length == 0 ? "" : "password");
            passFields[i].setEchoChar('\u2022');
            showPassChecks[i] = new JCheckBox("Show Password");
            remButtons[i] = new JButton("Remove");
            passFields[i].addFocusListener(new FocusListener() {

                @Override
                public void focusGained(FocusEvent e) {
                    focusPass(pos);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    unfocusPass(pos);
                }
            });
            showPassChecks[i].addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (((JCheckBox)e.getSource()).isSelected()) {
                        showPass(pos);
                    } else {
                        hidePass(pos);
                    }
                }
            });
            remButtons[i].addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    removePass(pos);
                }
            });
            
            scrollPaneView.add(nameFields[i]);
            scrollPaneView.add(passFields[i]);
            scrollPaneView.add(showPassChecks[i]);
            scrollPaneView.add(remButtons[i]);
        }
        for (int i = 0; i < unames.length; i++) {
            final int pos = i;
            nameFields[i].addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent evt) {
                    JTextField source;
                    switch (evt.getKeyCode()) {
                        case KeyEvent.VK_ESCAPE:
                            cancelButton.doClick();
                            break;
                        case KeyEvent.VK_ENTER:
                        case KeyEvent.VK_DOWN:
                            source = (JTextField)evt.getSource();
                            nameFields[(pos + 1) % nameFields.length].requestFocus();
                            break;
                        case KeyEvent.VK_UP:
                            source = (JTextField)evt.getSource();
                            nameFields[(pos - 1 + nameFields.length) % nameFields.length].requestFocus();
                            break;
                        case KeyEvent.VK_RIGHT:
                            source = (JTextField)evt.getSource();
                            passFields[pos].requestFocus();
                            break;
                    }
                }
            });
            passFields[i].addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent evt) {
                    JTextField source;
                    switch (evt.getKeyCode()) {
                        case KeyEvent.VK_ESCAPE:
                            cancelButton.doClick();
                            break;
                        case KeyEvent.VK_ENTER:
                        case KeyEvent.VK_DOWN:
                            source = (JTextField)evt.getSource();
                            passFields[(pos + 1) % passFields.length].requestFocus();
                            break;
                        case KeyEvent.VK_UP:
                            source = (JTextField)evt.getSource();
                            passFields[(pos - 1 + passFields.length) % passFields.length].requestFocus();
                            break;
                        case KeyEvent.VK_LEFT:
                            source = (JTextField)evt.getSource();
                            nameFields[pos].requestFocus();
                            break;
                    }
                }
            });
        }
        parent.revalidate();
        parent.repaint();
    }
    
    private void focusPass(int pos) {
        if (focus == pos) {
            return;
        }
        unfocusPass(focus);
        focus = pos;
        JPasswordField pf = passFields[pos];
        pf.setText(getDecrypted(pos));
    }
    
    private void unfocusPass(int pos) {
        if (focus == -1 || focus != pos) {
            return;
        }
        focus = -1;
        JPasswordField pf = passFields[pos];
        setEncrypted(pos, pf.getText());
        if (!showPassChecks[pos].isSelected()) {
            hidePass(pos);
        }
    }
    
    private void showPass(int pos) {
        JPasswordField pf = passFields[pos];
        pf.setText(getDecrypted(pos));
        pf.setEchoChar('\u0000');
    }
    
    private void hidePass(int pos) {
        JPasswordField pf = passFields[pos];
        pf.setEchoChar('\u2022');
        pf.setText(passes[pos] == null | passes[pos].length == 0 ? "" : "password");
    }
    
    private String getDecrypted(int pos) {
        if (passes[pos] == null | passes[pos].length == 0) {
            return "";
        }
        byte[] bytes = passes[pos];
        for (int i = ciphers.length - 1; i >= 0; --i) {
            try {
                bytes = ciphers[i].decrypt(bytes);
            } catch (DataLengthException | InvalidCipherTextException e) {
                JOptionPane.showMessageDialog(parent, "Could not decrypt password.");
                return "";
            }
        }
        String ret = new String(bytes, StandardCharsets.UTF_8);
        if (ciphers.length > 0) {
            Arrays.fill(bytes, (byte)0);
        }
        return ret;
    }
    
    private void setEncrypted(int pos, String pass) {
        if (pass.isEmpty()) {
            passes[pos] = new byte[0];
            return;
        }
        byte[] bytes = pass.getBytes(StandardCharsets.UTF_8);
        byte[] copy;
        for (int i = 0; i < ciphers.length; ++i) {
            try {
                copy = bytes;
                bytes = ciphers[i].encrypt(bytes);
                Arrays.fill(copy, (byte)0);
            } catch (DataLengthException | InvalidCipherTextException e) {
                JOptionPane.showMessageDialog(parent, "Could not encrypt password.");
                return;
            }
        }
        passes[pos] = bytes;
        QuestionableEncryptionThing.killString(pass);
    }
    
    public boolean getSuccess() {
        return success;
    }
    
    private void copyUsernames() {
        for (int i = 0; i < unames.length; ++i) {
            unames[i] = nameFields[i].getText();
        }
    }
    
    private void addPass() {
        copyUsernames();
        unames = Arrays.copyOf(unames, unames.length + 1);
        passes = Arrays.copyOf(passes, passes.length + 1);
        passes[passes.length - 1] = new byte[0];
        populateScrollPane();
    }
    
    private void removePass(int pos) {
        unfocusPass(focus);
        copyUsernames();
        if (unames.length == 1) {
            unames = new String[0];
            passes = new byte[0][];
        } else {
            String[] nnames = new String[unames.length - 1];
            System.arraycopy(unames, 0, nnames, 0, pos);
            System.arraycopy(unames, pos + 1, nnames, pos, unames.length - pos - 1);
            unames = nnames;

            byte[][] npass = new byte[passes.length - 1][];
            System.arraycopy(passes, 0, npass, 0, pos);
            System.arraycopy(passes, pos + 1, npass, pos, passes.length - pos - 1);
            passes = npass;
        }
        populateScrollPane();
    }
    
    public String[] getUsernames() {
        return unames;
    }
    
    public byte[][] getPasswords() {
        return passes;
    }
    
    /* 
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        passScrollPane = new javax.swing.JScrollPane();

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelPressed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okPressed(evt);
            }
        });

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 199, Short.MAX_VALUE)
                .addComponent(okButton)
                .addGap(18, 18, 18)
                .addComponent(cancelButton)
                .addContainerGap())
            .addComponent(passScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(passScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton)
                    .addComponent(addButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPressed
        addPass();
    }//GEN-LAST:event_addPressed

    private void okPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okPressed
        copyUsernames();
        success = true;
        parent.dispose();
    }//GEN-LAST:event_okPressed

    private void cancelPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelPressed
        parent.dispose();
    }//GEN-LAST:event_cancelPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    private javax.swing.JScrollPane passScrollPane;
    // End of variables declaration//GEN-END:variables
}

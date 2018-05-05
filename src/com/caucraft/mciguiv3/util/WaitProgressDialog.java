package com.caucraft.mciguiv3.util;


import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.JProgressBar;

/**
 *
 * @author caucow
 */
public class WaitProgressDialog extends JDialog {
    
    private WaitProgressDialog(Window owner, String title) {
        super(owner, title, ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        JProgressBar pbar = new JProgressBar();
        pbar.setIndeterminate(true);
        pbar.setPreferredSize(new Dimension(300, 25));
        getContentPane().add(pbar);
        pack();
        setLocationRelativeTo(owner);
    }
    
    public static <T> T waitForAction(Window parentWindow, String title, Function<T> f, Object... args) throws Exception {
        final WaitProgressDialog d = new WaitProgressDialog(parentWindow, title);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                d.setVisible(true);
            }
        });
        T t;
        try {
            t = f.doWork(args);
        } catch (Exception e) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    d.dispose();
                }
            });
            throw e;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                d.dispose();
            }
        });
        return t;
    }
    
}

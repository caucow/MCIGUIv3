package com.caucraft.mciguiv3.components;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * @author Stanislav Lapitsky
 * @version 1.0
 * @see http://java-sl.com/wrap.html
 */
public class WrapEditorKit extends StyledEditorKit {

    private final ViewFactory defaultFactory = new WrapColumnFactory();

    @Override
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }

    @Override
    public MutableAttributeSet getInputAttributes() {
        MutableAttributeSet mAttrs = super.getInputAttributes();
//        mAttrs.removeAttribute(WrapApp.LINE_BREAK_ATTRIBUTE_NAME);
        return mAttrs;
    }

    public static class NoWrapParagraphView extends ParagraphView {

        public NoWrapParagraphView(Element elem) {
            super(elem);
        }

        @Override
        public void layout(int width, int height) {
            super.layout(Short.MAX_VALUE, height);
        }

        @Override
        public float getMinimumSpan(int axis) {
            return super.getPreferredSpan(axis);
        }
    }

    public static class WrapLabelView extends LabelView {

        public WrapLabelView(Element elem) {
            super(elem);
        }

        @Override
        public int getBreakWeight(int axis, float pos, float len) {
            if (axis == View.X_AXIS) {
                checkPainter();
                int p0 = getStartOffset();
                int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
                if (p1 == p0) {
                    // can't even fit a single character
                    return View.BadBreakWeight;
                }
                try {
                    //if the view contains line break char return forced break
                    if (getDocument().getText(p0, p1 - p0).contains("\r")) {
                        return View.ForcedBreakWeight;
                    }
                } catch (BadLocationException ex) {
                    //should never happen
                }
            }
            return super.getBreakWeight(axis, pos, len);
        }

        @Override
        public View breakView(int axis, int p0, float pos, float len) {
            if (axis == View.X_AXIS) {
                checkPainter();
                int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
                try {
                    //if the view contains line break char break the view
                    int index = getDocument().getText(p0, p1 - p0).indexOf("\r");
                    if (index >= 0) {
                        GlyphView v = (GlyphView) createFragment(p0, p0 + index + 1);
                        return v;
                    }
                } catch (BadLocationException ex) {
                    //should never happen
                }
            }
            return super.breakView(axis, p0, pos, len);
        }
    }

    class WrapColumnFactory implements ViewFactory {

        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                switch (kind) {
                    case AbstractDocument.ContentElementName:
                        return new WrapLabelView(elem);
                    case AbstractDocument.ParagraphElementName:
                        return new NoWrapParagraphView(elem);
                    case AbstractDocument.SectionElementName:
                        return new BoxView(elem, View.Y_AXIS);
                    case StyleConstants.ComponentElementName:
                        return new ComponentView(elem);
                    case StyleConstants.IconElementName:
                        return new IconView(elem);
                    default:
                        break;
                }
            }

            // default to text display
            return new LabelView(elem);
        }
    }
}
//
//package articles.wrap;
//
//import java.awt.event.*;
//import javax.swing.*;
//import javax.swing.text.*;
//import java.awt.BorderLayout;
//
//public class WrapApp extends JFrame {
//    public static final String LINE_BREAK_ATTRIBUTE_NAME="line_break_attribute";
//    JEditorPane edit=new JEditorPane();
//    public WrapApp() {
//        super("Forsed wrap/no wrap example");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        edit.setEditorKit(new WrapEditorKit());
//        initKeyMap();
//
//        getContentPane().add(new JScrollPane(edit));
//        getContentPane().add(new JLabel("Press SHIFT+ENTER to insert line break."), BorderLayout.SOUTH);
//        setSize(300,200);
//        setLocationRelativeTo(null);
//    }
//
//    public static void main(String[] args) {
//        WrapApp m = new WrapApp();
//        m.setVisible(true);
//    }
//
//    protected void insertLineBreak() {
//        try {
//            int offs = edit.getCaretPosition();
//            Document doc = edit.getDocument();
//            SimpleAttributeSet attrs;
//            if (doc instanceof StyledDocument) {
//                attrs = new SimpleAttributeSet( ( (StyledDocument) doc).getCharacterElement(offs).getAttributes());
//            }
//            else {
//                attrs = new SimpleAttributeSet();
//            }
//            attrs.addAttribute(LINE_BREAK_ATTRIBUTE_NAME,Boolean.TRUE);
//            doc.insertString(offs, "\r", attrs);
//            edit.setCaretPosition(offs+1);
//        }
//        catch (BadLocationException ex) {
//            //should never happens
//            ex.printStackTrace();
//        }
//    }
//
//    protected void initKeyMap() {
//        Keymap kMap=edit.getKeymap();
//        Action a=new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                insertLineBreak();
//            }
//        };
//        kMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,KeyEvent.SHIFT_MASK),a);
//    }
//}

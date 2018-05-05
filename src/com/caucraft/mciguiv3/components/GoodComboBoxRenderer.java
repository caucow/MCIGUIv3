package com.caucraft.mciguiv3.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 *
 * @author caucow
 */
public class GoodComboBoxRenderer extends JLabel
    implements ListCellRenderer<Object> {

    private static final Border SELECTED_BORDER = new LineBorder(Color.WHITE);
    private static final Border FOCUS_BORDER = new LineBorder(Color.LIGHT_GRAY);
    
    private final JComboBox box;
    private final Color selectedFg;
    private final Color selectedBg;
    private final Color fg;
    private final Color bg;
    private final Color disabledFg;
    private final Color disabledBg;
    private Color drawFg;
    private Color drawBg;

    public GoodComboBoxRenderer(JComboBox box, Color selectedFg, Color selectedBg, Color fg, Color bg, Color disabledFg, Color disabledBg) {
        this.box = box;
        this.selectedFg = selectedFg;
        this.selectedBg = selectedBg;
        this.fg = fg;
        this.bg = bg;
        this.disabledFg = disabledFg;
        this.disabledBg = disabledBg;
    }
    
    @Override
    public void paint(Graphics g) {
        setBackground(drawBg);
        setForeground(drawFg);
        super.paint(g);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
        setComponentOrientation(list.getComponentOrientation());
        
        if (!box.isEnabled()) {
            setBackground(disabledBg);
            setForeground(disabledFg);
            drawBg = disabledBg;
            drawFg = disabledFg;
        } else if (isSelected || cellHasFocus) {
            setBackground(selectedBg);
            setForeground(selectedFg);
            drawBg = selectedBg;
            drawFg = selectedFg;
        } else {
            setBackground(bg);
            setForeground(fg);
            drawBg = bg;
            drawFg = fg;
        }
        if (index == -1 && box.isFocusOwner()) {
            setBackground(selectedBg);
            setForeground(selectedFg);
            drawBg = selectedBg;
            drawFg = selectedFg;
        }

        if (value instanceof Icon) {
            setIcon((Icon) value);
            setText("");
        } else {
            setIcon(null);
            setText((value == null) ? "" : value.toString());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());

        Border border = null;
        if (isSelected) {
            border = SELECTED_BORDER;
        } else if (cellHasFocus) {
            border = FOCUS_BORDER;
        }
        setBorder(border);

        return this;
    }
    
    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     *
     * @since 1.5
     * @return <code>true</code> if the background is completely opaque
     *         and differs from the JList's background;
     *         <code>false</code> otherwise
     */
    @Override
    public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if (p != null) {
            p = p.getParent();
        }
        // p should now be the JList.
        boolean colorMatch = (back != null) && (p != null) &&
            back.equals(p.getBackground()) &&
                        p.isOpaque();
        return !colorMatch && super.isOpaque();
    }

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void validate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    *
    * @since 1.5
    */
    @Override
    public void invalidate() {}
//
//   /**
//    * Overridden for performance reasons.
//    * See the <a href="#override">Implementation Note</a>
//    * for more information.
//    *
//    * @since 1.5
//    */
//    @Override
//    public void repaint() {}
//
//   /**
//    * Overridden for performance reasons.
//    * See the <a href="#override">Implementation Note</a>
//    * for more information.
//    */
//    @Override
//    public void revalidate() {}
//   /**
//    * Overridden for performance reasons.
//    * See the <a href="#override">Implementation Note</a>
//    * for more information.
//    */
//    @Override
//    public void repaint(long tm, int x, int y, int width, int height) {}
//
//   /**
//    * Overridden for performance reasons.
//    * See the <a href="#override">Implementation Note</a>
//    * for more information.
//    */
//    @Override
//    public void repaint(Rectangle r) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if (propertyName.equals("text") || ((propertyName.equals("font") || propertyName.equals("foreground"))
                    && oldValue != newValue
                    && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
}
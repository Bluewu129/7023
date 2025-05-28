package examblock.view.components;

import javax.swing.*;
import java.awt.*;

/**
 * A renderer that draws each alternate line in a listbox a different colour
 */
public class ListboxAlternatingRowRenderer extends DefaultListCellRenderer {

    /**
     * Background colour for even rows
     */
    private static final Color BACKGROUND_COLOR1 = new Color(240, 240, 240); // Light gray

    /**
     * Background colour for odd rows
     */
    private static final Color BACKGROUND_COLOR2 = Color.WHITE;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index,
                isSelected, cellHasFocus);
        if (isSelected) {
            c.setBackground(list.getSelectionBackground());
            c.setForeground(list.getSelectionForeground());
        } else {
            c.setBackground(index % 2 == 0 ? BACKGROUND_COLOR1 : BACKGROUND_COLOR2);
            c.setForeground(list.getForeground());
        }
        return c;
    }
}
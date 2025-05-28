package examblock.view.components;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

/**
 * Handler for inputting a double and have the TextBox take care of it
 */
public class DoubleTextField extends JTextField {

    /**
     * constructor
     */
    public DoubleTextField() {
        ((PlainDocument) getDocument()).setDocumentFilter(new DoubleFilter());
    }

    /**
     * set the double value
     *
     * @param value the value
     */
    public void setDouble(double value) {
        setText(String.valueOf(value));
    }

    /**
     * get the value from the TextField as a double
     *
     * @return the value
     */
    public double getDouble() {
        try {
            return Double.parseDouble(getText());
        } catch (NumberFormatException e) {
            return 0.0; // Or handle the error as needed
        }
    }

    private static class DoubleFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string,
                                 AttributeSet attr) throws BadLocationException {
            System.out.println("DoubleFilter.insertString: string=" + string);
            StringBuilder builder = new StringBuilder(string);
            for (int i = builder.length() - 1; i >= 0; i--) {
                char ch = builder.charAt(i);
                if (!Character.isDigit(ch) && ch != '.') {
                    builder.deleteCharAt(i);
                }
            }
            String filtered = builder.toString();
            System.out.println("DoubleFilter.insertString: filtered=" + filtered);
            super.insertString(fb, offset, filtered, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text,
                            AttributeSet attrs) throws BadLocationException {
            System.out.println("DoubleFilter.replace: text=" + text);
            if (text != null) {
                StringBuilder builder = new StringBuilder(text);
                for (int i = builder.length() - 1; i >= 0; i--) {
                    char ch = builder.charAt(i);
                    if (!Character.isDigit(ch) && ch != '.') {
                        builder.deleteCharAt(i);
                    }
                }
                text = builder.toString();
                System.out.println("DoubleFilter.replace: filtered=" + text);
            }
            super.replace(fb, offset, length, text, attrs);
        }
    }
}
package examblock.view.components;

import javax.swing.*;
import javax.swing.text.*;

/**
 * A JTextField that only accepts double values and provides spinner-like functionality.
 */
public class DoubleSpinnerField extends JTextField {
    private double minimum = Double.MIN_VALUE;
    private double maximum = Double.MAX_VALUE;
    private double stepSize = 0.1;

    public DoubleSpinnerField() {
        super();
        ((PlainDocument) getDocument()).setDocumentFilter(new DoubleFilter());
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
    }

    public void setDouble(double value) {
        setText(String.format("%.1f", value));
    }

    public double getDouble() {
        try {
            return Double.parseDouble(getText());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public void setColumns(int columns) {
        super.setColumns(columns);
    }

    private class DoubleFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string,
                                 AttributeSet attr) throws BadLocationException {
            StringBuilder builder = new StringBuilder(string);
            for (int i = builder.length() - 1; i >= 0; i--) {
                char ch = builder.charAt(i);
                if (!Character.isDigit(ch) && ch != '.' && ch != '-') {
                    builder.deleteCharAt(i);
                }
            }
            super.insertString(fb, offset, builder.toString(), attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text,
                            AttributeSet attrs) throws BadLocationException {
            if (text != null) {
                StringBuilder builder = new StringBuilder(text);
                for (int i = builder.length() - 1; i >= 0; i--) {
                    char ch = builder.charAt(i);
                    if (!Character.isDigit(ch) && ch != '.' && ch != '-') {
                        builder.deleteCharAt(i);
                    }
                }
                text = builder.toString();
            }
            super.replace(fb, offset, length, text, attrs);
        }
    }
}
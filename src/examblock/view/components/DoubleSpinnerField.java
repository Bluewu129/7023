package examblock.view.components;

import javax.swing.*;
import java.awt.*;

/**
 * A custom text field for entering and adjusting double values with spinner buttons.
 * Extends DoubleTextField to inherit double input validation and adds up/down buttons
 * to increment/decrement the value.
 * Values are rounded to one decimal place for display and arithmetic.
 * [14] Input validation for numeric spinner components.
 */
public class DoubleSpinnerField extends DoubleTextField {

    /**
     * The minimum allowed value for this spinner field.
     * Values below this minimum will be rejected when using the down button.
     */
    private double minimum = Double.NEGATIVE_INFINITY;

    /**
     * The maximum allowed value for this spinner field.
     * Values above this maximum will be rejected when using the up button.
     */
    private double maximum = Double.POSITIVE_INFINITY;

    /**
     * Constructs a new DoubleSpinnerField with default settings.
     * Initializes the text field for double input and adds
     * spinner buttons for incrementing/decrementing the value by 0.1.
     * The initial value is 0.0, displayed with one decimal place.
     * [14] Input validation through spinner button constraints.
     */
    public DoubleSpinnerField() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        buttonPanel.setPreferredSize(new Dimension(16, getPreferredSize().height));

        JButton upButton = new JButton("▲");
        upButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
        upButton.setMargin(new Insets(0, 0, 0, 0));
        upButton.setPreferredSize(new Dimension(16, 10));
        upButton.addActionListener(e -> {
            double currentValue = getDouble();
            double newValue = Math.round((currentValue + 0.1) * 10.0) / 10.0;
            if (newValue <= maximum) {
                setDouble(newValue);
            }
        });

        JButton downButton = new JButton("▼");
        downButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
        downButton.setMargin(new Insets(0, 0, 0, 0));
        downButton.setPreferredSize(new Dimension(16, 10));
        downButton.addActionListener(e -> {
            double currentValue = getDouble();
            double newValue = Math.round((currentValue - 0.1) * 10.0) / 10.0;
            if (newValue >= minimum) {
                setDouble(newValue);
            }
        });

        buttonPanel.add(upButton);
        buttonPanel.add(downButton);

        add(buttonPanel, BorderLayout.EAST);

        setDouble(0.0);
    }

    /**
     * Sets the value of the text field to the specified double, formatted to one decimal place.
     *[14] Input validation and formatting for double values.
     * @param value - the double value to set
     */
    @Override
    public void setDouble(double value) {
        super.setDouble(Math.round(value * 10.0) / 10.0);
    }

    /**
     * Sets the minimum allowed value for the spinner.
     * Values below this will be adjusted to the minimum.
     *
     * @param min - the minimum value
     */
    public void setMinimum(double min) {
        this.minimum = min;
    }

    /**
     * Sets the maximum allowed value for the spinner.
     * Values above this will be adjusted to the maximum.
     *
     * @param max - the maximum value
     */
    public void setMaximum(double max) {
        this.maximum = max;
    }

    /**
     * Returns the preferred size of the component, accounting for the text field and buttons.
     *
     * @return the preferred dimension
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension textSize = super.getPreferredSize();
        return new Dimension(textSize.width + 16, textSize.height);
    }
}

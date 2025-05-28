package examblock;

import examblock.controller.ExamBlockController;
import examblock.model.ExamBlockModel;
import examblock.view.ExamBlockView;
import examblock.view.components.Verbose;

import javax.swing.*;

/**
 * The class that runs the entire application
 */
public class App {

    /**
     * The main method for the whole application.
     * This method creates the controller which kicks off the entire application.
     *
     * @param args - command-line arguments. None are used in this app.
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(ExamBlockController::new);
    }
}

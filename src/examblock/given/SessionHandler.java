package examblock.given;

import examblock.model.*;
import examblock.view.components.DialogUtils;
import examblock.view.components.Verbose;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SessionHandler {

    /**
     * Schedule an exam into a venue
     *
     * @param model the model of the MVC
     * @param exam  the exam to schedule
     * @param venue the venue to hold the exam
     * @param aara  the aara status of the exam
     * @return true if the exam was scheduled, false if not.
     */
    public static boolean scheduleExam(ExamBlockModel model, Exam exam, Venue venue, boolean aara) {
        // Check if venue type matches requirements
        if (venue.isAara() != aara) {
            String message = aara ? "This is NOT an AARA venue. Please select an AARA venue for AARA students."
                    : "This is an AARA venue. Please select a non-AARA venue for regular students.";
            DialogUtils.showWarning(message);
            return false;
        }

        // Count students for this exam based on venue type
        int additionalStudents = 0;
        for (Student student : model.getStudents().all()) {
            if (student.isAara() == venue.isAara()) {
                // Check if student takes this subject
                for (Subject subject : student.getSubjectsList()) {
                    if (subject.equals(exam.getSubject())) {
                        additionalStudents++;
                        break;
                    }
                }
            }
        }

        // If no students need this exam in this venue type, warn the user
        if (additionalStudents == 0) {
            String venueType = venue.isAara() ? "AARA" : "non-AARA";
            DialogUtils.showWarning("No " + venueType + " students are taking " +
                    exam.getSubject().getTitle() + ".");
            return false;
        }

        // the number of available student desks in the venue.
        int numberSeats = venue.deskCount();

        // number of desks already allocated
        int currentStudents = model.getSessions().getExistingSessionTotal(venue, exam);

        // will we fit our extra students in here?
        if (sessionOverflow(numberSeats, currentStudents + additionalStudents)) {
            return false;
        }

        /* void */
        model.getSessions().getSessionNewTotal(venue, exam, additionalStudents);

        // Confirm scheduling
        String venueType = venue.isAara() ? "AARA " : "";
        String prompt = "CONFIRM scheduling the "
                + exam.getSubject().getTitle()
                + " exam into "
                + venueType
                + venue.venueId()
                + " (" + additionalStudents + " students)";

        if (DialogUtils.askQuestion(prompt) == JOptionPane.OK_OPTION) {
            model.getSessions().scheduleExam(venue, exam);
            return true;
        }
        return false;
    }

    private static boolean sessionOverflow(int numberSeats, int totalStudents) {
        if (numberSeats < totalStudents) {
            DialogUtils.showMessage("That venue only has " + numberSeats + " desks - "
                    + totalStudents
                    + " students won't fit in there!");
            return true;
        }
        return false;
    }

    /**
     * Allocate students to seats in the exams
     *
     * @param model the model of the MVC
     */
    public static void finaliseExamBlock(ExamBlockModel model) {

        model.getVenues().allocateStudents(model.getSessions(), model.getExams(), model.getStudents());

        // Get the save filename first.
        if (!model.saveToFile(model.getRegistry(), null, model.getTitle(), model.getVersion())) {
            return;
        }

        model.notifyObservers("finalised");
        String reportOutput = printEverything(model);

        // print the results
        String dateNow = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss").format(new Date());
        String fileName = model.getTitle()
                + " (v" + model.getVersion() + ") "
                + dateNow
                + "." + CSSE7023.FileType.EFR.getExtension();

        try {
            BufferedWriter bred = new BufferedWriter(new FileWriter(fileName));
            bred.write(reportOutput);
            bred.close();

            if (Verbose.isVerbose()) {
                DialogUtils.showMessage("Exam Block Finalise Report saved to file: " + fileName);
            }
        } catch (IOException io) {
            DialogUtils.showMessage("Unable to write Finalise Report to file: "
                    + io.getMessage());
        }
    }

    /**
     * prints everything to a popup viewing window.
     *
     * @param model the model of the MVC
     * @return the output for the window
     */
    public static String printEverything(ExamBlockModel model) {
        StringBuilder sb = new StringBuilder();
        String dateNow = new SimpleDateFormat("yyyy-MM-dd' at 'HH:mm:ss").format(new Date());
        sb.append("Here is the Exam Block ").append(System.lineSeparator());
        sb.append(model.getTitle()).append(" (version ").append(model.getVersion())
                .append(") on ").append(dateNow)
                .append(System.lineSeparator());
        sb.append("/====================================\\").append(System.lineSeparator());
        sb.append("|----------  ALL SUBJECTS  ----------|").append(System.lineSeparator());
        sb.append("\\====================================/").append(System.lineSeparator());

        sb.append(model.getSubjects().getFullDetail()).append(System.lineSeparator());

        sb.append("/================================\\").append(System.lineSeparator());
        sb.append("----------  ALL UNITS  ----------|").append(System.lineSeparator());
        sb.append("\\================================/").append(System.lineSeparator());

        sb.append(model.getUnits().getFullDetail()).append(System.lineSeparator());

        sb.append("/=================================\\").append(System.lineSeparator());
        sb.append("|----------  ALL EXAMS  ----------|").append(System.lineSeparator());
        sb.append("\\=================================/").append(System.lineSeparator());

        sb.append(model.getExams().getFullDetail()).append(System.lineSeparator());

        sb.append("/====================================\\").append(System.lineSeparator());
        sb.append("|----------  ALL STUDENTS  ----------|").append(System.lineSeparator());
        sb.append("\\====================================/").append(System.lineSeparator());

        sb.append(model.getStudents().getFullDetail()).append(System.lineSeparator());

        sb.append("=".repeat(60)).append(System.lineSeparator());

        model.getVenues().writeAllocations(sb, model.getSessions());
        String reportResult = sb.toString();

        DialogUtils.showTextViewer(reportResult, "Exam Block Viewer",
                DialogUtils.ViewerOptions.SCROLL,
                CSSE7023.FileType.EFR);

        return reportResult;
    }
}


package seedu.nursesched.shift;

import seedu.nursesched.exception.ExceptionMessage;
import seedu.nursesched.exception.NurseSchedException;
import seedu.nursesched.storage.ShiftStorage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Represents a work shift assigned to a nurse.
 * It stores details such as the start time, end time, date, and the assigned task.
 */
public class Shift {
    protected static ArrayList<Shift> shiftList = new ArrayList<>();
    private static final Logger logr = Logger.getLogger("Shift");

    private final LocalTime startTime;
    private final LocalTime endTime;
    private final LocalDate date;
    private final String shiftTask;
    private boolean isDone = false;
    private double overtimeHours = 0.0;

    static {
        try {
            shiftList = ShiftStorage.readFile();
        } catch (Exception e) {
            shiftList = new ArrayList<>();
            System.out.println("Failed to load shifts. Starting with empty list.");
            logr.warning("ShiftStorage.readFile failed: " + e.getMessage());
        }

        try {
            File logDir = new File("logs/shift");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            FileHandler fh = new FileHandler("logs/shift/shift.log", true);
            fh.setFormatter(new SimpleFormatter());
            logr.addHandler(fh);
            logr.setLevel(Level.ALL);
        } catch (IOException e) {
            System.out.println("Logger setup failed: " + e.getMessage());
        }
    }

    /**
     * Constructs a Shift object with specified details.
     *
     * @param startTime The start time of the shift.
     * @param endTime   The end time of the shift.
     * @param date      The date on which the shift occurs.
     * @param shiftTask The task assigned during the shift.
     */
    public Shift(LocalTime startTime, LocalTime endTime, LocalDate date, String shiftTask) {
        assert startTime != null : "Start time cannot be null";
        assert endTime != null : "End time cannot be null";
        assert date != null : "Date cannot be null";
        assert shiftTask != null && !shiftTask.isEmpty() : "Shift task cannot be null or empty";
        assert startTime.isBefore(endTime) : "Start time must be before end time";

        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.shiftTask = shiftTask;
        logr.info("Shift created: " + this);
    }

    /**
     * Checks whether a new shift would overlap with any existing shift on the same date.
     * A shift is considered overlapping if its time range intersects with any other shift's time range,
     * except when the new shift starts exactly when an existing one ends, or ends exactly when another starts.
     *
     * @param newStart     The proposed start time of the new or updated shift.
     * @param newEnd       The proposed end time of the new or updated shift.
     * @param date         The date on which the shift would occur.
     * @param ignoreIndex  The index of a shift to ignore during the check (used when editing an existing shift),
     *                     or -1 if no shift should be ignored (used when adding).
     * @return {@code true} if the new shift overlaps with any existing shift
     *         (excluding the one at {@code ignoreIndex}); {@code false} otherwise.
     */
    private static boolean hasOverlap(LocalTime newStart, LocalTime newEnd, LocalDate date, int ignoreIndex) {
        for (int i = 0; i < shiftList.size(); i++) {
            if (i == ignoreIndex) {
                continue;
            }

            Shift existing = shiftList.get(i);
            if (!existing.getDate().equals(date)) {
                continue;
            }

            // Check Overlap
            boolean overlaps = !(newEnd.compareTo(existing.getStartTime()) <= 0 ||
                    newStart.compareTo(existing.getEndTime()) >= 0);
            if (overlaps) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new shift to the shift list.
     *
     * @param startTime The start time of the shift.
     * @param endTime   The end time of the shift.
     * @param date      The date of the shift.
     * @param shiftTask The task assigned during the shift.
     */
    public static void addShift(LocalTime startTime, LocalTime endTime, LocalDate date,
                                String shiftTask) throws NurseSchedException {
        if (date.isBefore(LocalDate.now())) {
            logr.warning("Attempted to add shift with past date: " + date);
            throw new NurseSchedException(ExceptionMessage.INVALID_SHIFT_DATE);
        }

        if (!startTime.isBefore(endTime)) {
            throw new NurseSchedException(ExceptionMessage.INVALID_START_TIME);
        }

        if (hasOverlap(startTime, endTime, date, -1)) {
            logr.warning("Attempted to add overlapping shift: " + startTime + " to " + endTime + " on " + date);
            throw new NurseSchedException(ExceptionMessage.SHIFT_TIMING_OVERLAP);
        }

        Shift shift = new Shift(startTime, endTime, date, shiftTask);
        shiftList.add(shift);
        ShiftStorage.overwriteSaveFile(shiftList);
        System.out.println("Shift added");
    }

    /**
     * Deletes a shift from the shift list based on the given index.
     *
     * @param index The index of the shift to be removed (0-based index).
     */
    public static void deleteShiftByIndex(int index) {
        assert index >= 0 : "Shift index cannot be negative";
        if (index < 0 || index >= shiftList.size()) {
            logr.warning("Attempted to delete shift with invalid index: " + index);
            System.out.println("Invalid shift index.");
            return;
        }
        Shift removedShift = shiftList.remove(index);
        ShiftStorage.overwriteSaveFile(shiftList);
        logr.info("Shift deleted: " + removedShift);
        System.out.println("Shift deleted.");
    }

    /**
     * Displays all shifts currently stored in the shift list.
     * If no shifts are available, it notifies the user.
     */
    public static void listShifts() {
        if (shiftList.isEmpty()) {
            System.out.println("No shifts available.");
            return;
        }

        System.out.println("List of all shifts:");
        for (int i = 0; i < shiftList.size(); i++) {
            Shift shift = shiftList.get(i);
            System.out.printf("%d. %s %n", i + 1, shift);
        }
    }

    public static void markShift(int index) {
        assert index >= 0 && index < shiftList.size() : "Index must be valid and within bounds!";
        try {
            Shift shift = shiftList.get(index);
            if (shift.getStatus()) {
                System.out.println("Shift #" + (index + 1) + " is already marked as done.");
                logr.info("Attempted to mark an already marked shift at index " + index);
                return;
            }

            shift.setDone(true);
            System.out.println("Marked shift as done!");
            System.out.println(shift);
            logr.info("Shift marked: " + shift);
            ShiftStorage.overwriteSaveFile(shiftList);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("There is no shift with index: " + (index + 1));
            logr.warning("There is no shift with index: " + (index + 1));
        }
    }

    /**
     * Unmarks a shift from the shift list (sets it as not done) based on the given index.
     *
     * @param index The index of the shift to be unmarked (0-based index).
     */
    public static void unmarkShift(int index) {
        assert index >= 0 && index < shiftList.size() : "Index must be valid and within bounds!";
        try {
            Shift shift = shiftList.get(index);
            if (!shift.getStatus()) {
                System.out.println("Shift #" + (index + 1) + " is already unmarked.");
                logr.info("Attempted to unmark an already unmarked shift at index " + index);
                return;
            }

            shift.setDone(false);
            System.out.println("Marked shift as undone!");
            logr.info("Shift unmarked: " + shift);
            ShiftStorage.overwriteSaveFile(shiftList);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("There is no shift with index: " + (index + 1));
            logr.warning("There is no shift with index: " + (index + 1));
        }
    }

    /**
     * Edits the details of an existing shift at the given index.
     * Only non-null parameters will be updated; any null value means "no change".
     *
     * @param index        The index of the shift to edit (0-based).
     * @param newStartTime The new start time, or {@code null} to keep existing.
     * @param newEndTime   The new end time, or {@code null} to keep existing.
     * @param newDate      The new date, or {@code null} to keep existing.
     * @param newTask      The new task description, or {@code null} to keep existing.
     * @throws NurseSchedException If validation fails (e.g., invalid index, overlapping shift, etc.).
     */
    public static void editShift(int index, LocalTime newStartTime, LocalTime newEndTime,
                                 LocalDate newDate, String newTask) throws NurseSchedException {
        if (index < 0 || index >= shiftList.size()) {
            logr.warning("Attempted to edit shift with invalid index: " + index);
            throw new NurseSchedException(ExceptionMessage.INVALID_SHIFT_NUMBER);
        }

        Shift original = shiftList.get(index);

        LocalTime updatedStart = (newStartTime != null) ? newStartTime : original.getStartTime();
        LocalTime updatedEnd = (newEndTime != null) ? newEndTime : original.getEndTime();
        LocalDate updatedDate = (newDate != null) ? newDate : original.getDate();
        String updatedTask = (newTask != null && !newTask.isEmpty()) ? newTask : original.getShiftTask();

        if (updatedDate.isBefore(LocalDate.now())) {
            logr.warning("Attempted to edit shift to a past date: " + updatedDate);
            throw new NurseSchedException(ExceptionMessage.INVALID_SHIFT_DATE);
        }

        if (!updatedStart.isBefore(updatedEnd)) {
            throw new NurseSchedException(ExceptionMessage.INVALID_START_TIME);
        }

        if (hasOverlap(updatedStart, updatedEnd, updatedDate, index)) {
            logr.warning("Attempted to edit shift to overlapping time: " + updatedStart + " to " + updatedEnd);
            throw new NurseSchedException(ExceptionMessage.SHIFT_TIMING_OVERLAP);
        }

        Shift updated = new Shift(updatedStart, updatedEnd, updatedDate, updatedTask);
        updated.setDone(original.getStatus());
        updated.setOvertimeHours(original.getOvertimeHours());

        shiftList.set(index, updated);
        ShiftStorage.overwriteSaveFile(shiftList);
        System.out.println("Shift updated:");
        System.out.println(updated);
        logr.info("Shift updated at index " + index + ": " + updated);
    }

    /**
     * Logs the specified overtime hours for a shift at the given index.
     *
     * @param index The index of the shift to log overtime for (0-based).
     * @param hours The number of overtime hours to log. Must be non-negative.
     */
    public static void logOvertime(int index, double hours) {
        if (index < 0 || index >= shiftList.size()) {
            System.out.println("Invalid shift index.");
            return;
        }
        if (hours < 0) {
            System.out.println("Overtime cannot be negative.");
            return;
        }
        Shift shift = shiftList.get(index);
        shift.setOvertimeHours(hours);
        System.out.println("Logged overtime: " + hours + "h for shift:");
        System.out.println(shift);
        logr.info("Overtime logged for shift " + index + ": " + hours + "h");
        ShiftStorage.overwriteSaveFile(shiftList);
    }

    /**
     * Sorts the shift list in chronological order, first by date, then by start time.
     * Updates the list in place and prints confirmation.
     */
    public static void sortShiftsChronologically() {
        shiftList.sort(Comparator.comparing(Shift::getDate).thenComparing(Shift::getStartTime));
        System.out.println("Shifts sorted by date and start time.");
    }

    /**
     * Sets the done status of the shift.
     *
     * @param done True if the shift is marked as completed; false otherwise.
     */
    public void setDone(boolean done) {
        this.isDone = done;
    }

    /**
     * Returns the completion status of the shift.
     *
     * @return {@code true} if the shift is marked as done; {@code false} otherwise.
     */
    public boolean getStatus() {
        return this.isDone;
    }

    /**
     * Returns the completion status of the shift.
     *
     * @return {@code true} if the shift is marked as done; {@code false} otherwise.
     */
    public double getOvertimeHours() {
        return overtimeHours;
    }

    /**
     * Sets the number of overtime hours for the shift.
     *
     * @param hours The number of overtime hours to set. Must be non-negative.
     */
    public void setOvertimeHours(double hours) {
        this.overtimeHours = hours;
    }

    /**
     * Returns a formatted string representation of the shift details.
     *
     * @return A string describing the shift including start and end times, date, and task.
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedStartTime = startTime.format(formatter);
        String formattedEndTime = endTime.format(formatter);
        String markStatus = isDone ? "[X]" : "[ ]";

        String overtimeDisplay = overtimeHours > 0 ? ", Overtime: " + overtimeHours + "h" : "";

        return markStatus + " From: " + formattedStartTime + ", " +
                "To: " + formattedEndTime + ", " +
                "Date: " + date + ", " +
                "shiftTask: " + shiftTask + overtimeDisplay;
    }

    /**
     * Retrieves the start time of the shift.
     *
     * @return The start time as a {@link LocalTime} object.
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Retrieves the end time of the shift.
     *
     * @return The end time as a {@link LocalTime} object.
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Retrieves the date of the shift.
     *
     * @return The date as a {@link LocalDate} object.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Retrieves the task assigned to the shift.
     *
     * @return The task description as a {@link String}.
     */
    public String getShiftTask() {
        return shiftTask;
    }

    /**
     * Retrieves the list of all stored shifts.
     *
     * @return An {@code ArrayList<Shift>} containing all shifts.
     */
    public static ArrayList<Shift> getShiftList() {
        return shiftList;
    }
}

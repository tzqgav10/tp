package seedu.nursesched.parser;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import seedu.nursesched.exception.ExceptionMessage;
import seedu.nursesched.exception.NurseSchedException;

/**
 * The {@code ShiftParser} class is responsible for parsing shift-related commands.
 * It extracts the necessary details to create or delete a shift.
 */
public class ShiftParser extends Parser {
    private static final Logger logr = Logger.getLogger("ShiftParser");

    private final String command;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final LocalDate date;
    private final String shiftTask;
    private final int shiftIndex;

    static {
        try {
            LogManager.getLogManager().reset();
            FileHandler fh = new FileHandler("logs/parser/shiftParser.log", true);
            fh.setFormatter(new SimpleFormatter());
            logr.addHandler(fh);
            logr.setLevel(Level.ALL);
        } catch (IOException e) {
            logr.log(Level.SEVERE, "File logger not working", e);
        }
    }

    /**
     * Constructs a {@code ShiftParser} object with extracted shift details.
     *
     * @param command    The command type (either "add" or "del").
     * @param startTime  The start time of the shift.
     * @param endTime    The end time of the shift.
     * @param date       The date of the shift.
     * @param shiftTask  The task assigned during the shift.
     * @param shiftIndex The index of the shift (for deletion).
     */
    public ShiftParser(String command, LocalTime startTime, LocalTime endTime,
                       LocalDate date, String shiftTask, int shiftIndex) throws NurseSchedException {
        assert command != null : "Command should not be null";
        assert shiftIndex >= 0 : "Shift index should not be negative";

        this.command = command;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.shiftTask = shiftTask;
        this.shiftIndex = shiftIndex;

        logr.info("ShiftParser created: " + this);
    }

    /**
     * Parses the input command and extracts shift-related details.
     *
     * @param line The input command string.
     * @return A {@code ShiftParser} object containing the extracted shift details.
     * @throws NurseSchedException If the input is invalid or incorrectly formatted.
     */
    public static ShiftParser extractInputs(String line) throws NurseSchedException {
        assert line != null : "Input line should not be null";
        logr.info("Extracting inputs from: " + line);

        if (line == null || line.trim().isEmpty()) {
            logr.warning("Input is empty.");
            throw new NurseSchedException(ExceptionMessage.INPUT_EMPTY);
        }

        line = line.trim().replaceAll("\\s+", " ").toLowerCase();

        String[] parts = line.split(" ", 2);
        if (parts.length < 2) {
            logr.warning("Invalid input format: " + line);
            throw new NurseSchedException(ExceptionMessage.INVALID_FORMAT);
        }

        String remaining = parts[1];

        // Variables for extracted values
        String command = "";
        int shiftIndex = 0;
        LocalTime startTime = null;
        LocalTime endTime = null;
        LocalDate date = null;
        String shiftTask = "";

        try {
            // Extract actual command
            String[] commandParts = remaining.split(" ", 2);
            command = commandParts[0];
            remaining = (commandParts.length > 1) ? commandParts[1] : "";

            if (command.equals("add")) {
                return getShiftAddParser(remaining, command, shiftIndex);

            } else if (command.equals("del")) {
                return getShiftDelParser(remaining, command, startTime, endTime, date, shiftTask);

            }  else if (command.equals("mark") || command.equals("unmark")) {
                return getShiftMarkParser(remaining, command);

            }  else if (command.equals("list")) {
                return new ShiftParser("list", null, null, null, "", 0);

            } else if (command.equals("edit")) {
                return getShiftEditParser(remaining, command);

            } else if (command.equals("sort")) {
                return new ShiftParser("sort", null, null, null, "", 0);

            } else if (command.equals("logot")) {
                return getShiftOvertimeParser(remaining, command);

            } else {
                logr.warning("Invalid command: " + command);
                throw new NurseSchedException(ExceptionMessage.INVALID_COMMAND);
            }


        } catch (NurseSchedException e) {
            logr.severe("Parsing error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Parses a log overtime command and extracts the shift index and overtime hours.
     *
     * @param remaining The remaining command string containing the parameters.
     * @param command   The command type, e.g., "logot".
     * @return A {@code ShiftParser} object containing overtime details.
     * @throws NurseSchedException If the format is invalid or values are improperly specified.
     */
    private static ShiftParser getShiftOvertimeParser(String remaining, String command) throws NurseSchedException {
        logr.info("Parsing logot command: " + remaining);

        if (!remaining.contains("id/") || !remaining.contains("h/")) {
            throw new NurseSchedException(ExceptionMessage.INVALID_SHIFTLOGOT_FORMAT);
        }

        int index;
        double hours;

        try {
            index = Integer.parseInt(extractEditValue(remaining, "id/")) - 1;
        } catch (NumberFormatException e) {
            throw new NurseSchedException(ExceptionMessage.INVALID_SHIFT_NUMBER);
        }

        try {
            hours = Double.parseDouble(extractEditValue(remaining, "h/"));
        } catch (NumberFormatException e) {
            throw new NurseSchedException(ExceptionMessage.INVALID_SHIFTLOGOT_FORMAT);
        }

        return new ShiftParser(command, null, null, null, String.valueOf(hours), index);
    }

    /**
     * Parses an edit shift command and extracts updated shift details.
     * Only fields provided in the command will be parsed; missing fields will be set to {@code null}
     * and treated as unchanged in the edit operation.
     *
     * Requires: {@code id/} to specify which shift to edit.
     * Optional: {@code s/} (start time), {@code e/} (end time), {@code d/} (date), {@code st/} (task).
     *
     * @param remaining The remaining command string.
     * @param command   The command type ("edit").
     * @return A ShiftParser object with parsed values (nulls for omitted ones).
     * @throws NurseSchedException If format is invalid or values are malformed.
     */
    private static ShiftParser getShiftEditParser(String remaining, String command) throws NurseSchedException {
        assert remaining != null : "Remaining command should not be null";
        logr.info("Parsing edit command (partial supported): " + remaining);

        int shiftIndex;
        LocalDate date = null;
        LocalTime startTime = null;
        LocalTime endTime = null;
        String shiftTask = null;

        String idStr = extractEditValue(remaining, "id/");
        if (idStr == null || idStr.isEmpty()) {
            throw new NurseSchedException(ExceptionMessage.INVALID_SHIFTEDIT_FORMAT);
        }

        try {
            shiftIndex = Integer.parseInt(idStr) - 1;
            if (shiftIndex < 0) {
                throw new NurseSchedException(ExceptionMessage.INVALID_SHIFT_NUMBER);
            }
        } catch (NumberFormatException e) {
            throw new NurseSchedException(ExceptionMessage.INVALID_SHIFT_NUMBER);
        }

        String sStr = extractEditValue(remaining, "s/");
        if (sStr != null) {
            if (sStr.isEmpty()) {
                throw new NurseSchedException(ExceptionMessage.INVALID_TIME_FORMAT);
            }
            try {
                startTime = LocalTime.parse(sStr);
            } catch (DateTimeParseException e) {
                throw new NurseSchedException(ExceptionMessage.INVALID_TIME_FORMAT);
            }
        }

        String eStr = extractEditValue(remaining, "e/");
        if (eStr != null) {
            if (eStr.isEmpty()) {
                throw new NurseSchedException(ExceptionMessage.INVALID_TIME_FORMAT);
            }
            try {
                endTime = LocalTime.parse(eStr);
            } catch (DateTimeParseException e) {
                throw new NurseSchedException(ExceptionMessage.INVALID_TIME_FORMAT);
            }
        }

        String dStr = extractEditValue(remaining, "d/");
        if (dStr != null) {
            if (dStr.isEmpty()) {
                throw new NurseSchedException(ExceptionMessage.INVALID_DATE_FORMAT);
            }
            try {
                date = LocalDate.parse(dStr);
            } catch (DateTimeParseException e) {
                throw new NurseSchedException(ExceptionMessage.INVALID_DATE_FORMAT);
            }
        }

        String taskStr = extractEditValue(remaining, "st/");
        if (taskStr != null) {
            shiftTask = taskStr.trim();
            if (shiftTask.isEmpty()) {
                throw new NurseSchedException(ExceptionMessage.SHIFT_TASK_EMPTY);
            }
        }

        // Reject if no update fields are provided
        if (startTime == null && endTime == null && date == null && shiftTask == null) {
            throw new NurseSchedException(ExceptionMessage.INVALID_SHIFTEDIT_FORMAT);
        }

        return new ShiftParser(command, startTime, endTime, date, shiftTask, shiftIndex);
    }

    /**
     * Parses a mark or unmark shift command and extracts the shift index.
     *
     * @param remaining The remaining command string.
     * @param command   The command type (either "mark" or "unmark").
     * @return A ShiftParser object containing the shift index.
     * @throws NurseSchedException If the format is incorrect or shift index is invalid.
     */
    private static ShiftParser getShiftMarkParser(String remaining, String command)
            throws NurseSchedException {
        assert remaining != null : "Remaining command should not be null";
        logr.info("Parsing mark/unmark command: " + remaining);
        int shiftIndex;

        if (!remaining.contains("id/")) {
            if (command.equals("mark")) {
                throw new NurseSchedException(ExceptionMessage.INVALID_SHIFTMARK_FORMAT);
            } else {
                throw new NurseSchedException(ExceptionMessage.INVALID_SHIFTUNMARK_FORMAT);
            }
        }

        try {
            shiftIndex = Integer.parseInt(extractValue(remaining, "id/", null)) - 1;
            if (shiftIndex < 0) {
                throw new NurseSchedException(ExceptionMessage.INVALID_SHIFT_NUMBER);
            }
        } catch (NumberFormatException e) {
            throw new NurseSchedException(ExceptionMessage.INVALID_SHIFT_NUMBER);
        }

        return new ShiftParser(command, null, null, null, "", shiftIndex);
    }

    /**
     * Parses a delete shift command and extracts the shift index.
     *
     * @param remaining The remaining command string.
     * @param command   The command type.
     * @param startTime Placeholder for start time (not used).
     * @param endTime   Placeholder for end time (not used).
     * @param date      Placeholder for date (not used).
     * @param shiftTask Placeholder for shift task (not used).
     * @return A ShiftParser object containing the shift index.
     * @throws NurseSchedException If the format is incorrect or shift index is invalid.
     */
    private static ShiftParser getShiftDelParser(String remaining, String command, LocalTime startTime,
                                                 LocalTime endTime, LocalDate date,
                                                 String shiftTask) throws NurseSchedException {
        assert remaining != null : "Remaining command should not be null";
        logr.info("Parsing delete command: " + remaining);
        int shiftIndex;

        if (!remaining.contains("id/")) {
            logr.warning("Invalid delete format.");
            throw new NurseSchedException(ExceptionMessage.INVALID_SHIFTDEL_FORMAT);
        }
        try {
            shiftIndex = Integer.parseInt(extractValue(remaining, "id/", null)) - 1;
            if (shiftIndex < 0) {
                logr.warning("Invalid shift index: " + shiftIndex);
                throw new NurseSchedException(ExceptionMessage.INVALID_SHIFT_NUMBER);
            }
        } catch (NumberFormatException e) {
            logr.warning("Invalid shift index format.");
            throw new NurseSchedException(ExceptionMessage.INVALID_SHIFT_NUMBER);
        }

        return new ShiftParser(command, startTime, endTime, date, shiftTask, shiftIndex);
    }

    /**
     * Parses an add shift command and extracts the shift details.
     *
     * @param remaining  The remaining command string.
     * @param command    The command type.
     * @param shiftIndex Placeholder for shift index (not used).
     * @return A ShiftParser object containing shift details.
     * @throws NurseSchedException If the format is incorrect or values are invalid.
     */
    private static ShiftParser getShiftAddParser(String remaining, String command, int shiftIndex)
            throws NurseSchedException {
        assert remaining != null : "Remaining command should not be null";
        logr.info("Parsing add command: " + remaining);

        LocalDate date;
        LocalTime startTime;
        LocalTime endTime;
        String shiftTask;

        // Ensure all required markers exist
        if (!remaining.contains("s/") || !remaining.contains("e/") ||
                !remaining.contains("d/") || !remaining.contains("st/")) {
            logr.warning("Invalid add format.");
            throw new NurseSchedException(ExceptionMessage.INVALID_SHIFTADD_FORMAT);
        }

        try {
            startTime = LocalTime.parse(extractValue(remaining, "s/", "e/"));
            endTime = LocalTime.parse(extractValue(remaining, "e/", "d/"));
        } catch (DateTimeParseException e) {
            throw new NurseSchedException(ExceptionMessage.INVALID_TIME_FORMAT);
        }

        try {
            date = LocalDate.parse(extractValue(remaining, "d/", "st/"));
        } catch (DateTimeParseException e) {
            throw new NurseSchedException(ExceptionMessage.INVALID_DATE_FORMAT);
        }

        shiftTask = extractValue(remaining, "st/", null);
        if (shiftTask.isEmpty()) {
            logr.warning("Shift task is empty.");
            throw new NurseSchedException(ExceptionMessage.SHIFT_TASK_EMPTY);
        }

        // Validate start time before end time
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            logr.warning("Invalid start/end time: " + startTime + " - " + endTime);
            throw new NurseSchedException(ExceptionMessage.INVALID_START_TIME);
        }

        return new ShiftParser(command, startTime, endTime, date, shiftTask, shiftIndex);
    }

    /**
     * Extracts a value from a command string between specified markers.
     *
     * @param input       The command string.
     * @param startMarker The starting marker for the value.
     * @param endMarker   The ending marker for the value (can be {@code null}).
     * @return The extracted value as a {@code String}, or an empty string if the value is not found.
     */
    private static String extractValue(String input, String startMarker, String endMarker) {
        assert input != null : "Input string must not be null";
        assert startMarker != null : "Start marker must not be null";

        int start = input.indexOf(startMarker);
        if (start == -1) {
            return "";
        }

        start += startMarker.length();
        int end = (endMarker != null) ? input.indexOf(endMarker, start) : -1;

        return (end == -1) ? input.substring(start).trim() : input.substring(start, end).trim();
    }

    /**
     * Extracts a value from a command string using a specific prefix.
     * Designed for parsing commands where each argument starts with a unique prefix (e.g., id/, s/, e/, d/, st/).
     * This method is especially suited for commands with no strict ordering or with flexible spacing.
     *
     * @param input  The full command string (e.g., "id/1 s/09:00 e/11:00 d/2025-04-03 st/task").
     * @param prefix The prefix to locate and extract the value for (e.g., "s/").
     * @return The extracted value associated with the prefix, or an empty string if not found.
     */
    private static String extractEditValue(String input, String prefix) {
        assert input != null : "Input string must not be null";
        assert prefix != null : "Prefix must not be null";

        String[] tokens = input.split("\\s+(?=\\w+/)");
        for (String token : tokens) {
            if (token.startsWith(prefix)) {
                return token.substring(prefix.length()).trim();
            }
        }
        return null;
    }

    /**
     * Gets the command type.
     *
     * @return The command type as a {@code String}.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets the start time of the shift.
     *
     * @return The start time as a {@code LocalTime} object.
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Gets the end time of the shift.
     *
     * @return The end time as a {@code LocalTime} object.
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Gets the date of the shift.
     *
     * @return The date as a {@code LocalDate} object.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Gets the task assigned during the shift.
     *
     * @return The shift task as a {@code String}.
     */
    public String getNotes() {
        return shiftTask;
    }

    /**
     * Gets the index of the shift.
     *
     * @return The shift index as an {@code int}.
     */
    public int getIndex() {
        return shiftIndex;
    }

    /**
     * Gets the shift task directly.
     *
     * @return The task string associated with the shift.
     */
    public String getShiftTask() {
        return shiftTask;
    }
}

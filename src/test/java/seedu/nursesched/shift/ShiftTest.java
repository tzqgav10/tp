package seedu.nursesched.shift;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.nursesched.exception.NurseSchedException;
import seedu.nursesched.parser.ShiftParser;

class ShiftTest {
    private final List<Shift> shiftList = new ArrayList<>();

    @Test
    void addShift_shiftListAdd_expectCorrectOutput() throws NurseSchedException {
        String inputString = "shift add s/10:00 e/11:00 d/2004-01-01 st/test";
        ShiftParser shiftParser = ShiftParser.extractInputs(inputString);
        assertNotNull(shiftParser);

        shiftList.add(new Shift(shiftParser.getStartTime(), shiftParser.getEndTime(), shiftParser.getDate(),
                shiftParser.getShiftTask()));

        LocalTime startTime = shiftParser.getStartTime();
        LocalTime endTime = shiftParser.getEndTime();
        LocalDate date = shiftParser.getDate();
        String shiftTask = String.valueOf(shiftParser.getShiftTask());

        assertEquals(1, shiftList.size());
        Shift addedShift = shiftList.get(0);
        assertEquals(startTime, addedShift.getStartTime());
        assertEquals(endTime, addedShift.getEndTime());
        assertEquals(date, addedShift.getDate());
        assertEquals(shiftTask, addedShift.getShiftTask());
    }

    @Test
    void deleteShiftByIndex() throws NurseSchedException {
        String inputStringDelete = "shift del sn/1";
        String inputStringAdd = "shift add s/10:00 e/11:00 d/2004-01-01 st/test";

        ShiftParser shiftParserAdd = ShiftParser.extractInputs(inputStringAdd);
        assertNotNull(shiftParserAdd);

        shiftList.add(new Shift(shiftParserAdd.getStartTime(), shiftParserAdd.getEndTime(), shiftParserAdd.getDate(),
                shiftParserAdd.getShiftTask()));

        assertEquals(1, shiftList.size());

        ShiftParser shiftParserDelete = ShiftParser.extractInputs(inputStringDelete);
        shiftList.remove(shiftParserDelete.getIndex());

        assertEquals(0, shiftList.size());
    }

    @Test
    void listShifts() {
    }

    @Test
    void testToString() throws NurseSchedException {
        String inputString = "shift add s/10:00 e/11:00 d/2004-01-01 st/test";

        ShiftParser shiftParserAdd = ShiftParser.extractInputs(inputString);
        assertNotNull(shiftParserAdd);

        shiftList.add(new Shift(shiftParserAdd.getStartTime(), shiftParserAdd.getEndTime(), shiftParserAdd.getDate(),
                shiftParserAdd.getShiftTask()));

        String expected = "[ ] From: 10:00, To: 11:00, Date: 2004-01-01, shiftTask: test";

        assertEquals(expected, shiftList.get(0).toString(), "Shift toString output is incorrect!");
    }

    @Test
    void editShift_validIndex_shiftUpdatedCorrectly() throws NurseSchedException {
        ShiftParser parser = ShiftParser.extractInputs("shift add s/09:00 e/10:00 d/2004-01-01 st/original");
        Shift shift = new Shift(parser.getStartTime(), parser.getEndTime(), parser.getDate(), parser.getShiftTask());
        shiftList.add(shift);

        String inputStringEdit = "shift edit sn/1 s/11:00 e/12:00 d/2004-01-02 st/edited";
        ShiftParser editParser = ShiftParser.extractInputs(inputStringEdit);

        Shift editedShift = new Shift(
                editParser.getStartTime(),
                editParser.getEndTime(),
                editParser.getDate(),
                editParser.getShiftTask()
        );
        shiftList.set(editParser.getIndex(), editedShift);

        // Assertions
        Shift result = shiftList.get(0);
        assertEquals(LocalTime.of(11, 0), result.getStartTime());
        assertEquals(LocalTime.of(12, 0), result.getEndTime());
        assertEquals(LocalDate.of(2004, 1, 2), result.getDate());
        assertEquals("edited", result.getShiftTask());
    }

    @Test
    void markAndUnmarkShift_statusChangesCorrectly() throws NurseSchedException {
        ShiftParser parser = ShiftParser.extractInputs("shift add s/10:00 e/11:00 d/2004-01-01 st/test");
        Shift shift = new Shift(parser.getStartTime(), parser.getEndTime(), parser.getDate(), parser.getShiftTask());
        shiftList.add(shift);

        shift.setDone(true);
        assertEquals(true, shift.getStatus(), "Shift should be marked as done");

        shift.setDone(false);
        assertEquals(false, shift.getStatus(), "Shift should be unmarked (not done)");
    }

    @Test
    void shiftGetters_returnCorrectValues() throws NurseSchedException {
        ShiftParser parser = ShiftParser.extractInputs("shift add s/08:00 e/10:00 d/2004-01-01 st/rounds");
        Shift shift = new Shift(parser.getStartTime(), parser.getEndTime(), parser.getDate(), parser.getShiftTask());

        assertEquals(LocalTime.of(8, 0), shift.getStartTime());
        assertEquals(LocalTime.of(10, 0), shift.getEndTime());
        assertEquals(LocalDate.of(2004, 1, 1), shift.getDate());
        assertEquals("rounds", shift.getShiftTask());
        assertEquals(false, shift.getStatus());
    }
}

package seedu.nursesched.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import parser.PatientParser;
import patient.Patient;

public class ParserTest {
    @Test
    public void testExtractInputs_PatientAddCommand() {
        String input = "pf add p/Jean Doe a/25 n/Allergic to penicillin";
        PatientParser patientParser = PatientParser.extractInputs(input);

        assertNotNull(patientParser);
        assertEquals("add", patientParser.getCommand());
        assertEquals("Jean Doe", patientParser.getName());
        assertEquals("25", patientParser.getAge());
        assertEquals("Allergic to penicillin", patientParser.getNotes());
        assertEquals(-1, patientParser.getIndex());
    }

    @Test
    public void testExtractInputs_PatientDeleteCommand() {
        String input = "pf del 2";
        Patient patientOne = new Patient("Jean Doe", "25", "Allergic to penicillin");
        Patient patientTwo = new Patient("John Doe", "40", "Allergic to peanuts");
        Patient.addPatient(patientOne);
        Patient.addPatient(patientTwo);

        PatientParser patientParser = PatientParser.extractInputs(input);

        assertNotNull(patientParser);
        assertEquals("del", patientParser.getCommand());
        assertEquals("", patientParser.getName());
        assertEquals("", patientParser.getAge());
        assertEquals("", patientParser.getNotes());
        assertEquals(2, patientParser.getIndex());
    }

    @Test
    public void testExtractInputs_PatientDeleteInvalidIndexCommand() {
        String input = "pf del 2";

        PatientParser patientParser = PatientParser.extractInputs(input);

        assertNull(patientParser);
    }

    @Test
    public void testExtractInputs_PatientListCommand() {
        String input = "pf list";
        PatientParser patientParser = PatientParser.extractInputs(input);

        assertNotNull(patientParser);
        assertEquals("list", patientParser.getCommand());
        assertEquals("", patientParser.getName());
        assertEquals("", patientParser.getAge());
        assertEquals("", patientParser.getNotes());
        assertEquals(-1, patientParser.getIndex());
    }

    @Test
    public void testExtractInputs_PatientInvalidCommand() {
        String input = "pf invalid";
        PatientParser patientParser = PatientParser.extractInputs(input);

        assertNull(patientParser);
    }

    @Test
    public void testExtractInputs_PatientList() {
        String input = "pf list";
        PatientParser patientParser = PatientParser.extractInputs(input);

        assertNotNull(patientParser);
    }

    @Test
    public void testExtractInputs_PatientMissingParameters() {
        String input = "pf add p/Jean Doe a/25";
        PatientParser patientParser = PatientParser.extractInputs(input);

        assertNull(patientParser);
    }

    @Test
    public void testExtractInputs_PatientEmptyInput() {
        String input = "";
        PatientParser patientParser = PatientParser.extractInputs(input);

        assertNull(patientParser);
    }
}

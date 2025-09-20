package fr.farmvivi.fluxcord.plugins.music.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TimeParser.
 */
public class TimeParserTest {
    
    @Test
    void testParseTimeSeconds() {
        assertEquals(30000, TimeParser.parseTime("30"));
        assertEquals(90000, TimeParser.parseTime("90"));
        assertEquals(120000, TimeParser.parseTime("120"));
    }
    
    @Test
    void testParseTimeWithUnits() {
        assertEquals(30000, TimeParser.parseTime("30s"));
        assertEquals(60000, TimeParser.parseTime("1m"));
        assertEquals(90000, TimeParser.parseTime("1m30s"));
        assertEquals(3600000, TimeParser.parseTime("1h"));
        assertEquals(3660000, TimeParser.parseTime("1h1m"));
        assertEquals(3690000, TimeParser.parseTime("1h1m30s"));
        assertEquals(5400000, TimeParser.parseTime("1h30m"));
    }
    
    @Test
    void testParseTimeInvalid() {
        assertEquals(-1, TimeParser.parseTime(null));
        assertEquals(-1, TimeParser.parseTime(""));
        assertEquals(-1, TimeParser.parseTime("abc"));
        assertEquals(-1, TimeParser.parseTime("1x30y"));
    }
    
    @Test
    void testFormatTime() {
        assertEquals("0:30", TimeParser.formatTime(30000));
        assertEquals("1:30", TimeParser.formatTime(90000));
        assertEquals("10:00", TimeParser.formatTime(600000));
        assertEquals("1:00:00", TimeParser.formatTime(3600000));
        assertEquals("1:30:45", TimeParser.formatTime(5445000));
        assertEquals("∞", TimeParser.formatTime(-1));
    }
    
    @Test
    void testFormatTimeShort() {
        assertEquals("30s", TimeParser.formatTimeShort(30000));
        assertEquals("1m 30s", TimeParser.formatTimeShort(90000));
        assertEquals("10m", TimeParser.formatTimeShort(600000));
        assertEquals("1h", TimeParser.formatTimeShort(3600000));
        assertEquals("1h 30m 45s", TimeParser.formatTimeShort(5445000));
        assertEquals("∞", TimeParser.formatTimeShort(-1));
    }
}
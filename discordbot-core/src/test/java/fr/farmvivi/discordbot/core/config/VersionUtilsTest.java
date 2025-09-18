package fr.farmvivi.discordbot.core.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for VersionUtils class.
 */
class VersionUtilsTest {
    
    @Test
    void testValidVersions() {
        assertTrue(VersionUtils.isValidVersion("1.0.0"));
        assertTrue(VersionUtils.isValidVersion("1.2.3"));
        assertTrue(VersionUtils.isValidVersion("10.20.30"));
        assertTrue(VersionUtils.isValidVersion("1.0.0-alpha"));
        assertTrue(VersionUtils.isValidVersion("1.0.0-alpha.1"));
        assertTrue(VersionUtils.isValidVersion("1.0.0+build.1"));
        assertTrue(VersionUtils.isValidVersion("1.0.0-alpha+build.1"));
        assertTrue(VersionUtils.isValidVersion("2.3.27-SNAPSHOT"));
    }
    
    @Test
    void testInvalidVersions() {
        assertFalse(VersionUtils.isValidVersion("1.0"));
        assertFalse(VersionUtils.isValidVersion("1"));
        assertFalse(VersionUtils.isValidVersion("1.0.0.0"));
        assertFalse(VersionUtils.isValidVersion(""));
        assertFalse(VersionUtils.isValidVersion(null));
        assertFalse(VersionUtils.isValidVersion("abc"));
        assertFalse(VersionUtils.isValidVersion("1.0.abc"));
    }
    
    @Test
    void testVersionComparison() {
        assertEquals(0, VersionUtils.compareVersions("1.0.0", "1.0.0"));
        assertTrue(VersionUtils.compareVersions("1.0.1", "1.0.0") > 0);
        assertTrue(VersionUtils.compareVersions("1.0.0", "1.0.1") < 0);
        assertTrue(VersionUtils.compareVersions("1.1.0", "1.0.9") > 0);
        assertTrue(VersionUtils.compareVersions("2.0.0", "1.9.9") > 0);
        assertTrue(VersionUtils.compareVersions("1.0.0", "1.0.0-alpha") > 0);
        assertTrue(VersionUtils.compareVersions("1.0.0-alpha", "1.0.0-beta") < 0);
    }
    
    @Test
    void testVersionParsing() {
        VersionUtils.Version version = VersionUtils.parseVersion("1.2.3-alpha+build.1");
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(3, version.getPatch());
        assertEquals("alpha", version.getPreRelease());
        assertEquals("build.1", version.getBuildMetadata());
    }
    
    @Test
    void testVersionNormalization() {
        assertEquals("1.0.0", VersionUtils.normalizeVersion("1.0.0"));
        assertEquals("1.2.3", VersionUtils.normalizeVersion("  1.2.3  "));
        assertEquals("0.0.0", VersionUtils.normalizeVersion(""));
        assertEquals("0.0.0", VersionUtils.normalizeVersion(null));
        assertEquals("0.0.0", VersionUtils.normalizeVersion("invalid"));
        assertEquals("2.3.27-SNAPSHOT", VersionUtils.normalizeVersion("2.3.27-SNAPSHOT"));
    }
    
    @Test
    void testVersionToString() {
        VersionUtils.Version version1 = new VersionUtils.Version(1, 2, 3, null, null);
        assertEquals("1.2.3", version1.toString());
        
        VersionUtils.Version version2 = new VersionUtils.Version(1, 2, 3, "alpha", null);
        assertEquals("1.2.3-alpha", version2.toString());
        
        VersionUtils.Version version3 = new VersionUtils.Version(1, 2, 3, "alpha", "build.1");
        assertEquals("1.2.3-alpha+build.1", version3.toString());
        
        VersionUtils.Version version4 = new VersionUtils.Version(1, 2, 3, null, "build.1");
        assertEquals("1.2.3+build.1", version4.toString());
    }
    
    @Test
    void testVersionEquality() {
        VersionUtils.Version version1 = new VersionUtils.Version(1, 2, 3, "alpha", "build.1");
        VersionUtils.Version version2 = new VersionUtils.Version(1, 2, 3, "alpha", "build.2");
        VersionUtils.Version version3 = new VersionUtils.Version(1, 2, 3, "alpha", "build.1");
        
        assertEquals(version1, version2); // Build metadata should be ignored in equality
        assertEquals(version1, version3);
        assertEquals(version1.hashCode(), version2.hashCode());
    }
}
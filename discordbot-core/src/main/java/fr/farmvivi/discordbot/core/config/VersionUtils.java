package fr.farmvivi.discordbot.core.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for semantic version comparison and validation.
 */
public final class VersionUtils {
    
    private static final Pattern SEMVER_PATTERN = Pattern.compile(
        "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$"
    );
    
    private VersionUtils() {
        // Utility class
    }
    
    /**
     * Compares two semantic versions.
     *
     * @param version1 the first version
     * @param version2 the second version
     * @return negative if version1 < version2, 0 if equal, positive if version1 > version2
     */
    public static int compareVersions(String version1, String version2) {
        if (version1 == null && version2 == null) return 0;
        if (version1 == null) return -1;
        if (version2 == null) return 1;
        
        Version v1 = parseVersion(version1);
        Version v2 = parseVersion(version2);
        
        return v1.compareTo(v2);
    }
    
    /**
     * Checks if a version string is valid semantic versioning.
     *
     * @param version the version to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            return false;
        }
        return SEMVER_PATTERN.matcher(version.trim()).matches();
    }
    
    /**
     * Parses a version string into a Version object.
     *
     * @param versionString the version string
     * @return the parsed version
     * @throws IllegalArgumentException if the version is invalid
     */
    public static Version parseVersion(String versionString) {
        if (!isValidVersion(versionString)) {
            throw new IllegalArgumentException("Invalid version format: " + versionString);
        }
        
        Matcher matcher = SEMVER_PATTERN.matcher(versionString.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version format: " + versionString);
        }
        
        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = Integer.parseInt(matcher.group(3));
        String preRelease = matcher.group(4);
        String buildMetadata = matcher.group(5);
        
        return new Version(major, minor, patch, preRelease, buildMetadata);
    }
    
    /**
     * Normalizes a version string (removes whitespace, ensures proper format).
     *
     * @param version the version to normalize
     * @return the normalized version
     */
    public static String normalizeVersion(String version) {
        if (version == null) return "0.0.0";
        
        String trimmed = version.trim();
        if (trimmed.isEmpty()) return "0.0.0";
        
        // Try to parse and reformat to ensure consistency
        try {
            Version parsed = parseVersion(trimmed);
            return parsed.toString();
        } catch (IllegalArgumentException e) {
            // If parsing fails, return a default
            return "0.0.0";
        }
    }
    
    /**
     * Represents a semantic version with comparison capabilities.
     */
    public static final class Version implements Comparable<Version> {
        private final int major;
        private final int minor;
        private final int patch;
        private final String preRelease;
        private final String buildMetadata;
        
        public Version(int major, int minor, int patch, String preRelease, String buildMetadata) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.preRelease = preRelease;
            this.buildMetadata = buildMetadata;
        }
        
        public int getMajor() { return major; }
        public int getMinor() { return minor; }
        public int getPatch() { return patch; }
        public String getPreRelease() { return preRelease; }
        public String getBuildMetadata() { return buildMetadata; }
        
        @Override
        public int compareTo(Version other) {
            int result = Integer.compare(this.major, other.major);
            if (result != 0) return result;
            
            result = Integer.compare(this.minor, other.minor);
            if (result != 0) return result;
            
            result = Integer.compare(this.patch, other.patch);
            if (result != 0) return result;
            
            // Handle pre-release versions
            if (this.preRelease == null && other.preRelease == null) return 0;
            if (this.preRelease == null) return 1; // Non-prerelease > prerelease
            if (other.preRelease == null) return -1; // Prerelease < non-prerelease
            
            return this.preRelease.compareToIgnoreCase(other.preRelease);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(major).append('.').append(minor).append('.').append(patch);
            if (preRelease != null && !preRelease.isEmpty()) {
                sb.append('-').append(preRelease);
            }
            if (buildMetadata != null && !buildMetadata.isEmpty()) {
                sb.append('+').append(buildMetadata);
            }
            return sb.toString();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            Version version = (Version) obj;
            return major == version.major &&
                   minor == version.minor &&
                   patch == version.patch &&
                   java.util.Objects.equals(preRelease, version.preRelease);
            // Note: buildMetadata is ignored in equality comparison per semver spec
        }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(major, minor, patch, preRelease);
        }
    }
}
package io.quarkus.bootstrap.model;

import static java.util.Objects.requireNonNull;

import io.quarkus.maven.dependency.ArtifactCoords;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Objects;

/**
 * GroupId, artifactId, classifier, type, version
 *
 * @author Alexey Loubyansky
 */
public class AppArtifactCoords implements ArtifactCoords, Externalizable, Serializable {

    private static final long serialVersionUID = 3496560470612902844L;

    public static final String TYPE_JAR = "jar";
    public static final String TYPE_POM = "pom";

    public AppArtifactCoords() {
    }

    public static AppArtifactCoords fromString(String str) {
        return new AppArtifactCoords(split(str, new String[5]));
    }

    protected static String[] split(String str, String[] parts) {
        requireNonNull(str, "str is required");
        final int firstSep = str.indexOf(':');
        final int versionSep = str.lastIndexOf(':');
        if (firstSep < 0) {
            throw new IllegalArgumentException(
                    "Invalid AppArtifactCoords string without any separator: " + str);
        }
        if (firstSep == versionSep) {
            throw new IllegalArgumentException(
                    "Use AppArtifactKey instead of AppArtifactCoords to deal with 'groupId:artifactId': " + str);
        }
        if (versionSep <= 0 || versionSep == str.length() - 1) {
            throw new IllegalArgumentException("One of type, version or separating them ':' is missing from '" + str + "'");
        }
        parts[4] = str.substring(versionSep + 1);
        AppArtifactKey.split(str, parts, versionSep);
        return parts;
    }

    protected String groupId;
    protected String artifactId;
    protected String classifier;
    protected String type;
    protected String version;

    protected transient AppArtifactKey key;

    protected AppArtifactCoords(String[] parts) {
        groupId = parts[0];
        artifactId = parts[1];
        classifier = parts[2];
        type = parts[3] == null ? TYPE_JAR : parts[3];
        version = parts[4];
    }

    public AppArtifactCoords(AppArtifactKey key, String version) {
        this.key = key;
        this.groupId = key.getGroupId();
        this.artifactId = key.getArtifactId();
        this.classifier = key.getClassifier();
        this.type = key.getType();
        this.version = version;
    }

    public AppArtifactCoords(String groupId, String artifactId, String version) {
        this(groupId, artifactId, "", TYPE_JAR, version);
    }

    public AppArtifactCoords(String groupId, String artifactId, String type, String version) {
        this(groupId, artifactId, "", type, version);
    }

    public AppArtifactCoords(String groupId, String artifactId, String classifier, String type, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.classifier = classifier == null ? "" : classifier;
        this.type = type;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public AppArtifactKey getKey() {
        return key == null ? key = new AppArtifactKey(groupId, artifactId, classifier, type) : key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppArtifactCoords that = (AppArtifactCoords) o;
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(artifactId, that.artifactId) &&
                Objects.equals(classifier, that.classifier) &&
                Objects.equals(type, that.type) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, classifier, type, version);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        append(buf);
        return buf.toString();
    }

    protected StringBuilder append(final StringBuilder buf) {
        buf.append(groupId).append(':').append(artifactId).append(':');
        if (classifier != null && !classifier.isEmpty()) {
            buf.append(classifier);
        }
        return buf.append(':').append(type).append(':').append(version);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        if (in.readBoolean()) {
            groupId = in.readUTF();
        }
        if (in.readBoolean()) {
            artifactId = in.readUTF();
        }
        if (in.readBoolean()) {
            classifier = in.readUTF();
        }
        if (in.readBoolean()) {
            type = in.readUTF();
        }
        if (in.readBoolean()) {
            version = in.readUTF();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        if (groupId == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(groupId);
        }
        if (artifactId == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(artifactId);
        }
        if (classifier == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(classifier);
        }
        if (type == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(type);
        }
        if (version == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(version);
        }
    }
}

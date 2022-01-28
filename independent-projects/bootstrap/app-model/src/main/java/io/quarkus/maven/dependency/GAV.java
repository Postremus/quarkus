package io.quarkus.maven.dependency;

import io.quarkus.bootstrap.model.CustomSerDerUtil;
import io.quarkus.bootstrap.model.CustomSerDeriable;
import io.quarkus.bootstrap.workspace.WorkspaceModuleId;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Objects;

public class GAV implements WorkspaceModuleId, Serializable, CustomSerDeriable {

    private static final long serialVersionUID = -1110768961345248967L;

    private final String groupId;
    private final String artifactId;
    private final String version;

    public GAV(String groupId, String artifactId, String version) {
        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId, groupId, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GAV other = (GAV) obj;
        return Objects.equals(artifactId, other.artifactId) && Objects.equals(groupId, other.groupId)
                && Objects.equals(version, other.version);
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public void serialize(OutputStream out) {
        CustomSerDerUtil.writeString(groupId, out);
        CustomSerDerUtil.writeString(artifactId, out);
        CustomSerDerUtil.writeString(version, out);
    }

    public static GAV deserialize(ByteBuffer buffer) {
        String groupId = CustomSerDerUtil.readString(buffer);
        String artifactId = CustomSerDerUtil.readString(buffer);
        String version = CustomSerDerUtil.readString(buffer);

        return new GAV(groupId, artifactId, version);
    }
}

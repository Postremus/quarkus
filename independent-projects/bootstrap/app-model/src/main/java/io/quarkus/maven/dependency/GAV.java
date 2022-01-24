package io.quarkus.maven.dependency;

import io.quarkus.bootstrap.workspace.WorkspaceModuleId;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Objects;

public class GAV implements WorkspaceModuleId, Externalizable, Serializable {

    private static final long serialVersionUID = -3956504662061918319L;

    private String groupId;
    private String artifactId;
    private String version;

    public GAV(String groupId, String artifactId, String version) {
        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public GAV() {
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
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        if (in.readBoolean()) {
            groupId = in.readUTF();
        }
        if (in.readBoolean()) {
            artifactId = in.readUTF();
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
        if (version == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(version);
        }
    }
}

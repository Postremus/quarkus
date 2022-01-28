package io.quarkus.bootstrap.workspace;

import io.quarkus.bootstrap.model.CustomSerDerUtil;
import io.quarkus.bootstrap.model.CustomSerDeriable;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class DefaultArtifactSources implements ArtifactSources, Serializable, CustomSerDeriable {

    private final String classifier;
    private final Collection<SourceDir> sources;
    private final Collection<SourceDir> resources;

    public DefaultArtifactSources(String classifier, Collection<SourceDir> sources, Collection<SourceDir> resources) {
        this.classifier = Objects.requireNonNull(classifier, "The classifier is null");
        this.sources = sources;
        this.resources = resources;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    public void addSources(SourceDir src) {
        this.sources.add(src);
    }

    @Override
    public Collection<SourceDir> getSourceDirs() {
        return sources;
    }

    public void addResources(SourceDir src) {
        this.resources.add(src);
    }

    @Override
    public Collection<SourceDir> getResourceDirs() {
        return resources;
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        s.append(classifier);
        if (s.length() > 0) {
            s.append(' ');
        }
        s.append("sources: ").append(sources);
        s.append(" resources: ").append(resources);
        return s.toString();
    }

    @Override
    public void serialize(OutputStream out) {
        CustomSerDerUtil.writeString(classifier, out);
        CustomSerDerUtil.writeCollection(sources, out, (dir, fout) -> {
            ((DefaultSourceDir) dir).serialize(fout);
        });
        CustomSerDerUtil.writeCollection(resources, out, (dir, fout) -> {
            ((DefaultSourceDir) dir).serialize(fout);
        });
    }

    public static DefaultArtifactSources deserialize(ByteBuffer buffer) {
        String classifier = CustomSerDerUtil.readString(buffer);
        Collection<SourceDir> sources = CustomSerDerUtil.readCollection(buffer, DefaultSourceDir::deserialize, ArrayList::new);
        Collection<SourceDir> resources = CustomSerDerUtil.readCollection(buffer, DefaultSourceDir::deserialize,
                ArrayList::new);

        return new DefaultArtifactSources(classifier, sources, resources);
    }
}

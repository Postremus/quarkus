package io.quarkus.bootstrap.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CapabilityContract implements ExtensionCapabilities, Externalizable, Serializable {

    private static final long serialVersionUID = 8907253464219761821L;

    public CapabilityContract() {
    }

    public static CapabilityContract providesCapabilities(String extension, String commaSeparatedList) {
        final List<String> list = Arrays.asList(commaSeparatedList.split("\\s*,\\s*"));
        for (String provided : list) {
            if (provided.isEmpty()) {
                throw new IllegalArgumentException("Extension " + extension
                        + " was configured to provide a capability with an empty name: " + commaSeparatedList);
            }
        }
        return new CapabilityContract(extension, list);
    }

    private String extension;
    private List<String> providesCapabilities;

    public CapabilityContract(String extension, List<String> providesCapabilities) {
        this.extension = Objects.requireNonNull(extension, "extension can't be null");
        this.providesCapabilities = Objects.requireNonNull(providesCapabilities, "providesCapabilities can't be null");
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public List<String> getProvidesCapabilities() {
        return providesCapabilities;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        if (in.readBoolean()) {
            extension = in.readUTF();
        }
        if (in.readBoolean()) {
            providesCapabilities = (List<String>) in.readObject();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        if (extension == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(extension);
        }
        if (providesCapabilities == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(providesCapabilities);
        }
    }
}

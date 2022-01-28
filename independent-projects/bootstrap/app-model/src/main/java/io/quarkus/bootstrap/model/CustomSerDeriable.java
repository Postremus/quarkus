package io.quarkus.bootstrap.model;

import java.io.OutputStream;

public interface CustomSerDeriable {
    void serialize(OutputStream out);
}

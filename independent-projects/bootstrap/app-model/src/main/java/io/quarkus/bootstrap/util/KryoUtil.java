package io.quarkus.bootstrap.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.File;
import java.nio.file.Path;

public class KryoUtil {
    public static final ThreadLocal<Kryo> KRYO = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            // TODO: enable for better performance
            kryo.setRegistrationRequired(false);
            kryo.register(Path.class, new PathSerializer());
            kryo.register(File.class, new FileSerializer());
            kryo.setReferences(true);
            return kryo;
        };
    };

    private static class PathSerializer extends Serializer<Path> {

        @Override
        public void write(Kryo kryo, Output output, Path object) {
            output.writeString(object.toString());
        }

        @Override
        public Path read(Kryo kryo, Input input, Class<? extends Path> type) {
            return Path.of(input.readString());
        }
    }

    private static class FileSerializer extends Serializer<File> {

        @Override
        public void write(Kryo kryo, Output output, File object) {
            output.writeString(object.getAbsolutePath());
        }

        @Override
        public File read(Kryo kryo, Input input, Class<? extends File> type) {
            return new File(input.readString());
        }
    }
}

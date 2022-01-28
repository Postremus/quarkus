package io.quarkus.bootstrap.model;

import io.quarkus.maven.dependency.ArtifactKey;
import io.quarkus.paths.PathCollection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CustomSerDerUtil {
    public static void writeFile(File s, OutputStream out) {
        writeString(s.getAbsolutePath(), out);
    }

    public static void writeString(String s, OutputStream out) {

        try {
            if (s == null) {
                writeInt(-1, out);
                return;
            }
            byte[] data = s.getBytes(StandardCharsets.UTF_8);
            writeInt(data.length, out);
            out.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final void writeInt(int val, OutputStream os) {
        try {
            os.write(val >> 24);
            os.write(val >> 16);
            os.write(val >> 8);
            os.write(val);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final void writeBoolean(boolean val, OutputStream os) {
        try {
            os.write(val ? 1 : 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <K, V> void writeMap(Map<K, V> list, OutputStream out, BiConsumer<K, OutputStream> keyHandler,
            BiConsumer<V, OutputStream> valueHandler) {
        writeInt(list.size(), out);
        for (var e : list.entrySet()) {
            keyHandler.accept(e.getKey(), out);
            valueHandler.accept(e.getValue(), out);
        }
    }

    public static <T> void writeCollection(Collection<T> list, OutputStream out, BiConsumer<T, OutputStream> handler) {
        if (list == null) {
            writeInt(0, out);
            return;
        }
        writeInt(list.size(), out);
        for (T i : list) {
            handler.accept(i, out);
        }
    }

    public static File readFile(ByteBuffer buffer) {
        return new File(readString(buffer));
    }

    public static String readString(ByteBuffer buffer) {
        int length = readInt(buffer);
        if (length == -1) {
            return null;
        }
        byte[] data = new byte[length];
        buffer.get(data);
        return new String(data, StandardCharsets.UTF_8);
    }

    public static int readInt(ByteBuffer b) {
        int ret = 0;
        ret += (b.get() & 0xFF) << 24;
        ret += (b.get() & 0xFF) << 16;
        ret += (b.get() & 0xFF) << 8;
        ret += (b.get() & 0xFF);
        return ret;
    }

    public static boolean readBoolean(ByteBuffer b) {
        return b.get() > 0;
    }

    public static <K, V> Map<K, V> readMap(ByteBuffer buffer, Function<ByteBuffer, K> keyHandler,
            Function<ByteBuffer, V> valueHandler) {
        int size = readInt(buffer);
        Map<K, V> ret = new HashMap<>();
        for (int i = 0; i < size; ++i) {
            K key = keyHandler.apply(buffer);
            V value = valueHandler.apply(buffer);
            ret.put(key, value);
        }
        return ret;
    }

    public static <T, C extends Collection<T>> C readCollection(ByteBuffer buffer, Function<ByteBuffer, T> handler,
            Supplier<C> supplier) {
        int size = readInt(buffer);
        C ret = supplier.get();
        for (int i = 0; i < size; ++i) {
            ret.add(handler.apply(buffer));
        }
        return ret;
    }

    public static ArtifactKey readArtifactKey(ByteBuffer buffer) {
        String groupId = CustomSerDerUtil.readString(buffer);
        String artifactId = CustomSerDerUtil.readString(buffer);
        String classifier = CustomSerDerUtil.readString(buffer);
        String type = CustomSerDerUtil.readString(buffer);
        return new AppArtifactKey(groupId, artifactId, classifier, type);
    }

    public static PathCollection readPathCollection(ByteBuffer buffer) {
        int size = CustomSerDerUtil.readInt(buffer);
        List<Path> ret = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            ret.add(CustomSerDerUtil.readFile(buffer).toPath());
        }
        return PathsCollection.from(ret);
    }

    public static void writeArtifactKey(ArtifactKey t, OutputStream outputStream) {
        CustomSerDerUtil.writeString(t.getGroupId(), outputStream);
        CustomSerDerUtil.writeString(t.getArtifactId(), outputStream);
        CustomSerDerUtil.writeString(t.getClassifier(), outputStream);
        CustomSerDerUtil.writeString(t.getType(), outputStream);
    }

    public static void writePathCollection(PathCollection sourceParents, OutputStream out) {
        CustomSerDerUtil.writeInt(sourceParents.size(), out);
        for (var i : sourceParents) {
            CustomSerDerUtil.writeFile(i.toFile(), out);
        }
    }

    @Deprecated
    public static void writeObject(Object o, OutputStream out) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
            objectOutputStream.writeObject(o);
            objectOutputStream.close();

            byte[] bytes = baos.toByteArray();
            writeInt(bytes.length, out);
            out.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static <T> T readObject(ByteBuffer buffer) {
        try {
            int length = readInt(buffer);
            byte[] bytes = new byte[length];
            buffer.get(bytes);

            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(bais);
            T object = (T) objectInputStream.readObject();
            objectInputStream.close();
            bais.close();
            return object;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

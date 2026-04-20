package com.balatro.cache;

import com.balatro.api.Run;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JokerFile {

    // Fixed-size record overhead: 8-byte seed + 4-byte score + 4-byte count.
    private static final int RECORD_HEADER_BYTES = 8 + 4 + 4;

    public static void appendToCache(File jokerFile, @NotNull List<Run> runList) {
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(jokerFile, true)))) {
            for (Run run : runList) {
                writeTo(dos, new Data(run));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(@NotNull ByteArrayOutputStream baos, @NotNull CompressedData data) {
        // CompressedData still uses long[]; write via DataOutputStream to avoid per-long ByteBuffer allocation.
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            for (long datum : data.getData()) {
                dos.writeLong(datum);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(@NotNull ByteArrayOutputStream baos, @NotNull Data data) {
        try {
            writeTo(new DataOutputStream(baos), data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeTo(@NotNull DataOutputStream dos, @NotNull Data data) throws IOException {
        final byte[] seedBytes = data.getSeed().getBytes();
        dos.write(seedBytes, 0, 8);
        dos.writeInt(data.getScore());

        final int[] positions = data.getData();
        dos.writeInt(positions.length);
        for (int p : positions) {
            dos.writeInt(p);
        }
    }

    public static @NotNull List<Data> readFile(@NotNull File file) {
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            return read(dis, file.length());
        } catch (IOException e) {
            Logger.getLogger(PreProcessedSeeds.class.getName())
                    .log(Level.SEVERE, null, e);
        }

        return Collections.emptyList();
    }

    public static List<CompressedData> readCompressedFile(@NotNull File file) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            return readCompressed(bis);
        } catch (IOException e) {
            Logger.getLogger(PreProcessedSeeds.class.getName())
                    .log(Level.SEVERE, null, e);
        }

        return Collections.emptyList();
    }

    public static @NotNull List<CompressedData> readCompressed(@NotNull InputStream bis) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        byte[] bytes = new byte[8];

        List<CompressedData> compressedList = new ArrayList<>(bis.available() / 256);

        while (bis.read(bytes) != -1) {
            buffer.clear();
            buffer.put(bytes);
            long a = buffer.getLong(0);

            read(bis, bytes);
            buffer.clear();
            buffer.put(bytes);
            long b = buffer.getLong(0);

            read(bis, bytes);
            buffer.clear();
            buffer.put(bytes);
            long c = buffer.getLong(0);

            read(bis, bytes);
            buffer.clear();
            buffer.put(bytes);
            long d = buffer.getLong(0);

            compressedList.add(new CompressedData(new long[]{a, b, c, d}));
        }

        return compressedList;
    }

    /**
     * @deprecated use {@link #read(DataInputStream, long)} — this overload wraps the stream unconditionally.
     */
    @Deprecated
    public static @NotNull List<Data> read(@NotNull InputStream bis) throws IOException {
        return read(new DataInputStream(bis), -1L);
    }

    public static @NotNull List<Data> read(@NotNull DataInputStream dis, long fileSize) throws IOException {
        // Estimate capacity from file size to avoid ArrayList.grow() cycles on multi-million-entry caches.
        final int estimated = fileSize > 0
                ? (int) Math.min(fileSize / (RECORD_HEADER_BYTES + 8L * 4), Integer.MAX_VALUE)
                : 16;
        final List<Data> dataList = new ArrayList<>(Math.max(16, estimated));
        final byte[] seedBytes = new byte[8];

        while (true) {
            final int read = dis.read(seedBytes);
            if (read == -1) break;
            if (read != 8) {
                throw new IOException("Unexpected end of file mid-seed");
            }

            final String seed = new String(seedBytes);
            if (!isValidSeed(seed)) {
                throw new IllegalStateException("Invalid seed: '" + seed + "'");
            }

            final int score = dis.readInt();
            final int dataSize = dis.readInt();

            final int[] data = new int[dataSize];
            for (int i = 0; i < dataSize; i++) {
                data[i] = dis.readInt();
            }

            dataList.add(new Data(seed, score, data));
        }

        return dataList;
    }

    private static boolean isValidSeed(@NotNull String seed) {
        if (seed.length() != 8) return false;
        for (int i = 0; i < 8; i++) {
            final char c = seed.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) {
                return false;
            }
        }
        return true;
    }

    private static void read(@NotNull InputStream is, byte[] arr) throws IOException {
        if (is.read(arr) == -1) {
            throw new IOException("Unexpected end of file");
        }
    }

    public static void writeToFile(File file, @NotNull List<CompressedData> compressedData) {
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(file.toPath())))) {
            for (CompressedData run : compressedData) {
                for (long datum : run.getData()) {
                    dos.writeLong(datum);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

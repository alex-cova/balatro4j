package com.balatro.cache;

import com.balatro.api.Run;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JokerFile {

    public static void appendToCache(File jokerFile, @NotNull List<Run> runList) {
        try {
            var baos = new ByteArrayOutputStream();

            for (Run run : runList) {
                write(baos, new Data(run));
            }

            try (FileOutputStream fos = new FileOutputStream(jokerFile, true)) {
                fos.write(baos.toByteArray());
            }
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(@NotNull ByteArrayOutputStream baos, @NotNull CompressedData data) {
        for (long datum : data.getData()) {
            baos.writeBytes(ByteBuffer.allocate(Long.BYTES).putLong(datum).array());
        }
    }

    public static void write(@NotNull ByteArrayOutputStream baos, @NotNull Data data) {
        baos.writeBytes(data.getSeed().getBytes());

        final int[] positions = data.getData();

        final var intBuffer = ByteBuffer.allocate(Integer.BYTES);

        baos.writeBytes(ByteBuffer.allocate(Integer.BYTES).putInt(data.getScore()).array());
        baos.writeBytes(ByteBuffer.allocate(Integer.BYTES).putInt(positions.length).array());

        if (positions.length > 0) {
            var positionArray = new byte[positions.length * Integer.BYTES];

            for (int i = 0; i < positions.length; i++) {
                intBuffer.putInt(0, positions[i]);
                System.arraycopy(intBuffer.array(), 0, positionArray, i * Integer.BYTES, Integer.BYTES);
            }

            baos.writeBytes(positionArray);
        }

    }

    public static @NotNull List<Data> readFile(@NotNull File file) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            return read(bis);
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

        List<CompressedData> compressedList = new ArrayList<>();

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

    public static @NotNull List<Data> read(@NotNull InputStream bis) throws IOException {
        List<Data> dataList = new ArrayList<>();
        byte[] seedBytes = new byte[8];
        byte[] intBytes = new byte[Integer.BYTES];
        ByteBuffer intBuffer = ByteBuffer.allocate(Integer.BYTES);

        while (bis.read(seedBytes) != -1) {
            String seed = new String(seedBytes);
            if (!seed.matches("[a-zA-Z0-9]{8}")) {
                throw new IllegalStateException("Invalid seed: '" + seed + "'");
            }

            read(bis, intBytes);
            intBuffer.clear();
            intBuffer.put(intBytes);
            int score = intBuffer.getInt(0);

            read(bis, intBytes);
            intBuffer.clear();
            intBuffer.put(intBytes);
            int dataSize = intBuffer.getInt(0);

            int[] data = new int[dataSize];

            for (int i = 0; i < dataSize; i++) {
                read(bis, intBytes);
                intBuffer.clear();
                intBuffer.put(intBytes);
                data[i] = intBuffer.getInt(0);
            }

            dataList.add(new Data(seed, score, data));
        }

        return dataList;
    }

    private static void read(@NotNull InputStream is, byte[] arr) throws IOException {
        if (is.read(arr) == -1) {
            throw new IOException("Unexpected end of file");
        }
    }
}

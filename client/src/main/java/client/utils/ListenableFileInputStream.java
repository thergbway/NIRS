package client.utils;

import java.io.*;
import java.nio.file.Files;
import java.util.function.Consumer;

public final class ListenableFileInputStream extends InputStream {

    private final Consumer<Integer> progressConsumer;

    private final InputStream delegate;

    private final double factor;

    private long readBytes;

    private long lastCheckTimeMillis = System.currentTimeMillis();

    private ListenableFileInputStream(File fileToUpload, Consumer<Integer> progressConsumer) throws IOException {

        this.progressConsumer = progressConsumer;

        factor = 100.0 / fileToUpload.length();

        delegate = Files.newInputStream(fileToUpload.toPath());
    }

    private ListenableFileInputStream(Long fileSize, InputStream delegate, Consumer<Integer> progressConsumer) {

        factor = 100.0 / fileSize;

        this.delegate = delegate;
        this.progressConsumer = progressConsumer;
    }

    @Override
    public int read() throws IOException {

        int read = delegate.read();

        if(read != -1) ++readBytes;

        long currentTimeMillis = System.currentTimeMillis();

        if ((currentTimeMillis - lastCheckTimeMillis) > 50L) {

            progressConsumer
                    .accept((int)((double) readBytes * factor));

            lastCheckTimeMillis = currentTimeMillis;
        }

        return read;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    public static InputStream newListenableStream(Long fileSize, InputStream delegate, Consumer<Integer> progressConsumer) {
        return new ListenableFileInputStream(fileSize, delegate, progressConsumer);
    }

    public static InputStream newListenableStream(File fileToUpload, Consumer<Integer> progressConsumer) throws IOException {
        return new ListenableFileInputStream(fileToUpload, progressConsumer);
    }
}

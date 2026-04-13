package com.yachiyo.UserService.utils;

import lombok.Getter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class AsyncFileUtil {

    /**
     * 运行时路径
     */
    public static final String RUNTIME_FILE_PATH = System.getProperty("user.dir")+"/Common";

    /**
     * 上传文件路径
     */
    @Getter
    public static final String UPLOAD_FILE_PATH = RUNTIME_FILE_PATH + "/src/main/resources/static/upload/";

    /**
     * 异步读取文件全部内容
     */
    public static Mono<byte[]> readBytes(String filePath) {
        return Mono.create(sink -> {
            try {
                var path = Paths.get(UPLOAD_FILE_PATH + filePath);
                var channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
                long size = channel.size();

                if (size > Integer.MAX_VALUE) {
                    sink.error(new IllegalArgumentException("文件过大，无法一次性加载"));
                    return;
                }

                ByteBuffer buffer = ByteBuffer.allocate((int) size);

                channel.read(buffer, 0, null, new CompletionHandler<Integer, Void>() {
                    @Override
                    public void completed(Integer result, Void attachment) {
                        try {
                            buffer.flip();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            sink.success(bytes);
                        } catch (Exception e) {
                            sink.error(e);
                        } finally {
                            try {
                                channel.close();
                            } catch (Exception ignored) {}
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        try {
                            channel.close();
                        } catch (Exception ignored) {}
                        sink.error(exc);
                    }
                });

            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    /**
     * 异步写入文件
     */
    public static Mono<Void> writeBytes(String filePath, byte[] data) {
        return Mono.create(sink -> {
            try {
                var path = Paths.get(UPLOAD_FILE_PATH + filePath);
                var channel = AsynchronousFileChannel.open(path,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);

                ByteBuffer buffer = ByteBuffer.wrap(data);

                channel.write(buffer, 0, null, new CompletionHandler<Integer, Void>() {
                    @Override
                    public void completed(Integer result, Void attachment) {
                        try {
                            channel.close();
                        } catch (Exception ignored) {}
                        sink.success();
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        try {
                            channel.close();
                        } catch (Exception ignored) {}
                        sink.error(exc);
                    }
                });

            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    // 异步创建目录
    public static Mono<Void> createDirectory(String dirPath) {
        return Mono.fromRunnable(() -> {
            try {
                Path path = Paths.get(UPLOAD_FILE_PATH + dirPath);
                Files.createDirectories(path);
            } catch (Exception ignored) {}
        }).publishOn(Schedulers.boundedElastic()).then();
    }

    // 异步检查文件是否存在
    public static Mono<Boolean> exists(String filePath) {
        return Mono.fromCallable(() -> {
            try {
                Path path = Paths.get(UPLOAD_FILE_PATH + filePath);
                return Files.exists(path);
            } catch (Exception ignored) {
                return false;
            }
        }).publishOn(Schedulers.boundedElastic()).then().hasElement();
    }
}
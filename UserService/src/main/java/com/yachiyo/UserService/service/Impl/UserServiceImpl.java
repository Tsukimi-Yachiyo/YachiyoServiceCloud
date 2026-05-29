package com.yachiyo.UserService.service.Impl;

import com.yachiyo.UserService.client.FileClient;
import com.yachiyo.UserService.dto.UserDetailDTO;
import com.yachiyo.UserService.entity.UserDetail;
import com.yachiyo.UserService.result.Result;
import com.yachiyo.UserService.service.UserService;
import com.yachiyo.UserService.tool.FileClientTool;
import com.yachiyo.UserService.tool.ReactiveCacheEvict;
import com.yachiyo.UserService.tool.ReactiveCacheable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Files;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final FileClient fileClient;

    private final R2dbcEntityTemplate template;

    private static final String AVATAR_PATH_FORMAT = "%d/avatar.jpg";

    private final FileClientTool fileClientTool;

    private Mono<MultipartFile> filePartToMultipartFile(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new MultipartFile() {
                        @Override
                        public String getName() {
                            return filePart.name();
                        }

                        @Override
                        public String getOriginalFilename() {
                            return filePart.filename();
                        }

                        @Override
                        public String getContentType() {
                            return filePart.headers().getContentType().toString();
                        }

                        @Override
                        public boolean isEmpty() {
                            return bytes.length == 0;
                        }

                        @Override
                        public long getSize() {
                            return bytes.length;
                        }

                        @Override
                        public byte @NonNull [] getBytes() {
                            return bytes;
                        }

                        @Override
                        public @NonNull InputStream getInputStream() {
                            return new ByteArrayInputStream(bytes);
                        }

                        @Override
                        public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
                            Files.write(dest.toPath(), bytes);
                        }
                    };
                });
    }

    @Override
    @ReactiveCacheEvict(cacheName = "user:detail", key = "#userId")
    public Mono<Result<Boolean>> updateUserDetail(Long userId, UserDetailDTO userDetailDTO) {
        return template.selectOne(Query.query(Criteria.where("id").is(userId)), UserDetail.class)
                .flatMap(existUser -> {
                    if (userDetailDTO.getUserName() != null) {
                        existUser.setUserName(userDetailDTO.getUserName());
                    }
                    if (userDetailDTO.getUserIntroduction() != null) {
                        existUser.setUserIntroduction(userDetailDTO.getUserIntroduction());
                    }
                    if (userDetailDTO.getUserCity() != null) {
                        existUser.setUserCity(userDetailDTO.getUserCity());
                    }
                    if (userDetailDTO.getUserGender() != null) {
                        existUser.setUserGender(userDetailDTO.getUserGender());
                    }
                    if (userDetailDTO.getUserBirthday() != null) {
                        existUser.setUserBirthday(userDetailDTO.getUserBirthday());
                    }
                    return template.update(existUser);
                })
                .map(_ -> Result.success(true))
                .defaultIfEmpty(Result.error("404", "用户不存在"));
    }

    @Override
    @ReactiveCacheEvict(cacheName = "user:avatar", key = "#userId")
    public Mono<Result<Boolean>> updateUserAvatar(Long userId, FilePart userAvatar) {
        String filePath = String.format(AVATAR_PATH_FORMAT, userId);
        return filePartToMultipartFile(userAvatar)
                .flatMap(multipartFile -> fileClientTool.callFileClient(
                        () -> Result.success(fileClient.uploadFile(filePath, multipartFile)),
                        "上传头像失败"
                ))
                .map(success -> Result.success(true))
                .onErrorResume(e -> Mono.just(Result.error("500", e.getMessage())));
    }

    @Override
    @ReactiveCacheable(cacheName = "user:avatar", key = "#userId")
    public Mono<Result<String>> getUserAvatar(Long userId) {
        String filePath = String.format(AVATAR_PATH_FORMAT, userId);
        return fileClientTool.callFileClient(
                () -> Result.success(fileClient.getUrl(filePath, 1000L)),
                "获取头像URL失败"
        )
                .map(Result::success)
                .onErrorResume(e -> Mono.just(Result.error("500", e.getMessage())));
    }
}
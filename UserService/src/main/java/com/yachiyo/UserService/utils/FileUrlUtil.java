package com.yachiyo.UserService.utils;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static cn.hutool.crypto.digest.DigestUtil.md5Hex;

@Component
public class FileUrlUtil {

    private static final String KEY = "yachiyo_file_url"+System.currentTimeMillis();


    /**
     * 生成带签名的文件URL（异步版本）
     */
    public Mono<String> generateFileUrl(String fileName, long expireSeconds) {
        // 直接用静态方法！不需要注入！
        return AsyncFileUtil.exists(fileName)
                .mapNotNull(exists -> {
                    // 文件不存在 → 返回 null
                    if (!exists) {
                        return null;
                    }

                    long expire = System.currentTimeMillis() / 1000 + expireSeconds;
                    String sign = DigestUtil.md5Hex(fileName + expire + KEY);
                    return "/file/" + fileName + "?expire=" + expire + "&sign=" + sign;
                });
    }

    /**
     * 校验签名
     */
    public boolean verify(String filename, long expire, String sign) {
        if (System.currentTimeMillis() / 1000 > expire) {
            return false;
        }
        String realSign = DigestUtil.md5Hex(filename + expire + KEY);
        return realSign.equalsIgnoreCase(sign);
    }
}


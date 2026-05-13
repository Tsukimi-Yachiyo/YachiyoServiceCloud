package com.yachiyo.QQBotService.utils;

import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.yachiyo.QQBotService.dto.file.UploadFileRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Slf4j
@Component
public class CQCodeUtils {
    @Autowired
    private NapCatUtils napCatUtils;

    // 文件、视频、语音、图片（仅sub_type=0，即普通图片，排除了动画表情等其它类型）
    public final Predicate<ArrayMsg> SHOULD_SAVE = msg -> {
        if (typeEq(msg, MsgTypeEnum.image)) {
            return isNormalImage(msg);
        }
        return isDownloadableType(msg);
    };

    public boolean typeEq(ArrayMsg arrayMsg, Predicate<ArrayMsg> condition) {
        return condition.test(arrayMsg);
    }

    public boolean typeEq(ArrayMsg arrayMsg, MsgTypeEnum... type) {
        for (MsgTypeEnum t : type) {
            if (arrayMsg.getType() == t) {
                return true;
            }
        }
        return false;
    }

    public boolean typeEq(ArrayMsg arrayMsg, MsgTypeEnum type) {
        return arrayMsg.getType() == type;
    }

    public boolean typeEq(ArrayMsg arrayMsg, String... rawType) {
        for (String t : rawType) {
            if (arrayMsg.getRawType().equals(t)) {
                return true;
            }
        }
        return false;
    }

    public boolean typeEq(ArrayMsg arrayMsg, String rawType) {
        return arrayMsg.getRawType().equals(rawType);
    }

    public boolean atEq(ArrayMsg msg, Long userId) {
        return typeEq(msg, MsgTypeEnum.at) && msg.getLongData("qq") == userId;
    }

    public boolean atEq(List<ArrayMsg> arrayMsgList, Long userId) {
        return arrayMsgList.stream().anyMatch(msg -> atEq(msg, userId));
    }

    /**
     * 从 CQ 码列表中查找回复的消息的id，若没有则返回 null
     * @param arrayMsgList CQ 码列表
     * @return 回复的消息的id或null
     */
    public Integer findReplyId(List<ArrayMsg> arrayMsgList) {
        return arrayMsgList.stream()
                .filter(msg -> typeEq(msg, MsgTypeEnum.reply))
                .map(msg -> (int) msg.getLongData("id")) // 消息id就是int类型的
                .findFirst()
                .orElse(null);
    }

    /**
     * 从 CQ 码列表中筛选出所有的 at CQ 码
     * @param arrayMsgList CQ 码列表
     * @return 包含所有 at CQ 码的列表
     */
    public List<ArrayMsg> getAtCQCodeList(List<ArrayMsg> arrayMsgList) {
        return arrayMsgList.stream()
                .filter(msg -> typeEq(msg, MsgTypeEnum.at))
                .toList();
    }

    /**
     * 从 CQ 码列表中筛选出所有 at CQ 码中的 QQ 号或 all
     * @param arrayMsgList CQ 码列表
     * @return 包含所有 at CQ 码中 QQ 号或 all 的列表
     */
    public List<String> getAtIdList(List<ArrayMsg> arrayMsgList) {
        return arrayMsgList.stream()
                .filter(msg -> typeEq(msg, MsgTypeEnum.at))
                .map(msg -> msg.getStringData("qq"))
                .toList();
    }

    /**
     * 从 CQ 码列表中筛选出所有 at CQ 码中的 QQ 号，并将其添加到提供的 atList 中
     * @param arrayMsgList CQ 码列表
     * @param atList QQ 号的列表
     */
    public void computeAtList(List<ArrayMsg> arrayMsgList, List<String> atList) {
        arrayMsgList.stream()
                .filter(msg -> typeEq(msg, MsgTypeEnum.at))
                .map(msg -> msg.getStringData("qq"))
                .forEach(atList::add);
    }

    /**
     * 从 CQ 码列表中合并转发消息的 id
     * @param arrayMsgList CQ 码列表
     * @return 合并转发消息的 id，若不存在转发消息则返回 -1
     */
    public long findForwardId(List<ArrayMsg> arrayMsgList) {
        return arrayMsgList.stream()
                .filter(msg -> typeEq(msg, MsgTypeEnum.forward))
                .mapToLong(msg -> msg.getLongData("id"))
                .findFirst()
                .orElse(-1);
    }

    /**
     * 判断消息响应中是否包含合并转发消息
     * @param msgResp 消息响应对象
     * @return 消息响应中是否包含合并转发消息
     */
    public boolean containsForward(MsgResp msgResp) {
        return msgResp.getArrayMsg().stream()
                .anyMatch(msg -> typeEq(msg, MsgTypeEnum.forward));
    }

    /**
     * 将 CQ 码列表转换为字符串形式格式
     * @param arrayMsgList CQ 码列表
     * @return CQ 码字符串
     */
    public String toCQCode(List<ArrayMsg> arrayMsgList) {
        StringBuilder sb = new StringBuilder();
        for (ArrayMsg arrayMsg : arrayMsgList) {
            sb.append(arrayMsg.toCQCode());
        }
        return sb.toString();
    }

    /**
     * 从 CQ 码中提取需要保存和上传的信息，若 CQ 码类型不符合要求或提取失败则返回 null
     * @param arrayMsg 要提取信息的 CQ 码
     * @return 包含文件名和 URL 的 UploadFileRequest 对象，或 null
     */
    public UploadFileRequest getUploadFileRequest(ArrayMsg arrayMsg) {
        if (typeEq(arrayMsg, SHOULD_SAVE)) {
            String url = findHttpOrLocalUrl(arrayMsg);
            if (url == null) return null;
            return new UploadFileRequest(arrayMsg.getStringData("file"), url);
        } else {
            return null;
        }
    }

    /**
     * 筛选需要保存和上传的 CQ 码中的信息
     * @param arrayMsgList CQ 码列表
     * @return UploadFileRequest 列表，返回空列表而非 null
     */
    public List<UploadFileRequest> getUploadFileRequestList(List<ArrayMsg> arrayMsgList) {
        return arrayMsgList.stream()
                .filter(msg -> typeEq(msg, SHOULD_SAVE))
                .map(arrayMsg -> {
                    String url = findHttpOrLocalUrl(arrayMsg);
                    if (url == null) return null;
                    return new UploadFileRequest(arrayMsg.getStringData("file"), url);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 从 CQ 码中获取可下载的 URL，对于一般文件时使用file_id下载并获取localUrl，图片或视频等直接获取 httpUrl
     * @param arrayMsg 要获取 URL 的 CQ 码
     * @return localUrl或httpUrl，获取失败时返回 null
     */
    public String findHttpOrLocalUrl(ArrayMsg arrayMsg) {
        try {
            if (hasUrl(arrayMsg)) {
                return arrayMsg.getStringData("url");
            }
            if (isFile(arrayMsg)) {
                return napCatUtils.downloadFile(arrayMsg.getStringData("file_id"));
            }
            return null;
        } catch (Exception e) {
            log.error("获取URL失败：{}", arrayMsg, e);
            return null;
        }
    }

    /**
     * 判断 CQ 码是否是普通图片（即 sub_type=0 的图片，排除动画表情的情况）
     * @param arrayMsg 要判断的 image 类 CQ 码
     * @return CQ 码是否是普通图片而不是动画表情，获取 sub_type 失败时返回 false
     */
    public boolean isNormalImage(ArrayMsg arrayMsg) {
        try {
            String subType = arrayMsg.getStringData("sub_type");
            if (subType == null || subType.isBlank()) {
                // 不存在或为空时是系列动图表情
                return false;
            }
            return "0".equals(subType); // 仅存在且为 0 时是普通图片
        } catch (Exception e) {
            log.error("获取sub_type失败：{}", arrayMsg, e);
            return false;
        }
    }

    public boolean hasUrl(ArrayMsg arrayMsg) {
        return typeEq(arrayMsg, MsgTypeEnum.image, MsgTypeEnum.record, MsgTypeEnum.video);
    }

    public boolean isFile(ArrayMsg arrayMsg) {
        return typeEq(arrayMsg, "file");
    }

    /**
     * 判断 CQ 码是否是可下载的类型（文件、视频、语音、图片）
     * @param arrayMsg 要判断的 CQ 码
     * @return CQ 码是否是可下载的类型
     */
    public boolean isDownloadableType(ArrayMsg arrayMsg) {
        return isFileLike(arrayMsg) || typeEq(arrayMsg, MsgTypeEnum.record);
    }

    /**
     * 判断 CQ 码是否是文件类消息（包括图片、视频、文件）
     * @param arrayMsg 要判断的 CQ 码
     * @return CQ 码是否是文件类消息
     */
    public boolean isFileLike(ArrayMsg arrayMsg) {
        return typeEq(arrayMsg, MsgTypeEnum.video, MsgTypeEnum.image) || typeEq(arrayMsg, "file");
    }
}

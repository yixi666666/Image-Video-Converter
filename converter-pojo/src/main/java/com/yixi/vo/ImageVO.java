package com.yixi.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageVO {
    private String imageBase64;  // 图片的Base64编码
    private String fileName;     // 文件名
    private String fileType;     // 文件类型（MIME类型）
}

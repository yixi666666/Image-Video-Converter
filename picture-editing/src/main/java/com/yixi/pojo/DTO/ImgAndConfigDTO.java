package com.yixi.pojo.DTO;

import lombok.Data;

@Data
public class ImgAndConfigDTO {
    private ImageDTO image;                    // 图片信息
    private BrailleConvertConfigDTO config;    // 转换配置
}

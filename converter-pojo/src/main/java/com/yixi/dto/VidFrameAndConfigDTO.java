package com.yixi.dto;

import lombok.Data;

@Data
public class VidFrameAndConfigDTO {
    private VideoFrameDTO image;                    // 视频帧信息
    private BrailleConvertConfigDTO config;    // 转换配置
}

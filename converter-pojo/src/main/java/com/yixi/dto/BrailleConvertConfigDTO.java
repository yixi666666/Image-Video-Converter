package com.yixi.dto;

import lombok.Data;

@Data
public class BrailleConvertConfigDTO {
    private Boolean isDithering;      // 是否采用抖动算法，用于优化图像转换效果
    private Boolean isMonospaced;      // 是否等宽显示，控制输出字符的宽度是否一致
    private Boolean isInverted;        // 是否反色处理，反转输出字符的颜色
    private int outputWidth;           // 输出字符宽度，控制转换后输出的宽度大小

}

package com.yixi.converter;

import java.awt.*;
import java.awt.image.BufferedImage;

import static com.yixi.common.ImageUtils.toGrayscale;

public class BrailleConverter {
    //将图像转换为ASCII艺术
    public static String convertToAscii(BufferedImage image,boolean isMonospace,boolean isInverted) {
        int width = image.getWidth();
        int height = image.getHeight();
        StringBuilder asciiArt = new StringBuilder();

        // 按2x4像素块处理
        for (int y = 0; y < height; y += 4) {
            for (int x = 0; x < width; x += 2) {
                boolean[] brailleDots = new boolean[8];
                int dotIndex = 0;

                // 处理2x4像素块
                for (int dx = 0; dx < 2; dx++) {
                    for (int dy = 0; dy < 4; dy++) {
                        if (x + dx < width && y + dy < height) {
                            Color color = new Color(image.getRGB(x + dx, y + dy));
                            int gray = toGrayscale(color.getRed(), color.getGreen(), color.getBlue());

                            // 根据灰度和反色设置决定点是否显示
                            boolean dotVisible = isInverted ? gray >= 128 : gray <= 128;//原数值128  gray >= 128 : gray <= 128
                            brailleDots[dotIndex] = dotVisible;
                        }
                        dotIndex++;
                    }
                }

                // 转换为盲文字符
                char brailleChar = pixelsToBraille(brailleDots, isMonospace);
                asciiArt.append(brailleChar);
            }
            asciiArt.append("\n");
        }

        return asciiArt.toString();
    }

    // 将像素点数组转换为盲文字符
    public static char pixelsToBraille(boolean[] dots,boolean isMonospace) {
        // 盲文点位置对应的位偏移
        int[] shiftValues = {0, 1, 2, 6, 3, 4, 5, 7};
        int codePointOffset = 0;

        // 计算码点偏移量
        for (int i = 0; i < dots.length; i++) {
            if (dots[i]) {
                codePointOffset += 1 << shiftValues[i];
            }
        }

        // 处理全空情况
        //codePointOffset == 0 说明是一个空格。
        // 如果想要保证等宽，将空格变为显示一个点，保证所有字符宽度都一样。
        // 避免在有些字体中一个空格和一个盲文点字所占空间不一样，最终导致图片变形问题
        //不过也使得图片每一处都均匀的分布着一个点，整体不是很干净
        if (codePointOffset == 0 && isMonospace) {
            codePointOffset = 4; // 使用单点字符
        }


        // 返回Unicode盲文字符
        return (char) (0x2800 + codePointOffset);
    }


}

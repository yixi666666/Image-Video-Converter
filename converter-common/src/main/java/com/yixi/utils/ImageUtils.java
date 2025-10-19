package com.yixi.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class ImageUtils {
    /**
     * 将BufferedImage转换为Base64编码字符串(默认png格式)
     * @param image 要转换的图像对象
     * @return 转换后的Base64编码字符串（含前缀）
     * @throws IOException 当图像写入或编码失败时抛出
     */
    public static String bufferedImageToBase64(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            boolean writeSuccess = ImageIO.write(image, "png", baos);
            if (!writeSuccess) {
                throw new IOException("无法将图像写入输出流，不支持的图像格式: png ");
            }

            byte[] imageBytes = baos.toByteArray();
            String pureBase64 = Base64.getEncoder().encodeToString(imageBytes);

            // 拼接对应格式的前缀
            return "data:image/png;base64," + pureBase64;
        }
    }

    /**
     * 将Base64编码字符串转换为BufferedImage
     * @param base64Data 纯Base64编码字符串（不含前缀）
     * @return 转换后的BufferedImage对象
     * @throws IOException 当解码或图像读取失败时抛出
     */
    public static BufferedImage base64ToBufferedImage(String base64Data) throws IOException {
        // 1. 去除Base64前缀（如果有）
        if (base64Data.contains(",")) {
            base64Data = base64Data.split(",")[1];
        }
        // Base64解码为字节数组
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        // 将字节数组转换为BufferedImage
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new IOException("无法解析Base64数据为图像，可能是无效的图像格式");
            }
            return image;
        }
    }

    // 灰度转换
    public static int toGrayscale(int r, int g, int b) {
        //默认平均值法
        switch ("average") {
            case "luminance"://亮度法（Luminance）
                return (int) (0.22 * r + 0.72 * g + 0.06 * b);
            case "lightness"://明度法（Lightness）
                return (Math.max(r, Math.max(g, b)) + Math.min(r, Math.min(g, b))) / 2;
            case "average"://平均值法（Average）
                return (r + g + b) / 3;
            case "value"://最大值法（Value）
                return Math.max(r, Math.max(g, b));
            default:
                System.err.println("无效的灰度模式: ");
                return 0;
        }
    }


}

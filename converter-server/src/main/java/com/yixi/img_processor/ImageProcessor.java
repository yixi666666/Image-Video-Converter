package com.yixi.img_processor;

import com.yixi.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;


public class ImageProcessor {
    //图像加载与预处理
    public static BufferedImage loadImage(BufferedImage originalImage, int outputWidth) throws IOException {

        int targetWidth = outputWidth * 2;
        double ratio = (double) originalImage.getHeight() / originalImage.getWidth();
        int targetHeight =(int) Math.round(targetWidth * ratio);
        // 确保尺寸为偶数
        //targetWidth = targetWidth - (targetWidth % 2); //前面乘以二，所以肯定是偶数
        targetHeight = targetHeight - (targetHeight %4 <= 2 ? targetHeight %4:-1);

        // 创建调整大小后的图像
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, targetWidth, targetHeight);

        // 绘制图像
        if(outputWidth<=75){
            //双三次插值（Bicubic）结合抗锯齿（Anti-aliasing）能显著提升图像缩放质量，适合对细节要求高的场景
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
        }else if(outputWidth<=300){
            //双线性插值（Bilinear）是一种折衷方案，质量优于默认插值，性能优于双三次：
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        return resizedImage;
    }

    //Floyd-Steinberg抖动
    public static BufferedImage floyd_SteinbergDithering(BufferedImage image) {// Floyd-Steinberg抖动
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage ditheredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // 创建像素数组
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        // Floyd-Steinberg抖动算法
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                Color originalColor = new Color(pixels[index]);

                // 转换为灰度
                int gray = ImageUtils.toGrayscale(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue());

                // 二值化
                int newGray = gray < 128 ? 0 : 255;

                // 计算误差
                int error = gray - newGray;

                // 设置当前像素
                Color newColor = new Color(newGray, newGray, newGray);
                ditheredImage.setRGB(x, y, newColor.getRGB());

                // 误差扩散
                if (x + 1 < width) {
                    addError(pixels, width, height, x + 1, y, error, 7.0 / 16.0);
                }
                if (x - 1 >= 0 && y + 1 < height) {
                    addError(pixels, width, height, x - 1, y + 1, error, 3.0 / 16.0);
                }
                if (y + 1 < height) {
                    addError(pixels, width, height, x, y + 1, error, 5.0 / 16.0);
                }
                if (x + 1 < width && y + 1 < height) {
                    addError(pixels, width, height, x + 1, y + 1, error, 1.0 / 16.0);
                }
            }
        }

        return ditheredImage;
    }
    // 辅助方法：添加误差到相邻像素
    public static void addError(int[] pixels, int width, int height, int x, int y, int error, double factor) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            int index = y * width + x;
            Color color = new Color(pixels[index]);

            int r = clamp((int) (color.getRed() + error * factor), 0, 255);
            int g = clamp((int) (color.getGreen() + error * factor), 0, 255);
            int b = clamp((int) (color.getBlue() + error * factor), 0, 255);

            pixels[index] = new Color(r, g, b).getRGB();
        }
    }
    // 辅助方法：确保值在范围内
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }



}

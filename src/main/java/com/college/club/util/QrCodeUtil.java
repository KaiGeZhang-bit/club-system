package com.college.club.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 * 生成Base64格式二维码，前端可直接通过<img>标签展示，无需处理图片文件
 * 适配扫码签到业务，直接调用即可
 */
public class QrCodeUtil {
    // 二维码默认宽高（前端展示300*300足够清晰，无需调整）
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;
    // 二维码字符编码（固定UTF-8，避免中文乱码）
    private static final String CHARSET = "UTF-8";
    // 二维码图片格式（固定PNG，兼容性最好）
    private static final String FORMAT = "PNG";

    /**
     * 生成二维码Base64字符串（默认宽高300*300，推荐使用）
     * @param content 二维码要存储的内容（如：club_activity_1）
     * @return 纯Base64字符串（前端需拼接data:image/png;base64,前缀展示）
     */
    public static String generateQrCodeBase64(String content) {
        return generateQrCodeBase64(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成二维码Base64字符串（自定义宽高，拓展使用）
     * @param content 二维码存储内容
     * @param width 二维码宽度
     * @param height 二维码高度
     * @return 纯Base64字符串
     */
    public static String generateQrCodeBase64(String content, int width, int height) {
        try {
            // 二维码核心配置：高容错、UTF-8编码、紧凑边距
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 最高容错级别（扫码模糊/远也能识别）
            hints.put(EncodeHintType.CHARACTER_SET, CHARSET); // 字符编码，避免乱码
            hints.put(EncodeHintType.MARGIN, 1); // 边距1，二维码更紧凑，减少空白

            // 初始化二维码写入器，生成二维码矩阵
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            // 将二维码矩阵转换成字节数组，再转Base64字符串
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageConfig config = new MatrixToImageConfig(0xFF000000, 0xFFFFFFFF); // 黑底白字（扫码最清晰）
            MatrixToImageWriter.writeToStream(bitMatrix, FORMAT, outputStream, config);

            // 生成纯Base64字符串（无前端展示前缀，前缀由前端拼接）
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            // 异常包装，方便排查问题（如依赖缺失、内容非法等）
            throw new RuntimeException("二维码生成失败：" + e.getMessage(), e);
        }
    }
}
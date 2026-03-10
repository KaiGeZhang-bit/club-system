package com.college.club.common.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ActivitySignQrVo",description = "生成签到二维码返回结果")
public class ActivitySignQrVo {
    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "签到二维码Base64字符串（前端直接拼接前缀展示")
    private String qrCodeBase64;

    @Schema(description = "二维码原始内容（扫码签到时前端需要传入）")
    private String qrContent;

}

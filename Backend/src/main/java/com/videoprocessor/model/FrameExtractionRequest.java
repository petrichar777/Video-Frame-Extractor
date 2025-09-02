package com.videoprocessor.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 帧提取请求参数模型
 */
public class FrameExtractionRequest {
    
    @NotNull(message = "视频文件不能为空")
    private String videoFileName;
    
    // 提取间隔（秒），如果为null则按原帧率提取所有帧
    @Min(value = 1, message = "提取间隔必须大于0秒")
    private Integer intervalSeconds;
    
    // 开始时间（秒），默认从0开始
    @Min(value = 0, message = "开始时间不能为负数")
    private Integer startTimeSeconds = 0;
    
    // 结束时间（秒），如果为null则提取到视频结束
    private Integer endTimeSeconds;
    
    // 输出图片格式，默认jpg
    private String outputFormat = "jpg";
    
    // 图片质量（0-100），默认85
    @Min(value = 1, message = "图片质量必须在1-100之间")
    private Integer imageQuality = 85;
    
    // 是否返回base64编码，默认true
    private Boolean returnBase64 = true;

    public FrameExtractionRequest() {}

    // Getters and Setters
    public String getVideoFileName() {
        return videoFileName;
    }

    public void setVideoFileName(String videoFileName) {
        this.videoFileName = videoFileName;
    }

    public Integer getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(Integer intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public Integer getStartTimeSeconds() {
        return startTimeSeconds;
    }

    public void setStartTimeSeconds(Integer startTimeSeconds) {
        this.startTimeSeconds = startTimeSeconds;
    }

    public Integer getEndTimeSeconds() {
        return endTimeSeconds;
    }

    public void setEndTimeSeconds(Integer endTimeSeconds) {
        this.endTimeSeconds = endTimeSeconds;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Integer getImageQuality() {
        return imageQuality;
    }

    public void setImageQuality(Integer imageQuality) {
        this.imageQuality = imageQuality;
    }

    public Boolean getReturnBase64() {
        return returnBase64;
    }

    public void setReturnBase64(Boolean returnBase64) {
        this.returnBase64 = returnBase64;
    }

    @Override
    public String toString() {
        return "FrameExtractionRequest{" +
                "videoFileName='" + videoFileName + '\'' +
                ", intervalSeconds=" + intervalSeconds +
                ", startTimeSeconds=" + startTimeSeconds +
                ", endTimeSeconds=" + endTimeSeconds +
                ", outputFormat='" + outputFormat + '\'' +
                ", imageQuality=" + imageQuality +
                ", returnBase64=" + returnBase64 +
                '}';
    }
}
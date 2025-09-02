package com.videoprocessor.model;

import java.util.List;

/**
 * 帧提取响应结果模型
 */
public class FrameExtractionResponse {
    
    private boolean success;
    private String message;
    private VideoInfo videoInfo;
    private List<FrameData> frames;
    private int totalFramesExtracted;
    private long processingTimeMs;

    public FrameExtractionResponse() {}

    public FrameExtractionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // 内部类：帧数据
    public static class FrameData {
        private int frameNumber;
        private double timestampSeconds;
        private String base64Data;
        private String fileName; // 如果保存为文件
        private int width;
        private int height;

        public FrameData() {}

        public FrameData(int frameNumber, double timestampSeconds, String base64Data, int width, int height) {
            this.frameNumber = frameNumber;
            this.timestampSeconds = timestampSeconds;
            this.base64Data = base64Data;
            this.width = width;
            this.height = height;
        }

        // Getters and Setters
        public int getFrameNumber() {
            return frameNumber;
        }

        public void setFrameNumber(int frameNumber) {
            this.frameNumber = frameNumber;
        }

        public double getTimestampSeconds() {
            return timestampSeconds;
        }

        public void setTimestampSeconds(double timestampSeconds) {
            this.timestampSeconds = timestampSeconds;
        }

        public String getBase64Data() {
            return base64Data;
        }

        public void setBase64Data(String base64Data) {
            this.base64Data = base64Data;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }

    public List<FrameData> getFrames() {
        return frames;
    }

    public void setFrames(List<FrameData> frames) {
        this.frames = frames;
    }

    public int getTotalFramesExtracted() {
        return totalFramesExtracted;
    }

    public void setTotalFramesExtracted(int totalFramesExtracted) {
        this.totalFramesExtracted = totalFramesExtracted;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}
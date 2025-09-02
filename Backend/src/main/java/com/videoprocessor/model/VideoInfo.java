package com.videoprocessor.model;

/**
 * 视频信息模型类
 */
public class VideoInfo {
    private String fileName;
    private long duration; // 视频时长（毫秒）
    private double frameRate; // 帧率
    private int width; // 视频宽度
    private int height; // 视频高度
    private long fileSize; // 文件大小（字节）
    private String format; // 视频格式
    private int totalFrames; // 总帧数

    public VideoInfo() {}

    public VideoInfo(String fileName, long duration, double frameRate, int width, int height, long fileSize, String format) {
        this.fileName = fileName;
        this.duration = duration;
        this.frameRate = frameRate;
        this.width = width;
        this.height = height;
        this.fileSize = fileSize;
        this.format = format;
        this.totalFrames = (int) (duration * frameRate / 1000);
    }

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public double getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(double frameRate) {
        this.frameRate = frameRate;
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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public void setTotalFrames(int totalFrames) {
        this.totalFrames = totalFrames;
    }

    @Override
    public String toString() {
        return "VideoInfo{" +
                "fileName='" + fileName + '\'' +
                ", duration=" + duration +
                ", frameRate=" + frameRate +
                ", width=" + width +
                ", height=" + height +
                ", fileSize=" + fileSize +
                ", format='" + format + '\'' +
                ", totalFrames=" + totalFrames +
                '}';
    }
}
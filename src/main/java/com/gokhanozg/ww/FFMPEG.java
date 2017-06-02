package com.gokhanozg.ww;

import java.io.File;
import java.math.BigDecimal;

/**
 * Created by mephala on 6/2/17.
 */
public class FFMPEG {
    private Integer totalImages;
    private Long captureLen;
    private Integer width;
    private Integer heigth;
    private File outputFolder;
    private BigDecimal fps;


    public FFMPEG(Integer totalImages, Long captureLen, Integer width, Integer heigth, File outputFolder) {
        this.totalImages = totalImages;
        this.captureLen = captureLen;
        this.width = width;
        this.heigth = heigth;
        this.outputFolder = outputFolder;
        calculateFPS();
    }

    private void calculateFPS() {
        BigDecimal total = new BigDecimal(totalImages);
        BigDecimal captureLenSeconds = new BigDecimal(captureLen / 1000);
        this.fps = total.divide(captureLenSeconds, 2, BigDecimal.ROUND_HALF_UP);
    }

    public String createExeCommand() {
        return "ffmpeg -r " + this.fps.toPlainString() + " -s " + this.width + "x" + this.heigth + " -i " + this.outputFolder.getAbsolutePath() + File.separator + "img%d.jpg  -vcodec libx264 -crf 25 -pix_fmt yuv420p " + this.outputFolder.getAbsolutePath() + File.separator + this.outputFolder.getName() + ".mp4";
    }
}

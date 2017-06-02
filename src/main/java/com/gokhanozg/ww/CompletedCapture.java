package com.gokhanozg.ww;

import java.io.File;
import java.math.BigDecimal;

/**
 * Created by mephala on 6/2/17.
 */
public class CompletedCapture implements Comparable<CompletedCapture> {

    private File src;
    private File serverFile;
    private String timeFrame;
    private BigDecimal moveRate;
    private String href;
    private Long startTimeStamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompletedCapture that = (CompletedCapture) o;

        return timeFrame.equals(that.timeFrame);
    }

    @Override
    public int hashCode() {
        return timeFrame.hashCode();
    }

    public File getSrc() {
        return src;
    }

    public void setSrc(File src) {
        this.src = src;
    }

    public Long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(Long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public File getServerFile() {
        return serverFile;
    }

    public void setServerFile(File serverFile) {
        this.serverFile = serverFile;
    }

    public String getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(String timeFrame) {
        this.timeFrame = timeFrame;
    }

    public BigDecimal getMoveRate() {
        return moveRate;
    }

    public void setMoveRate(BigDecimal moveRate) {
        this.moveRate = moveRate;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public int compareTo(CompletedCapture o) {
        return o.getStartTimeStamp().compareTo(this.startTimeStamp); // higher the stamp , comes first the element. I love devrik cumle.
    }
}

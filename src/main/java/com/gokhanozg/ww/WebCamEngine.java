package com.gokhanozg.ww;

import com.github.sarxos.webcam.Webcam;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mephala on 6/2/17.
 */
public class WebCamEngine {
    //Constants
    private static final String WCAM_NAME = "C920";
    private static final Integer CAPTURE_WIDTH = 1920;
    private static final Integer CAPTURE_HEIGTH = 1080;
        private static final String RECORD_FOLDER = "/home/mephala/wcam/";
//    private static final String RECORD_FOLDER = "D:\\wcam\\";
    private static final Long CHUNK_LEN = 60000L;
    private static final Integer TARGET_FPS = 60;
    private static final Boolean DEBUG_FFMPEG_ = Boolean.FALSE;
        private static final String SERVER_WEBAPP_FOLDER = "/usr/local/programming/projects/webcamWebWatcher/src/main/webapp/static";
//    private static final String SERVER_WEBAPP_FOLDER = "C:\\Users\\masmas\\Desktop\\Programming\\projects\\webcamWebWatcher\\src\\main\\webapp\\static";
    private static final Locale LOCALE = Locale.forLanguageTag("tr");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd MMMMM EEEEEE HH:mm", LOCALE);
    private static final boolean WINDOWS = false;
    private static Long STOP_BETWEEN_IMAGES = null;
    private static WebCamEngine instance;


    static {
        BigDecimal bd = BigDecimal.ONE;
        BigDecimal stopSeconds = bd.divide(BigDecimal.valueOf(TARGET_FPS), 3, BigDecimal.ROUND_HALF_UP);
        stopSeconds = stopSeconds.multiply(BigDecimal.valueOf(1000));
        STOP_BETWEEN_IMAGES = stopSeconds.longValue();
    }

    //Control
    private volatile boolean record = true;
    private Logger logger = Logger.getLogger(this.getClass());
    private ExecutorService executor = Executors.newCachedThreadPool();
    private Set<CompletedCapture> captures = new TreeSet<>();

    private WebCamEngine() {
        start();
    }


    public static synchronized WebCamEngine getInstance() {
        if (instance == null)
            instance = new WebCamEngine();
        return instance;
    }

    private static BigDecimal calculateMotionRateInsideFolder(final String folderPath) throws InterruptedException {
        final AtomicInteger movementDetectionRate = new AtomicInteger(0);
        ExecutorService movementDetectionWorkers = Executors.newFixedThreadPool(1);
        for (int i = 0; i < 29; i++) {
            int startImgNum = i * 5;
            final String img1 = "img" + startImgNum + ".jpg";
            startImgNum += 5;
            final String img2 = "img" + startImgNum + ".jpg";
            movementDetectionWorkers.submit(new Runnable() {
                public void run() {
                    detectMovementRate(img1, img2, folderPath, movementDetectionRate);
                }
            });

        }
        movementDetectionWorkers.shutdown();
        movementDetectionWorkers.awaitTermination(99999L, TimeUnit.HOURS);
        BigDecimal hundred = new BigDecimal(100);
        BigDecimal rate = hundred.multiply(new BigDecimal(movementDetectionRate.get())).divide(new BigDecimal(29), 2, BigDecimal.ROUND_HALF_UP);
        System.out.println("Calculated motion rate of folder:" + folderPath + " =====>" + rate + ", which is favored by #threads:" + movementDetectionRate.get());
        return rate;
    }

    private static void detectMovementRate(String img1, String img2, String folderPath, AtomicInteger movementDetectionRate) {
        Long time = Long.parseLong(new File(folderPath).getName());
        String img1Path = folderPath + File.separator + img1;
        File img1File = new File(img1Path);
        String img2Path = folderPath + File.separator + img2;
        File img2File = new File(img2Path);
        if (!img1File.exists() || !img2File.exists()) {
            System.out.println("FPS is going to be lower than expected for folder:" + folderPath);
            return;
        }
        int[][][] img1pixels = getImagePixels(img1Path);
        int[][][] img1pixelMeans = createPixelMeans(img1pixels, time);
        int[][][] img2pixels = getImagePixels(img2Path);
        int[][][] img2pixelMeans = createPixelMeans(img2pixels, time);
        boolean motionDetected = motionDetected(img1pixelMeans, img2pixelMeans, time);
        if (motionDetected)
            movementDetectionRate.getAndIncrement();
    }

    private static boolean motionDetected(int[][][] img1pixelMeans, int[][][] img2pixelMeans, Long time) {
        int boxSize = getBoxSize(time);
        int xlen = 1920 / boxSize;
        int ylen = 1080 / boxSize;
        int redTolerance = 5;
        int greenTolerance = 5;
        int blueTolerance = 5;
        boolean motionDetected = false;
        for (int x = 0; x < xlen; x++) {
            for (int y = 0; y < ylen; y++) {
                int r1 = img1pixelMeans[x][y][0];
                int r2 = img2pixelMeans[x][y][0];
                int g1 = img1pixelMeans[x][y][1];
                int g2 = img2pixelMeans[x][y][1];
                int b1 = img1pixelMeans[x][y][2];
                int b2 = img2pixelMeans[x][y][2];
                boolean redDiffer = Math.abs(r1 - r2) > redTolerance;
                boolean greenDiffer = Math.abs(r1 - r2) > greenTolerance;
                boolean blueDiffer = Math.abs(r1 - r2) > blueTolerance;
                if (redDiffer && greenDiffer && blueDiffer)
                    motionDetected = true;
            }
        }
        return motionDetected;
    }

    private static int[][][] getImagePixels(String filePath) {
        int[][][] imagePixels = new int[1920][1080][3];
        try {

            BufferedImage bi = ImageIO.read(new File(filePath));
            int w = bi.getWidth();
            int h = bi.getHeight();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int[] rgb = getPixelData(bi, x, y);
                    imagePixels[x][y] = rgb;
                }
            }


        } catch (Throwable t) {
            System.err.println("Failed for filePath:" + filePath);
            t.printStackTrace();
        }
        return imagePixels;
    }

    private static int[][][] createPixelMeans(int[][][] imgPixels, Long time) {
        int boxSize = getBoxSize(time);
        int xlen = 1920 / boxSize;
        int ylen = 1080 / boxSize;
        int[][][] pixelMeans = new int[xlen][ylen][3];
        for (int x = 0; x < xlen; x++) {
            for (int y = 0; y < ylen; y++) {
                int istart = x * boxSize;
                int jstart = y * boxSize;
                int rtop = 0;
                int gtop = 0;
                int btop = 0;
                for (int i = istart; i < istart + boxSize; i++) {
                    for (int j = jstart; j < jstart + boxSize; j++) {
                        try {
                            rtop += imgPixels[i][j][0];
                            gtop += imgPixels[i][j][1];
                            btop += imgPixels[i][j][2];
                        } catch (Throwable t) {
                            t.printStackTrace();
                            System.err.println(i + " ," + j + "," + istart + "," + jstart);
                            System.exit(-1);
                        }

                    }
                }
                int rmean = rtop / (boxSize * boxSize);
                int gmean = gtop / (boxSize * boxSize);
                int bmean = btop / (boxSize * boxSize);
                pixelMeans[x][y][0] = rmean;
                pixelMeans[x][y][1] = gmean;
                pixelMeans[x][y][2] = bmean;
            }
        }
        return pixelMeans;
    }

    private static int getBoxSize(Long time) {
        int boxSize = 20;
        Date calculationTime = new Date(time);
        Calendar c = Calendar.getInstance();
        c.setTime(calculationTime);
        c.set(Calendar.HOUR_OF_DAY, 7);
        Date morning = c.getTime();
        c.set(Calendar.HOUR_OF_DAY, 17);
        Date night = c.getTime();
        if (calculationTime.after(night) || calculationTime.before(morning)) {
            boxSize = 120;
        }
        return boxSize;
    }

    private static int[] getPixelData(BufferedImage img, int x, int y) {
        int argb = img.getRGB(x, y);

        int rgb[] = new int[]{
                (argb >> 16) & 0xff, //red
                (argb >> 8) & 0xff, //green
                (argb) & 0xff  //blue
        };
        return rgb;
    }

    private void start() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    startRecording();
                } catch (Throwable t) {
                    logger.fatal("!!! Failed to progress webcam capturing !!!", t);
                    try {
                        Thread.sleep(50000L);
                    } catch (InterruptedException e) {
                        logger.fatal("!!! Failed to wait after error !!!", e);
                    }
                    start(); // restarting procedure.
                }
            }
        });
    }

    private void addTimeStamp(BufferedImage old, Date captureTime) {
        int w = old.getWidth();
        int h = old.getHeight();
        Graphics2D g2d = old.createGraphics();
        g2d.setFont(new Font("Serif", Font.BOLD, 20));
        String s = captureTime.toString();
        FontMetrics fm = g2d.getFontMetrics();
        int x = old.getWidth() - fm.stringWidth(s) - 5;
        int y = fm.getHeight();
        g2d.drawString(s, x, y);
        g2d.dispose();
    }

    private void startRecording() {
        Webcam webcam = null;
        webcam = getWebcam(webcam);
        setDimensions(webcam);
        webcam.open();
        long recordStart = System.currentTimeMillis();
        File recordFolder = null;
        int imageIndex = 0;
        while (this.record) {
            if (System.currentTimeMillis() <= recordStart + CHUNK_LEN) {
                if (recordFolder == null) {
                    recordFolder = new File(RECORD_FOLDER + System.currentTimeMillis());
                    recordFolder.mkdir();
                    final String recordFolderPath = recordFolder.getAbsolutePath();
                    logger.info("Recording in folder:" + recordFolderPath);
                }
                final BufferedImage image = webcam.getImage();
                pushTimeStampOnImage(recordFolder, imageIndex, image);
                imageIndex++;
            } else {
                final File completedRecordFolder = recordFolder;
                processCompletedFolder(completedRecordFolder);
                recordFolder = null;
                recordStart = System.currentTimeMillis();
                imageIndex = 0;
            }
            waitForFPS();
        }
    }

    private void waitForFPS() {
        try {
            Thread.sleep(STOP_BETWEEN_IMAGES);
        } catch (Throwable t) {
            logger.fatal("!!! FAILED TO WAIT FOR FPS !!!", t);
        }
    }

    private void processCompletedFolder(final File completedRecordFolder) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Future<Boolean> motionDetectionFuture = calculateMovementRateForFolder(completedRecordFolder);
                    File[] subfiles = completedRecordFolder.listFiles();
                    if (subfiles == null) {
                        throw new Exception("Completed Record folder is empty!!!");
                    }
                    int count = 0;
                    for (File subfile : subfiles) {
                        if (subfile.isFile() && subfile.getName().endsWith("jpg")) {
                            count++;
                        }
                    }
                    FFMPEG ffmpeg = new FFMPEG(count, CHUNK_LEN, CAPTURE_WIDTH, CAPTURE_HEIGTH, completedRecordFolder);
                    String command = ffmpeg.createExeCommand();
                    Process p = null;
                    logger.info("Running command:" + command);
                    if (!WINDOWS) {
                        p = Runtime.getRuntime().exec(command);
                        showFFMPEGDebugIfNecessary(p);
                    } else {
                        String[] wcommand =
                                {
                                        "cmd",
                                };
                        p = Runtime.getRuntime().exec(wcommand);
                        PrintWriter stdin = new PrintWriter(p.getOutputStream());
                        stdin.println(command);
                        stdin.close();
                        showFFMPEGDebugIfNecessary(p);
                    }
                    int returnCode = p.waitFor();
                    while (!motionDetectionFuture.isDone()) ;
                    if (returnCode == 0 && motionDetectionFuture.get()) {
                        logger.info("Processing is completed for folder:" + completedRecordFolder);
                        File target = new File(SERVER_WEBAPP_FOLDER + File.separator + completedRecordFolder.getName() + ".mp4");
                        File src = new File(completedRecordFolder.getAbsolutePath() + File.separator + completedRecordFolder.getName() + ".mp4");
                        FileUtils.copyFile(src, target);
                        //TODO delete images...
                        logger.info("File moved to target :" + target.getAbsolutePath());
                        CompletedCapture cc = new CompletedCapture();
                        cc.setHref("http://94.55.177.210:2442/" + SecurityInterceptor.serverAppRoot + "/static/" + completedRecordFolder.getName() + ".mp4");
                        File[] subFiles = completedRecordFolder.listFiles();
                        for (File subFile : subFiles) {
                            if (subFile.getName().endsWith("mr")) {
                                cc.setMoveRate(new BigDecimal(subFile.getName().replaceAll("\\.mr", "")));
                            } else if (subFile.getName().endsWith(".jpg")) {
                                subFile.delete();
                            }
                        }
                        cc.setStartTimeStamp(Long.valueOf(completedRecordFolder.getName()));
                        cc.setTimeFrame(SDF.format(new Date(Long.parseLong(completedRecordFolder.getName()))));
                        captures.add(cc);
                    } else {
                        logger.error("!!! Failed to process for folder:" + completedRecordFolder + ", ffmpeg returnCode:" + returnCode + " , motionDetectionStatus:" + motionDetectionFuture.get());
                    }

                } catch (Throwable t) {
                    logger.error("!!! Failed to process completed folder !!!", t);
                }
            }
        });
    }

    private void showFFMPEGDebugIfNecessary(Process p) {
        if (WINDOWS) {
            new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
            new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
        } else {
            if (Boolean.TRUE.equals(DEBUG_FFMPEG_)) {
                new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
                new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
                //                    PrintWriter stdin = new PrintWriter(p.getOutputStream());
//                    stdin.println(command);
//                    stdin.close();
            }
        }
    }

    public Set<CompletedCapture> getCaptures() {
        return captures;
    }

    private Future<Boolean> calculateMovementRateForFolder(final File completedRecordFolder) {
        return executor.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                try {
                    BigDecimal movementRate = calculateMotionRateInsideFolder(completedRecordFolder.getAbsolutePath());
                    boolean createMRFile = new File(completedRecordFolder.getAbsolutePath() + File.separator + movementRate.toPlainString() + ".mr").createNewFile();
                    if (createMRFile) {
                        logger.info("Calculated movement rate for folder:" + completedRecordFolder.getAbsolutePath() + ", rate is :" + movementRate.toPlainString());
                    } else {
                        throw new Exception(" Failed to save movement rate file ");
                    }
                    return Boolean.TRUE;
                } catch (Throwable t) {
                    logger.error("!!! Failed to process motion detection !!!", t);
                    return Boolean.FALSE;
                }
            }

        });
    }

    private void pushTimeStampOnImage(File recordFolder, int imageIndex, final BufferedImage image) {
        final Date imageTimeStamp = new Date(System.currentTimeMillis());
        final Integer currentImageIndex = imageIndex;
        final String recordFolderPath = recordFolder.getAbsolutePath();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    String filePath = recordFolderPath + File.separator + "img" + currentImageIndex + ".jpg";
                    addTimeStamp(image, imageTimeStamp);
                    ImageIO.write(image, "JPG", new File(filePath));
                } catch (Throwable t) {
                    logger.error("!!! Failed to push timestamp on image !!! ", t);
                }
            }
        });
    }

    private void setDimensions(Webcam webcam) {
        Dimension dimension = new Dimension();
        dimension.setSize(CAPTURE_WIDTH, CAPTURE_HEIGTH);
        webcam.getDevice().setResolution(dimension);
    }

    private Webcam getWebcam(Webcam webcam) {
        List<Webcam> webcamList = Webcam.getWebcams();
        for (Webcam wcam : webcamList) {
            System.out.println(wcam.getName());
            if (wcam.getName().contains(WCAM_NAME)) {
                webcam = wcam;
                break;
            }
        }
        if (webcam == null)
            webcam = Webcam.getDefault();
        if (webcam == null) {
            throw new RuntimeException("No webcam to capture.");
        }
        return webcam;
    }

    static class SyncPipe implements Runnable {
        private final OutputStream ostrm_;
        private final InputStream istrm_;

        public SyncPipe(InputStream istrm, OutputStream ostrm) {
            istrm_ = istrm;
            ostrm_ = ostrm;
        }

        public void run() {
            try {
                final byte[] buffer = new byte[1024];
                for (int length = 0; (length = istrm_.read(buffer)) != -1; ) {
                    ostrm_.write(buffer, 0, length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

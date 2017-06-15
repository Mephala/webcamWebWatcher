import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

/**
 * Created by mephala on 6/2/17.
 */
@RunWith(JMockit.class)
public class TestFFMPegCommand {

    @Test
    public void testTriggeringFFMPEG() {
        try {
            Process p = Runtime.getRuntime().exec("ffmpeg -r 15.57 -s 1920x1080 -i /home/mephala/wcam/1496426680576/img%d.jpg  -vcodec libx264 -crf 25 -pix_fmt yuv420p /home/mephala/wcam/1496426680576/1496426680576.mp4");
            int code = p.waitFor();
            assertTrue(code == 0);
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }

    @Test
    public void testTriggeringFFMPEGWindows() {
        try {
            String[] wcommand =
                    {
                            "cmd",
                    };
            Process p = Runtime.getRuntime().exec(wcommand);
            PrintWriter stdin = new PrintWriter(p.getOutputStream());
            String command = "ffmpeg -r 5.02 -s 1920x1080 -i D:\\wcam\\1497529629705\\img%d.jpg  -vcodec libx264 -crf 25 -pix_fmt yuv420p D:\\wcam\\1497529629705\\" + System.currentTimeMillis() + ".mp4";
            stdin.println(command);
            stdin.close();
            new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
            new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
            int code = p.waitFor();
            System.out.println("Finished with code:" + code);
            assertTrue(code == 0);
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
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

import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

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
}

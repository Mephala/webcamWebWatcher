import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import static junit.framework.TestCase.fail;

/**
 * Created by mephala on 7/7/17.
 */
@RunWith(JMockit.class)
public class TestAudio {


    @Test
    public void testPlayingVoice() {
        try {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Clip clip = AudioSystem.getClip();
                        AudioInputStream ais = AudioSystem.getAudioInputStream(this.getClass().getResourceAsStream("mtw.wav"));
                        clip.open(ais);
                        clip.start();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }).start();
            Thread.sleep(100000000L);
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }
}

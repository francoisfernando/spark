package nl.ypmania.fs20;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class FS20DecoderTest {
  private FS20Decoder decoder;
  
  @Before
  public void setup() {
    decoder = new FS20Decoder();
  }
  
  @Test
  public void fs20_sequence_1_is_decoded() {
    for (int w: new int[] {115,87,106,94,104,94,103,97,101,97,102,96,102,96,102,97,101,98,101,97,102,97,101,97,149,148,102,97,102,96,102,98,148,148,150,148,103,95,151,146,152,146,103,97,101,97,101,96,102,97,149,148,150,148,102,96,150,147,152,147,102,96,102,96,102,97,101,97,101,97,102,96,103,96,149,148,150,148,103,95,102,96,101,98,101,97,149,148,103,96,102,96,150,148,102,96,102,97,102,96,150,148,102,95,150,148,103,95,103,96,102,96,150,147,151,147,103}) {
      decoder.handlePulse(w * 4);
    }
    assertArrayEquals (new int[] { 27, 27, 3, 18, 81 }, decoder.getData());
    assertEquals (new FS20Packet(new FS20Address (12341234, 1114), Command.TOGGLE), decoder.getResult());
  }
  
  @Test
  public void fs20_sequence_2_is_decoded() {
    for (int w: new int[] {210,86,189,121,85,102,94,103,95,103,96,102,97,101,97,101,97,101,98,102,96,102,96,102,97,102,96,150,148,103,95,102,97,102,96,150,147,151,147,103,96,151,146,152,146,103,96,101,97,102,97,101,97,150,147,151,147,103,96,150,147,151,147,103,96,102,97,101,96,102,97,102,97,101,98,101,96,152,146,149,148,103,96,104,95,101,98,101,97,148,150,101,97,101,96,150,147,103,96,102,97,101,97,149,149,102,96,150,147,105,94,103,95,103,96,150,148,150,147,103}) {
      decoder.handlePulse(w * 4);
    }
    assertArrayEquals (new int[] { 27, 27, 3, 18, 81 }, decoder.getData());
    assertEquals (new FS20Packet(new FS20Address (12341234, 1114), Command.TOGGLE), decoder.getResult());
  }
  
  @Test
  public void fs20_motion_sensor_1_is_decoded() {
    for (int w: new int[] {101,98,96,104,95,101,102,96,98,103,96,100,100,99,97,102,97,148,149,102,99,100,96,102,97,149,147,151,147,103,96,150,149,149,147,103,96,102,97,101,97,101,98,149,148,150,148,103,96,149,148,150,147,104,95,102,97,101,98,100,98,101,97,100,98,101,97,102,97,101,99,99,98,101,97,101,98,149,148,151,146,151,147,103,97,150,148,102,97,101,98,101,97,149,148,103,96,102,97,149,148,151,146,151,147,151,147,151,146,151,147,151,146,102,96,103,96,102,97,149,148,102,96,149,148,103,96,102}) {
      decoder.handlePulse(w * 4);
    }
    assertArrayEquals (new int[] { 27, 27, 0, 58, 79, 197}, decoder.getData());
    assertEquals (new FS20Packet(new FS20Address (12341234, 1111), Command.TIMED_ON_PREVIOUS, 60), decoder.getResult());
  }

  @Test
  public void fs20_motion_sensor_2_is_decoded() {
    for (int w: new int[] {110,87,106,93,103,95,102,97,101,98,100,98,100,99,100,98,100,98,100,98,100,97,100,99,148,148,101,97,102,97,101,98,148,148,150,147,101,97,149,149,150,147,103,96,102,97,101,98,101,97,149,148,150,147,103,97,149,148,150,147,104,96,101,97,100,98,100,99,100,99,99,99,100,98,100,99,100,98,101,98,100,99,100,98,148,148,150,148,151,147,102,96,150,148,103,96,102,97,102,98,148,147,102,97,101,98,149,148,150,148,150,147,150,147,151,147,151,146,150,147,103,96,102,97,101,98,149,148,102,97,149,148,102,97,101}) {
      decoder.handlePulse(w * 4);
    }
    assertArrayEquals (new int[] { 27, 27, 0, 58, 79, 197}, decoder.getData());
    assertEquals (new FS20Packet(new FS20Address (12341234, 1111), Command.TIMED_ON_PREVIOUS, 60), decoder.getResult());
  }

  @Test
  public void fs20_motion_sensor_3_is_decoded() {
    for (int w: new int[] {104,95,103,99,99,96,102,97,101,100,99,98,100,97,101,99,100,98,101,97,149,149,101,97,102,97,100,98,149,149,150,147,102,97,151,146,150,148,103,96,101,98,101,98,100,98,149,149,150,147,102,96,150,148,150,148,102,96,101,97,102,97,101,98,100,98,101,97,101,98,101,98,100,99,100,98,100,98,101,99,148,148,150,148,150,148,103,96,150,147,103,96,102,97,102,97,149,147,103,96,102,98,149,148,150,147,151,146,150,147,151,147,151,148,150,148,102,98,100,97,102,97,149,148,102,97,149,148,102,98,101}) {
      decoder.handlePulse(w * 4);
    }
    assertArrayEquals (new int[] { 27, 27, 0, 58, 79, 197}, decoder.getData());
    assertEquals (new FS20Packet(new FS20Address (12341234, 1111), Command.TIMED_ON_PREVIOUS, 60), decoder.getResult());
  }
}

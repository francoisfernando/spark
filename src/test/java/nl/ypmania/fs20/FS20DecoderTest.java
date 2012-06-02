package nl.ypmania.fs20;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

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
    assertEquals (new Packet(new Address (12341234, 1114), Command.TOGGLE), decoder.getResult());
  }
  
  @Test
  public void fs20_sequence_2_is_decoded() {
    for (int w: new int[] {210,86,189,121,85,102,94,103,95,103,96,102,97,101,97,101,97,101,98,102,96,102,96,102,97,102,96,150,148,103,95,102,97,102,96,150,147,151,147,103,96,151,146,152,146,103,96,101,97,102,97,101,97,150,147,151,147,103,96,150,147,151,147,103,96,102,97,101,96,102,97,102,97,101,98,101,96,152,146,149,148,103,96,104,95,101,98,101,97,148,150,101,97,101,96,150,147,103,96,102,97,101,97,149,149,102,96,150,147,105,94,103,95,103,96,150,148,150,147,103}) {
      decoder.handlePulse(w * 4);
    }
    assertArrayEquals (new int[] { 27, 27, 3, 18, 81 }, decoder.getData());
    assertEquals (new Packet(new Address (12341234, 1114), Command.TOGGLE), decoder.getResult());
  }
  
  @Test
  public void fs20_sequence_3_is_decoded() {
    for (int w: new int[] {97,150,147,151,147,103,94,152,146,152,146,104,95,103,95,103,96,102,96,150,147,151,147,103,95,151,146,152,146,104,94,103,96,102,96,102,96,103,95,102,96,102,97,102,96,102,97,102,96,103,95,103,95,102,96,150,148,103,95,103,96,151,146,104,95,103,96,102,95,151,147,104,94,104,95,152,145,152,146,152,146,104,94,103,95,103,116,1,85,106,92,105,94,103,95,103}) {
      decoder.handlePulse(w * 4);
    }
    assertArrayEquals (new int[] { 27, 27, 3, 18, 81 }, decoder.getData());
    assertEquals (new Packet(new Address (12341234, 1114), Command.TOGGLE), decoder.getResult());
  }
  
  
  public void successive_sequences_are_decoded() {
    for (int w: new int[] {210,86,189,121,85,102,94,103,95,103,96,102,97,101,97,101,97,101,98,102,96,102,96,102,97,102,96,150,148,103,95,102,97,102,96,150,147,151,147,103,96,151,146,152,146,103,96,101,97,102,97,101,97,150,147,151,147,103,96,150,147,151,147,103,96,102,97,101,96,102,97,102,97,101,98,101,96,152,146,149,148,103,96,104,95,101,98,101,97,148,150,101,97,101,96,150,147,103,96,102,97,101,97,149,149,102,96,150,147,105,94,103,95,103,96,150,148,150,147,103}) {
      decoder.handlePulse(w * 4);
    }
    for (int w: new int[] {115,87,106,94,104,94,103,97,101,97,102,96,102,96,102,97,101,98,101,97,102,97,101,97,149,148,102,97,102,96,102,98,148,148,150,148,103,95,151,146,152,146,103,97,101,97,101,96,102,97,149,148,150,148,102,96,150,147,152,147,102,96,102,96,102,97,101,97,101,97,102,96,103,96,149,148,150,148,103,95,102,96,101,98,101,97,149,148,103,96,102,96,150,148,102,96,102,97,102,96,150,148,102,95,150,148,103,95,103,96,102,96,150,147,151,147,103}) {
      decoder.handlePulse(w * 4);
    }
    assertArrayEquals (new int[] { 27, 27, 3, 18, 81 }, decoder.getData());
    assertEquals (new Packet(new Address (12341234, 1114), Command.TOGGLE), decoder.getResult());
    
  }
  
  @Test
  public void non_fs20_sequence_is_ignored() {
    
  }

}

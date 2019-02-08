package su.os3.lbkx;

import org.junit.Test;

import static org.junit.Assert.*;

public class lbkxArrayUtilTest {
    @Test
    public void arrayConcTest()
    {
        assertArrayEquals(new byte[]{1,2,3,4,5,6}, lbkxArrayUtil.byteArrayConc(new byte[]{1,2}, new byte[]{3,4}, new byte[]{5,6}));
    }
    @Test
    public void doubleToByteFirstTest(){
        byte[] byteValue=new byte[]{64, -101, 4, -79, 12, -78, -107, -22};
        //byte[] byteValue=new byte[]{-22, -107, -78, 12, -79, 4, -101, 64};
        assertArrayEquals(byteValue, lbkxArrayUtil.doubleToByte(1729.1729));
    }
    @Test
    public void doubleToByteSecondTest(){
        byte[] byteValue=new byte[]{64, 73, 15, -51, 109, -36, 124, 109};
        assertArrayEquals(byteValue, lbkxArrayUtil.doubleToByte(50.1234567));
    }
    @Test
    public void intToByteTest(){
        byte[] byteValue=new byte[]{0, 0, 0, 2};
        assertArrayEquals(byteValue, lbkxArrayUtil.intToByte(2));
    }
}

package com.leyou;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UploadTest {
    @Test
    public void  testString(){
    String a="image/*";
    String b="image/jpg";
        System.out.println(a.equals(b));
    }
}

package com.chasion.community;

import java.io.IOException;

public class WKTests {
    public static void main(String[] args) {
        // 异步执行
        String cmd = "d:/work/wkhtmltopdf/bin/wkhtmltopdf https://www.nowcoder.com d:/work/data/wk-pdfs/2.pdf";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

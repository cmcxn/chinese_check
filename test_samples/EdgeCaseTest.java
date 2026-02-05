package com.example.test;

public class EdgeCaseTest {
    
    // Test escaped quotes: "He said \"hello\""
    private String quote = "He said \"你好\"";
    
    // String in comment should not be detected as variable: String x = "中文";
    private int value = 123;
    
    /* Multi-line */ String afterComment = "测试";
}

package com.chinese.check;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java代码中文检查器
 * 扫描指定文件夹中的Java文件，检查注释、常量、变量中是否含有中文字符
 */
public class ChineseChecker {
    
    // 中文字符正则表达式（包括CJK统一汉字）
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");
    
    // 单行注释
    private static final Pattern SINGLE_LINE_COMMENT = Pattern.compile("//.*");
    
    // 多行注释开始
    private static final Pattern MULTI_LINE_COMMENT_START = Pattern.compile("/\\*");
    
    // 多行注释结束
    private static final Pattern MULTI_LINE_COMMENT_END = Pattern.compile("\\*/");
    
    // 字符串字面量
    private static final Pattern STRING_LITERAL = Pattern.compile("\"([^\"]*)\"");
    
    // 变量和常量声明（简化版本）
    private static final Pattern VARIABLE_DECLARATION = Pattern.compile(
        "\\b(public|private|protected|static|final|transient|volatile)?\\s*" +
        "\\b(int|long|short|byte|char|float|double|boolean|String|[A-Z][a-zA-Z0-9_]*)" +
        "\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*[=;]"
    );
    
    private List<CheckResult> results = new ArrayList<>();
    
    /**
     * 检查结果类
     */
    public static class CheckResult {
        String filePath;
        int lineNumber;
        String type; // "注释", "常量", "变量"
        String content;
        
        public CheckResult(String filePath, int lineNumber, String type, String content) {
            this.filePath = filePath;
            this.lineNumber = lineNumber;
            this.type = type;
            this.content = content;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s:%d - %s", type, filePath, lineNumber, content);
        }
    }
    
    /**
     * 扫描指定文件夹
     */
    public void scanDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("错误: 目录不存在或不是有效目录: " + directoryPath);
            return;
        }
        
        scanDirectoryRecursive(directory);
    }
    
    /**
     * 递归扫描目录
     */
    private void scanDirectoryRecursive(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectoryRecursive(file);
            } else if (file.getName().endsWith(".java")) {
                scanJavaFile(file);
            }
        }
    }
    
    /**
     * 扫描单个Java文件
     */
    private void scanJavaFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            boolean inMultiLineComment = false;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmedLine = line.trim();
                
                // 检查多行注释
                if (inMultiLineComment) {
                    if (containsChinese(line)) {
                        results.add(new CheckResult(file.getPath(), lineNumber, "注释", trimmedLine));
                    }
                    if (MULTI_LINE_COMMENT_END.matcher(line).find()) {
                        inMultiLineComment = false;
                    }
                    continue;
                }
                
                // 检查多行注释开始
                if (MULTI_LINE_COMMENT_START.matcher(line).find()) {
                    inMultiLineComment = true;
                    if (containsChinese(line)) {
                        results.add(new CheckResult(file.getPath(), lineNumber, "注释", trimmedLine));
                    }
                    if (MULTI_LINE_COMMENT_END.matcher(line).find()) {
                        inMultiLineComment = false;
                    }
                    continue;
                }
                
                // 检查单行注释
                Matcher singleCommentMatcher = SINGLE_LINE_COMMENT.matcher(line);
                if (singleCommentMatcher.find()) {
                    String comment = singleCommentMatcher.group();
                    if (containsChinese(comment)) {
                        results.add(new CheckResult(file.getPath(), lineNumber, "注释", trimmedLine));
                    }
                }
                
                // 检查字符串字面量（可能是常量值）
                Matcher stringMatcher = STRING_LITERAL.matcher(line);
                while (stringMatcher.find()) {
                    String stringContent = stringMatcher.group(1);
                    if (containsChinese(stringContent)) {
                        // 判断是否是常量（简化判断：包含final或全大写变量名）
                        if (line.contains("final") || line.matches(".*\\b[A-Z_][A-Z0-9_]*\\s*=.*")) {
                            results.add(new CheckResult(file.getPath(), lineNumber, "常量", trimmedLine));
                        } else {
                            results.add(new CheckResult(file.getPath(), lineNumber, "变量", trimmedLine));
                        }
                    }
                }
                
                // 检查变量名本身是否包含中文
                Matcher varMatcher = VARIABLE_DECLARATION.matcher(line);
                while (varMatcher.find()) {
                    String varName = varMatcher.group(3);
                    if (containsChinese(varName)) {
                        if (line.contains("final") || varName.matches("[A-Z_][A-Z0-9_]*")) {
                            results.add(new CheckResult(file.getPath(), lineNumber, "常量", 
                                "变量名包含中文: " + varName));
                        } else {
                            results.add(new CheckResult(file.getPath(), lineNumber, "变量", 
                                "变量名包含中文: " + varName));
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("读取文件失败: " + file.getPath() + " - " + e.getMessage());
        }
    }
    
    /**
     * 检查字符串是否包含中文
     */
    private boolean containsChinese(String text) {
        if (text == null) {
            return false;
        }
        Matcher matcher = CHINESE_PATTERN.matcher(text);
        return matcher.find();
    }
    
    /**
     * 获取检查结果
     */
    public List<CheckResult> getResults() {
        return results;
    }
    
    /**
     * 打印检查结果
     */
    public void printResults() {
        if (results.isEmpty()) {
            System.out.println("未发现含有中文的代码");
            return;
        }
        
        System.out.println("\n========== 检查结果 ==========");
        System.out.println("发现 " + results.size() + " 处包含中文的位置:\n");
        
        for (CheckResult result : results) {
            System.out.println(result);
        }
        
        // 统计
        long commentCount = results.stream().filter(r -> r.type.equals("注释")).count();
        long constantCount = results.stream().filter(r -> r.type.equals("常量")).count();
        long variableCount = results.stream().filter(r -> r.type.equals("变量")).count();
        
        System.out.println("\n========== 统计 ==========");
        System.out.println("注释中含中文: " + commentCount + " 处");
        System.out.println("常量中含中文: " + constantCount + " 处");
        System.out.println("变量中含中文: " + variableCount + " 处");
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("使用方法: java ChineseChecker <目录路径>");
            System.out.println("示例: java ChineseChecker ./src");
            return;
        }
        
        String directoryPath = args[0];
        ChineseChecker checker = new ChineseChecker();
        
        System.out.println("开始扫描目录: " + directoryPath);
        checker.scanDirectory(directoryPath);
        checker.printResults();
    }
}

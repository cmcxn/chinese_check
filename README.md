# chinese_check
代码中文检测器

## 功能描述

这是一个Java代码扫描器，用于检测指定文件夹中的Java文件是否包含中文字符。

扫描内容包括：
- **注释**：单行注释（//）和多行注释（/* */）中的中文
- **常量**：常量值中的中文字符
- **变量**：变量值和变量名中的中文字符

## 使用方法

### 1. 编译代码

```bash
./build.sh
```

或手动编译：

```bash
javac -d bin src/main/java/com/chinese/check/ChineseChecker.java
```

### 2. 运行扫描器

```bash
java -cp bin com.chinese.check.ChineseChecker <要扫描的目录路径>
```

示例：

```bash
# 扫描test_samples目录
java -cp bin com.chinese.check.ChineseChecker ./test_samples

# 扫描src目录
java -cp bin com.chinese.check.ChineseChecker ./src
```

## 示例输出

```
开始扫描目录: test_samples

========== 检查结果 ==========
发现 12 处包含中文的位置:

[注释] test_samples/TestSample.java:4 - * 这是一个测试类
[注释] test_samples/TestSample.java:5 - * 用于演示中文检测功能
[注释] test_samples/TestSample.java:9 - // 这是单行注释，包含中文
[常量] test_samples/TestSample.java:10 - private static final String MESSAGE = "你好世界";
[变量] test_samples/TestSample.java:16 - private String userName = "张三";

========== 统计 ==========
注释中含中文: 9 处
常量中含中文: 1 处
变量中含中文: 2 处
```

## 项目结构

```
chinese_check/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── chinese/
│                   └── check/
│                       └── ChineseChecker.java  # 主扫描器类
├── test_samples/                                # 测试样例
│   ├── TestSample.java                         # 包含中文的测试文件
│   └── CleanSample.java                        # 不包含中文的测试文件
├── build.sh                                     # 编译脚本
└── README.md                                    # 说明文档
```

## 技术说明

- 使用正则表达式检测中文字符（Unicode范围：\u4e00-\u9fa5）
- 递归扫描指定目录下的所有.java文件
- 分析注释、字符串字面量和变量声明
- 生成详细的检查报告，包括文件路径、行号、类型和内容

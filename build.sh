#!/bin/bash

# 编译Java代码扫描器
echo "正在编译..."
javac -d bin src/main/java/com/chinese/check/ChineseChecker.java

if [ $? -eq 0 ]; then
    echo "编译成功！"
    echo ""
    echo "使用方法:"
    echo "  java -cp bin com.chinese.check.ChineseChecker <目录路径>"
    echo ""
    echo "示例:"
    echo "  java -cp bin com.chinese.check.ChineseChecker ./test_samples"
else
    echo "编译失败！"
    exit 1
fi

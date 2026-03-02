#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
使用二进制方式强制将所有 AIDL 文件转换为纯 UTF-8 编码（无 BOM）
"""
import os
import sys

def remove_bom(data):
    """移除 UTF-8 BOM"""
    if data.startswith(b'\xef\xbb\xbf'):
        return data[3:]
    return data

def clean_text(content):
    """清理文本内容，确保只包含可打印字符和标准换行符"""
    # 标准化换行符为 \n
    content = content.replace('\r\n', '\n').replace('\r', '\n')
    # 移除不可打印的控制字符（保留 \n 和 \t）
    cleaned = []
    for char in content:
        code = ord(char)
        if code == 10 or code == 9 or (code >= 32 and code < 127) or code > 127:
            cleaned.append(char)
    return ''.join(cleaned)

def convert_file_binary(file_path):
    """使用二进制方式读取和转换文件"""
    try:
        print(f"处理文件: {os.path.basename(file_path)}")
        
        # 以二进制方式读取
        with open(file_path, 'rb') as f:
            raw_data = f.read()
        
        # 移除 BOM
        raw_data = remove_bom(raw_data)
        
        # 尝试解码
        content = None
        encodings = ['utf-8', 'gbk', 'gb2312', 'latin1', 'cp1252', 'iso-8859-1']
        
        for enc in encodings:
            try:
                content = raw_data.decode(enc)
                print(f"  使用编码 {enc} 成功解码")
                break
            except (UnicodeDecodeError, UnicodeError):
                continue
        
        if content is None:
            # 使用错误处理策略
            content = raw_data.decode('utf-8', errors='replace')
            print(f"  使用 UTF-8 (替换错误) 解码")
        
        # 清理内容
        content = clean_text(content)
        
        # 确保以换行符结尾
        if content and not content.endswith('\n'):
            content += '\n'
        
        # 以二进制方式写入 UTF-8（无 BOM）
        utf8_bytes = content.encode('utf-8')
        with open(file_path, 'wb') as f:
            f.write(utf8_bytes)
        
        print(f"  ✓ 已转换为 UTF-8 (无 BOM)")
        return True
        
    except Exception as e:
        print(f"  ✗ 错误: {e}")
        import traceback
        traceback.print_exc()
        return False

def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    aidl_dir = os.path.join(base_dir, 'terminal', 'src', 'main', 'aidl')
    
    if not os.path.exists(aidl_dir):
        print(f"错误: AIDL 目录不存在: {aidl_dir}")
        return 1
    
    aidl_files = []
    for root, dirs, files in os.walk(aidl_dir):
        for file in files:
            if file.endswith('.aidl'):
                aidl_files.append(os.path.join(root, file))
    
    if not aidl_files:
        print(f"在 {aidl_dir} 中未找到 AIDL 文件")
        return 1
    
    print(f"找到 {len(aidl_files)} 个 AIDL 文件\n")
    print("=" * 60)
    
    success_count = 0
    for aidl_file in aidl_files:
        if convert_file_binary(aidl_file):
            success_count += 1
        print()
    
    print("=" * 60)
    print(f"\n处理完成: {success_count}/{len(aidl_files)} 个文件成功转换")
    
    if success_count == len(aidl_files):
        print("\n✓ 所有文件已成功转换！")
        print("请运行: gradlew clean build")
        return 0
    else:
        print("\n✗ 部分文件转换失败，请检查错误信息")
        return 1

if __name__ == '__main__':
    sys.exit(main())


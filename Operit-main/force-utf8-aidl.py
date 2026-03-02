#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
强制将所有 AIDL 文件转换为 UTF-8 编码（无 BOM）
"""
import os
import sys
import codecs

def convert_to_utf8(file_path):
    """将文件转换为 UTF-8 编码（无 BOM）"""
    try:
        # 尝试用不同编码读取文件
        encodings = ['utf-8', 'utf-8-sig', 'gbk', 'gb2312', 'latin1', 'cp1252']
        content = None
        used_encoding = None
        
        for enc in encodings:
            try:
                with open(file_path, 'r', encoding=enc) as f:
                    content = f.read()
                    used_encoding = enc
                    print(f"成功使用 {enc} 编码读取: {os.path.basename(file_path)}")
                    break
            except (UnicodeDecodeError, UnicodeError):
                continue
        
        if content is None:
            # 如果所有编码都失败，尝试用二进制模式读取并忽略错误
            with open(file_path, 'rb') as f:
                raw = f.read()
            content = raw.decode('utf-8', errors='ignore')
            print(f"使用 UTF-8 (忽略错误) 读取: {os.path.basename(file_path)}")
            used_encoding = 'binary'
        
        # 使用 UTF-8 无 BOM 编码保存
        with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
            f.write(content)
        print(f"已转换为 UTF-8: {os.path.basename(file_path)}")
        return True
        
    except Exception as e:
        print(f"处理文件 {file_path} 时出错: {e}")
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
    
    success_count = 0
    for aidl_file in aidl_files:
        if convert_to_utf8(aidl_file):
            success_count += 1
        print()
    
    print(f"\n处理完成: {success_count}/{len(aidl_files)} 个文件成功转换")
    return 0 if success_count == len(aidl_files) else 1

if __name__ == '__main__':
    sys.exit(main())


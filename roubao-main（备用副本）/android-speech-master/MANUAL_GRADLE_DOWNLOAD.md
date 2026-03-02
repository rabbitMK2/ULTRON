# 手动下载Gradle解决超时问题

## 问题
所有镜像源都超时，无法自动下载Gradle。

## 解决方案：手动下载并放置

### 步骤1：找到Gradle缓存目录

Gradle会尝试下载到以下目录：
```
C:\Users\你的用户名\.gradle\wrapper\dists\gradle-8.0-bin\随机字符串\
```

**如何找到"随机字符串"文件夹？**

1. 让Android Studio尝试下载一次（即使会失败）
2. 打开文件管理器，导航到：`C:\Users\你的用户名\.gradle\wrapper\dists\`
3. 你会看到类似这样的结构：
   ```
   gradle-8.0-bin\
     └── 随机字符串（如：a1b2c3d4e5f6...）\
         └── gradle-8.0-bin.zip（这里放下载的文件）
   ```

### 步骤2：下载Gradle 8.0

选择一个可用的下载源：

#### 选项A：使用浏览器直接下载
1. 打开浏览器，访问以下任一链接：
   - **华为云镜像**：https://mirrors.huaweicloud.com/gradle/gradle-8.0-bin.zip
   - **清华大学镜像**：https://mirrors.tuna.tsinghua.edu.cn/gradle/gradle-8.0-bin.zip
   - **腾讯云镜像**：https://mirrors.cloud.tencent.com/gradle/gradle-8.0-bin.zip
   - **官方源**：https://services.gradle.org/distributions/gradle-8.0-bin.zip

2. 下载 `gradle-8.0-bin.zip` 文件（约150MB）

#### 选项B：使用下载工具
如果浏览器下载也慢，可以使用：
- IDM（Internet Download Manager）
- 迅雷
- 其他下载工具

### 步骤3：放置文件

1. **如果随机字符串文件夹已存在**：
   - 直接将下载的 `gradle-8.0-bin.zip` 放到该文件夹中
   - **不要解压！** 直接放zip文件

2. **如果随机字符串文件夹不存在**：
   - 先让Android Studio尝试同步一次（会创建文件夹）
   - 或者手动创建：`C:\Users\你的用户名\.gradle\wrapper\dists\gradle-8.0-bin\随机字符串\`
   - 然后将zip文件放入

### 步骤4：验证

1. 在Android Studio中：
   - `File` -> `Invalidate Caches / Restart`
   - 选择 `Invalidate and Restart`
   
2. 重启后：
   - `File` -> `Sync Project with Gradle Files`
   - 应该能正常同步了

## 快速操作脚本（PowerShell）

如果随机字符串文件夹已存在，可以使用以下PowerShell命令：

```powershell
# 替换 YOUR_USERNAME 为你的Windows用户名
# 替换 RANDOM_STRING 为实际的随机字符串文件夹名
$gradlePath = "C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.0-bin\RANDOM_STRING"
$zipFile = "C:\Users\YOUR_USERNAME\Downloads\gradle-8.0-bin.zip"  # 下载文件的位置

# 复制文件
Copy-Item $zipFile -Destination $gradlePath -Force
```

## 替代方案：使用本地Gradle

如果手动下载还是有问题，可以配置使用本地已安装的Gradle：

### 步骤1：下载并安装Gradle

1. 从 https://gradle.org/releases/ 下载Gradle 8.0
2. 解压到某个目录，如：`C:\gradle\gradle-8.0`

### 步骤2：配置Android Studio

1. `File` -> `Settings`（或 `Ctrl+Alt+S`）
2. `Build, Execution, Deployment` -> `Build Tools` -> `Gradle`
3. 选择 `Use local gradle distribution`
4. 指定Gradle安装路径：`C:\gradle\gradle-8.0`
5. 点击 `Apply` 和 `OK`

### 步骤3：同步项目

`File` -> `Sync Project with Gradle Files`

## 检查Gradle版本兼容性

当前项目配置：
- **Android Gradle Plugin**: 8.1.0
- **需要的Gradle版本**: 8.0 或更高

如果使用其他Gradle版本，需要相应调整AGP版本。

## 常见问题

### Q: 如何知道我的用户名？
A: 在PowerShell中运行：`echo $env:USERNAME`

### Q: 随机字符串文件夹名是什么？
A: 让Android Studio尝试下载一次，会自动创建。或者查看 `.gradle\wrapper\dists\gradle-8.0-bin\` 下的文件夹。

### Q: 下载的文件需要解压吗？
A: **不需要！** 直接放zip文件，Gradle会自动解压。

### Q: 可以删除旧的下载尝试吗？
A: 可以，删除 `.gradle\wrapper\dists\` 下失败的下载文件夹，重新尝试。

## 推荐方案

1. **首选**：使用浏览器下载华为云或清华镜像的zip文件，手动放置
2. **备选**：使用下载工具（如IDM）下载
3. **最后**：配置使用本地Gradle安装


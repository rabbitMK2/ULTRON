# Gradle 下载超时问题解决方案

## 问题描述
```
Could not install Gradle distribution from 'https://services.gradle.org/distributions/gradle-6.5-bin.zip'.
Reason: java.net.SocketTimeoutException: Connect timed out
```

## 已实施的解决方案

### 1. 配置国内镜像源

已更新以下文件使用国内镜像：

#### `gradle/wrapper/gradle-wrapper.properties`
- 使用腾讯云镜像下载Gradle
- 如果腾讯云镜像也超时，可以尝试其他镜像（见文件中的注释）

#### `build.gradle`
- 添加了阿里云Maven镜像源
- 优先使用国内镜像，备用官方源

### 2. 手动下载Gradle（如果镜像也超时）

如果所有镜像都超时，可以手动下载：

#### 步骤1：下载Gradle
1. 访问以下任一地址下载 Gradle 8.0：
   - 腾讯云镜像：https://mirrors.cloud.tencent.com/gradle/gradle-8.0-bin.zip
   - 阿里云镜像：https://mirrors.aliyun.com/macports/distfiles/gradle/gradle-8.0-bin.zip
   - 官方源：https://services.gradle.org/distributions/gradle-8.0-bin.zip

2. 下载完成后，将zip文件放到：
   ```
   C:\Users\你的用户名\.gradle\wrapper\dists\gradle-8.0-bin\随机字符串\
   ```
   注意：`随机字符串` 文件夹会在Android Studio首次尝试下载时自动创建

#### 步骤2：让Android Studio识别
1. 在Android Studio中点击 `File` -> `Invalidate Caches / Restart`
2. 选择 `Invalidate and Restart`
3. 重新同步项目

### 3. 配置代理（可选）

如果您有代理，可以在 `gradle.properties` 中添加：

```properties
systemProp.http.proxyHost=your.proxy.host
systemProp.http.proxyPort=8080
systemProp.https.proxyHost=your.proxy.host
systemProp.https.proxyPort=8080
```

### 4. 使用本地Gradle（推荐）

如果以上方法都不行，可以配置使用本地已安装的Gradle：

1. 下载并安装Gradle 8.0到本地
2. 在Android Studio中：
   - `File` -> `Settings` -> `Build, Execution, Deployment` -> `Build Tools` -> `Gradle`
   - 选择 `Use local gradle distribution`
   - 指定Gradle安装路径

## 验证修复

完成配置后：
1. 在Android Studio中点击 `File` -> `Sync Project with Gradle Files`
2. 查看 `Build` 窗口，应该能看到从镜像源下载
3. 如果还有问题，查看错误日志中的具体URL

## 其他镜像源（备用）

如果腾讯云镜像也超时，可以尝试修改 `gradle-wrapper.properties` 中的 `distributionUrl`：

### 华为云镜像
```
distributionUrl=https\://mirrors.huaweicloud.com/gradle/gradle-8.0-bin.zip
```

### 清华大学镜像
```
distributionUrl=https\://mirrors.tuna.tsinghua.edu.cn/gradle/gradle-8.0-bin.zip
```

### 直接使用官方源（如果网络改善）
```
distributionUrl=https\://services.gradle.org/distributions/gradle-8.0-bin.zip
```

## 常见问题

### Q: 修改后还是超时？
A: 
1. 检查网络连接
2. 尝试使用VPN或代理
3. 手动下载Gradle（见步骤2）
4. 尝试其他镜像源

### Q: 如何知道Gradle下载路径？
A: 
1. 让Android Studio尝试下载一次（会创建随机字符串文件夹）
2. 查看 `C:\Users\你的用户名\.gradle\wrapper\dists\` 下的文件夹
3. 将下载的zip文件放到对应的随机字符串文件夹中

### Q: 可以降级Gradle版本吗？
A: 
可以，但需要同时降级Android Gradle Plugin版本：
- Gradle 7.5 对应 AGP 7.4.x
- Gradle 7.6 对应 AGP 7.4.x
- Gradle 8.0 对应 AGP 8.0+

当前项目使用AGP 8.1.0，需要Gradle 8.0或更高版本。


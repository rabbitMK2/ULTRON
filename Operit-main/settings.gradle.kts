pluginManagement {
    repositories {
        // 先查本地以避免外网 TLS 问题
        maven { url = uri("$rootDir/local-m2") }
        flatDir { dirs(rootDir) }
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 本地临时仓库与平铺目录，用于手工下载的 aapt2-proto
        maven { url = uri("$rootDir/local-m2") }
        flatDir { dirs(rootDir) }
        // 先尝试国内镜像以避免 TLS 阻断
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://dl.bintray.com/rikkaw/Shizuku") }
        maven { url = uri("https://api.xposed.info/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    }
}

rootProject.name = "Operit"
include(":app")
include(":dragonbones")
include(":terminal")
include(":mnn")
include(":miniscrcpy")
include(":showerclient")
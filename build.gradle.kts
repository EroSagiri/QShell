plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.10.1"
}

group = "me.sagiri.mirai.plugin.QShell"
version = "0.2.0"

repositories {
    mavenLocal()
//    maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
    mavenCentral()
    jcenter()
    maven { url = uri("https://dl.bintray.com/karlatemp/misc") }
}
//
dependencies {
//    runtimeOnly("net.mamoe:mirai-login-solver-selenium:1.0-dev-16")
    implementation(group = "com.github.kevinsawicki", version = "6.0", name = "http-request")
}
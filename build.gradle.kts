plugins {
    val kotlinVersion = "1.4.31"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.4.1"
}

group = "me.sagiri.mirai.plugin.QShell"
version = "0.1.1"

repositories {
    mavenLocal()
//    maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
    mavenCentral()
    jcenter()
    maven { url = uri("https://dl.bintray.com/karlatemp/misc") }
}
//
//dependencies {
//    runtimeOnly("net.mamoe:mirai-login-solver-selenium:1.0-dev-16")
//}
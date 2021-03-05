# mirai-QShell
在qq代理执行系统Shell命令

## 如何使用
从Releases下载jar文件放到mirai-console的plugins  
关于如何安装mirai-console看这里[mirai-native](https://github.com/iTXTech/mirai-native)
启动mcl会在config/QShell/生成配置文件

## 配置文件
master 主人的qq,主人有执行所有命令的权限  
shellList 是一个列表，每一条代表着一个shell解析器  
shell解析器  
  name shell解析器的名字  
  commandRegex 匹配命令的正则表达式  
  commandList 这是一个运行命令的列表，第一个要是可执行的程序，后面的是传递参数，$x会被替换成匹配到的分组 
  trustList 能执行这个shell的用户，当这个列表里存在用户0时，所有人都有权限执行  
  isEnabled true时该shell是启用的,false时该shell是关闭的  
  description 这个shell的描述  
  notPresentMessage 匹配到命令但是没有权限时返回的消息  

### 运行bash的配置  
```
master: 2476255563
shellList: 
  - name: shell
    commandRegex: '^\$(.+)'
    commandList: 
      - bash
      - '-c'
      - '$1'
```

### 运行python的配置
```
  - name: python
    commandRegex: '^#python(.+)'
    commandList: 
      - python
      - '-c'
      - '$1'
```

###在容器中运行bash
确保当前有权限执行docker命令  
运行一个容器,守护进程方式，并且执行bash,不如ta退出
docker container run -d -it --rm --name=bash archlinux /bin/bash  
不要用bash去执行docker（很容易被提权,直接使用docker  
```
  - name: shell
    commandRegex: '^\$(.+)'
    commandList: 
      - docker
      - exec
      - f4e54ee3a981
      - bash
      - '-c'
      - '$1'
```

### 奇怪的配置
复读机  
```
  - name: echo
    commandRegex: '(.)'
    commandList: 
      - echo
      - $1
```

但有人发送你好的时候回复你好呀  
```
  - name: shell
    commandRegex: '你好'
    commandList: 
      - echo
      - '你好呀'
```

## 指令
/qs help 获取帮助  
/qs list 获取shell列表  
/qs info 获取shell详细信息  
/qs echo 回复指定消息  
/qs trust 添加用户到指定信任列表  
/qs deny 从信任列表移除用户  
/qs denyAll 清楚指定的shell所有信任列表  
/qs enable 开启一个shell  
/qs disable 关闭一个shell  
/qs add 添加一个shell  
/qs set 设置shell的指定值  
/qs cmd 编辑命令行  
/qs reload 重新加载配置  

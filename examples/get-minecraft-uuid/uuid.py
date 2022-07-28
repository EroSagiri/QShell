#!/bin/env python
import requests, json, sys, datetime, time

class MinecraftApi:
    def __init__(self):
        pass

    @staticmethod
    def getUuid(name: str):
        """
        通过用户名得到uuid
        :param name: 玩家用户名
        :return: uuidData 对象包含name id
        """
        url = f"https://api.mojang.com/users/profiles/minecraft/{name}"
        content = MinecraftApi.get(url)

        if content != None:
            jsonObject = json.loads(content)

            return UuidData(jsonObject["name"], jsonObject["id"])
        else:
            return None

    @staticmethod
    def getAllNames(uuid: str):
        """
        通过uuid获取玩家所有名字
        :param uuid: 玩家uuid
        :return: 一个名字数组
        """
        url = f"https://api.mojang.com/user/profiles/{uuid}/names"
        content = MinecraftApi.get(url)

        if content != None:
            jsonObject = json.loads(content)
            names = []
            for name in jsonObject:
                username = name["name"]
                changedToAt = 0
                try :
                    changedToAt = int(name["changedToAt"])
                except KeyError :
                    changedToAt = 0

                names.append(NameData(username, changedToAt))

            return names
        else:
            return None

    @staticmethod
    def getProfile(uuid: str):
        """
        得到名字和皮肤
        :param uuid: 玩家uuid
        :return: 返回 Profile对象或者None
        """
        url = f"https://sessionserver.mojang.com/session/minecraft/profile/{uuid}"
        content = MinecraftApi.get(url)
        if content != None:
            jsonObject = json.loads(content)
            properties = []
            for propertie in jsonObject["properties"]:
                name = propertie["name"]
                value = propertie["value"]

                properties.append(PropertieData(name, value))

            return ProfileData(jsonObject["name"], jsonObject["id"], properties)
        else:
            return None

    @staticmethod
    def get(url: str) -> str:
        try:
            req = requests.get(url)
            code = req.status_code
            if code == 200:
                return req.text
            else:
                print(f"请求 {url} 状态码 : {code}")
                return None
        except ValueError:
            print("检查网络")
            return None
        except requests.exceptions.ConnectionError:
            print("检查网络")
            return None
        except :
            print("未知错误")
            return None


class UuidData:
    name = None
    id = None

    def __init__(self, name: str, id: str):
        self.name = name
        self.id = id

    def __str__(self):
        return f'name : {self.name}, id : {self.id}'


class ProfileData:
    name = None
    id = None
    properties = None

    def __init__(self, name: str, id: str, properties: list):
        """
        构造方法
        :param name: 玩家名
        :param id: 玩家皮肤id
        :param properties: 一个PropertieData列表 皮肤，披风 name ==  textures 是皮肤 value 是一个base64编码的皮肤
        """
        self.name = name
        self.id = id
        self.properties = properties

    def __str__(self):
        return f"name: {self.name}, id: {self.id}"


class PropertieData:
    name = None
    value = None

    def __init__(self, name : str, value : str):
        self.name = name
        self.value = value

class NameData:
    # 名字
    name = None
    # 时间戳
    changedToAt = None

    def __init__(self, name : str, changedToAt : int = 0) :
        self.name = name
        self.changedToAt = changedToAt


if __name__ == '__main__':
    if len(sys.argv) <= 1 :
        exit(1)
    username = sys.argv[1]
    uuid = MinecraftApi.getUuid(username)
    if uuid != None:
        names = MinecraftApi.getAllNames(uuid.id)
        # profile = MinecraftApi.getProfile(uuid.id)
        # for name in names :
        #     if name.name == uuid.name:
        #         names.remove(name)
        print(f"玩家: {uuid.name}\nUUID: {uuid.id}")

        if len(names) > 1 :
            print("使用过名字: ")
            for name in names :
                print(f"  {name.name}", end="")
                if name.changedToAt != 0:
                    changedToAtTime = time.localtime(int(name.changedToAt)/1000)
                    print(f'    {time.strftime("%Y-%m-%d", changedToAtTime)}')

                print()

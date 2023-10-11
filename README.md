# plugin-equipments

Halo 2.0 的装备管理插件, 支持在 Console 进行管理以及为主题端提供 `/equipments` 页面路由。

## 使用方式

1. 下载，目前提供以下两个下载方式：
    - GitHub Releases：访问 [Releases](https://github.com/chengzhongxue/plugin-equipments/releases) 下载 Assets 中的 JAR 文件。
2. 安装，插件安装和更新方式可参考：<https://docs.halo.run/user-guide/plugins>
3. 安装完成之后，访问 Console 左侧的**我的装备**菜单项，即可进行管理。
4. 前台访问地址为 `/equipments`，需要注意的是，此插件需要主题提供模板（equipments.html）才能访问 `/equipments`。

## 开发环境

```bash
git clone git@github.com:chengzhongxue/plugin-equipments.git

# 或者当你 fork 之后

git clone git@github.com:{your_github_id}/plugin-equipments.git
```

```bash
cd path/to/plugin-equipments
```

```bash
# macOS / Linux
./gradlew pnpmInstall

# Windows
./gradlew.bat pnpmInstall
```

```bash
# macOS / Linux
./gradlew build

# Windows
./gradlew.bat build
```

修改 Halo 配置文件：

```yaml
halo:
  plugin:
    runtime-mode: development
    classes-directories:
      - "build/classes"
      - "build/resources"
    lib-directories:
      - "libs"
    fixedPluginPath:
      - "/path/to/plugin-equipments"
```

## 主题适配

目前此插件为主题端提供了 `/equipments` 路由，模板为 `equipments.html`，也提供了 [Finder API](https://docs.halo.run/developer-guide/theme/finder-apis)，可以将装备列表渲染到任何地方。

### 模板变量

#### 路由信息

- 模板路径：/templates/equipments.html
- 访问路径：/equipments | /equipments/page/{page}

#### 路由可选参数

group: 装备分组名称, 对应 [#EquipmentGroupVo](#equipmentgroupvo).metadata.name

示例：/equipments?group=equipment-group-UEcvi | /equipments/page/1?group=equipment-group-UEcvi

#### 变量

groups

##### 变量类型

List<[#EquipmentGroupVo](#equipmentgroupvo)>

##### 示例

```html
<th:block th:each="group : ${groups}">
    <h2 th:text="${group.spec.displayName}"></h2>
    <ul>
        <li th:each="equipment : ${group.equipments}">
            <img th:src="${equipment.spec.url}" th:alt="${equipment.spec.displayName}" width="280">
        </li>
    </ul>
</th:block>
```

#### 变量

equipments

##### 变量类型

[#UrlContextListResult\<EquipmentVo>](#urlcontextlistresult)

##### 示例

```html
<ul>
    <li th:each="equipment : ${equipments.items}">
        <img th:src="${equipment.spec.url}" th:alt="${equipment.spec.displayName}" width="280">
    </li>
</ul>
<div th:if="${equipments.hasPrevious() || equipments.hasNext()}">
   <a th:href="@{${equipments.prevUrl}}">
      <span>上一页</span>
   </a>
   <span th:text="${equipments.page}"></span>
   <a th:href="@{${equipments.nextUrl}}">
      <span>下一页</span>
   </a>
</div>
```

### Finder API

#### groupBy()

##### 描述

获取全部分组内容。

##### 参数

无

##### 返回值

List<[#EquipmentGroupVo](#equipmentgroupvo)>

##### 示例

```html
<th:block th:each="group : ${equipmentFinder.groupBy()}">
    <h2 th:text="${group.spec.displayName}"></h2>
    <ul>
        <li th:each="equipment : ${group.equipments}">
            <img th:src="${equipment.spec.url}" th:alt="${equipment.spec.displayName}" width="280">
        </li>
    </ul>
</th:block>
```

#### listAll()

##### 描述

获取全部装备内容。

##### 参数

无

##### 返回值

List<[#EquipmentVo](#equipmentvo)>

##### 示例

```html
<ul>
    <li th:each="equipment : ${equipmentFinder.listAll()}" style="display: inline;">
        <img th:src="${equipment.spec.url}" th:alt="${equipment.spec.displayName}" width="280">
    </li>
</ul>
```

#### listBy(group)

##### 描述

根据分组获取装备列表。

##### 参数

1. `group: string` - 装备分组名称, 对应 EquipmentGroupVo.metadata.name

##### 返回值

List<[#EquipmentVo](#equipmentvo)>

##### 示例

```html
<ul>
    <li th:each="equipment : ${equipmentFinder.listBy('equipment-group-UEcvi')}" style="display: inline;">
        <img th:src="${equipment.spec.url}" th:alt="${equipment.spec.displayName}" width="280">
    </li>
</ul>
```

#### list(page, size)

##### 描述

根据分页参数获取装备列表。

##### 参数

1. `page: int` - 分页页码，从 1 开始
2. `size: int` - 分页条数

##### 返回值

[ListResult\<EquipmentVo>](#listresult-equipmentvo)

##### 示例

```html
<th:block th:with="equipments = ${equipmentFinder.list(1, 10)}">
    <ul>
        <li th:each="equipment : ${equipments.items}">
            <img th:src="${equipment.spec.url}" th:alt="${equipment.spec.displayName}" width="280">
        </li>
    </ul>
    <div>
        <span th:text="${equipments.page}"></span>
    </div>
</th:block>
```

#### list(page, size, group)

##### 描述

根据分页参数及装备所在组获取装备列表。

##### 参数

1. `page: int` - 分页页码，从 1 开始
2. `size: int` - 分页条数
3. `group: string` - 装备分组名称, 对应 EquipmentGroupVo.metadata.name

##### 返回值

[ListResult\<EquipmentVo>](#listresult-equipmentvo)

##### 示例

```html
<th:block th:with="equipments = ${equipmentFinder.list(1, 10, 'equipment-group-UEcvi')}">
    <ul>
        <li th:each="equipment : ${equipments.items}">
            <img th:src="${equipment.spec.url}" th:alt="${equipment.spec.displayName}" width="280">
        </li>
    </ul>
    <div>
        <span th:text="${equipments.page}"></span>
    </div>
</th:block>
```

### 类型定义

#### EquipmentVo

```json
{
  "metadata": {
    "name": "string",                                   // 唯一标识
    "labels": {
      "additionalProp1": "string"
    },
    "annotations": {
      "additionalProp1": "string"
    },
    "creationTimestamp": "2022-11-20T13:06:38.512Z",    // 创建时间
  },
  "spec": {
    "displayName": "string",                            // 装备名称
    "description": "string",                            // 装备描述
    "url": "string",                                    // 装备链接
    "cover": "string",                                  // 封面链接
    "priority": 0,                                      // 优先级
    "groupName": "string",                              // 分组名称，对应分组 metadata.name
  },
}
```

#### EquipmentGroupVo

```json
{
  "metadata": {
    "name": "string",                                   // 唯一标识
    "labels": {
      "additionalProp1": "string"
    },
    "annotations": {
      "additionalProp1": "string"
    },
    "creationTimestamp": "2022-11-20T13:06:38.512Z",    // 创建时间
  },
  "spec": {
    "displayName": "string",                            // 分组名称
    "priority": 0,                                      // 分组优先级
  },
  "status": {
    "equipmentCount": 0,                                    // 分组下装备数量
  },
  "equipments": "List<#EquipmentVo>",                           // 分组下所有装备列表
}
```

#### ListResult<EquipmentVo>

```json
{
  "page": 0,                                   // 当前页码
  "size": 0,                                   // 每页条数
  "total": 0,                                  // 总条数
  "items": "List<#EquipmentVo>",                   // 装备列表数据
  "first": true,                               // 是否为第一页
  "last": true,                                // 是否为最后一页
  "hasNext": true,                             // 是否有下一页
  "hasPrevious": true,                         // 是否有上一页
  "totalPages": 0                              // 总页数
}
```

#### UrlContextListResult<EquipmentVo>

```json
{
  "page": 0,                                   // 当前页码
  "size": 0,                                   // 每页条数
  "total": 0,                                  // 总条数
  "items": "List<#EquipmentVo>",               // 装备列表数据
  "first": true,                               // 是否为第一页
  "last": true,                                // 是否为最后一页
  "hasNext": true,                             // 是否有下一页
  "hasPrevious": true,                         // 是否有上一页
  "totalPages": 0,                             // 总页数
  "prevUrl": "string",                         // 上一页链接
  "nextUrl": "string"                          // 下一页链接
}
```

### Annotations 元数据适配

根据 Halo 的[元数据表单定义文档](https://docs.halo.run/developer-guide/annotations-form/)和[模型元数据文档](https://docs.halo.run/developer-guide/theme/annotations)，Halo 支持为部分模型的表单添加元数据表单，此插件同样适配了此功能，如果你作为主题开发者，需要为链接或者链接分组添加额外的字段，可以参考上述文档并结合下面的 TargetRef 列表进行适配。

| 对应模型   | group            | kind       |
| ---------- | ---------------- | ---------- |
| 我的装备       | core.halo.run | Equipment       |
| 装备分组 | core.halo.run | EquipmentGroup |
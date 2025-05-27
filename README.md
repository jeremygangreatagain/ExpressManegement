### 本项目为《基于Spring Boot+Vue.js+MySQL的快递业务信息管理系统》

使用技术栈：

- Java 17
- Vue 3
- MySQL 8.0.x
- Redis
- MyBatis-Plus
- TailwindCSS
- Element-Plus

本项目设计了一个基于Spring Boot和Vue.js的三角色协同快递业务信息管理系统，采用了前后端分离的架构。后端通过Maven构建RESTful API，前端使用Vue3实现基于组件的开发。MySQL8.0数据库和Redis缓存的组合提高了数据处理效率，MD5动态盐加密、JWT令牌和图形验证码的双重身份验证确保了系统安全

![首页图](C:\Users\gan\Desktop\GraduationProject\图\首页图.png)



本项目为本人的毕业设计项目，请勿商用，开源仅做分享，当前项目还有一些没有完善的地方，各位可以自行改进。

未完善的地方：

- 表格未对齐
- 统计数据貌似有什么地方错了，不会显示总的表单内数据数量，找了很久都没找到
- 敏感信息加密方式为MD5，但可以通过暴力破解/彩虹桥等方式获取敏感信息，故需要进一步升级加密方式。


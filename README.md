# Spring-mini
参考Spring的IOC，MVC模块，实现的一个简易版的Spring框架。
框架可以读取用户自定义的Properties文件配置自动扫描装配等功能。
通过反射实现IOC功能和部分依赖注入功能，实现了Autowired，Service，Controller注解。
实现MVC请求处理，页面渲染功能，实现了RequestMapping，RequestParam注解。

# 快速开始
测试MVC：1.通过maven的Plugins中jetty启动web服务。

![image](https://user-images.githubusercontent.com/52808768/130034957-c654c4f2-bc5d-4a1d-baa1-b966086305f8.png)

2.通过action包下定义的Controller，MyAction。在浏览器下输入url进行测试。

![image](https://user-images.githubusercontent.com/52808768/130035945-fe9ac62e-e2f3-4cf3-a270-3469e4cdd5b7.png)


测试IOC容器：3.进入test包，可以测试容器启动、获取bean、依赖注入、循环依赖。

![image](https://user-images.githubusercontent.com/52808768/130036790-e1eb1632-c85f-4a2b-9082-97773492b8d2.png)

测试结果

![image](https://user-images.githubusercontent.com/52808768/130037951-29441e3a-7334-494a-af89-97b44df06f4d.png)


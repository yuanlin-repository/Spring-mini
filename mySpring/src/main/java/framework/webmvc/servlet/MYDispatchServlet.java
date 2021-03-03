package framework.webmvc.servlet;

import framework.annotation.MYController;
import framework.annotation.MYRequestMapping;
import framework.context.MYApplicationContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MYDispatchServlet extends HttpServlet {

    private final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    // IOC 容器
    private MYApplicationContext context;

    // 保存 HandlerMapping 的容器（用于判断能否处理外部请求）
    private List<MYHandlerMapping> handlerMappings = new ArrayList<MYHandlerMapping>();

    // 保存 <HandlerMapping，HandlerAdpter> 映射关系的容器（用于获取执行方法的请求适配器）
    private Map<MYHandlerMapping, MYHandlerAdapter> handereAdpters = new HashMap<MYHandlerMapping, MYHandlerAdapter>();

    // 保存视图解析器的容器
    private List<MYViewResolver> viewResolvers = new ArrayList<MYViewResolver>();

    @Override
    public void init(ServletConfig config) throws ServletException {

        // 1.初始化ApplicationContext ！！！
        // tomcat会加载web.xml并创建其中配置的servlet（即DispatchServlet），同时会执行init方法
        // 这里的config即web.xml配置信息，其中 contextConfigLocation 参数配置的是 application.properties 路径
        context = new MYApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATION));

        // 2.初始化SpringMVC九大组件
        initStrategies(context);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            // 顶层异常处理，如果处理请求的方法抛出异常，在这里捕获后返回提前写好的500页面
            resp.getWriter().println("500 Exception,Details:\r\n" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "").replaceAll(",\\s", "\r\n"));
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 1.通过从Request中拿到URL，去匹配一个HandlerMapping
        MYHandlerMapping handler = getHandler(req);
        // 如果当前没有处理当前请求的方法，返回404页面
        if (handler == null) {
            processDispatchResult(req, resp, new MYModelAndView("404"));
            return;
        }

        // 2.获取当前handler对应的处理参数的Adpter
        MYHandlerAdapter handlerAdapter = getHandlerAdapter(handler);

        // 3.Adpter负责处理 request 中携带的参数然后执行处理请求的方法
        // 执行的结果可能是null(增加、删除、异常...）也可能是ModelAndView（查询...）
        // Adpter真正调用处理请求的方法,返回ModelAndView（存储了页面上值，和页面模板的名称）
        MYModelAndView mv = handlerAdapter.handle(req, resp, handler);

        // 4.真正输出,将方法执行进行处理然后返回
        // 如果上面返回的是 ModelAndView ，那么还要通过视图解析器和模板引擎进行解析
        processDispatchResult(req, resp, mv);
    }

    private MYHandlerAdapter getHandlerAdapter(MYHandlerMapping handler) {
        if (this.handereAdpters == null) {
            return null;
        }
        MYHandlerAdapter handlerAdapter = this.handereAdpters.get(handler);
        // 判断当前handler能否被当前 adaptor 适配
        if (handlerAdapter.supports(handler)) {
            return handlerAdapter;
        }
        return null;
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, MYModelAndView mv) throws Exception {
        // null 表示方法返回类型是void，或返回值是null。不做额外处理
        if(mv == null) {
            return;
        }

        // 如果没有视图解析器就返回，因为无法处理ModelAndView
        if (this.viewResolvers.isEmpty()) {
            return;
        }
        for (MYViewResolver viewResolver : viewResolvers) {
            MYVIEW myview = viewResolver.resolveViewName(mv.getViewName(), null);
            myview.render(mv.getModel(), req, resp);
            return;
        }
    }

    private MYHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) return null;
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");


        for (MYHandlerMapping handler : this.handlerMappings) {
            Matcher matcher = handler.getPattern().matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return handler;
        }
        return null;
    }

    private void initStrategies(MYApplicationContext context) {
        // 多文件上传的组件
        initMultipartResolver(context);
        // 初始化本地语言环境
        initLocaleResolver(context);
        // 初始化模板处理器
        initThemeResolver(context);

        // handlerMapping，必须实现
        initHandlerMappings(context);
        // 初始化参数适配器，必须实现
        initHandlerAdapters(context);
        // 初始化异常拦截器
        initHandlerExceptionResolvers(context);
        // 初始化视图预处理器
        initRequestToViewNameTranslator(context);

        // 初始化视图转换器，必须实现
        initViewResolvers(context);
        // 参数缓存器
        initFlashMapManager(context);
    }

    private void initHandlerAdapters(MYApplicationContext context) {
        for (MYHandlerMapping handlerMapping : handlerMappings) {
            this.handereAdpters.put(handlerMapping, new MYHandlerAdapter());
        }
    }

    private void initHandlerMappings(MYApplicationContext context) {
        // 通过 ApplicationContext#getBeanDefinitionNames 拿到所有的 beanName
        String[] beanNames = context.getBeanDefinitionNames();

        try {
            for (String beanName : beanNames) {
                // System.out.println(beanName);
                /**
                 * 从容器中获取 bean，判断是不是 MYController 注解修饰
                 */
                // 获取到具体的bean（由于是单例的，在factoryBeanObjectCache容器中就能获取到）
                Object controller = context.getBean(beanName);
                // 获取到bean的Class，然后判断是否有 @MYController 注解
                Class<?> clazz = controller.getClass();
                // 如果不是 Controller 就返回进行下一轮循环
                if (!clazz.isAnnotationPresent(MYController.class)) {
                    continue;
                }

                /**
                 * 获取 MYController 类上的公有 url
                 */
                // 获取当前 Controller 的公有url，即类上 @RequestMapping 的路径
                String baseUrl = "";
                if (clazz.isAnnotationPresent(MYRequestMapping.class)) {
                    MYRequestMapping annotation = clazz.getAnnotation(MYRequestMapping.class);
                    baseUrl = annotation.value();
                }

                /**
                 * 获取所有方法的处理路径，一个方法（一个请求路径）对应一个 HandlerMapping
                 */
                // 获取所有方法的处理路径
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(MYRequestMapping.class)) {
                        continue;
                    }

                    MYRequestMapping annotation = method.getAnnotation(MYRequestMapping.class);
                    String regex = ("/" + baseUrl + "/" + annotation.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    // 构建处理器，并加入handlerMapping
                    this.handlerMappings.add(new MYHandlerMapping(controller, method, pattern));
                    log.info("Mapped " + regex + "," + method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViewResolvers(MYApplicationContext context) {
        // 拿到在配置文件中配置的模板存放路径(layouts)
        String templateRoot = context.getConfig().getProperty("templateRoot");

        // 通过相对路径找到目标后，获取到绝对路径
        // 注：getResourse返回的是URL对象，getFile返回文件的绝对路径
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        // 拿到模板目录下的所有文件名（这里是所有html名）
        File templateRootDir = null;
        try {
            templateRootDir = new File(URLDecoder.decode(templateRootPath, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String[] templates = templateRootDir.list();

        // 视图解析器可以有多种，且不同的模板需要不同的Resolver去解析成不同的View（jsp，html，json。。）
        // 但这里其实就只有一种（解析成html）
        // 为了仿真才写了这个循环，其实只循环一次
        for (int i = 0; i < templates.length; i ++) {
            this.viewResolvers.add(new MYViewResolver(templateRoot));
        }
    }

    private void initFlashMapManager(MYApplicationContext context) {
    }

    private void initRequestToViewNameTranslator(MYApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(MYApplicationContext context) {
    }

    private void initThemeResolver(MYApplicationContext context) {
    }

    private void initLocaleResolver(MYApplicationContext context) {
    }

    private void initMultipartResolver(MYApplicationContext context) {
    }

}

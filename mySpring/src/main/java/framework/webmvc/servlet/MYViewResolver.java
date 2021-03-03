package framework.webmvc.servlet;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

public class MYViewResolver {

    private final String DEFAULT_TEMPALTE_SUFIX = ".html";

    // 视图目录
    private File templateRootDir;

    public MYViewResolver(String templateRoot) {
        try {
            String templateRootPath = URLDecoder.decode(this.getClass().getClassLoader().getResource(templateRoot).getFile(), "utf-8");
            this.templateRootDir = new File(templateRootPath);
        } catch (UnsupportedEncodingException e) {
            System.out.println("【DEBUG】---- 解析视图时发生乱码...");
        }
    }

    public MYVIEW resolveViewName(String viewName, Locale locale) throws Exception {
        if (null == viewName || "".equals(viewName.trim())) {
            return null;
        }
        // 给没有 .html的加上后缀
        viewName = viewName.endsWith(DEFAULT_TEMPALTE_SUFIX) ? viewName : (viewName+DEFAULT_TEMPALTE_SUFIX);
        // 返回相应视图
        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/"));
        return new MYVIEW(templateFile);
    }
}

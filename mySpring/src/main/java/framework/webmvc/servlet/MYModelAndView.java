package framework.webmvc.servlet;

import java.util.Map;

public class MYModelAndView {

    private String ViewName;

    private Map<String, ?> model;

    public MYModelAndView(String viewName) {
        ViewName = viewName;
    }

    public MYModelAndView(String viewName, Map<String, ?> model) {
        ViewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return ViewName;
    }

    public void setViewName(String viewName) {
        ViewName = viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

    public void setModel(Map<String, ?> model) {
        this.model = model;
    }
}

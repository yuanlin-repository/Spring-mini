package com.xupt.yl.demo.action;

import com.xupt.yl.demo.service.IModifyService;
import com.xupt.yl.demo.service.IQueryService;

import framework.annotation.MYAutowired;
import framework.annotation.MYController;
import framework.annotation.MYRequestMapping;
import framework.annotation.MYRequestParam;
import framework.webmvc.servlet.MYModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@MYController
@MYRequestMapping("/web")
public class MyAction {

	@MYAutowired
	IQueryService queryService;
	@MYAutowired
	IModifyService modifyService;

	/**
	 * MYRequestParam测试
	 */
	@MYRequestMapping("/first.html")
    public MYModelAndView query(@MYRequestParam("name") String name) {
        String result = queryService.query(name);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("name", name);
        model.put("data", result);
        model.put("token", 123456);
        return new MYModelAndView("first", model);
    }

	@MYRequestMapping("/edit.json")
	public MYModelAndView edit(HttpServletRequest request, HttpServletResponse response,
							   @MYRequestParam("id") Integer id,
							   @MYRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}

	/**
	 * 返回json数据
	 */
	@MYRequestMapping("/query.json")
	public MYModelAndView query(HttpServletRequest request, HttpServletResponse response,
                                @MYRequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}

	/**
	 * 抛出异常
	 */
	@MYRequestMapping("/add*.json")
	public MYModelAndView add(HttpServletRequest request, HttpServletResponse response,
                              @MYRequestParam("name") String name, @MYRequestParam("addr") String addr){
		String result = null;
		try {
			// 该方法会抛出自定义异常
			result = modifyService.add(name,addr);
			return out(response,result);
		} catch (Exception e) {
			// 将异常信息保存在Map中，然后放入Model
			Map<String,Object> model = new HashMap<String,Object>();
			// 注：这里在单独测 mvc 模块时要去掉getCause
			model.put("detail",e.getMessage());
			model.put("stackTrace", Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));

			return new MYModelAndView("500",model);
		}
	}

	@MYRequestMapping("/remove.json")
	public MYModelAndView remove(HttpServletRequest request, HttpServletResponse response,
                                 @MYRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}

	private MYModelAndView out(HttpServletResponse resp, String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void test(String name) {
        System.out.println(queryService.query(name));
    }

}

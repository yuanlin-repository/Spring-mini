package com.xupt.yl.demo.service.impl;

import com.xupt.yl.demo.service.IModifyService;
import framework.annotation.MYService;

@MYService
public class ModifyService implements IModifyService {

	public String add(String name,String addr) throws Exception {
		throw new Exception("这是故意抛的异常！！");
		//return "modifyService add,name=" + name + ",addr=" + addr;
	}


	public String edit(Integer id,String name) {
		return "modifyService edit,id=" + id + ",name=" + name;
	}

	public String remove(Integer id) {
		return "modifyService id=" + id;
	}
	
}

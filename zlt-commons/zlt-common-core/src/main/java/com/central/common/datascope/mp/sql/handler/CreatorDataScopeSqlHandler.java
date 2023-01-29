package com.central.common.datascope.mp.sql.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.central.common.context.LoginUserContextHolder;
import com.central.common.enums.DataScope;
import com.central.common.feign.UserService;
import com.central.common.model.SysRole;
import com.central.common.model.SysUser;
import com.central.common.properties.DataScopeProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 个人权限的处理器
 *
 * @author jarvis create by 2023/1/10
 */
public class CreatorDataScopeSqlHandler implements SqlHandler{

    @Autowired
    UserService userService;

    @Autowired
    private DataScopeProperties dataScopeProperties;

    /**
     * 返回需要增加的where条件，返回空字符的话则代表不需要权限控制
     *
     * @return where条件
     * 如果角色是全部权限的话则不进行控制，如果是个人权限的话则自动加入create_id = user_id
     */
    @Override
    public String handleScopeSql() {
        SysUser user = LoginUserContextHolder.getUser();
        Assert.notNull(user, "登陆人不能为空");
        List<SysRole> roleList = userService.findRolesByUserId(user.getId());
        return StrUtil.isBlank(dataScopeProperties.getScopeColumn())
                ||(CollUtil.isNotEmpty(roleList)&& roleList.stream().anyMatch(item-> DataScope.ALL.equals(item.getDataScope())))
                ? DO_NOTHING:
                // 这里确保有配置权限范围控制的字段
                // 1. 如果没有配置角色的情况默认采用只读个人创建的记录
                // 2. 如果有配置角色的话判断是否存在有ALL的情况，如果没有ALL的话读取个人创建记录
                String.format("%s.%s = '%s'", ALIAS_SYNBOL, dataScopeProperties.getScopeColumn(), user.getId());
    }
}

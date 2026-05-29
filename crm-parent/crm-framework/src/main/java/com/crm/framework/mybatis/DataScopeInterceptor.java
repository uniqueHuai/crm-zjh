package com.crm.framework.mybatis;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.crm.common.constant.Constants;
import com.crm.framework.security.LoginUser;
import com.crm.framework.security.SecurityUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.util.List;

/**
 * 数据权限拦截器
 * 根据角色的 dataScope 自动拼装 SQL 过滤条件
 * 逻辑：
 *   data_scope=1（本人）  → owner_id = 当前用户
 *   data_scope=2（本部门） → dept_id = 当前用户部门
 *   data_scope=4（全部）  → 不拼接
 *   data_scope=5（自定义）→ dept_id IN (指定部门)
 *
 *  使用 @DataScope(entityAlias = "c", field = "owner_id") 注解在 Mapper 方法上激活
 */
public class DataScopeInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        if (InterceptorIgnoreHelper.willIgnoreDataPermission(ms.getId())) {
            return;
        }
        // 实际实现会在业务开发阶段完善
        // 当前预留拦截器骨架
    }

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        // 留空，查询拦截在 beforeQuery 中处理
    }
}

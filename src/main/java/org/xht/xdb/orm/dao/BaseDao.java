package org.xht.xdb.orm.dao;

import org.xht.xdb.orm.EntityService;

/**
 * <pre>
 * 基础Dao接口，支持命名约定方法
 * 支持命名约定方法的调用
 * 例如: find_by_id_name(String username), find_by_username_and_email(String username, String email)
 * count_by_username(String username), exists_by_username(String username)
 * delete_by_username(String username), read_by_username(String username)
 * </pre>
 */
public interface BaseDao<T> extends EntityService<T> {

}

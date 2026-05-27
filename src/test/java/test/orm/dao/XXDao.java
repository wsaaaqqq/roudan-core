package test.orm.dao;

import org.xht.xdb.orm.dao.BaseDao;
import test.orm.po.PoJPA;

import java.util.List;
import java.util.Optional;

public interface XXDao extends BaseDao<PoJPA> {
    List<PoJPA> find_by_name(String name);

    List<PoJPA> find_by_name_and_type(String name, String type);

    Long count_by_name(String name);

    boolean exists_by_name(String name);

    List<PoJPA> find_by_name_like(String name);

    Optional<PoJPA> find_by_id(String id);

    List<PoJPA> find_by_name_contain(String name);

    List<PoJPA> find_by_name_end_with(String name);

    List<PoJPA> find_by_name_start_with(String name);

    void delete_by_name(String name);

    // 添加其他命名规则方法
    List<PoJPA> find_by_idx_gt(Integer order);

    List<PoJPA> find_by_idx_lt(Integer order);

    List<PoJPA> find_by_idx_gte(Integer order);

    List<PoJPA> find_by_idx_lte(Integer order);

    List<PoJPA> find_by_name_not(String name);

    List<PoJPA> find_by_name_in(List<String> nameList);

    List<PoJPA> find_by_idx_between(List<Integer> orderRange);

    List<PoJPA> find_by_name_or_type(String name, String type);

    List<PoJPA> find_by_idx_and_name_like(Integer order, String name);

    List<PoJPA> find_by_idx_gt_and_name_start_with(Integer order, String name);

    int count_by_name_or_name(String name1, String name2);

    void delete_by_name_or_name(String name1, String name2);

    default void delete_by_name_or_id(String name1, String name2) {
        System.out.println("delete nothing");
    }
}

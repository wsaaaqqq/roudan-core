package test.orm.po;

import lombok.Data;
import org.xht.xdb.orm.anno.Id;
import org.xht.xdb.orm.anno.Table;

import java.io.Serializable;

/**
 * T_TEST
 */
@Data
@Table("T_TEST")
public class PoKeys implements Serializable {

    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    @Id
    private String code;

    /**
     * 编码类型
     */
    @Id
    private String type;

    /**
     * 字典排序
     */
    private Integer idx;

}

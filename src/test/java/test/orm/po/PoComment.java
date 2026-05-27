package test.orm.po;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * T_TEST
 */
@Data
@ToString(callSuper = true)
@Accessors(chain = true)
public class PoComment implements Serializable {

    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 编码类型
     */
    private String type;

    /**
     * 字典排序
     */
    private Integer idx;

}

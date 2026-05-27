package test.orm.po;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * T_TEST
 */
@Data
@ToString
@Accessors(chain = true)
@Table(value = "T_TEST")
public class PoFlex implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column("ID")
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

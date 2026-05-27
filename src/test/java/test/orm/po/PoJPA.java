package test.orm.po;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * T_TEST
 */
@Data
@ToString
@Accessors(chain = true)
@Table(name = "T_TEST")
public class PoJPA implements Serializable {
    @Id
    @Column(name = "ID")
    private String id;
    private String name;
    private String code;
    private String type;
    private Integer idx;
}

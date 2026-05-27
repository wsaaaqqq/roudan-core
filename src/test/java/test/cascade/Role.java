package test.cascade;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Accessors(chain = true)
@com.mybatisflex.annotation.Table(value = "T_ROLE")
@Table(name = "T_ROLE")
public class Role {
    @Id
    @com.mybatisflex.annotation.Id
    @Column(name = "ID")
    private String id;
    private String userId;
    private String name;
}

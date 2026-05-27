package test.cascade;

import lombok.Data;
import lombok.experimental.Accessors;
import org.xht.xdb.orm.anno.Ignore;
import org.xht.xdb.orm.cascade.Cascade;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

@Data
@Accessors(chain = true)
@com.mybatisflex.annotation.Table(value = "T_USER")
@Table(name = "T_USER")
public class User {
    @Id
    @com.mybatisflex.annotation.Id
    @Column(name = "ID")
    private String id;
    private String deptId;
    private String name;

    @Cascade
    @Transient
    @Ignore
    @com.mybatisflex.annotation.Column(ignore = true)
    private List<Role> roles;

    public void setRoles(List<Role> roles) {
        this.roles = roles;
        roles.forEach(role -> role.setUserId(id));
    }
}

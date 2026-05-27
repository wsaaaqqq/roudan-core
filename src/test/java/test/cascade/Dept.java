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
@Table(name = "T_DEPT")
@com.mybatisflex.annotation.Table(value = "T_DEPT")
public class Dept {
    @Id
    @com.mybatisflex.annotation.Id
    @Column(name = "ID")
    private String id;
    private String pid;

    @Cascade
    @Transient
    @Ignore
    @com.mybatisflex.annotation.Column(ignore = true)
    private List<Dept> children;

    @Cascade
    @Transient
    @Ignore
    @com.mybatisflex.annotation.Column(ignore = true)
    private List<User> users;

    private String name;

    public void setChildren(List<Dept> children) {
        this.children = children;
        children.forEach(child -> child.setPid(this.id));
    }

    public void setUsers(List<User> users) {
        this.users = users;
        users.forEach(user -> user.setDeptId(this.id));
    }
}

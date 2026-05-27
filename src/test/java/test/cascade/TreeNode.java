package test.cascade;

import lombok.Data;
import org.xht.xdb.orm.cascade.Cascade;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

@Data
@Table(name = "T_TREE")
public class TreeNode {
    @Id
    private String id;
    private String pid;
    private String text;
    @Cascade
    @Transient
    private List<TreeNode> childNodes;

    public void setChildNodes(List<TreeNode> childNodes) {
        this.childNodes = childNodes;
        childNodes.forEach(c->c.setPid(this.getId()));
    }
}

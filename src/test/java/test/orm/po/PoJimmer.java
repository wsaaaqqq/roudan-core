package test.orm.po;

import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;
import org.babyfish.jimmer.sql.Table;

@Entity
@Table(name = "T_TEST")
public interface PoJimmer {

    @Id
    @Column(name = "ID")
    String id();

    String name();

    String code();

    String type();

    Integer idx();

}

package test.orm.test;


import test.orm.po.PoJPA;

public class EE {

    public static PoJPA cc(int i) {
        return new PoJPA().setId("id_" + i).setName("name_" + i).setCode("code_" + i).setType("type_" + i).setIdx(i);
    }

}

package test.util;

import org.xht.xdb.util.MergeBeanUtil;
import test.orm.po.PoJPA;

import java.util.Arrays;

public class MergeBeanUtilTest {

    public static void main(String[] args) {
        //EXT_HN_T_JY_ZD
        MergeBeanUtil
                .init(PoJPA.class)
                .dataOld(Arrays.asList(new PoJPA().setId("1").setName("1"),
                                       new PoJPA().setId("2").setName("2"),
                                       new PoJPA().setId("3").setName("3")
                ))
                .dataNew(Arrays.asList(
                        new PoJPA().setId("1").setName("1"),
                        new PoJPA().setId("4").setName("4"),
                        new PoJPA().setId("5").setName("5")
                ))
                .keyColumns(PoJPA::getId, PoJPA::getName)
                .debug()
        ;
    }
}

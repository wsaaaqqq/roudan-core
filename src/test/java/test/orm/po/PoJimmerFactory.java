package test.orm.po;

public class PoJimmerFactory {

    public static PoJimmer create() {
        return new PoJimmer() {

            @Override
            public String id() {
                return "";
            }

            @Override
            public String name() {
                return "";
            }

            @Override
            public String code() {
                return "";
            }

            @Override
            public String type() {
                return "";
            }

            @Override
            public Integer idx() {
                return 0;
            }
        };
    }

}

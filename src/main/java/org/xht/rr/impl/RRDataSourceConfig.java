package org.xht.rr.impl;

import org.xht.xdb.Xdb;
import org.xht.xdb.enums.DbType;

import javax.sql.DataSource;

public class RRDataSourceConfig {
    public static final RRDataSourceConfig INSTANCE = new RRDataSourceConfig();
    private final Xdb xdb = Xdb.init();

    public RRDataSourceConfig clearDataSource(String... dataSourceNames) {
        xdb.clearDataSource(dataSourceNames);
        return this;
    }

    public RRDataSourceConfig addDataSource(DataSource dataSource, DbType dbType, String dataSourceName) {
        xdb.addDataSource(dataSource, dbType, dataSourceName);
        return this;
    }

    public RRDataSourceConfig addDataSource(DataSource dataSource, String dataSourceName) {
        xdb.addDataSource(dataSource, dataSourceName);
        return this;
    }

    public RRDataSourceConfig addDataSource(DataSource dataSource) {
        xdb.addDataSource(dataSource);
        return this;
    }

    public RRDataSourceConfig addDataSourceDefault(DataSource dataSource, DbType dbType) {
        xdb.addDataSourceDefault(dataSource, dbType);
        return this;
    }

    public RRDataSourceConfig addDataSourceDefault(DataSource dataSource) {
        xdb.addDataSourceDefault(dataSource);
        return this;
    }

    public RRDataSourceConfig selectDataSourceDefault(String dataSourceName) {
        Xdb.selectDataSourceDefault(dataSourceName);
        return this;
    }

    public RRDataSourceConfig selectDataSource(String dataSourceName) {
        Xdb.selectDataSourceByName(dataSourceName);
        return this;
    }

}

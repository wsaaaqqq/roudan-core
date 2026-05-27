package test.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.xht.xdb.Xdb;
import org.xht.xdb.datasource.LimitedDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LimitedDataSourceTest {

    private static final int MAX_POOL_SIZE = 2;//1个用于监视连接数
    private static final int THREAD_COUNT = 10; // 尝试用 20 个线程获取连接

    public static void main(String[] args) throws InterruptedException {
        HikariConfig config = getConfig();
        DataSource ds = new HikariDataSource(config);
        @Cleanup LimitedDataSource dataSource = new LimitedDataSource(ds, config.getMaximumPoolSize());
        new Thread(() -> watchConnectionCount(config)).start();
        Thread.sleep(1000);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        for (int i = 0; i < THREAD_COUNT; i++) {
            int finalI = i;
            executor.submit(() -> dbOperation(dataSource, startLatch, finalI));
        }
        startLatch.countDown();//倒计时：确保所有线程同时开始
        executor.shutdown();
    }

    public static HikariConfig getConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:dm://192.168.10.221:5236/SECDEV_DB");
        config.setUsername("DAEM_DB");
        config.setPassword("DAEM_DBtest89");
        config.setDriverClassName("dm.jdbc.driver.DmDriver");
        config.setMaximumPoolSize(MAX_POOL_SIZE);
        return config;
    }

    @SneakyThrows
    private static void dbOperation(DataSource dataSource, CountDownLatch startLatch, int finalI) {
        Xdb.init().addDataSourceDefault(dataSource);
        startLatch.await(); // 等待所有线程就绪
        try (Connection connection = Xdb.getConnection()) {
            System.out.println("Thread " + finalI + " get connection: " + connection);
            Thread.sleep(3000); // 模拟持有连接
        }
    }

    @SneakyThrows
    private static void watchConnectionCount(HikariConfig config) {
        @Cleanup Connection connection = DriverManager.getConnection(config.getJdbcUrl(),
                config.getUsername(),
                config.getPassword());
        Xdb.setConnection(connection);
        String sql = "select count(1)  from v$sessions where clnt_ip='::ffff:192.168.9.202'";
        while (true) {
            Integer count = Xdb.sql(sql).executeQuery(false).firstBean(Integer.class);
            System.out.println("================== 当前db活动连接数: " + count);
            //noinspection BusyWait
            Thread.sleep(1000);
        }
    }
}

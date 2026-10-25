package hexlet.code.repository;

import javax.sql.DataSource;

public abstract class BaseRepository {
    protected static DataSource dataSource;

    public static void setDataSource(DataSource source) {
        dataSource = source;
    }
}

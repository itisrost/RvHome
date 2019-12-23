import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.AccountsDao;
import dao.TransactionsDao;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import model.BaseResponce;
import model.ResponceStatusEnum;
import org.apache.commons.lang3.StringUtils;

import static spark.Spark.*;

@Slf4j
public class RvlService {

//    private static final Logger logger = LoggerFactory.getLogger(RvlService.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static AccountsDao accountsDao = new AccountsDao(getConnection());
    private static TransactionsDao transactionsDao = new TransactionsDao(getConnection(), accountsDao);

    public static void main(String[] args) {

        try {
            Connection connection = getConnection();

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new liquibase.Liquibase("db.changelog/changelog-master.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());

            //если вот тут закрыть соединение то потом getAccounts не находит таблицу и падает.
            // База стерлась? Получается соединение закрывать в конце main?
//            connection.close();

            get("/hello", (req, res) -> {
                log.warn("/hello called log");
//                logger.warn("/hello called logger");
                return objectMapper.writeValueAsString("Hello, world!");
            });

            get("/hello/:name", (req, res) -> {
                log.warn("/hello/" + req.params(":name") + "called");
                return "Hello, " + req.params(":name");
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        get("/accounts/:id", (req, res) -> {
            try {
                res.type("application/json");
                return writeJson(new BaseResponce(ResponceStatusEnum.SUCCESS, null, accountsDao.getAccountById(Integer.parseInt(req.params(":id")))));
            } catch (Exception e) {
                e.printStackTrace();
                return writeJson(new BaseResponce(ResponceStatusEnum.ERROR, e.getMessage(), null));
            }
        });

        get("/accounts", (req, res) -> {
            try {
                res.type("application/json");
                log.debug("get accounts called");
                return writeJson(new BaseResponce(ResponceStatusEnum.SUCCESS, null, accountsDao.getAccounts()));
            } catch (SQLException e) {
                e.printStackTrace();
                return writeJson(new BaseResponce(ResponceStatusEnum.ERROR, e.getMessage(), null));
            }
        });

        post("/accounts", (req, res) -> {
            res.type("application/json");
            try {
                List<String> nullParams = req.queryMap().toMap().values().stream()
                        .map(s -> s[0])
                        .filter(StringUtils::isBlank)
                        .collect(Collectors.toList());
                if (req.queryMap().toMap().keySet().size() != 3 || !nullParams.isEmpty()) {
                    return writeJson(new BaseResponce(ResponceStatusEnum.ERROR, "Not enough parameters.", null));
                }

                accountsDao.addAccount(req.queryParams("owner"), new BigDecimal(req.queryParams("balance")), req.queryParams("currency"));

                return writeJson(new BaseResponce(ResponceStatusEnum.SUCCESS, null, accountsDao.getAccounts()));

            } catch (SQLException e) {
                e.printStackTrace();
                return writeJson(new BaseResponce(ResponceStatusEnum.ERROR, "SQL Exception", null));
            }
        });

        post("/transactions", (req, res) -> {
            res.type("application/json");
            try {
                transactionsDao.makeTransaction(Integer.parseInt(req.queryParams("accountFrom")), Integer.parseInt(req.queryParams("accountTo")), new BigDecimal(req.queryParams("amount")));
                return writeJson(new BaseResponce(ResponceStatusEnum.SUCCESS, "Transaction saved", null));
            } catch (SQLException e) {
                e.printStackTrace();
                return writeJson(new BaseResponce(ResponceStatusEnum.ERROR, "SQLException", null));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return writeJson(new BaseResponce(ResponceStatusEnum.ERROR, e.getMessage(), null));
            }
        });
    }

    // UTILS
    private static Connection getConnection() {
        String url = "jdbc:h2:mem:test";
        String user = "sa";
        String passwd = "sa";
        try {
            return DriverManager.getConnection(url, user, passwd);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String writeJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not convert object to JSON", e);
        }
    }
}

package net.heimeng.sdk.btapi.api.database;

import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.database.DatabaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 数据库操作工具类
 * <p>
 * 提供一组便捷的数据库操作辅助方法，包括检查数据库是否存在、获取数据库信息等功能。
 * </p>
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class DatabaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);

    /**
     * 检查指定名称的数据库是否存在
     *
     * @param apiManager API管理器实例
     * @param dbName 数据库名称
     * @return 数据库是否存在
     */
    public static boolean isDatabaseExists(BtApiManager apiManager, String dbName) {
        if (apiManager == null || dbName == null) {
            return false;
        }

        try {
            GetDatabasesApi getApi = new GetDatabasesApi();
            BtResult<List<DatabaseInfo>> result = apiManager.execute(getApi);
            return result.isSuccess() && result.getData() != null &&
                    result.getData().stream().anyMatch(db -> dbName.equals(db.getName()));
        } catch (BtApiException e) {
            logger.error("检查数据库是否存在时发生异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 根据名称获取数据库信息
     *
     * @param apiManager API管理器实例
     * @param dbName 数据库名称
     * @return 数据库信息对象，如果不存在则返回null
     */
    public static DatabaseInfo getDatabaseInfoByName(BtApiManager apiManager, String dbName) {
        if (apiManager == null || dbName == null) {
            return null;
        }

        try {
            GetDatabasesApi getApi = new GetDatabasesApi();
            BtResult<List<DatabaseInfo>> result = apiManager.execute(getApi);
            if (result.isSuccess() && result.getData() != null) {
                return result.getData().stream()
                        .filter(db -> dbName.equals(db.getName()))
                        .findFirst()
                        .orElse(null);
            }
            return null;
        } catch (BtApiException e) {
            logger.error("获取数据库信息时发生异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 根据ID获取数据库信息
     *
     * @param apiManager API管理器实例
     * @param dbId 数据库ID
     * @return 数据库信息对象，如果不存在则返回null
     */
    public static DatabaseInfo getDatabaseInfoById(BtApiManager apiManager, int dbId) {
        if (apiManager == null || dbId < 0) {
            return null;
        }

        try {
            GetDatabasesApi getApi = new GetDatabasesApi();
            BtResult<List<DatabaseInfo>> result = apiManager.execute(getApi);
            if (result.isSuccess() && result.getData() != null) {
                return result.getData().stream()
                        .filter(db -> db.getId() == dbId)
                        .findFirst()
                        .orElse(null);
            }
            return null;
        } catch (BtApiException e) {
            logger.error("根据ID获取数据库信息时发生异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 等待数据库创建完成（适用于异步操作场景）
     *
     * @param apiManager API管理器实例
     * @param dbName 数据库名称
     * @param timeoutMs 超时时间（毫秒）
     * @return 数据库信息对象，如果超时则返回null
     */
    public static DatabaseInfo waitForDatabaseCreation(BtApiManager apiManager, String dbName, long timeoutMs) {
        if (apiManager == null || dbName == null) {
            return null;
        }

        long startTime = System.currentTimeMillis();
        long waitTime = 1000; // 初始等待时间1秒
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                DatabaseInfo dbInfo = getDatabaseInfoByName(apiManager, dbName);
                if (dbInfo != null) {
                    return dbInfo;
                }
                Thread.sleep(waitTime);
                // 指数退避，但不超过5秒
                waitTime = Math.min(waitTime * 2, 5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("等待数据库创建时被中断");
                return null;
            }
        }
        
        logger.warn("等待数据库创建超时: {}", dbName);
        return null;
    }

    /**
     * 等待数据库删除完成（适用于异步操作场景）
     *
     * @param apiManager API管理器实例
     * @param dbName 数据库名称
     * @param timeoutMs 超时时间（毫秒）
     * @return 数据库是否成功删除
     */
    public static boolean waitForDatabaseDeletion(BtApiManager apiManager, String dbName, long timeoutMs) {
        if (apiManager == null || dbName == null) {
            return false;
        }

        long startTime = System.currentTimeMillis();
        long waitTime = 1000; // 初始等待时间1秒
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                if (!isDatabaseExists(apiManager, dbName)) {
                    return true;
                }
                Thread.sleep(waitTime);
                // 指数退避，但不超过5秒
                waitTime = Math.min(waitTime * 2, 5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("等待数据库删除时被中断");
                return false;
            }
        }
        
        logger.warn("等待数据库删除超时: {}", dbName);
        return false;
    }

    /**
     * 安全删除数据库（包含幂等性处理）
     *
     * @param apiManager API管理器实例
     * @param dbName 数据库名称
     * @return 删除操作是否成功
     */
    public static boolean safelyDeleteDatabase(BtApiManager apiManager, String dbName) {
        if (apiManager == null || dbName == null) {
            return false;
        }

        try {
            // 先检查数据库是否存在
            DatabaseInfo dbInfo = getDatabaseInfoByName(apiManager, dbName);
            if (dbInfo == null) {
                // 数据库不存在，视为删除成功（幂等性）
                logger.info("数据库不存在，幂等性处理: {}", dbName);
                return true;
            }

            // 执行删除操作
            DeleteDatabaseApi deleteApi = new DeleteDatabaseApi(dbName, dbInfo.getId());
            BtResult<Boolean> result = apiManager.execute(deleteApi);
            
            // 等待删除完成（异步操作场景）
            waitForDatabaseDeletion(apiManager, dbName, TimeUnit.SECONDS.toMillis(5));
            
            return result.isSuccess();
        } catch (Exception e) {
            logger.error("安全删除数据库时发生异常: {}", e.getMessage());
            return false;
        }
    }
}
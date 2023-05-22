package services;

import javax.persistence.EntityManager;

import utilties.DB_Utility;

/**
 * DB接続に関わる共通処理を行うクラス
 */
public class ServiceBase {

    /**
     * EntityManagerインスタンス
     */
    protected EntityManager entityManager = DB_Utility.createEntityManager();

    /**
     * EntityManagerのクローズ
     */
    public void close() {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }
}
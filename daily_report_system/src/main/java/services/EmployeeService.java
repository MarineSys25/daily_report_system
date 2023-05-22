package services;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.NoResultException;

import actions.views.EmployeeConverter;
import actions.views.EmployeeView;
import constants.JpaConst;
import models.Employee;
import models.validators.EmployeeValidator;
import utilties.EncryptUtil;

/**
 * 従業員テーブルの操作に関わる処理を行うクラス
 */
public class EmployeeService extends ServiceBase {

    /**
     * 指定されたページ数の一覧画面に表示するデータを取得し、EmployeeViewのリストで返却する
     * @param page ページ数
     * @return 表示するデータのリスト
     */
    public List<EmployeeView> getPerPage(int page) {
        List<Employee> employees = entityManager.createNamedQuery(JpaConst.Q_EMP_GET_ALL, Employee.class)
                .setFirstResult(JpaConst.ROW_PER_PAGE * (page - 1))
                .setMaxResults(JpaConst.ROW_PER_PAGE)
                .getResultList();

        return EmployeeConverter.toViewList(employees);
    }

    /**
     * 従業員テーブルのデータの件数を取得し、返却する
     * @return 従業員テーブルのデータの件数
     */
    public long countAll() {
        long empCount = (long) entityManager.createNamedQuery(JpaConst.Q_EMP_COUNT, Long.class)
                .getSingleResult();

        return empCount;
    }

    /**
     * 社員番号、パスワードを条件に取得したデータをEmployeeViewのインスタンスで返却する
     * @param code 社員番号
     * @param plainPass パスワード文字列
     * @param pepper pepper文字列
     * @return 取得データのインスタンス 取得できない場合null
     */
    public EmployeeView findOne(String code, String plainPass, String pepper) {
        Employee employee = null;
        try {
            //パスワードのハッシュ化
            String pass = EncryptUtil.getPasswordEncrypt(plainPass, pepper);

            //社員番号とハッシュ化済パスワードを条件に未削除の従業員を1件取得する
            employee = entityManager.createNamedQuery(JpaConst.Q_EMP_GET_BY_CODE_AND_PASS, Employee.class)
                    .setParameter(JpaConst.JPQL_PARM_CODE, code)
                    .setParameter(JpaConst.JPQL_PARM_PASSWORD, pass)
                    .getSingleResult();

        } catch (NoResultException ex) {
        }

        return EmployeeConverter.toView(employee);

    }

    /**
     * idを条件に取得したデータをEmployeeViewのインスタンスで返却する
     * @param id
     * @return 取得データのインスタンス
     */
    public EmployeeView findOne(int id) {
        Employee employee = findOneInternal(id);
        return EmployeeConverter.toView(employee);
    }

    /**
     * 社員番号を条件に該当するデータの件数を取得し、返却する
     * @param code 社員番号
     * @return 該当するデータの件数
     */
    public long countByCode(String code) {

        //指定した社員番号を保持する従業員の件数を取得する
        long employees_count = (long) entityManager.createNamedQuery(JpaConst.Q_EMP_COUNT_REGISTERED_BY_CODE, Long.class)
                .setParameter(JpaConst.JPQL_PARM_CODE, code)
                .getSingleResult();
        return employees_count;
    }

    /**
     * 画面から入力された従業員の登録内容を元にデータを1件作成し、従業員テーブルに登録する
     * @param employeeView 画面から入力された従業員の登録内容
     * @param pepper pepper文字列
     * @return バリデーションや登録処理中に発生したエラーのリスト
     */
    public List<String> create(EmployeeView employeeView, String pepper) {

        //パスワードをハッシュ化して設定
        String pass = EncryptUtil.getPasswordEncrypt(employeeView.getPassword(), pepper);
        employeeView.setPassword(pass);

        //登録日時、更新日時は現在時刻を設定する
        LocalDateTime now = LocalDateTime.now();
        employeeView.setCreatedAt(now);
        employeeView.setUpdatedAt(now);

        //登録内容のバリデーションを行う
        List<String> errors = EmployeeValidator.validate(this, employeeView, true, true);

        //バリデーションエラーがなければデータを登録する
        if (errors.size() == 0) {
            create(employeeView);
        }

        //エラーを返却（エラーがなければ0件の空リスト）
        return errors;
    }

    /**
     * 画面から入力された従業員の更新内容を元にデータを1件作成し、従業員テーブルを更新する
     * @param employeeView 画面から入力された従業員の登録内容
     * @param pepper pepper文字列
     * @return バリデーションや更新処理中に発生したエラーのリスト
     */
    public List<String> update(EmployeeView employeeView, String pepper) {

        //idを条件に登録済みの従業員情報を取得する
        EmployeeView savedEmp = findOne(employeeView.getId());

        boolean validateCode = false;
        if (!savedEmp.getCode().equals(employeeView.getCode())) {
            //社員番号を更新する場合

            //社員番号についてのバリデーションを行う
            validateCode = true;
            //変更後の社員番号を設定する
            savedEmp.setCode(employeeView.getCode());
        }

        boolean validatePass = false;
        if (employeeView.getPassword() != null && !employeeView.getPassword().equals("")) {
            //パスワードに入力がある場合

            //パスワードについてのバリデーションを行う
            validatePass = true;

            //変更後のパスワードをハッシュ化し設定する
            savedEmp.setPassword(
                    EncryptUtil.getPasswordEncrypt(employeeView.getPassword(), pepper));
        }

        savedEmp.setName(employeeView.getName()); //変更後の氏名を設定する
        savedEmp.setAdminFlag(employeeView.getAdminFlag()); //変更後の管理者フラグを設定する

        //更新日時に現在時刻を設定する
        LocalDateTime today = LocalDateTime.now();
        savedEmp.setUpdatedAt(today);

        //更新内容についてバリデーションを行う
        List<String> errors = EmployeeValidator.validate(this, savedEmp, validateCode, validatePass);

        //バリデーションエラーがなければデータを更新する
        if (errors.size() == 0) {
            update(savedEmp);
        }

        //エラーを返却（エラーがなければ0件の空リスト）
        return errors;
    }

    /**
     * idを条件に従業員データを論理削除する
     * @param id
     */
    public void destroy(Integer id) {

        //idを条件に登録済みの従業員情報を取得する
        EmployeeView savedEmp = findOne(id);

        //更新日時に現在時刻を設定する
        LocalDateTime today = LocalDateTime.now();
        savedEmp.setUpdatedAt(today);

        //論理削除フラグをたてる
        savedEmp.setDeleteFlag(JpaConst.EMP_DEL_TRUE);

        //更新処理を行う
        update(savedEmp);

    }

    /**
     * 社員番号とパスワードを条件に検索し、データが取得できるかどうかで認証結果を返却する
     * @param code 社員番号
     * @param plainPass パスワード
     * @param pepper pepper文字列
     * @return 認証結果を返却す(成功:true 失敗:false)
     */
    public Boolean validateLogin(String code, String plainPass, String pepper) {

        boolean isValidEmployee = false;
        if (code != null && !code.equals("") && plainPass != null && !plainPass.equals("")) {
            EmployeeView employeeView = findOne(code, plainPass, pepper);

            if (employeeView != null && employeeView.getId() != null) {

                //データが取得できた場合、認証成功
                isValidEmployee = true;
            }
        }

        //認証結果を返却する
        return isValidEmployee;
    }

    /**
     * idを条件にデータを1件取得し、Employeeのインスタンスで返却する
     * @param id
     * @return 取得データのインスタンス
     */
    private Employee findOneInternal(int id) {
        Employee e = entityManager.find(Employee.class, id);

        return e;
    }

    /**
     * 従業員データを1件登録する
     * @param employeeView 従業員データ
     * @return 登録結果(成功:true 失敗:false)
     */
    private void create(EmployeeView employeeView) {

        entityManager.getTransaction().begin();
        entityManager.persist(EmployeeConverter.toModel(employeeView));
        entityManager.getTransaction().commit();

    }

    /**
     * 従業員データを更新する
     * @param employeeView 画面から入力された従業員の登録内容
     */
    private void update(EmployeeView employeeView) {

        entityManager.getTransaction().begin();
        Employee e = findOneInternal(employeeView.getId());
        EmployeeConverter.copyViewToModel(e, employeeView);
        entityManager.getTransaction().commit();

    }

}

package actions;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
import constants.PropertyConst;
import services.EmployeeService;

public class EmployeeAction extends ActionBase {

    private EmployeeService employeeService;

    /**
     * メソッドを実行する
     */
    @Override
    public void process() throws ServletException, IOException {

        employeeService = new EmployeeService();

        //メソッドを実行
//        パラメータのcommandの値に該当するメソッドを実行します。
//        commandの値が不正の場合はエラー画面を呼び出します。
        invoke();

        employeeService.close();
    }

    /**
     * ログイン中の従業員が管理者かどうかチェックし、管理者でなければエラー画面を表示
     * true: 管理者 false: 管理者ではない
     * @throws ServletException
     * @throws IOException
     */
    private boolean checkAdmin() throws ServletException, IOException {

        //セッションからログイン中の従業員情報を取得
        EmployeeView employeeView = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //管理者でなければエラー画面を表示
        if (employeeView.getAdminFlag() != AttributeConst.ROLE_ADMIN.getIntegerValue()) {

            forward(ForwardConst.FW_ERR_UNKNOWN);
            return false;

        } else {

            return true;
        }

    }

    /**
     * 一覧画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void index() throws ServletException, IOException {

        if(checkAdmin()) {
            //指定されたページ数の一覧画面に表示するデータを取得

            int page = getPage();
            List<EmployeeView> employees = employeeService.getPerPage(page);

            //全ての従業員データの件数を取得
            long employeeCount = employeeService.countAll();

            putRequestScope(AttributeConst.EMPLOYEES, employees); //取得した従業員データ
            putRequestScope(AttributeConst.EMP_COUNT, employeeCount); //全ての従業員データの件数
            putRequestScope(AttributeConst.PAGE, page); //ページ数
            putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); //1ページに表示するレコードの数

            //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
            String flush = getSessionScope(AttributeConst.FLUSH);
            if (flush != null) {
                putRequestScope(AttributeConst.FLUSH, flush);
                removeSessionScope(AttributeConst.FLUSH);
            }

            //一覧画面を表示
            forward(ForwardConst.FW_EMP_INDEX);

        }


    }

    public void entryNew() throws ServletException, IOException {

        if(checkAdmin()) {
            putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
            putRequestScope(AttributeConst.EMPLOYEE, new EmployeeView()); //空の従業員インスタンス

            //新規登録画面を表示
            forward(ForwardConst.FW_EMP_NEW);
        }


    }

    public void create() throws ServletException, IOException {

        //CSRF対策 tokenのチェック
        if (checkToken() && checkAdmin()) {

            //パラメータの値を元に従業員情報のインスタンスを作成する
            EmployeeView enployeeView = new EmployeeView(
                    null,
                    getRequestParam(AttributeConst.EMP_CODE),
                    getRequestParam(AttributeConst.EMP_NAME),
                    getRequestParam(AttributeConst.EMP_PASS),
                    toNumber(getRequestParam(AttributeConst.EMP_ADMIN_FLG)),
                    null,
                    null,
                    AttributeConst.DEL_FLAG_FALSE.getIntegerValue());

            //アプリケーションスコープからpepper文字列を取得
            String pepper = getContextScope(PropertyConst.PEPPER);

            //従業員情報登録
            List<String> errors = employeeService.create(enployeeView, pepper);

            if (errors.size() > 0) {
                //登録中にエラーがあった場合

                putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                putRequestScope(AttributeConst.EMPLOYEE, enployeeView); //入力された従業員情報
                putRequestScope(AttributeConst.ERR, errors); //エラーのリスト

                //新規登録画面を再表示
                forward(ForwardConst.FW_EMP_NEW);

            } else {
                //登録中にエラーがなかった場合

                //セッションに登録完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_REGISTERED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
            }

        }
    }

    public void show() throws ServletException, IOException {

        if(checkAdmin()) {
            //idを条件に従業員データを取得する

            EmployeeView enployeeView = employeeService.findOne(toNumber(getRequestParam(AttributeConst.EMP_ID)));

            if (enployeeView == null || enployeeView.getDeleteFlag() == AttributeConst.DEL_FLAG_TRUE.getIntegerValue()) {

                //データが取得できなかった、または論理削除されている場合はエラー画面を表示
                forward(ForwardConst.FW_ERR_UNKNOWN);
                return;
            }

            putRequestScope(AttributeConst.EMPLOYEE, enployeeView); //取得した従業員情報

            //詳細画面を表示
            forward(ForwardConst.FW_EMP_SHOW);

        }

    }

    public void edit() throws ServletException, IOException {

        if(checkAdmin()) {

            //idを条件に従業員データを取得する
            EmployeeView employeeView = employeeService.findOne(toNumber(getRequestParam(AttributeConst.EMP_ID)));

            if (employeeView == null || employeeView.getDeleteFlag() == AttributeConst.DEL_FLAG_TRUE.getIntegerValue()) {

                //データが取得できなかった、または論理削除されている場合はエラー画面を表示
                forward(ForwardConst.FW_ERR_UNKNOWN);
                return;
            }

            putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
            putRequestScope(AttributeConst.EMPLOYEE, employeeView); //取得した従業員情報

            //編集画面を表示する
            forward(ForwardConst.FW_EMP_EDIT);
        }
    }

    /**
     * 更新を行う
     * @throws ServletException
     * @throws IOException
     */
    public void update() throws ServletException, IOException {

        //CSRF対策 tokenのチェック
        if (checkToken() && checkAdmin()) {
            //パラメータの値を元に従業員情報のインスタンスを作成する
            EmployeeView employeeView = new EmployeeView(
                    toNumber(getRequestParam(AttributeConst.EMP_ID)),
                    getRequestParam(AttributeConst.EMP_CODE),
                    getRequestParam(AttributeConst.EMP_NAME),
                    getRequestParam(AttributeConst.EMP_PASS),
                    toNumber(getRequestParam(AttributeConst.EMP_ADMIN_FLG)),
                    null,
                    null,
                    AttributeConst.DEL_FLAG_FALSE.getIntegerValue());

            //アプリケーションスコープからpepper文字列を取得
            String pepper = getContextScope(PropertyConst.PEPPER);

            //従業員情報更新
            List<String> errors = employeeService.update(employeeView, pepper);

            if (errors.size() > 0) {
                //更新中にエラーが発生した場合

                putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                putRequestScope(AttributeConst.EMPLOYEE, employeeView); //入力された従業員情報
                putRequestScope(AttributeConst.ERR, errors); //エラーのリスト

                //編集画面を再表示
                forward(ForwardConst.FW_EMP_EDIT);
            } else {
                //更新中にエラーがなかった場合

                //セッションに更新完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_UPDATED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
            }
        }
    }
    /**
     * 論理削除を行う
     * @throws ServletException
     * @throws IOException
     */
    public void destroy() throws ServletException, IOException {

        //CSRF対策 tokenのチェック
        if (checkToken() && checkAdmin()) {

            //idを条件に従業員データを論理削除する
            employeeService.destroy(toNumber(getRequestParam(AttributeConst.EMP_ID)));

            //セッションに削除完了のフラッシュメッセージを設定
            putSessionScope(AttributeConst.FLUSH, MessageConst.I_DELETED.getMessage());

            //一覧画面にリダイレクト
            redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
        }
    }


}

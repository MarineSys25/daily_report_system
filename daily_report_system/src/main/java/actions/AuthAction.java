package actions;

import java.io.IOException;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import constants.AttributeConst;
import constants.ClassConst;
import constants.ForwardConst;
import constants.MessageConst;
import constants.PropertyConst;
import services.EmployeeService;

/**
 * 認証に関する処理を行うActionクラス
 *
 */
public class AuthAction extends ActionBase {

    private EmployeeService employeeService;

    /**
     * メソッドを実行する
     */
    @Override
    public void process() throws ServletException, IOException {

        employeeService = new EmployeeService();

        //メソッドを実行
        invoke();

        employeeService.close();
    }

    /**
     * ログイン画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void showLogin() throws ServletException, IOException {

        //CSRF対策用トークンを設定
        putRequestScope(AttributeConst.TOKEN, getTokenId());

        //セッションにフラッシュメッセージが登録されている場合はリクエストスコープに設定する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        //ログイン画面を表示
        forward(ForwardConst.FW_LOGIN);
    }

    /**
     * ログイン処理を行う
     * @throws ServletException
     * @throws IOException
     */
    public void login() throws ServletException, IOException {

        String code = getRequestParam(AttributeConst.EMP_CODE);
        String plainPass = getRequestParam(AttributeConst.EMP_PASS);
        String pepper = getContextScope(PropertyConst.PEPPER);

        //有効な従業員か認証する
        Boolean isValidEmployee = employeeService.validateLogin(code, plainPass, pepper);

        if (isValidEmployee) {
            //認証成功の場合

            //CSRF対策 tokenのチェック
            if (checkToken()) {

                //ログインした従業員のDBデータを取得
                EmployeeView employeeView = employeeService.findOne(code, plainPass, pepper);
                //セッションにログインした従業員を設定
                putSessionScope(AttributeConst.LOGIN_EMP, employeeView);
                //セッションにログイン完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_LOGINED.getMessage());
                //set Class
                putSessionScope(AttributeConst.FLUSH_CLASS, ClassConst.I_LOGINED_CLASS.getClassName());
                //トップページへリダイレクト
                redirect(ForwardConst.ACT_TOP, ForwardConst.CMD_INDEX);
            }
        } else {
            //認証失敗の場合

            //CSRF対策用トークンを設定
            putRequestScope(AttributeConst.TOKEN, getTokenId());
            //認証失敗エラーメッセージ表示フラグをたてる
            putRequestScope(AttributeConst.LOGIN_ERR, true);
            //入力された従業員コードを設定
            putRequestScope(AttributeConst.EMP_CODE, code);

            //ログイン画面を表示
            forward(ForwardConst.FW_LOGIN);
        }
    }

    /**
     * ログアウト処理を行う
     * @throws ServletException
     * @throws IOException
     */
    public void logout() throws ServletException, IOException {

        //セッションからログイン従業員のパラメータを削除
        removeSessionScope(AttributeConst.LOGIN_EMP);

        //セッションにログアウト時のフラッシュメッセージを追加
        putSessionScope(AttributeConst.FLUSH, MessageConst.I_LOGOUT.getMessage());

        //set Class
        putSessionScope(AttributeConst.FLUSH_CLASS, ClassConst.I_LOGOUT_CLASS.getClassName());
        //ログイン画面にリダイレクト
        redirect(ForwardConst.ACT_AUTH, ForwardConst.CMD_SHOW_LOGIN);

    }

}
package constants;

public enum ClassConst {

    I_LOGINED_CLASS("logined_flush"),
    I_LOGOUT_CLASS("logout_flush"),;


    private String className;
    ClassConst(String className) {
        // TODO 自動生成されたコンストラクター・スタブ
        this.className = className;
    }

    public String getClassName(){
        return className;
    }
}

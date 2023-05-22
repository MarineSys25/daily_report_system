package actions.views;

import java.util.ArrayList;
import java.util.List;

import constants.AttributeConst;
import constants.JpaConst;
import models.Employee;

public class EmployeeConverter {

    /**
     * ViewモデルのインスタンスからDTOモデルのインスタンスを作成する
     * @param ev EmployeeViewのインスタンス
     * @return Employeeのインスタンス
     */
    public static Employee toModel(EmployeeView employeeView) {

        return new Employee(
                employeeView.getId(),
                employeeView.getCode(),
                employeeView.getName(),
                employeeView.getPassword(),
                employeeView.getAdminFlag() == null
                        ? null
                        : employeeView.getAdminFlag() == AttributeConst.ROLE_ADMIN.getIntegerValue()
                                ? JpaConst.ROLE_ADMIN
                                : JpaConst.ROLE_GENERAL,
                employeeView.getCreatedAt(),
                employeeView.getUpdatedAt(),
                employeeView.getDeleteFlag() == null
                        ? null
                        : employeeView.getDeleteFlag() == AttributeConst.DEL_FLAG_TRUE.getIntegerValue()
                                ? JpaConst.EMP_DEL_TRUE
                                : JpaConst.EMP_DEL_FALSE);
    }

    /**
     * DTOモデルのインスタンスからViewモデルのインスタンスを作成する
     * @param e Employeeのインスタンス
     * @return EmployeeViewのインスタンス
     */
    public static EmployeeView toView(Employee employee) {

        if(employee == null) {
            return null;
        }

        return new EmployeeView(
                employee.getId(),
                employee.getCode(),
                employee.getName(),
                employee.getPassword(),
                employee.getAdminFlag() == null
                        ? null
                        : employee.getAdminFlag() == JpaConst.ROLE_ADMIN
                                ? AttributeConst.ROLE_ADMIN.getIntegerValue()
                                : AttributeConst.ROLE_GENERAL.getIntegerValue(),
                employee.getCreatedAt(),
                employee.getUpdatedAt(),
                employee.getDeleteFlag() == null
                        ? null
                        : employee.getDeleteFlag() == JpaConst.EMP_DEL_TRUE
                                ? AttributeConst.DEL_FLAG_TRUE.getIntegerValue()
                                : AttributeConst.DEL_FLAG_FALSE.getIntegerValue());
    }

    /**
     * DTOモデルのリストからViewモデルのリストを作成する
     * @param list DTOモデルのリスト
     * @return Viewモデルのリスト
     */
    public static List<EmployeeView> toViewList(List<Employee> employeeList) {
        List<EmployeeView> employeeViewList = new ArrayList<>();

        for (Employee employee : employeeList) {
            employeeViewList.add(toView(employee));
        }

        return employeeViewList;
    }

    /**
     * Viewモデルの全フィールドの内容をDTOモデルのフィールドにコピーする
     * @param e DTOモデル(コピー先)
     * @param ev Viewモデル(コピー元)
     */
    public static void copyViewToModel(Employee employee, EmployeeView employeeView) {
        employee.setId(employeeView.getId());
        employee.setCode(employeeView.getCode());
        employee.setName(employeeView.getName());
        employee.setPassword(employeeView.getPassword());
        employee.setAdminFlag(employeeView.getAdminFlag());
        employee.setCreatedAt(employeeView.getCreatedAt());
        employee.setUpdatedAt(employeeView.getUpdatedAt());
        employee.setDeleteFlag(employeeView.getDeleteFlag());

    }

}

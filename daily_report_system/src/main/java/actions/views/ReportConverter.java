package actions.views;

import java.util.ArrayList;
import java.util.List;

import models.Report;

/**
 * 日報データのDTOモデル⇔Viewモデルの変換を行うクラス
 *
 */
public class ReportConverter {

    /**
     * ViewモデルのインスタンスからDTOモデルのインスタンスを作成する
     * @param reportView ReportViewのインスタンス
     * @return Reportのインスタンス
     */
    public static Report toModel(ReportView reportView) {
        return new Report(
                reportView.getId(),
                EmployeeConverter.toModel(reportView.getEmployee()),
                reportView.getReportDate(),
                reportView.getTitle(),
                reportView.getContent(),
                reportView.getCreatedAt(),
                reportView.getUpdatedAt());
    }

    /**
     * DTOモデルのインスタンスからViewモデルのインスタンスを作成する
     * @param report Reportのインスタンス
     * @return ReportViewのインスタンス
     */
    public static ReportView toView(Report report) {

        if (report == null) {
            return null;
        }

        return new ReportView(
                report.getId(),
                EmployeeConverter.toView(report.getEmployee()),
                report.getReportDate(),
                report.getTitle(),
                report.getContent(),
                report.getCreatedAt(),
                report.getUpdatedAt());
    }

    /**
     * DTOモデルのリストからViewモデルのリストを作成する
     * @param list DTOモデルのリスト
     * @return Viewモデルのリスト
     */
    public static List<ReportView> toViewList(List<Report> list) {
        List<ReportView> evs = new ArrayList<>();

        for (Report report : list) {
            evs.add(toView(report));
        }

        return evs;
    }

    /**
     * Viewモデルの全フィールドの内容をDTOモデルのフィールドにコピーする
     * @param report DTOモデル(コピー先)
     * @param reportView Viewモデル(コピー元)
     */
    public static void copyViewToModel(Report report, ReportView reportView) {
        report.setId(reportView.getId());
        report.setEmployee(EmployeeConverter.toModel(reportView.getEmployee()));
        report.setReportDate(reportView.getReportDate());
        report.setTitle(reportView.getTitle());
        report.setContent(reportView.getContent());
        report.setCreatedAt(reportView.getCreatedAt());
        report.setUpdatedAt(reportView.getUpdatedAt());

    }

}

package org.openmrs.module.reportingrest.dto;

import org.openmrs.module.reporting.report.ReportRequest;

import java.util.List;

public class ReportRequestDTO {

  private List<ReportRequest> reportRequests;

  private Long reportRequestsCount;

  public ReportRequestDTO(List<ReportRequest> reportRequests, Long reportRequestsCount) {
    this.reportRequests = reportRequests;
    this.reportRequestsCount = reportRequestsCount;
  }

  public List<ReportRequest> getReportRequests() {
    return reportRequests;
  }

  public void setReportRequests(List<ReportRequest> reportRequests) {
    this.reportRequests = reportRequests;
  }

  public Long getReportRequestsCount() {
    return reportRequestsCount;
  }

  public void setReportRequestsCount(Long reportRequestsCount) {
    this.reportRequestsCount = reportRequestsCount;
  }
}

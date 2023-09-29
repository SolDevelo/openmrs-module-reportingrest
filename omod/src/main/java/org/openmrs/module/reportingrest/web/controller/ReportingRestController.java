/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.reportingrest.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for {@link CohortDefinition}s
 */
@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE)
public class ReportingRestController extends MainResourceController {

    public static final String REPORTING_REST_NAMESPACE = "/reportingrest";

    private final Log LOGGER = LogFactory.getLog(ReportingRestController.class);

    /**
     * @see org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController#getNamespace()
     */
    @Override
    public String getNamespace() {
        return RestConstants.VERSION_1 + REPORTING_REST_NAMESPACE;
    }

    @RequestMapping(value = "/runReport", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void runReport(@RequestBody Map<String, Object> body) {
        String reportDefinitionUuid = (String) body.get("reportDefinitionUuid");
        String renderModeUuid = (String) body.get("renderModeUuid");
        Map<String, Object> reportParameters = (Map<String, Object>) body.get("reportParameters");

        ReportDefinition reportDefinition = Context.getService(ReportDefinitionService.class)
            .getDefinitionByUuid(reportDefinitionUuid);

        ReportService reportService = Context.getService(ReportService.class);

        Map<String, Object> parameterValues = new HashMap<String, Object>();
        for (Parameter parameter : reportDefinition.getParameters()) {
            Object convertedObj =
                convertParamValueToObject(reportParameters.get(parameter.getName()), parameter.getType());
            parameterValues.put(parameter.getName(), convertedObj);
        }

        List<RenderingMode> renderingModes = reportService.getRenderingModes(reportDefinition);
        RenderingMode renderingMode = null;
        for (RenderingMode mode : renderingModes) {
            if (StringUtils.equals(mode.getArgument(), renderModeUuid)) {
                renderingMode = mode;
                break;
            }
        }

        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReportDefinition(new Mapped<ReportDefinition>(reportDefinition, parameterValues));
        reportRequest.setRenderingMode(renderingMode);

        reportService.queueReport(reportRequest);
        reportService.processNextQueuedReports();
    }

    @RequestMapping(value = "/cancelReport", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void cancelReport(@RequestParam String reportRequestUuid) {
        ReportService reportService = Context.getService(ReportService.class);
        ReportRequest reportRequest = reportService.getReportRequestByUuid(reportRequestUuid);
        if (reportRequest != null) {
            reportService.purgeReportRequest(reportRequest);
        }
    }

    private Object convertParamValueToObject(Object value, Class<?> type) {
        Object convertedObject = value;

        if (type.equals(Date.class)) {
            try {
                convertedObject = DateUtils.parseDate((String) value, "MM/dd/yyyy");
            } catch (ParseException e) {
                LOGGER.error("Error while parsing date");
            }
        }

        if (type.equals(Integer.class)) {
            convertedObject = Integer.valueOf((String) value);
        }

        if (type.equals(Location.class)) {
            convertedObject = Context.getLocationService().getLocationByUuid((String) value);
        }

        return convertedObject;
    }
}

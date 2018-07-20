<#ftl encoding="UTF-8">
${request.setHeader("icon", "fa fa-bell-o")}
${request.setHeader("severity", "info")}
${request.setHeader("link", "/bpm/processConsole.jsf?tid=" + headers.ProcessInstanceId?string["#########"])}

${headers.description}


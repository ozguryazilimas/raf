<#ftl encoding="UTF-8">
${request.setHeader("icon", "fa fa-bell-o")}
${request.setHeader("severity", "info")}
${request.setHeader("link", "/bpm/taskConsole.jsf?tid=" + headers.TaskId?string["#########"])}

${headers.TaskSubject}
${headers.TaskDescription} 

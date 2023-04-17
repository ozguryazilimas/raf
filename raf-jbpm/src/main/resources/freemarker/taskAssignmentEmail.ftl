<#ftl encoding="UTF-8">
${body} 

Görev       : ${headers.TaskName}
Konu        : ${headers.TaskSubject}
Açıklama    : ${headers.TaskDescription}
Görev Linki : ${headers.linkDomain}bpm/taskConsole.jsf?tid=${headers.TaskId?string['#########']}
<#ftl encoding="UTF-8">
${body} 

Görev       : ${headers.TaskName}
Konu        : ${headers.TaskSubject}
Açıklama    : ${headers.TaskDescription}
Görev Linki : http://raf.tspb.org.tr/dolap/bpm/taskConsole.jsf?tid=${headers.TaskId?string['#########']}
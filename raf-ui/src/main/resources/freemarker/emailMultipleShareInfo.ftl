<#ftl encoding="UTF-8">
Merhaba,

Aşağıdaki belgeler ${headers.sharedBy} tarafından sizinle paylaşıldı.
Parola: ${headers.password}

<#list headers.shareList as fileInfo>
    • ${fileInfo}
</#list>

--
${headers.footerAppName} tarafından gönderilmiştir. ${headers.footerAppLink}
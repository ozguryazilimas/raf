<#ftl encoding="UTF-8">

${headers.date} tarihli dosya içerik sorgulama işi sonucu içeriği bulunamayan dosyaların listesi:
<#list headers.nodes as node>
* ${node}
</#list>
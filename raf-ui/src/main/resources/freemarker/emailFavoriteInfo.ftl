<#ftl encoding="UTF-8">
${headers.messages["email.entity.Title"]} ${headers.consumer.firstName} ${headers.consumer.lastName}

${headers.name} ${headers.surname} tarafından "${headers.objectName}" isimli ${headers.messages["email.body.favorites.type." + headers.objectType]} ${headers.messages["email.body.favorites.action." + headers.actionType]}

Link: ${headers.link}

Bu bildirimleri almak istemiyorsanız ${headers.notificationSettingsLink} linkine tıklayarak e-posta bildirimlerini kapatabilirsiniz.

--
provided by telve email channel
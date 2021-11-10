mvn clean compile war:exploded -PDevelopment -DskipTests=true

cd target/deployments/raf.war/

rm -rf WEB-INF/lib/raf-core-1.1.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-ui-1.1.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-service-1.1.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-pdf-1.1.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-bpmn-1.1.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-invoice-1.1.0-SNAPSHOT.jar
#rm -rf WEB-INF/lib/raf-jbpm-1.1.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-forms-1.1.0-SNAPSHOT.jar
#rm -rf WEB-INF/lib/raf-record-1.1.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-converter-1.1.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-webdav-1.1.0-SNAPSHOT.jar
# rm -rf WEB-INF/lib/raf-cmis-1.1.0-SNAPSHOT.jar


ln -s ~/git/raf/raf-core/target/classes/ WEB-INF/lib/raf-core-1.1.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-ui/target/classes/ WEB-INF/lib/raf-ui-1.1.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-service/target/classes/ WEB-INF/lib/raf-service-1.1.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-pdf/target/classes/ WEB-INF/lib/raf-pdf-1.1.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-bpmn/target/classes/ WEB-INF/lib/raf-bpmn-1.1.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-invoice/target/classes/ WEB-INF/lib/raf-invoice-1.1.0-SNAPSHOT.jar
#ln -s ~/gi/raf/raf-jbpm/target/classes/ WEB-INF/lib/raf-jbpm-1.1.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-forms/target/classes/ WEB-INF/lib/raf-forms-1.1.0-SNAPSHOT.jar
#ln -s ~/gi/raf/raf-record/target/classes/ WEB-INF/lib/raf-record-1.1.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-converter/target/classes/ WEB-INF/lib/raf-converter-1.1.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-webdav/target/classes/ WEB-INF/lib/raf-webdav-1.1.0-SNAPSHOT.jar
# ln -s ~/git/raf/raf-cmis/target/classes/ WEB-INF/lib/raf-cmis-1.1.0-SNAPSHOT.jar

cd ..
touch raf.war.dodeploy
cd ..
cd ..

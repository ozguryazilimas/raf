mvn clean compile war:exploded -PDevelopment -DskipTests=true
version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

cd target/deployments/raf.war/

rm -rf WEB-INF/lib/raf-core-$version.jar
rm -rf WEB-INF/lib/raf-ui-$version.jar
rm -rf WEB-INF/lib/raf-service-$version.jar
rm -rf WEB-INF/lib/raf-pdf-$version.jar
rm -rf WEB-INF/lib/raf-bpmn-$version.jar
rm -rf WEB-INF/lib/raf-invoice-$version.jar
#rm -rf WEB-INF/lib/raf-jbpm-$version.jar
rm -rf WEB-INF/lib/raf-forms-$version.jar
#rm -rf WEB-INF/lib/raf-record-$version.jar
rm -rf WEB-INF/lib/raf-converter-$version.jar
rm -rf WEB-INF/lib/raf-webdav-$version.jar
# rm -rf WEB-INF/lib/raf-cmis-$version.jar


ln -s ~/git/raf/raf-core/target/classes/ WEB-INF/lib/raf-core-$version.jar
ln -s ~/git/raf/raf-ui/target/classes/ WEB-INF/lib/raf-ui-$version.jar
ln -s ~/git/raf/raf-service/target/classes/ WEB-INF/lib/raf-service-$version.jar
ln -s ~/git/raf/raf-pdf/target/classes/ WEB-INF/lib/raf-pdf-$version.jar
ln -s ~/git/raf/raf-bpmn/target/classes/ WEB-INF/lib/raf-bpmn-$version.jar
ln -s ~/git/raf/raf-invoice/target/classes/ WEB-INF/lib/raf-invoice-$version.jar
#ln -s ~/gi/raf/raf-jbpm/target/classes/ WEB-INF/lib/raf-jbpm-$version.jar
ln -s ~/git/raf/raf-forms/target/classes/ WEB-INF/lib/raf-forms-$version.jar
#ln -s ~/gi/raf/raf-record/target/classes/ WEB-INF/lib/raf-record-$version.jar
ln -s ~/git/raf/raf-converter/target/classes/ WEB-INF/lib/raf-converter-$version.jar
ln -s ~/git/raf/raf-webdav/target/classes/ WEB-INF/lib/raf-webdav-$version.jar
# ln -s ~/git/raf/raf-cmis/target/classes/ WEB-INF/lib/raf-cmis-$version.jar

cd ..
touch raf.war.dodeploy
cd ..
cd ..

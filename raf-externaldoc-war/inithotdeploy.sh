mvn clean compile war:exploded -PDevelopment -DskipTests=true

cd target/deployments/raf.war/

#rm -rf WEB-INF/lib/raf-bpmn-1.0.0-SNAPSHOT.jar
#rm -rf WEB-INF/lib/raf-cmis-1.0.0-SNAPSHOT.jar
#rm -rf WEB-INF/lib/raf-converter-1.0.0-SNAPSHOT.jar
#rm -rf WEB-INF/lib/raf-core-1.0.0-SNAPSHOT.jar
#rm -rf WEB-INF/lib/raf-emaildoc-1.0.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-externaldoc-1.0.0-SNAPSHOT.jar
#rm -rf WEB-INF/lib/raf-forms-1.0.0-SNAPSHOT.jar
#rm -rf WEB-INF/lib/raf-invoice-1.0.0-SNAPSHOT.jar
#rm -rf WEB-INF/lib/raf-jbpm-1.0.0-SNAPSHOT.jar
#rm -rf WEB-INF/lib/raf-model-1.0.0-SNAPSHOT.jar
#rm -rf WEB-INF/lib/raf-pdf-1.0.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-record-1.0.0-SNAPSHOT.jar
#rm -rf WEB-INF/lib/raf-service-1.0.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-ui-1.0.0-SNAPSHOT.jar
#rm -rf WEB-INF/lib/raf-webdav-1.0.0-SNAPSHOT.jar




#ln -s ~/git_ozgur_yazilim/raf/raf-bpmn/target/classes/ WEB-INF/lib/raf-bpmn-1.0.0-SNAPSHOT.jar
#ln -s ~/git_ozgur_yazilim/raf/raf-cmis/target/classes/ WEB-INF/lib/raf-cmis-1.0.0-SNAPSHOT.jar
#ln -s ~/git_ozgur_yazilim/raf/raf-converter/target/classes/ WEB-INF/lib/raf-converter-1.0.0-SNAPSHOT.jar
#ln -s ~/git_ozgur_yazilim/raf/raf-core/target/classes/ WEB-INF/lib/raf-core-1.0.0-SNAPSHOT.jar
#ln -s ~/git_ozgur_yazilim/raf/raf-emaildoc/target/classes/ WEB-INF/lib/raf-emaildoc-1.0.0-SNAPSHOT.jar
ln -s ~/git_ozgur_yazilim/raf/raf-externaldoc/target/classes/ WEB-INF/lib/raf-externaldoc-1.0.0-SNAPSHOT.jar
#ln -s ~/git_ozgur_yazilim/raf/raf-forms/target/classes/ WEB-INF/lib/raf-forms-1.0.0-SNAPSHOT.jar
#ln -s ~/git_ozgur_yazilim/raf/raf-invoice/target/classes/ WEB-INF/lib/raf-invoice-1.0.0-SNAPSHOT.jar
#ln -s ~/git_ozgur_yazilim/raf/raf-jbpm/target/classes/ WEB-INF/lib/raf-jbpm-1.0.0-SNAPSHOT.jar
#ln -s ~/git_ozgur_yazilim/raf/raf-model/target/classes/ WEB-INF/lib/raf-model-1.0.0-SNAPSHOT.jar
#ln -s ~/git_ozgur_yazilim/raf/raf-pdf/target/classes/ WEB-INF/lib/raf-pdf-1.0.0-SNAPSHOT.jar
ln -s ~/git_ozgur_yazilim/raf/raf-record/target/classes/ WEB-INF/lib/raf-record-1.0.0-SNAPSHOT.jar
#ln -s ~/git_ozgur_yazilim/raf/raf-service/target/classes/ WEB-INF/lib/raf-service-1.0.0-SNAPSHOT.jar
ln -s ~/git_ozgur_yazilim/raf/raf-ui/target/classes/ WEB-INF/lib/raf-ui-1.0.0-SNAPSHOT.jar
#ln -s ~/git_ozgur_yazilim/raf/raf-webdav/target/classes/ WEB-INF/lib/raf-webdav-1.0.0-SNAPSHOT.jar



cd ..
touch raf.war.dodeploy
cd ..
cd ..

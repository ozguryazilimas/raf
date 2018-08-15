mvn clean compile war:exploded -PDevelopment -DskipTests=true

cd target/deployments/raf.war/

rm -rf WEB-INF/lib/telve-core-4.0.0.Beta6-SNAPSHOT.jar
#rm -rf WEB-INF/lib/telve-core-model-4.0.0.Beta6-SNAPSHOT.jar
#rm -rf WEB-INF/lib/telve-idm-model-4.0.0.Beta6-SNAPSHOT.jar
rm -rf WEB-INF/lib/telve-idm-4.0.0.Beta6-SNAPSHOT.jar
rm -rf WEB-INF/lib/telve-layout-4.0.0.Beta6-SNAPSHOT.jar
#rm -rf WEB-INF/lib/telve-keycloak-4.0.0.Beta6-SNAPSHOT.jar
#rm -rf WEB-INF/lib/telve-bpm-4.0.0.Beta6-SNAPSHOT.jar
#rm -rf WEB-INF/lib/telve-jcr-4.0.0.Beta6-SNAPSHOT.jar
#rm -rf WEB-INF/lib/telve-dynaform-4.0.0.Beta6-SNAPSHOT.jar
#rm -rf WEB-INF/lib/telve-gallery-4.0.0.Beta6-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-core-1.0.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-ui-1.0.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-service-1.0.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-pdf-1.0.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-bpmn-1.0.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-invoice-1.0.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-jbpm-1.0.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-forms-1.0.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-record-1.0.0-SNAPSHOT.jar
rm -rf WEB-INF/lib/raf-converter-1.0.0-SNAPSHOT.jar


ln -s ~/git/telve4/telve-core/target/classes/ WEB-INF/lib/telve-core-4.0.0.Beta6-SNAPSHOT.jar
#ln -s ~/git/telve4/telve-core-model/target/classes/ WEB-INF/lib/telve-core-model-4.0.0.Beta6-SNAPSHOT.jar
#ln -s ~/git/telve4/telve-idm-model/target/classes/ WEB-INF/lib/telve-idm-model-4.0.0.Beta6-SNAPSHOT.jar
ln -s ~/git/telve4/telve-idm/target/classes/ WEB-INF/lib/telve-idm-4.0.0.Beta6-SNAPSHOT.jar
ln -s ~/git/telve4/telve-layout/target/classes/ WEB-INF/lib/telve-layout-4.0.0.Beta6-SNAPSHOT.jar
#ln -s ~/git/telve4/telve-keycloak/target/classes/ WEB-INF/lib/telve-keycloak-4.0.0.Beta6-SNAPSHOT.jar
#ln -s ~/git/telve4/telve-bpm/target/classes/ WEB-INF/lib/telve-bpm-4.0.0.Beta6-SNAPSHOT.jar
#ln -s ~/git/telve4/telve-jcr/target/classes/ WEB-INF/lib/telve-jcr-4.0.0.Beta6-SNAPSHOT.jar
#ln -s ~/git/telve4/telve-dynaform/target/classes/ WEB-INF/lib/telve-dynaform-4.0.0.Beta6-SNAPSHOT.jar
#ln -s ~/git/telve4/telve-gallery/target/classes/ WEB-INF/lib/telve-gallery-4.0.0.Beta6-SNAPSHOT.jar
ln -s ~/git/raf/raf-core/target/classes/ WEB-INF/lib/raf-core-1.0.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-ui/target/classes/ WEB-INF/lib/raf-ui-1.0.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-service/target/classes/ WEB-INF/lib/raf-service-1.0.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-pdf/target/classes/ WEB-INF/lib/raf-pdf-1.0.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-bpmn/target/classes/ WEB-INF/lib/raf-bpmn-1.0.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-invoice/target/classes/ WEB-INF/lib/raf-invoice-1.0.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-jbpm/target/classes/ WEB-INF/lib/raf-jbpm-1.0.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-forms/target/classes/ WEB-INF/lib/raf-forms-1.0.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-record/target/classes/ WEB-INF/lib/raf-record-1.0.0-SNAPSHOT.jar
ln -s ~/git/raf/raf-converter/target/classes/ WEB-INF/lib/raf-converter-1.0.0-SNAPSHOT.jar



cd ..
touch raf.war.dodeploy
cd ..
cd ..

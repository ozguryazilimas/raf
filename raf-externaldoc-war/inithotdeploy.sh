mvn clean compile war:exploded -PDevelopment -DskipTests=true

cd target/deployments/raf.war/
cd ..
touch raf.war.dodeploy
cd ..
cd ..

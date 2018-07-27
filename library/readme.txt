mvn install:install-file -Dfile=C:\RSAutotest\library\javax.resource.jar -DgroupId=javax.resource -DartifactId=javax.resource -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=C:\RSAutotest\library\javax.transaction-api.jar -DgroupId=javax.transaction-api -DartifactId=javax.transaction-api -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=C:\RSAutotest\library\jms.jar -DgroupId=javax.jms -DartifactId=jms -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=db2jcc4-4.23.42.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc4 -Dversion=4.23.42 -Dpackaging=jar

7.5.0.2
mvn install:install-file -Dfile=dhbcore-7.5.0.2.jar -DgroupId=com.ibm.mq -DartifactId=dhbcore -Dversion=7.5.0.2 -Dpackaging=jar
mvn install:install-file -Dfile=mq-7.5.0.2.jar -DgroupId=com.ibm.mq -DartifactId=mq -Dversion=7.5.0.2 -Dpackaging=jar
mvn install:install-file -Dfile=mqjms-7.5.0.2.jar -DgroupId=com.ibm.mq -DartifactId=mqjms -Dversion=7.5.0.2 -Dpackaging=jar

0.1
mvn install:install-file -Dfile=C:\RSAutotest\library\ibm\com.ibm.dhbcore.jar -DgroupId=com.ibm.disthub2 -DartifactId=com.ibm.dhbcore -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=C:\RSAutotest\library\ibm\com.ibm.mqjms.jar -DgroupId=com.ibm.mq -DartifactId=com.ibm.mqjms -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=C:\RSAutotest\library\ibm\com.ibm.mq.jar -DgroupId=com.ibm.mq -DartifactId=com.ibm.mq -Dversion=1.0 -Dpackaging=jar
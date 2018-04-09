/////////////////////////////////////////////////////////////////////////////////////////
// Instructions
/////////////////////////////////////////////////////////////////////////////////////////
// Step 1. download jars into /home/datastax/app:
// 		db2jcc.jar
//		spark-cassandra-connector_2.11-2.0.7.jar
//		jsr166e-1.1.0.jar
//		joda-convert-2.0.1.jar
// Step 2. launch spark-shell with the command below:
//      spark-shell --driver-class-path /home/datastax/app/db2jcc.jar:/home/datastax/app/spark-cassandra-connector_2.11-2.0.7.jar:/home/datastax/app/jsr166e-1.1.0.jar:/home/datastax/app/joda-convert-2.0.1.jar
// Step 3. load the scala file to run in the spark-shell:
//      :load db2cassandra.scala
/////////////////////////////////////////////////////////////////////////////////////////
sc.stop

import java.sql.{DriverManager, ResultSet}
import java.util.UUID

import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark._
import org.apache.spark.rdd.JdbcRDD

val cassandraHost: String = s"192.168.3.120"
val cassandraKeysapce: String = s"testks"
val cassandraTable: String = s"employees"

val dbUrl: String = s"jdbc:db2://192.168.3.120:50000/sample;user=db2user;password=db2user"
val dbDriver: String = s"com.ibm.db2.jcc.DB2Driver"
  
val conf = new SparkConf().setAppName("db2ToCassandra").setMaster("local[*]").set("spark.cassandra.connection.host", cassandraHost)
	
// add @transient so SparkContext will not be serialized
@transient val sc = new SparkContext(conf)

Class.forName(dbDriver).newInstance
	
CassandraConnector(conf).withSessionDo { session =>
		session.execute("CREATE KEYSPACE IF NOT EXISTS testks WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
		session.execute("CREATE TABLE IF NOT EXISTS testks.employees (work_deparment text,employee_number text,firstname text,middlename text,lastname text,phone_number text,hiredate text,birthdate text,edlevel int, sex text, job text,salary float, bonus float, commission float, PRIMARY KEY (work_deparment, employee_number, lastname)) WITH CLUSTERING ORDER BY (employee_number DESC)")
}	
	
val highestSalary: Long = 100000
val startingSalary: Long = 100
val numberOfPartitions = 2;
val employees = new JdbcRDD(sc, () => { DriverManager.getConnection(dbUrl)},
		"select WORKDEPT,EMPNO,FIRSTNME,MIDINIT, LASTNAME, PHONENO,HIREDATE,BIRTHDATE,EDLEVEL,SEX,JOB,SALARY,BONUS,COMM from db2inst1.emp where SALARY >= ? and SALARY <= ?", startingSalary, highestSalary, numberOfPartitions,
		(r: ResultSet) => {
		  (r.getString("WORKDEPT"),
			r.getString("EMPNO"),
			r.getString("FIRSTNME"),
			r.getString("MIDINIT"),
			r.getString("LASTNAME"),
			r.getString("PHONENO"),
			r.getString("HIREDATE"),
			r.getString("BIRTHDATE"),
			r.getInt("EDLEVEL"),
			r.getString("SEX"),
			r.getString("JOB"),
			r.getFloat("SALARY"),
			r.getFloat("BONUS"),
			r.getFloat("COMM")
			)
		})

employees.saveToCassandra(cassandraKeysapce, cassandraTable, SomeColumns("work_deparment", "employee_number", "firstname", "middlename", "lastname", "phone_number","hiredate","birthdate","edlevel","sex","job","salary","bonus","commission"))
	
sc.cassandraTable(cassandraKeysapce,cassandraTable).collect.foreach(println)

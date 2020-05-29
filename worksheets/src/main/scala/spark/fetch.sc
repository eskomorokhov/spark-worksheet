import org.apache.spark._
import org.apache.spark.sql.{DataFrame, Row}
import scalaj.http.Http
import org.apache.spark.sql.types._
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("SimpleOut").error("test logger")

// switch off security manager to be able to write
System.setSecurityManager(null)
def getCurrentDir = {
  scala.reflect.io.Directory(".")
}
val curDir =
  getCurrentDir.toCanonical.path

System.setProperty("user.dir", curDir)
System.setProperty("HADOOP_USER_NAME","cupid")

val warehouseLocation = s"file:${curDir}/spark-warehouse"

val spark = org.apache.spark.sql.SparkSession
  .builder()
  .config(new SparkConf(true).setAll(
    Seq(
      ("submit_user","jeka"),
      ("hadoop_user", "jeka"),
      ("spark.sql.warehouse.dir", warehouseLocation)
    )
  ))
  .master("local[3]")
  //.enableHiveSupport()
  .getOrCreate()

import spark.sqlContext.implicits._

//set new runtime options
spark.conf.set("spark.sql.shuffle.partitions", 6)
spark.conf.set("spark.executor.memory", "2g")
spark.conf.set("spark.sql.sources.partitionOverwriteMode", "DYNAMIC")

//get all settings
val configMap:Map[String, String] = spark.conf.getAll

val jsDf = spark.read.json(s"$curDir/res.json")
jsDf.toDF().persist()
jsDf.count()
jsDf.select($"result").show(truncate = false)
jsDf.mapPartitions[(Int)]( (it: Iterator[Row]) => {
  println(s"Process in ${Thread.currentThread().getId}")
  Thread.sleep(3000)
  Iterator(1)
})

jsDf.write.mode("Overwrite").json(s"$curDir/js_enriched1")



def wGet(param1: String, param2: String) = {
  val url =s"https://jsonplaceholder.typicode.com/todos/1"
  Http(url).params("language"->"en", "text"-> s"$param1 $param2")
    //.proxy("localhost",3128)
    .header("content-type","application/json")
    .header("authorization", "Basic")
    .timeout(5000, 5000)
    .asString
}

wGet("test1", "test2")


spark.stop()
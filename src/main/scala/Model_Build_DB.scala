
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import java.sql.{ResultSet, DriverManager, Connection}
import org.apache.spark.rdd.JdbcRDD

import scala.collection.mutable.ArrayBuffer


object  Model_Build_DB {
  def myfun(a: Array[Double]): Unit ={
    val incoming=a
//    val incoming_array=incoming.split(",")
    val digitizing=incoming.map(line=>line.toDouble)
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://ec2-52-37-241-124.us-west-2.compute.amazonaws.com/credit_data"
    val username = "root"
    val password = "password"
    System.setProperty("hadoop.home.dir", "C:\\Users\\Sharath\\Desktop\\hadoop-common-2.2.0-bin-master\\")
    val conf = new SparkConf().setAppName("Bayes Model").setMaster("local")
    val sc = new SparkContext(conf)
    var connection: Connection = null;
    connection = DriverManager.getConnection(url, username, password)
    val statement = connection.createStatement()
    val resultset = statement.executeQuery("select count(*) from credit_data")

    try{
      Class.forName(driver)
      val data = new JdbcRDD( sc, () =>
        DriverManager.getConnection(url,username,password) ,
        "SELECT * FROM credit_data limit ?,?",
        1, 120, 4, resultset => resultset.getInt( 1 )+ "," +resultset.getInt( 2 )+ "," +resultset.getInt( 3 )+ "," +resultset.getInt( 4 )+ "," +resultset.getInt( 5 )+
          "," +resultset.getInt( 6 )+ "," +resultset.getInt( 7 )+ "," +resultset.getInt( 8 )+ "," +resultset.getInt( 9 )+ "," +resultset.getInt( 10 )+ "," +
          resultset.getInt( 11 )+ "," +resultset.getInt( 12 )+ "," +resultset.getInt( 13 )+ "," +resultset.getInt( 14 )+ "," +resultset.getInt( 15 ) )
      println("Size of the RDD is :" +data.count()+"\n****************")

      val parsedData=data.map{line =>
        val parts=line.split(',')
        LabeledPoint(parts(14).toDouble, Vectors.dense(parts(1).toDouble,
          parts(2).toDouble,
          parts(3).toDouble,
          parts(4).toDouble,
          parts(5).toDouble,
          parts(6).toDouble,
          parts(7).toDouble,
          parts(8).toDouble,
          parts(9).toDouble,
          parts(10).toDouble,
          parts(11).toDouble,
          parts(12).toDouble,
          parts(13).toDouble))
      }
      val training=parsedData
      val testval=a
      println("This is the test val: " + testval)
      val test = Vectors.dense(testval)
      val model=NaiveBayes.train(training, lambda = 1.0, modelType = "multinomial")
//      model.save(sc,"/NaiveBayesModel")
      val predictionlabel= model.predict(test)
      println("The prediction for the input is: "+ predictionlabel.toString)

    }

    catch{
      case e: Exception => {
        print(e)
      }
    }


  }
  def main(args: Array[String]) = {
    val incoming=Array(41,0.04,2,10,4,0.04,0,1,1,0,1,560,1)
        myfun(incoming)
  }


}

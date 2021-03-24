package app

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import util.{MyKafkaSink, MyKafkaUtil}

object SparkStreamingTest {
  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setMaster("local[4]").setAppName("SparkStreamingTest")
    val ssc = new StreamingContext(sparkConf, Seconds(3))

    val topic = "test"
    val groupId = "group1"
    var orderInfoRecordDStream = MyKafkaUtil.getKafkaStream(topic, ssc, groupId)

    orderInfoRecordDStream.map(_.value()).print()
    orderInfoRecordDStream.foreachRDD(rdd => {
      rdd.foreach({
        MyKafkaSink.send("test_produce", "123")
        println(_)
      })
    })

    ssc.start()
    ssc.awaitTermination()

  }

}

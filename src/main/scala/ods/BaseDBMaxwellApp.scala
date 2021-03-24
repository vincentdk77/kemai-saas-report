package ods

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import util.{MyKafkaSink, MyKafkaUtil, OffsetManagerUtil}

object BaseDBMaxwellApp {
  val log = Logger.getLogger(this.getClass.getName)
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName("BaseDBMaxwellApp").setMaster("local[4]")
    val ssc = new StreamingContext(conf,Seconds(5))

//    var topic = "report_maxwell"
//    var groupId = "test"
//
//    val recordDStream = MyKafkaUtil.getKafkaStream(topic,ssc,groupId)

//    recordDStream.map(_.value()).print()

//    //从Redis中获取偏移量
//    val offsetMap: Map[TopicPartition, Long] = OffsetManagerUtil.getOffset(topic,groupId)
//    var recordDStream: InputDStream[ConsumerRecord[String, String]] = null
//    if(offsetMap!=null && offsetMap.size >0){
//      recordDStream = MyKafkaUtil.getKafkaStream(topic,ssc,offsetMap,groupId)
//    }else{
//      recordDStream = MyKafkaUtil.getKafkaStream(topic,ssc,groupId)
//    }
//
//    //获取当前采集周期中读取的主题对应的分区以及偏移量
//    var offsetRanges: Array[OffsetRange] = Array.empty[OffsetRange]
//    val offsetDStream: DStream[ConsumerRecord[String, String]] = recordDStream.transform {
//      rdd => {
//        offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
//        rdd
//      }
//    }
//
//    //对读取的数据进行结构的转换   ConsumerRecord<K,V> ==>V(jsonStr)==>V(jsonObj)
//    val jsonObjDStream: DStream[JSONObject] = offsetDStream.map {
//      record => {
//        val jsonStr: String = record.value()
//        //将json字符串转换为json对象
//        val jsonObj: JSONObject = JSON.parseObject(jsonStr)
//        jsonObj
//      }
//    }
//    //分流
//    jsonObjDStream.foreachRDD{
//      rdd=>{
//        rdd.foreach{
//          jsonObj=>{
//            //获取操作类型
//            val opType: String = jsonObj.getString("type")
//            //获取操作的数据
//            val dataJsonObj: JSONObject = jsonObj.getJSONObject("data")
//            //获取表名
//            val tableName: String = jsonObj.getString("table")
//
//            if(dataJsonObj!=null && !dataJsonObj.isEmpty ){
//              if(
//                ("order_warn".equals(tableName)&&"insert".equals(opType))
//                  || (tableName.equals("order_detail") && "insert".equals(opType))
//                  ||  tableName.equals("base_province")
//                  ||  tableName.equals("user_warn")
//                  ||  tableName.equals("sku_warn")
//                  ||  tableName.equals("base_trademark")
//                  ||  tableName.equals("base_category3")
//                  ||  tableName.equals("spu_warn")
//              ){
//                //拼接要发送到的主题
//                var sendTopic = "ods_" + tableName
//                MyKafkaSink.send(sendTopic,dataJsonObj.toString)
//              }
//            }
//          }
//        }
//        //手动提交偏移量
//        OffsetManagerUtil.saveOffset(topic,groupId,offsetRanges)
//      }
//    }

    var topic = "report_maxwell"
    var groupId = "test"

    val recordDStream = MyKafkaUtil.getKafkaStream(topic,ssc,groupId)

    //获取当前采集周期中读取的主题对应的分区以及偏移量
    var offsetRanges: Array[OffsetRange] = Array.empty[OffsetRange]
    val offsetDStream: DStream[ConsumerRecord[String, String]] = recordDStream.transform {
      rdd => {
        offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        rdd
      }
    }

    //对读取的数据进行结构的转换   ConsumerRecord<K,V> ==>V(jsonStr)==>V(jsonObj)
    val jsonObjDStream: DStream[JSONObject] = offsetDStream.map {
      record => {
        val jsonStr: String = record.value()
        println("###########"+jsonStr)
        log.warn("###########"+jsonStr)
        //将json字符串转换为json对象
        val jsonObj: JSONObject = JSON.parseObject(jsonStr)
        jsonObj
      }
    }

    //分流
    jsonObjDStream.foreachRDD{
      rdd=>{
        rdd.foreach{
          jsonObj=>{
            //获取操作类型
            val opType: String = jsonObj.getString("type")
            //获取操作的数据
            val dataJsonObj: JSONObject = jsonObj.getJSONObject("data")
            //获取表名
            val tableName: String = jsonObj.getString("table")

            if(dataJsonObj!=null && !dataJsonObj.isEmpty ){
              if("t_province".equals(tableName)){
                //拼接要发送到的主题
                var sendTopic = "ods_" + tableName
                MyKafkaSink.send(sendTopic,dataJsonObj.toString)
              }
            }
          }
        }
        //手动提交偏移量
        OffsetManagerUtil.saveOffset(topic,groupId,offsetRanges)
      }
    }


    ssc.start()
    ssc.awaitTermination()
  }
}

package dim

import com.alibaba.fastjson.JSON
import entity.TProvince
import org.apache.hadoop.conf.Configuration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import util.{MyKafkaUtil, OffsetManagerUtil}

object ProvinceApp {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName("ProvinceApp").setMaster("local[4]")
    val ssc = new StreamingContext(conf,Seconds(5))
    val sc = ssc.sparkContext
    val spark = SparkSession.builder().config(conf).getOrCreate()

    var topic = "ods_t_province"
    var groupId = "province_info_group"

    //==============1.从Kafka中读取数据===============
    //1.1获取偏移量
//    val offsetMap: Map[TopicPartition, Long] = OffsetManagerUtil.getOffset(topic,groupId)
//    //1.2根据偏移量获取数据
//    var recordDStream: InputDStream[ConsumerRecord[String, String]] = null
//    if(offsetMap != null && offsetMap.size >0){
//      recordDStream = MyKafkaUtil.getKafkaStream(topic,ssc,offsetMap,groupId)
//    }else{
//      recordDStream = MyKafkaUtil.getKafkaStream(topic,ssc,groupId)
//    }
    var recordDStream = MyKafkaUtil.getKafkaStream(topic,ssc,groupId)

    //1.3获取当前批次获取偏移量情况
    var offsetRanges: Array[OffsetRange] = Array.empty[OffsetRange]
    val offsetDStream: DStream[ConsumerRecord[String, String]] = recordDStream.transform {
      rdd => {
        offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        rdd
      }
    }

    //2.===========保存数据到Phoenix===========

    offsetDStream.foreachRDD{
      rdd=>{
        val provinceInfoRDD: RDD[TProvince] = rdd.map {
          record => {
            //获取省份的json格式字符串
            val jsonStr: String = record.value()
            //将json格式字符串封装为ProvinceInfo对象
            val provinceInfo: TProvince = JSON.parseObject(jsonStr, classOf[TProvince])
            println(provinceInfo)
            provinceInfo
          }
        }

        import org.apache.phoenix.spark._
        provinceInfoRDD.saveToPhoenix(
          "T_PROVINCE",
          Seq("ID","NAME","CODE","PCODE","FIELD_KEY","DISTRICT_ID"),
          new Configuration,
          Some("node11,node12,node13,node14,node15:2181")
        )
        //保存偏移量
        OffsetManagerUtil.saveOffset(topic,groupId,offsetRanges)
      }
    }
    ssc.start()
    ssc.awaitTermination()
  }
}

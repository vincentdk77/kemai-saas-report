package util

import com.alibaba.fastjson.JSONObject
import org.apache.kafka.common.TopicPartition

object OffsetManagerM {

  //获取偏移量
  /**
    * 从Mysql中读取偏移量
    * @param consumerGroupId
    * @param topic
    * @return
    */
  def getOffset(topic: String, consumerGroupId: String): Map[TopicPartition, Long] = {
    val sql=" select group_id,topic,topic_offset,partition_id from offset_2020 " +
      " where topic='"+topic+"' and group_id='"+consumerGroupId+"'"

    val jsonObjList: List[JSONObject] = MySQLUtil.queryList(sql)

    val topicPartitionList: List[(TopicPartition, Long)] = jsonObjList.map {
      jsonObj =>{
        val topicPartition: TopicPartition = new TopicPartition(topic, jsonObj.getIntValue("partition_id"))
        val offset: Long = jsonObj.getLongValue("topic_offset")
        (topicPartition, offset)
      }
    }
    val topicPartitionMap: Map[TopicPartition, Long] = topicPartitionList.toMap
    topicPartitionMap
  }

}

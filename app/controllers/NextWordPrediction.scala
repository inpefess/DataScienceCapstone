package controllers

import javax.inject._

import org.apache.spark.sql.SparkSession
import play.api.mvc._
import play.api.libs.Jsonp
import play.api.libs.json.Json
import sys.process._

@Singleton
class NextWordPrediction @Inject() extends Controller {
  // For running Spark Standalone on local machine
  val hostname = ("hostname" !!).trim

  val spark = SparkSession.builder
    .appName("Next Word Prediction")
    .master(s"spark://$hostname:7077")
    .getOrCreate
  import spark.implicits._

  // load N-Gram model Parquet file to SparkSQL temp view
  val maxN = 3
  val whoami = ("whoami" !!).trim
  val search = spark.read
    .parquet(s"/home/$whoami/Documents/final/res")
    .cache()
  search.createOrReplaceTempView("search")

  // main route for Play
  def nextWordPrediction(sentence: String, callback: String) = Action {implicit request =>
    // screening single quotes in the input sentence for SparkSQL compatibility
    val words = sentence.replaceAll("'", "\\\\'").split(" ", -1)
    // this is the sentence for which we want to predict the next word
    val beginning = words.slice(0, words.length - 1).mkString(" ")
    // this is lazy iteration to get at least three predictions using 1, 2, .. maxN-grams
    // or simple word frequency for N == 0
    val top3 = (maxN to 0 by -1).iterator.flatMap {N =>
      val cnt = words.length - N - 1
      // concatenated N-gram to search
      val whatToSearch = words
        .slice(if (cnt > 0) cnt else 0, words.length - 1)
        .mkString(" ")
      // top 3 prediction from the model
      spark.sql(
        s"""
           |SELECT CONCAT('$beginning ', nextWord)
           |FROM search
           |WHERE sentence = '$whatToSearch'
           |AND nextWord LIKE '${words.last}%'
           |ORDER BY count DESC
           |LIMIT 3
        """.stripMargin)
        .as[String]
        .collect()
    }.take(3)

    // we escape double quotes in the response for JSON compatibility
    val response = top3.map(
      _.replaceAll("\"", "\\\\\"")
      .trim
    )
    // replying with a valid JSON
    val json = Json.parse("[\"" + response.mkString("\", \"") + "\"]")
    Ok(Jsonp(callback, json))
  }

}

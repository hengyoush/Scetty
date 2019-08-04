import scala.collection.mutable
import scala.io.Source

object Test {
  def main(args: Array[String]): Unit = {
    val lines = Source.fromFile(args(0)).getLines().toList
    def widthOfLineLength(s: String)= s.length.toString.length
    val longestString = lines.reduceLeft((a, b) => if (a.length > b.length) a else b)
    val maxWidth = widthOfLineLength(longestString)
    for (line <- lines) {
      val numSpace = maxWidth - widthOfLineLength(line)
      val padding = " " * numSpace
      println(padding + line.length + " |" + line)
    }
  }
}

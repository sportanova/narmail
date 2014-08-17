package specs.prepare.IO

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.Index1IO
import com.textMailer.models.Index1
import com.textMailer.IO.Eq
import scala.util.Try

class Index1IOSpec extends MutableScalatraSpec {
  val prepare = PrepareData()

  "Index1IO.write" should {
    "write to the db" in {
      val index1 = Index1("someId", Map("value1"->"valuea"))
      val writtenIndex = Index1IO().asyncWrite(index1)
      val foundIndex = Index1IO().find(List(Eq("indexed_value_1","someId")), 10)
      foundIndex.headOption.get.data === index1.data
    }
  }
}
package models

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models._
import java.util.Date

package object Mapper {

  implicit val projectReader = (
    (__ \ "id").read[Long] and
    (__ \ "name").read[String])(Project)

  implicit val taskReader = (
    (__ \ "id").read[Long] and
    (__ \ "name").read[String] and
    (__ \ "due_on").readNullable[Date] and
    (__ \ "completed").readNullable[Boolean])(Task)
}

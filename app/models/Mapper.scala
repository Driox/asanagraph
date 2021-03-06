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

  implicit val tagReader = (
    (__ \ "id").read[Long] and
    (__ \ "name").read[String])(Tag)

  implicit val taskReader = (
    (__ \ "id").read[Long] and
    (__ \ "name").read[String] and
    (__ \ "due_on").readNullable[Date] and
    (__ \ "completed_at").readNullable[Date] and
    (__ \ "tags").readNullable[List[Tag]])(Task)

  implicit val tagWrites: Writes[Tag] = (
    (JsPath \ "id").write[Long] and
    (JsPath \ "name").write[String])(unlift(Tag.unapply))

  implicit val taskWrites: Writes[Task] = (
    (JsPath \ "id").write[Long] and
    (JsPath \ "name").write[String] and
    (JsPath \ "due_on").writeNullable[Date] and
    (JsPath \ "completed_at").writeNullable[Date] and
    (JsPath \ "tags").writeNullable[List[Tag]])(unlift(Task.unapply))

}

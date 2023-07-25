package controllers

import com.google.inject.Inject
import models.Stock
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.Singleton
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

@Singleton
class StocksController @Inject()(val userDao: DAO, val controllerComponents: ControllerComponents
                                )extends BaseController {
  implicit val userListJson: OFormat[Stock] = Json.format[Stock]

  def list: Action[AnyContent] = Action {
    val sList = Await.result(userDao.all(), 5 seconds)
    Ok(Json.toJson(sList))
  }

  def listById(id: Long): Action[AnyContent] = Action {
    val sList = Await.result(userDao.byId(id), 5 seconds)
    Ok(Json.toJson(sList))
  }

  def add(): Action[AnyContent] = Action { implicit request =>
    val content = request.body.asJson
    content match {
      case Some(x) => x.validate[Stock].asOpt match {
        case Some(stock) => userDao.add(stock)
          Ok(s"""created stock $stock""")
        case None => BadRequest("Invalid Request")
      }
      case None => BadRequest("Invalid")
    }
  }
}

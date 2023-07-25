package controllers

import models.User
import play.api.libs.json._
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.language.postfixOps

@Singleton
class UserController @Inject()(val controllerComponents: ControllerComponents, val userDao: DAO) extends BaseController {
  implicit val userListJson: OFormat[User] = Json.format[User]
  def addNewItem(): Action[AnyContent] = Action { implicit request =>
    val content = request.body
    val jsonObject = content.asJson
    val userListItem: Option[User] = {
      jsonObject.flatMap(
        Json.fromJson[User](_).asOpt
      )
    }
    userListItem match {
      case Some(newItem) =>
        if (userDao.findUserByUserId(newItem.id) == null) {
          userDao.addNewUser(newItem)
          Created(Json.toJson(newItem))
        } else {
          Ok("User Id Already Exists")
        }
      case None =>
        BadRequest
    }
  }

  def getAll: Action[AnyContent] = Action {
    val listOfUsers: List[User] = userDao.findAllUser()
    Ok(Json.toJson(listOfUsers))
  }

  def getUserByUserId(id: Int): Action[AnyContent] = Action {
    val listOfUsers: User = userDao.findUserByUserId(id)
    Ok(Json.toJson(listOfUsers))
  }

  def updateUser(id: Int): Action[AnyContent] = Action { implicit request =>
    val content = request.body
    val jsonObject = content.asJson
    val userListItem: Option[User] = {
      jsonObject.flatMap(
        Json.fromJson[User](_).asOpt
      )
    }
    userListItem match {
      case Some(newItem) =>
        if (userDao.findUserByUserId(id) == null) {
          Ok("USER NOT EXISTS !!")
        } else {
          userDao.updateUserByUserId(id, newItem)
          Created(Json.toJson(newItem))
        }
      case None =>
        BadRequest
    }
  }

  def deleteUser(id: Int): Action[AnyContent] = Action {
    if (userDao.findUserByUserId(id) == null) {
      Ok("User Not Found")
    } else {
      userDao.deleteUserByUserId(id)
      Ok("deleted")
    }
  }

  def getUserById(id: Int): Action[AnyContent] = Action {
    val user: User = userDao.findById(id)
    Ok(Json.toJson(user))
  }


//  def handleFutureString(): String = {
//    val futureString: Future[String] = Future.successful("hii")
//    // using callbacks
//    futureString.onComplete {
//      case scala.util.Success(result) => // handle the successful result(String)
//      case scala.util.Failure(exception) => // handle the exception
//    }.toString
//  }
//
//  def handleFutureInt(): Int = {
//    val futureInt: Future[Int] = Future.successful(1)
//    // using callbacks
//    futureInt.onComplete {
//      case scala.util.Success(result) => // handle the successful result(String)
//      case scala.util.Failure(exception) => // handle the exception
//    }.toString.toInt
//  }
}
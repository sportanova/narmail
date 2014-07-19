package com.textMailer.routes

import org.scalatra._
import scalate.ScalateSupport
import com.textMailer.TextmailerStack
import java.net.URL
import java.net.URLEncoder._
import com.stackmob.newman.{ETagAwareHttpClient, ApacheHttpClient}
import com.stackmob.newman._
import com.stackmob.newman.caching.InMemoryHttpResponseCacher
import com.stackmob.newman.dsl._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.stackmob.newman.response.HttpResponse
import org.scalatra.util.RicherString
import java.net.URLEncoder


class OAuthServlet extends TextmailerStack with MethodOverride {
  implicit val httpClient = new ApacheHttpClient

  //https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=909952895511-tnpddhu4dc0ju1ufbevtrp9qt2b4s8d6.apps.googleusercontent.com&approval_prompt=force&access_type=offline&redirect_uri=http://localhost:8080/oauth/oauth2callback&scope=https://mail.google.com/
  // refreshToken = 1/zfAy-hiQ7T3LQNww_HEe7z1M3L1aL27zZFdMszWfJlg
  def makeRequest(reqTok: String) = {
    var redirectURL = "http://localhost:8080/oauth/oauth2callback"
	val oauthURL = new URL("https://accounts.google.com/o/oauth2/token")
    val req = POST(oauthURL).addHeaders(("Content-Type", "application/x-www-form-urlencoded")).addBody(s"code=${URLEncoder.encode(reqTok, "UTF-8")}&redirect_uri=${URLEncoder.encode(redirectURL, "UTF-8")}&client_id=${URLEncoder.encode("909952895511-tnpddhu4dc0ju1ufbevtrp9qt2b4s8d6.apps.googleusercontent.com", "UTF-8")}&scope=&client_secret=${URLEncoder.encode("qaCfjCbleg8GpHVeZXljeXT0", "UTF-8")}&grant_type=${URLEncoder.encode("authorization_code", "UTF-8")}")
    val res1 = Await.result(req.apply, 10.second)
	val json1 = res1.toJson()
	  println(s"<<<<<<<< res ${res1.bodyString}")
	  println(s"<<<<<<<< json ${json1}") 
  }
  
  
  put("/accessToken/:provider") {
    val provider = params.getOrElse("provider", "no provider")
    println(s"@@@@@@@@@@ provider $provider")
    "success"
  }
  
  get("/oauth2callback") {
    val xxx = params.getOrElse("code", "no code")
    makeRequest(xxx)
    // oauth access token ya29.HABXYAohBMf6hR8AAADpZJ_9u40WEh5A1BHDFnYJtDxiYiIaIm0kdlcWmNZfzA
    // 4/V9-9qr1Ysco6XgU1tHRJLNuL1pzM.Ep9Lb059XI8d3oEBd8DOtND3e0QmjAI access code
    println(s"<<<<<<<<<<<<< xxx $xxx")
  }
}
package specs.prepare.IO

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.EmailIO
import com.textMailer.models.Email
import com.textMailer.IO.Eq
import com.textMailer.IO.SendEmail
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.actors.Futures
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils
import net.liftweb.json.JsonParser
import annotation.tailrec
import net.liftweb.json.JsonAST.JValue
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import scala.util.Try
import java.io.ByteArrayOutputStream
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Extraction._
import java.net.URL
import com.stackmob.newman.{ETagAwareHttpClient, ApacheHttpClient}
import com.stackmob.newman._
import com.stackmob.newman.caching.InMemoryHttpResponseCacher
import com.stackmob.newman.dsl._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.stackmob.newman.response.HttpResponse

class TestSpec extends MutableScalatraSpec { // specs.prepare.IO.TestSpec
  PrepareData()
  "removing replies from email" should {
    "do something" in {
//      val string = """ <div dir="ltr">Let&#39;s make this happen.. I&#39;ve made a survey. Please submit your votes by <b>Thursday, 11:59pm</b> just in case we wanna do something this weekend.<div><br></div><div>Also, if we happen to do SF this time, let&#39;s do Oakland next time.<br>\r\n<div><br></div><div><a href="https://docs.google.com/forms/d/1hdg1a7XiBunsnb5LKp4XrDtvcy31N-02CJ0DqZ22b2Q/viewform?usp=send_form">https://docs.google.com/forms/d/1hdg1a7XiBunsnb5LKp4XrDtvcy31N-02CJ0DqZ22b2Q/viewform?usp=send_form</a><br>\r\n</div></div></div><div class="gmail_extra"><br><br><div class="gmail_quote">On Mon, Aug 11, 2014 at 11:54 AM, Guy Morita <span dir="ltr">&lt;<a href="mailto:guymorita@gmail.com" target="_blank">guymorita@gmail.com</a>&gt;</span> wrote:<br>\r\n<blockquote class="gmail_quote" style="margin:0 0 0 .8ex;border-left:1px #ccc solid;padding-left:1ex"><div dir="ltr">I&#39;m out on the 22nd to Peru, but this weekend could work for brunch.</div><div class="gmail_extra"><div>\r\n<div class="h5"><br><br><div class="gmail_quote">On Sun, Aug 10, 2014 at 9:29 PM, Gregory Hilkert <span dir="ltr">&lt;<a href="mailto:ghilkert@gmail.com" target="_blank">ghilkert@gmail.com</a>&gt;</span> wrote:<br>\r\n<blockquote class="gmail_quote" style="margin:0 0 0 .8ex;border-left:1px #ccc solid;padding-left:1ex"><div dir="ltr">I&#39;m in for sure, just let me know when / where<div hspace="streak-pt-mark" style="max-height:1px"><img style="width:0px;max-height:0px" src="https://mailfoogae.appspot.com/t?sender=aZ2hpbGtlcnRAZ21haWwuY29t&amp;type=zerocontent&amp;guid=a488928a-e46d-4818-8cd3-415a975143ff"><font color="#ffffff" size="1">ᐧ</font></div>\r\n\r\n\r\n\r\n</div><div><div><div class="gmail_extra"><br><br><div class="gmail_quote">On Tue, Aug 5, 2014 at 9:35 PM, Barry M. Wong <span dir="ltr">&lt;<a href="mailto:barry@barrymwong.com" target="_blank">barry@barrymwong.com</a>&gt;</span> wrote:<br>\r\n\r\n\r\n\r\n<blockquote class="gmail_quote" style="margin:0 0 0 .8ex;border-left:1px #ccc solid;padding-left:1ex"><div dir="ltr">I&#39;d like to meet up too!</div><div><div><div class="gmail_extra"><br><br>\r\n<div class="gmail_quote">\r\nOn Mon, Aug 4, 2014 at 8:43 PM, Hao Liu <span dir="ltr">&lt;<a href="mailto:haoliu119@gmail.com" target="_blank">haoliu119@gmail.com</a>&gt;</span> wrote:<br>\r\n<blockquote class="gmail_quote" style="margin:0 0 0 .8ex;border-left:1px #ccc solid;padding-left:1ex"><div dir="ltr"><div class="gmail_extra">I&#39;m free after Aug 13th. Whatever Sat/Sun afternoon happy hour in a bar?<div>\r\n\r\n\r\n\r\n\r\n<div><br><br><div class="gmail_quote">On Mon, Aug 4, 2014 at 7:54 PM, Christopher Sita <span dir="ltr">&lt;<a href="mailto:cs.sita@me.com" target="_blank">cs.sita@me.com</a>&gt;</span> wrote:<br>\r\n\r\n<blockquote class="gmail_quote" style="margin:0 0 0 .8ex;border-left:1px #ccc solid;padding-left:1ex"><div style="word-wrap:break-word">Brunch and/or drinks within the next two weeks is fine with me. I&#39;m another SF folk willing to travel to Oakland.\xa0<div>\r\n\r\n\r\n\r\n\r\n\r\n\r\n<br></div><div>Looking forward to it.<div><br></div><div>Chris</div><div><div><div>\xa0<div><br></div><div><br><div><div>On Aug 4, 2014, at 6:15 PM, al lin // 林冠祥 &lt;<a href="mailto:lguanxiang@gmail.com" target="_blank">lguanxiang@gmail.com</a>&gt; wrote:</div>\r\n\r\n\r\n\r\n\r\n\r\n\r\n<br><blockquote type="cite"><div dir="ltr">Seconding Sprague&#39;s suggestion of doing it these next two weeks, or sometime in September after Burning Man / Labor Day. The caveat is, if we do it <i>during</i> Burning Man, places won&#39;t be packed at all.<div>\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n<br></div><div>Thanks for taking the initiative, Tuhin! And I&#39;ll be one of the few SF folk willing to head to Oakland, Jake.</div></div><div class="gmail_extra"><br><br><div class="gmail_quote">On Mon, Aug 4, 2014 at 4:54 PM, Gary Ryan <span dir="ltr">&lt;<a href="mailto:gary.thomas.ryan@gmail.com" target="_blank">gary.thomas.ryan@gmail.com</a>&gt;</span> wrote:<br>\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n<blockquote class="gmail_quote" style="margin:0 0 0 .8ex;border-left:1px #ccc solid;padding-left:1ex"><div dir="auto"><div>I&#39;m down! I would be more able to commit to brunch over drinks, but would try to make it to either.<br>\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n<br>Sent from my iPhone</div><div><div><div><br>On Aug 4, 2014, at 4:14 PM, Tuhin Chakraborty &lt;<a href="mailto:tuhin.c@gmail.com" target="_blank">tuhin.c@gmail.com</a>&gt; wrote:<br><br></div><blockquote type="cite">\r\n<div><div dir="ltr">Hey guys!<div><br></div><div>I miss you all dearly and really want to see your little cherub faces again. To that end, I propose a reunion!!</div><div><br></div><div>Here are some reasons you should come:</div>\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n<div><br></div><div>1. You miss Hack Reactor!!!!!!!!!!!!!!</div><div>2. You want to brainstorm million dollar app ideas (and become a millionaire)</div><div>3. You want to make sure you have people to connect you to your next job, whenever you need to find it</div>\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n<div>4. You want to eat delicious food / drink with people you don&#39;t normally do that with</div><div>5. You want to keep your Hack Reactor connections fresh (admit it, your office network is small in comparison to the entire industry)</div>\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n<div>6. You miss Al&#39;s rageface</div><div>7. You think I&#39;m really good looking</div><div><br></div><div>I&#39;m thinking either Sunday brunch or drinks after work. Can you guys respond to this thread with your preferences / suggestions?<br>\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n</div><div><br></div><div>Thanks!</div><div><br></div><div>Peace and love,</div><div><br></div><div>Tuhin<br clear="all"><div><br></div>-- <br><div dir="ltr"><div style="font-family:arial;font-size:small">Tuhin Chakraborty</div>\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n<div style="font-family:arial;font-size:small">Duke University, &#39;11</div></div>\r\n</div></div>\r\n</div></blockquote></div></div></div></blockquote></div><br><br clear="all"><div><br></div>-- <br><div dir="ltr">al lin // 林冠祥<div><a href="http://cmdoptesc.com/" target="_blank">cmdoptesc.com</a></div></div>\r\n</div>\r\n</blockquote></div><br></div></div></div></div></div></div></blockquote></div><br></div></div></div></div>\r\n</blockquote></div><br></div>\r\n</div></div></blockquote></div><br></div>\r\n</div></div></blockquote></div><br><br clear="all"><div><br></div></div></div><span class="HOEnZb"><font color="#888888">-- <br><div dir="ltr"><p><font size="1"><a name="147c66c2161a3274_SafeHtmlFilter__MailAutoSig"><b><span style="color:rgb(113,112,115)">Guy Morita</span></b></a><span style="color:rgb(113,112,115)"> </span></font></p>\r\n\r\n\r\n\r\n<p><font size="1"><b><span style="color:rgb(31,73,125)">T</span></b> <span style="color:gray"><span title="Call with Google Voice"><a href="tel:206.240.4846" value="+12062404846" target="_blank">206.240.4846</a></span></span><span style="color:rgb(113,112,115)">\xa0 <b>l</b></span>\xa0\xa0<a href="mailto:guymorita@gmail.com" target="_blank"><b><span style="color:blue">guymorita@gmail.com</span></b></a><b><span style="color:rgb(113,112,115)"></span></b></font></p>\r\n\r\n\r\n\r\n<p><font size="1"><a href="http://www.linkedin.com/in/guymorita" target="_blank"><span style="color:rgb(31,73,125)">LinkedIn</span></a><span style="color:rgb(31,73,125)"> | </span><a href="http://www.guymorita.com/" target="_blank"><span style="color:rgb(31,73,125)">Personal</span></a><span style="color:rgb(31,73,125)"> | <a href="https://github.com/guymorita" target="_blank">Github</a> |\xa0</span><a href="http://guymorita.tumblr.com" target="_blank"><span style="color:rgb(31,73,125)">Developer Blog</span></a><u><span style="color:rgb(31,73,125)"></span></u></font></p>\r\n\r\n\r\n\r\n<p><b><span style="color:rgb(31,73,125)"><font size="1">...............................................................................<br>\r\n...............................................................................<br>\r\n...............................................................................</font></span></b></p></div>\r\n</font></span></div>\r\n</blockquote></div><br><br clear="all"><div><br></div>-- <br><div dir="ltr">al lin // 林冠祥<div><div><a href="http://cmdoptesc.com" target="_blank">cmdoptesc.com</a></div></div></div>\r\n</div>\r\n"""
//      val string1 =  """ <html><head><meta http-equiv="Content-Type" content="text/html charset=windows-1252"></head><body style="word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space;">I’m having some trouble here and I’m worried we won’t have enough time to cover what you need before my next meeting. What time zone are you in? I can reschedule for later today.<div><br></div><div>Patrick</div><div><br><div><div>On Aug 22, 2014, at 9:32 AM, Patrick McFadin &lt;<a href="mailto:patrick@datastax.com">patrick@datastax.com</a>&gt; wrote:</div><br class="Apple-interchange-newline"><blockquote type="cite"><meta http-equiv="Content-Type" content="text/html charset=windows-1252"><div style="word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space;">Good question. I can send a hangout.<div><br><div><div>On Aug 22, 2014, at 9:21 AM, <a href="mailto:sportano@gmail.com">sportano@gmail.com</a> wrote:</div><br class="Apple-interchange-newline"><blockquote type="cite"><span itemscope="" itemtype="http://schema.org/EmailMessage"><p itemprop="description">Hey guys - are we doing this over phone, or skype / hangout?</p></span><div style=""><table cellspacing="0" cellpadding="8" border="0" summary="" style="width:100%;font-family:Arial,Sans-serif;border:1px Solid #ccc;border-width:1px 2px 2px 1px;background-color:#fff;" itemscope="" itemtype="http://schema.org/Event"><tbody><tr><td><meta itemprop="eventStatus" content="http://schema.org/EventScheduled"><div style="padding:2px"><span itemprop="publisher" itemscope="" itemtype="http://schema.org/Organization"><meta itemprop="name" content="Google Calendar"></span><meta itemprop="eventId/googleCalendar" content="461ll6trhe11jvs1a4kg4371ug"><h3 style="padding:0 0 6px 0;margin:0;font-family:Arial,Sans-serif;font-size:16px;font-weight:bold;color:#222"><span itemprop="name">Office Hours w/ Stephen Portanova to discuss Meetup presentatio</span></h3><div style="padding-bottom:15px;font-size:13px;color:#222;white-space:pre-wrap!important;white-space:-moz-pre-wrap!important;white-space:-pre-wrap!important;white-space:-o-pre-wrap!important;white-space:pre;word-wrap:break-word">Help Stephen with his first full length talk for a meetup</div><table cellpadding="0" cellspacing="0" border="0" summary="Event details"><tbody><tr><td style="padding:0 1em 10px 0;font-family:Arial,Sans-serif;font-size:13px;color:#888;white-space:nowrap" valign="top"><i style="font-style:normal">When</i></td><td style="padding-bottom:10px;font-family:Arial,Sans-serif;font-size:13px;color:#222" valign="top"><time itemprop="startDate" datetime="20140822T163000Z"></time><time itemprop="endDate" datetime="20140822T170000Z"></time>Fri Aug 22, 2014 9:30am – 10am <span style="color:#888">Pacific Time</span></td></tr><tr><td style="padding:0 1em 10px 0;font-family:Arial,Sans-serif;font-size:13px;color:#888;white-space:nowrap" valign="top"><i style="font-style:normal">Who</i></td><td style="padding-bottom:10px;font-family:Arial,Sans-serif;font-size:13px;color:#222" valign="top"><table cellspacing="0" cellpadding="0"><tbody><tr><td style="padding-right:10px;font-family:Arial,Sans-serif;font-size:13px;color:#222"><span style="font-family:Courier New,monospace">•</span></td><td style="padding-right:10px;font-family:Arial,Sans-serif;font-size:13px;color:#222"><div style="margin:0 0 0.3em 0"><span itemprop="attendee" itemscope="" itemtype="http://schema.org/Person"><span itemprop="name">Lina Tran</span><meta itemprop="email" content="ltran@datastax.com"></span><span style="font-size:11px;color:#888"> - organizer</span></div></td></tr><tr><td style="padding-right:10px;font-family:Arial,Sans-serif;font-size:13px;color:#222"><span style="font-family:Courier New,monospace">•</span></td><td style="padding-right:10px;font-family:Arial,Sans-serif;font-size:13px;color:#222"><div style="margin:0 0 0.3em 0"><span itemprop="attendee" itemscope="" itemtype="http://schema.org/Person"><span itemprop="name">Patrick McFadin</span><meta itemprop="email" content="patrick@datastax.com"></span></div></td></tr><tr><td style="padding-right:10px;font-family:Arial,Sans-serif;font-size:13px;color:#222"><span style="font-family:Courier New,monospace">•</span></td><td style="padding-right:10px;font-family:Arial,Sans-serif;font-size:13px;color:#222"><div style="margin:0 0 0.3em 0"><span itemprop="attendee" itemscope="" itemtype="http://schema.org/Person"><span itemprop="name">Stephen Portanova</span><meta itemprop="email" content="sportano@gmail.com"></span></div></td></tr></tbody></table></td></tr></tbody></table></div></td></tr></tbody></table></div></blockquote></div><br></div></div></blockquote></div><br></div></body></html>"""
      // <div> ???        
//      val newString = string1.split("""<div class="gmail_extra">""").toList.head;
//      println(s"############## stringLength ${newString.length}")
//      println(s"############## string ${newString}")
    }
    
//    "futures" in {
//      def asyncify[A, B](f: A => B): A => scala.actors.Future[B] = (a => Futures.future(f(a)))
//      def sleepFor(seconds: Int) = {
//        Thread.sleep(seconds * 1000)
//        println(s"######## seconds $seconds")
//        seconds
//      }
//      
//      val asyncSleepFor = asyncify(sleepFor)
//      val test = asyncSleepFor(5) // now it does NOT block
////      val a = test()
//      println("waiting...")         // prints "waiting..." rightaway
////      println("future returns %d".format(test())) // prints "future returns 5" after 5 seconds
//    }
  }
  
  "decoding strings" should {
    "decode" in {
      val text = StringUtils.newStringUtf8(Base64.decodeBase64("SSBkb24ndCB0aGluayB5b3UgY29udHJvbCB3aGljaCBob3N0IGhlIHJlY2VpdmVyIHJ1bnMgb24sIHJpZ2h0PyBTbyB0aGF0DQpTcGFyayBjYW4gaGFuZGxlIHRoZSBmYWlsdXJlIG9mIHRoYXQgbm9kZSBhbmQgcmVhc3NpZ24gdGhlIHJlY2VpdmVyLg0KT24gU2VwIDI3LCAyMDE0IDI6NDMgQU0sICJjZW50ZXJxaSBodSIgPGNlbnRlcnFpQGdtYWlsLmNvbT4gd3JvdGU6DQoNCj4gdGhlIHJlY2VpdmVyIGlzIG5vdCBydW5uaW5nIG9uIHRoZSBtYWNoaW5lIEkgZXhwZWN0DQo-DQo-DQo-DQo-IDIwMTQtMDktMjYgMTQ6MDkgR01UKzA4OjAwIFNlYW4gT3dlbiA8c293ZW5AY2xvdWRlcmEuY29tPjoNCj4gPiBJIHRoaW5rIHlvdSBtYXkgYmUgbWlzc2luZyBhIGtleSB3b3JkIGhlcmUuIEFyZSB5b3Ugc2F5aW5nIHRoYXQgdGhlDQo-IG1hY2hpbmUNCj4gPiBoYXMgbXVsdGlwbGUgaW50ZXJmYWNlcyBhbmQgaXQgaXMgbm90IHVzaW5nIHRoZSBvbmUgeW91IGV4cGVjdCBvciB0aGUNCj4gPiByZWNlaXZlciBpcyBub3QgcnVubmluZyBvbiB0aGUgbWFjaGluZSB5b3UgZXhwZWN0Pw0KPg0KPiAtLQ0KPiBjZW50ZXJxaUBnbWFpbC5jb2186b2Q5b-gDQo-DQo="));
//      println(s"############## text $text")
      
      val html = StringUtils.newStringUtf8(Base64.decodeBase64("PHAgZGlyPSJsdHIiPkkgZG9uJiMzOTt0IHRoaW5rIHlvdSBjb250cm9sIHdoaWNoIGhvc3QgaGUgcmVjZWl2ZXIgcnVucyBvbiwgcmlnaHQ_IFNvIHRoYXQgU3BhcmsgY2FuIGhhbmRsZSB0aGUgZmFpbHVyZSBvZiB0aGF0IG5vZGUgYW5kIHJlYXNzaWduIHRoZSByZWNlaXZlci4gPC9wPg0KPGRpdiBjbGFzcz0iZ21haWxfcXVvdGUiPk9uIFNlcCAyNywgMjAxNCAyOjQzIEFNLCAmcXVvdDtjZW50ZXJxaSBodSZxdW90OyAmbHQ7PGEgaHJlZj0ibWFpbHRvOmNlbnRlcnFpQGdtYWlsLmNvbSI-Y2VudGVycWlAZ21haWwuY29tPC9hPiZndDsgd3JvdGU6PGJyIHR5cGU9ImF0dHJpYnV0aW9uIj48YmxvY2txdW90ZSBjbGFzcz0iZ21haWxfcXVvdGUiIHN0eWxlPSJtYXJnaW46MCAwIDAgLjhleDtib3JkZXItbGVmdDoxcHggI2NjYyBzb2xpZDtwYWRkaW5nLWxlZnQ6MWV4Ij50aGUgcmVjZWl2ZXIgaXMgbm90IHJ1bm5pbmcgb24gdGhlIG1hY2hpbmUgSSBleHBlY3Q8YnI-DQo8YnI-DQo8YnI-DQo8YnI-DQoyMDE0LTA5LTI2IDE0OjA5IEdNVCswODowMCBTZWFuIE93ZW4gJmx0OzxhIGhyZWY9Im1haWx0bzpzb3dlbkBjbG91ZGVyYS5jb20iPnNvd2VuQGNsb3VkZXJhLmNvbTwvYT4mZ3Q7Ojxicj4NCiZndDsgSSB0aGluayB5b3UgbWF5IGJlIG1pc3NpbmcgYSBrZXkgd29yZCBoZXJlLiBBcmUgeW91IHNheWluZyB0aGF0IHRoZSBtYWNoaW5lPGJyPg0KJmd0OyBoYXMgbXVsdGlwbGUgaW50ZXJmYWNlcyBhbmQgaXQgaXMgbm90IHVzaW5nIHRoZSBvbmUgeW91IGV4cGVjdCBvciB0aGU8YnI-DQomZ3Q7IHJlY2VpdmVyIGlzIG5vdCBydW5uaW5nIG9uIHRoZSBtYWNoaW5lIHlvdSBleHBlY3Q_PGJyPg0KPGJyPg0KLS08YnI-DQo8YSBocmVmPSJtYWlsdG86Y2VudGVycWlAZ21haWwuY29tIj5jZW50ZXJxaUBnbWFpbC5jb208L2E-fOm9kOW_oDxicj4NCjwvYmxvY2txdW90ZT48L2Rpdj4NCg=="));
//      println(s"############## html $html")
    }
  }
  
  "do" should {
    "something" in {
      val str2 = """{
 "id": "148bc67eb4ce4445",
 "threadId": "148bc67eb4ce4445",
 "labelIds": [
  "CATEGORY_FORUMS",
  "UNREAD",
  "Label_10"
 ],
 "snippet": "All Sorry this is spark related, but I thought some of you in San Francisco might be interested in",
 "historyId": "1894333",
 "payload": {
  "partId": "",
  "mimeType": "text/plain",
  "filename": "",
  "headers": [
   {
    "name": "Delivered-To",
    "value": "sportano@gmail.com"
   },
   {
    "name": "Received",
    "value": "by 10.60.70.66 with SMTP id k2csp200112oeu;        Sun, 28 Sep 2014 06:16:40 -0700 (PDT)"
   },
   {
    "name": "X-Received",
    "value": "by 10.70.13.193 with SMTP id j1mr12287200pdc.51.1411910200300;        Sun, 28 Sep 2014 06:16:40 -0700 (PDT)"
   },
   {
    "name": "Return-Path",
    "value": "\u003cuser-return-17001-sportano=gmail.com@spark.apache.org\u003e"
   },
   {
    "name": "Received",
    "value": "from mail.apache.org (hermes.apache.org. [140.211.11.3])        by mx.google.com with SMTP id bf2si18517826pbb.76.2014.09.28.06.16.39        for \u003csportano@gmail.com\u003e;        Sun, 28 Sep 2014 06:16:40 -0700 (PDT)"
   },
   {
    "name": "Received-SPF",
    "value": "pass (google.com: domain of user-return-17001-sportano=gmail.com@spark.apache.org designates 140.211.11.3 as permitted sender) client-ip=140.211.11.3;"
   },
   {
    "name": "Authentication-Results",
    "value": "mx.google.com;       spf=pass (google.com: domain of user-return-17001-sportano=gmail.com@spark.apache.org designates 140.211.11.3 as permitted sender) smtp.mail=user-return-17001-sportano=gmail.com@spark.apache.org"
   },
   {
    "name": "Received",
    "value": "(qmail 42936 invoked by uid 500); 28 Sep 2014 13:16:39 -0000"
   },
   {
    "name": "Mailing-List",
    "value": "contact user-help@spark.apache.org; run by ezmlm"
   },
   {
    "name": "Precedence",
    "value": "bulk"
   },
   {
    "name": "List-Help",
    "value": "\u003cmailto:user-help@spark.apache.org\u003e"
   },
   {
    "name": "List-Unsubscribe",
    "value": "\u003cmailto:user-unsubscribe@spark.apache.org\u003e"
   },
   {
    "name": "List-Post",
    "value": "\u003cmailto:user@spark.apache.org\u003e"
   },
   {
    "name": "List-Id",
    "value": "\u003cuser.spark.apache.org\u003e"
   },
   {
    "name": "Delivered-To",
    "value": "mailing list \u003cuser@spark.apache.org\u003e"
   },
   {
    "name": "Received",
    "value": "(qmail 42926 invoked by uid 99); 28 Sep 2014 13:16:39 -0000"
   },
   {
    "name": "Received",
    "value": "from athena.apache.org (HELO athena.apache.org) (140.211.11.136)    by apache.org (qpsmtpd/0.29) with ESMTP; Sun, 28 Sep 2014 13:16:39 +0000"
   },
   {
    "name": "X-ASF-Spam-Status",
    "value": "No, hits=-0.7 required=5.0 tests=RCVD_IN_DNSWL_LOW,SPF_PASS"
   },
   {
    "name": "X-Spam-Check-By",
    "value": "apache.org"
   },
   {
    "name": "Received-SPF",
    "value": "pass (athena.apache.org: domain of chester@alpinenow.com designates 209.85.220.51 as permitted sender)"
   },
   {
    "name": "Received",
    "value": "from [209.85.220.51] (HELO mail-pa0-f51.google.com) (209.85.220.51)    by apache.org (qpsmtpd/0.29) with ESMTP; Sun, 28 Sep 2014 13:16:34 +0000"
   },
   {
    "name": "Received",
    "value": "by mail-pa0-f51.google.com with SMTP id lj1so1060561pab.24        for \u003cuser@spark.apache.org\u003e; Sun, 28 Sep 2014 06:16:13 -0700 (PDT)"
   },
   {
    "name": "X-Google-DKIM-Signature",
    "value": "v=1; a=rsa-sha256; c=relaxed/relaxed;        d=1e100.net; s=20130820;        h=x-gm-message-state:subject:from:content-type:message-id:date:to         :content-transfer-encoding:mime-version;        bh=nPN0h2ZH/ME+xNd9/9+e6C82lt1BxAVI508XrsRfSlQ=;        b=QaPOlbypo/rxlwVzvIzlmKxC7HuoE+m7DR5Zjr3VpUjqbYIdWW96RZm9L/14KeZRoc         UnNjM1y9pN+9tOEIJYlJvqDrC1mbXkpvAPOFGET4pH/lg9E/sGTzLD5881xxtg6TWdyC         R/FiCP3C8VWPbB1enqB8kcmflzXJwJSq2axLxmQsTFU/khWzgQcLB4+H3XOSHrmvGJyX         jr2iHBpb9CCe4Q6EtpL506ZNkp341vuEJRRfwyCbFiszCYtBqPSVLyKzainmO8+2zf0k         eewVfMVQT9fkiZb5Uk5NPqS1WznJ7mgTT624zR1GtQm7cFr5/31M/SLb28b0sEJt6YUe         tm/Q=="
   },
   {
    "name": "X-Gm-Message-State",
    "value": "ALoCoQmg+vUzHXy+oIm3rm70EEZt9mcmUguXJBsFz/8carqXho14A3rubqpgiRK850/yyAiH+k68"
   },
   {
    "name": "X-Received",
    "value": "by 10.67.1.195 with SMTP id bi3mr50527030pad.74.1411910173086;        Sun, 28 Sep 2014 06:16:13 -0700 (PDT)"
   },
   {
    "name": "Received",
    "value": "from [192.168.2.5] ([24.5.225.89])        by mx.google.com with ESMTPSA id pn1sm5958345pdb.65.2014.09.28.06.16.11        for \u003cuser@spark.apache.org\u003e        (version=TLSv1 cipher=ECDHE-RSA-RC4-SHA bits=128/128);        Sun, 28 Sep 2014 06:16:11 -0700 (PDT)"
   },
   {
    "name": "Subject",
    "value": "[SF Machine Learning meetup] talk by Prof. C J Lin, large-scale linear classification: status and changllenges"
   },
   {
    "name": "From",
    "value": "Chester At Work \u003cchester@alpinenow.com\u003e"
   },
   {
    "name": "Content-Type",
    "value": "text/plain; charset=us-ascii"
   },
   {
    "name": "X-Mailer",
    "value": "iPad Mail (8F191)"
   },
   {
    "name": "Message-Id",
    "value": "\u003cF5031A69-8465-4682-881E-CA4343D21CE9@alpinenow.com\u003e"
   },
   {
    "name": "Date",
    "value": "Sun, 28 Sep 2014 06:39:30 -0700"
   },
   {
    "name": "To",
    "value": "\"user@spark.apache.org\" \u003cuser@spark.apache.org\u003e"
   },
   {
    "name": "Content-Transfer-Encoding",
    "value": "quoted-printable"
   },
   {
    "name": "Mime-Version",
    "value": "1.0 (iPad Mail 8F191)"
   },
   {
    "name": "X-Virus-Checked",
    "value": "Checked by ClamAV on apache.org"
   }
  ],
  "body": {
   "size": 664,
   "data": "QWxsDQogICAgIFNvcnJ5IHRoaXMgaXMgc3BhcmsgcmVsYXRlZCwgYnV0IEkgdGhvdWdodCBzb21lIG9mIHlvdSBpbiBTYW4gRnJhbmNpc2NvIG1pZ2h0IGJlIGludGVyZXN0ZWQgaW4gdGhpcyB0YWxrLiBXZSBhbm5vdW5jZWQgdGhpcyB0YWxrIHJlY2VudGx5LCBpdCB3aWxsIGJlIGF0IHRoZSBlbmQgb2YgbmV4dCBtb250aCAob2N0KQ0KDQpodHRwOi8vd3d3Lm1lZXR1cC5jb20vc2ZtYWNoaW5lbGVhcm5pbmcvZXZlbnRzLzIwODA3ODU4Mi8NCg0KUHJvZiBDSiBMaW4gaXMgZmFtb3VzIGZvciBoaXMgd29yayBvbiBsaWJzdm0gYW5kIExpYmxpbmVhciwgaGUgd2lsbCB2aXNpdCBzZXZlcmFsIGNvbXBhbmllcyBpbiBTYW4gRnJhbmNpc2NvIG9uIGhpcyB3YXkgYmFjayB0byBUYWl3YW4sIHNvIHdlIGludml0ZWQgaGltIHRvIGdpdmUgdGhpcyB0YWxrLg0KDQoNCkNoZXN0ZXIgQ2hlbg0KQWxwaW5lIERhdGEgTGFicw0KDQoNCg0KLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tDQpUbyB1bnN1YnNjcmliZSwgZS1tYWlsOiB1c2VyLXVuc3Vic2NyaWJlQHNwYXJrLmFwYWNoZS5vcmcNCkZvciBhZGRpdGlvbmFsIGNvbW1hbmRzLCBlLW1haWw6IHVzZXItaGVscEBzcGFyay5hcGFjaGUub3JnDQoNCg=="
  }
 },
 "sizeEstimate": 4515
}"""
        
        
        
        
        
      val str = """{
 "id": "148bba9e8c1fd001",
 "threadId": "148bba9e8c1fd001",
 "labelIds": [
  "CATEGORY_FORUMS",
  "UNREAD",
  "Label_10"
 ],
 "snippet": "Hi We have used LogisticRegression with two different optimization method SGD and LBFGS in MLlib.",
 "historyId": "1893924",
 "payload": {
  "mimeType": "multipart/alternative",
  "filename": "",
  "headers": [
   {
    "name": "Delivered-To",
    "value": "sportano@gmail.com"
   },
   {
    "name": "Received",
    "value": "by 10.60.70.66 with SMTP id k2csp179601oeu;        Sun, 28 Sep 2014 02:49:08 -0700 (PDT)"
   },
   {
    "name": "X-Received",
    "value": "by 10.68.139.232 with SMTP id rb8mr14325099pbb.20.1411897747875;        Sun, 28 Sep 2014 02:49:07 -0700 (PDT)"
   },
   {
    "name": "Return-Path",
    "value": "\u003cuser-return-17000-sportano=gmail.com@spark.apache.org\u003e"
   },
   {
    "name": "Received",
    "value": "from mail.apache.org (hermes.apache.org. [140.211.11.3])        by mx.google.com with SMTP id da2si5149951pbb.46.2014.09.28.02.49.07        for \u003csportano@gmail.com\u003e;        Sun, 28 Sep 2014 02:49:07 -0700 (PDT)"
   },
   {
    "name": "Received-SPF",
    "value": "pass (google.com: domain of user-return-17000-sportano=gmail.com@spark.apache.org designates 140.211.11.3 as permitted sender) client-ip=140.211.11.3;"
   },
   {
    "name": "Authentication-Results",
    "value": "mx.google.com;       spf=pass (google.com: domain of user-return-17000-sportano=gmail.com@spark.apache.org designates 140.211.11.3 as permitted sender) smtp.mail=user-return-17000-sportano=gmail.com@spark.apache.org;       dkim=pass header.i=@gmail.com;       dmarc=pass (p=NONE dis=NONE) header.from=gmail.com"
   },
   {
    "name": "Received",
    "value": "(qmail 4331 invoked by uid 500); 28 Sep 2014 09:49:06 -0000"
   },
   {
    "name": "Mailing-List",
    "value": "contact user-help@spark.apache.org; run by ezmlm"
   },
   {
    "name": "Precedence",
    "value": "bulk"
   },
   {
    "name": "List-Help",
    "value": "\u003cmailto:user-help@spark.apache.org\u003e"
   },
   {
    "name": "List-Unsubscribe",
    "value": "\u003cmailto:user-unsubscribe@spark.apache.org\u003e"
   },
   {
    "name": "List-Post",
    "value": "\u003cmailto:user@spark.apache.org\u003e"
   },
   {
    "name": "List-Id",
    "value": "\u003cuser.spark.apache.org\u003e"
   },
   {
    "name": "Delivered-To",
    "value": "mailing list \u003cuser@spark.apache.org\u003e"
   },
   {
    "name": "Received",
    "value": "(qmail 4312 invoked by uid 99); 28 Sep 2014 09:49:06 -0000"
   },
   {
    "name": "Received",
    "value": "from nike.apache.org (HELO nike.apache.org) (192.87.106.230)    by apache.org (qpsmtpd/0.29) with ESMTP; Sun, 28 Sep 2014 09:49:06 +0000"
   },
   {
    "name": "X-ASF-Spam-Status",
    "value": "No, hits=1.5 required=5.0 tests=HTML_MESSAGE,RCVD_IN_DNSWL_LOW,SPF_PASS"
   },
   {
    "name": "X-Spam-Check-By",
    "value": "apache.org"
   },
   {
    "name": "Received-SPF",
    "value": "pass (nike.apache.org: domain of yanbohappy@gmail.com designates 74.125.82.48 as permitted sender)"
   },
   {
    "name": "Received",
    "value": "from [74.125.82.48] (HELO mail-wg0-f48.google.com) (74.125.82.48)    by apache.org (qpsmtpd/0.29) with ESMTP; Sun, 28 Sep 2014 09:48:40 +0000"
   },
   {
    "name": "Received",
    "value": "by mail-wg0-f48.google.com with SMTP id x13so8583588wgg.7        for \u003cmultiple recipients\u003e; Sun, 28 Sep 2014 02:48:39 -0700 (PDT)"
   },
   {
    "name": "DKIM-Signature",
    "value": "v=1; a=rsa-sha256; c=relaxed/relaxed;        d=gmail.com; s=20120113;        h=mime-version:date:message-id:subject:from:to:content-type;        bh=ElK7FtZW0YzQKDJsGDFaw/UJLXU0XU+QJhro9Xal8HA=;        b=QBgRaMZT1j63E5SD+WpXEyYKpOQFcIJ+M8TdDwSjlCYKlpJFTC7J1ME+nlq2O7CHja         vkmGE7hST4x4YZQ/e2W0Px/o+31g1oOmoPg9T/dlRkEcvSAml/wYEBHhl0hzyCGavlrO         K8zbhRgNgOwUGGSHxY58r8FKxEv/uhpCPm4j/dWc2qHZv7nbdIqw/zE5GwM5QGDVKyrc         hjY0WeJzSkgAan8b761qwkr9eS/vqBbqQwt/M+QX44ZPIDLFG6OfuWegHGNLIh5/7e3V         UGDsVttdnAbJpQZfB6doc6hGZJVEno9wTECmaqkBZtNNujbol3YiJmSF5pa0oZkY506Q         TzSQ=="
   },
   {
    "name": "MIME-Version",
    "value": "1.0"
   },
   {
    "name": "X-Received",
    "value": "by 10.194.133.135 with SMTP id pc7mr26320881wjb.54.1411897719635; Sun, 28 Sep 2014 02:48:39 -0700 (PDT)"
   },
   {
    "name": "Received",
    "value": "by 10.217.107.135 with HTTP; Sun, 28 Sep 2014 02:48:39 -0700 (PDT)"
   },
   {
    "name": "Date",
    "value": "Sun, 28 Sep 2014 17:48:39 +0800"
   },
   {
    "name": "Message-ID",
    "value": "\u003cCALDQvdewbW4D-a1o=HLboQ10WBszBmFM4gSEWU3=8bBoitExeA@mail.gmail.com\u003e"
   },
   {
    "name": "Subject",
    "value": "[MLlib] LogisticRegressionWithSGD and LogisticRegressionWithLBFGS converge with different weights."
   },
   {
    "name": "From",
    "value": "Yanbo Liang \u003cyanbohappy@gmail.com\u003e"
   },
   {
    "name": "To",
    "value": "\"dev@spark.apache.org\" \u003cdev@spark.apache.org\u003e, \"user@spark.apache.org\" \u003cuser@spark.apache.org\u003e, Xiangrui Meng \u003cmengxr@gmail.com\u003e, DB Tsai \u003cdbtsai@dbtsai.com\u003e"
   },
   {
    "name": "Content-Type",
    "value": "multipart/alternative; boundary=089e0122978a09c6e005041d0ea7"
   },
   {
    "name": "X-Virus-Checked",
    "value": "Checked by ClamAV on apache.org"
   }
  ],
  "body": {
   "size": 0
  },
  "parts": [
   {
    "partId": "0",
    "mimeType": "text/plain",
    "filename": "",
    "headers": [
     {
      "name": "Content-Type",
      "value": "text/plain; charset=UTF-8"
     }
    ],
    "body": {
     "size": 735,
     "data": "SGkNCg0KV2UgaGF2ZSB1c2VkIExvZ2lzdGljUmVncmVzc2lvbiB3aXRoIHR3byBkaWZmZXJlbnQgb3B0aW1pemF0aW9uIG1ldGhvZCBTR0QNCmFuZCBMQkZHUyBpbiBNTGxpYi4NCldpdGggdGhlIHNhbWUgZGF0YXNldCBhbmQgdGhlIHNhbWUgdHJhaW5pbmcgYW5kIHRlc3Qgc3BsaXQsIGJ1dCBnZXQNCmRpZmZlcmVudCB3ZWlnaHRzIHZlY3Rvci4NCg0KRm9yIGV4YW1wbGUsIHdlIHVzZQ0Kc3BhcmstMS4xLjAvZGF0YS9tbGxpYi9zYW1wbGVfYmluYXJ5X2NsYXNzaWZpY2F0aW9uX2RhdGEudHh0DQphcyBvdXIgdHJhaW5pbmcgYW5kIHRlc3QgZGF0YXNldC4NCldpdGggTG9naXN0aWNSZWdyZXNzaW9uV2l0aFNHRCBhbmQgTG9naXN0aWNSZWdyZXNzaW9uV2l0aExCRkdTIGFzIHRyYWluaW5nDQptZXRob2QgYW5kIHRoZSBzYW1lIG90aGVyIHBhcmFtZXRlcnMuDQoNClRoZSBwcmVjaXNpb25zIG9mIHRoZXNlIHR3byBtZXRob2RzIGFsbW9zdCBuZWFyIDEwMCUgYW5kIEFVQ3MgYXJlIGFsc28gbmVhcg0KMS4wLg0KQXMgZmFyIGFzIEkga25vdywgdGhlIGNvbnZleCBvcHRpbWl6YXRpb24gcHJvYmxlbSB3aWxsIGNvbnZlcmdlIHRvIHRoZQ0KZ2xvYmFsIG1pbmltdW0gdmFsdWUuIChXZSB1c2UgU0dEIHdpdGggbWluaSBiYXRjaCBmcmFjdGlvbiBhcyAxLjApDQpCdXQgSSBnb3QgdHdvIGRpZmZlcmVudCB3ZWlnaHRzIHZlY3Rvcj8gSXMgdGhpcyBleHBlY3RhdGlvbiBvciBtYWtlIHNlbnNlPw0K"
    }
   },
   {
    "partId": "1",
    "mimeType": "text/html",
    "filename": "",
    "headers": [
     {
      "name": "Content-Type",
      "value": "text/html; charset=UTF-8"
     },
     {
      "name": "Content-Transfer-Encoding",
      "value": "quoted-printable"
     }
    ],
    "body": {
     "size": 1920,
     "data": "PGRpdiBkaXI9Imx0ciI-PGZvbnQgZmFjZT0iYXJpYWwsIGhlbHZldGljYSwgc2Fucy1zZXJpZiI-SGnCoDwvZm9udD48ZGl2Pjxmb250IGZhY2U9ImFyaWFsLCBoZWx2ZXRpY2EsIHNhbnMtc2VyaWYiPjxicj48L2ZvbnQ-PC9kaXY-PGRpdj48Zm9udCBmYWNlPSJhcmlhbCwgaGVsdmV0aWNhLCBzYW5zLXNlcmlmIj5XZSBoYXZlIHVzZWQgTG9naXN0aWNSZWdyZXNzaW9uIHdpdGggdHdvIGRpZmZlcmVudCBvcHRpbWl6YXRpb24gbWV0aG9kIFNHRCBhbmQgTEJGR1MgaW4gTUxsaWIuPC9mb250PjwvZGl2PjxkaXY-PGZvbnQgZmFjZT0iYXJpYWwsIGhlbHZldGljYSwgc2Fucy1zZXJpZiI-V2l0aCB0aGUgc2FtZSBkYXRhc2V0IGFuZCB0aGUgc2FtZSB0cmFpbmluZyBhbmQgdGVzdCBzcGxpdCwgYnV0IGdldCBkaWZmZXJlbnQgd2VpZ2h0cyB2ZWN0b3IuPC9mb250PjwvZGl2PjxkaXY-PGZvbnQgZmFjZT0iYXJpYWwsIGhlbHZldGljYSwgc2Fucy1zZXJpZiI-PGJyPjwvZm9udD48L2Rpdj48ZGl2Pjxmb250IGZhY2U9ImFyaWFsLCBoZWx2ZXRpY2EsIHNhbnMtc2VyaWYiPkZvciBleGFtcGxlLCB3ZSB1c2XCoDxzcGFuIHN0eWxlPSJjb2xvcjpyZ2IoMCwwLDApO2xpbmUtaGVpZ2h0OjIwcHgiPnNwYXJrLTEuMS4wL2RhdGEvbWxsaWIvc2FtcGxlX2JpbmFyeV9jbGFzc2lmaWNhdGlvbl9kYXRhLnR4dCBhcyBvdXIgdHJhaW5pbmcgYW5kIHRlc3QgZGF0YXNldC48L3NwYW4-PC9mb250PjwvZGl2PjxkaXY-PGZvbnQgZmFjZT0iYXJpYWwsIGhlbHZldGljYSwgc2Fucy1zZXJpZiI-PGZvbnQgY29sb3I9IiMwMDAwMDAiPjxzcGFuIHN0eWxlPSJsaW5lLWhlaWdodDoyMHB4Ij5XaXRowqA8L3NwYW4-PC9mb250PjxzcGFuIHN0eWxlPSJsaW5lLWhlaWdodDoxNi43OTk5OTkyMzcwNjA1cHg7d2hpdGUtc3BhY2U6cHJlIj48Zm9udCBjb2xvcj0iIzAwMDAwMCI-TG9naXN0aWNSZWdyZXNzaW9uV2l0aFNHRCBhbmQgPC9mb250Pjwvc3Bhbj48c3BhbiBzdHlsZT0iY29sb3I6cmdiKDAsMCwwKTtsaW5lLWhlaWdodDoxNi43OTk5OTkyMzcwNjA1cHg7d2hpdGUtc3BhY2U6cHJlIj5Mb2dpc3RpY1JlZ3Jlc3Npb25XaXRoTEJGR1MgYXMgdHJhaW5pbmcgbWV0aG9kIGFuZCB0aGUgc2FtZSBvdGhlciBwYXJhbWV0ZXJzLjwvc3Bhbj48L2ZvbnQ-PC9kaXY-PGRpdj48Zm9udCBmYWNlPSJhcmlhbCwgaGVsdmV0aWNhLCBzYW5zLXNlcmlmIj48c3BhbiBzdHlsZT0iY29sb3I6cmdiKDAsMCwwKTtsaW5lLWhlaWdodDoxNi43OTk5OTkyMzcwNjA1cHg7d2hpdGUtc3BhY2U6cHJlIj48YnI-PC9zcGFuPjwvZm9udD48L2Rpdj48ZGl2Pjxmb250IGZhY2U9ImFyaWFsLCBoZWx2ZXRpY2EsIHNhbnMtc2VyaWYiPjxzcGFuIHN0eWxlPSJjb2xvcjpyZ2IoMCwwLDApO2xpbmUtaGVpZ2h0OjE2Ljc5OTk5OTIzNzA2MDVweDt3aGl0ZS1zcGFjZTpwcmUiPlRoZSBwcmVjaXNpb25zIG9mIHRoZXNlIHR3byBtZXRob2RzIGFsbW9zdCBuZWFyIDEwMCUgYW5kIEFVQ3MgYXJlIGFsc28gbmVhciAxLjAuPC9zcGFuPjwvZm9udD48L2Rpdj48ZGl2Pjxmb250IGZhY2U9ImFyaWFsLCBoZWx2ZXRpY2EsIHNhbnMtc2VyaWYiPjxzcGFuIHN0eWxlPSJjb2xvcjpyZ2IoMCwwLDApO2xpbmUtaGVpZ2h0OjE2Ljc5OTk5OTIzNzA2MDVweDt3aGl0ZS1zcGFjZTpwcmUiPkFzIGZhciBhcyBJIGtub3csIHRoZSBjb252ZXggb3B0aW1pemF0aW9uIHByb2JsZW0gd2lsbCBjb252ZXJnZSB0byB0aGUgZ2xvYmFsIG1pbmltdW0gdmFsdWUuIChXZSB1c2UgU0dEIHdpdGggbWluaSBiYXRjaCBmcmFjdGlvbiBhcyAxLjApPC9zcGFuPjwvZm9udD48L2Rpdj48ZGl2PkJ1dCBJIGdvdCB0d28gZGlmZmVyZW50IHdlaWdodHMgdmVjdG9yPyBJcyB0aGlzIGV4cGVjdGF0aW9uIG9yIG1ha2Ugc2Vuc2U_PC9kaXY-DQoNCg0KDQoNCg0KDQoNCjwvZGl2Pg0K"
    }
   }
  ]
 },
 "sizeEstimate": 6638
}"""
        
      val json = JsonParser.parse(str2)
//      println(s"###################### json ${json.values.asInstanceOf[Map[String,Any]].get("payload").get.asInstanceOf[Map[String,Any]].get("parts")}")
      val message = (for {
          payload <- json.values.asInstanceOf[Map[String,Any]].get("payload")
          headers <- payload.asInstanceOf[Map[String,Any]].get("headers")
//          parts <- payload.asInstanceOf[Map[String,Any]].get("parts")
//            messages <- JsonParser.parse(body.toString).values.asInstanceOf[Map[String,Any]].get("messages")
      } yield(payload, headers))
//      println(s"########## message ${message.get._1}")
      
//      val subject = message.get._2.find(h => h.)
      
      // way #1
//      val bodies = for {
//        parts <- message.get._1.asInstanceOf[Map[String,Any]].get("parts")
//        textMap <- parts.asInstanceOf[List[Map[String,Any]]].find(part => part.get("mimeType") == Some("text/plain"))
//        text <- textMap.get("body").get.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
//        htmlMap <- parts.asInstanceOf[List[Map[String,Any]]].find(part => part.get("mimeType") == Some("text/html"))
//        html <- htmlMap.get("body").get.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
//      } yield(text, html)
      
      val bodies = for {
        body <- message.get._1.asInstanceOf[Map[String,Any]].get("body")
        text <- body.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
      } yield(text, "")

      val textBody = bodies match {
        case Some(b) => StringUtils.newStringUtf8(Base64.decodeBase64(b._1))
        case None => ""
      }
      val htmlBody = bodies match {
        case Some(b) => StringUtils.newStringUtf8(Base64.decodeBase64(b._2))
        case None => ""
      }
      
//      println(s"############ textBody $textBody")
//      println(s"############ htmlBody $htmlBody")
    }
    
    "this" in {
      val json = """{
  "id":"148e99df7eea150d",
  "threadId":"148d32159ffa00ef",
  "labelIds":["CATEGORY_FORUMS","UNREAD","Label_10"],
  "snippet":"Hi Landon I had a problem very similar to your, where we have to process around 5 million relatively",
  "historyId":"1935213",
  "payload":{
    "partId":"",
    "mimeType":"text/plain",
    "filename":"",
    "headers":[{
      "name":"Delivered-To",
      "value":"sportano@gmail.com"
    },{
      "name":"Received",
      "value":"by 10.60.60.73 with SMTP id f9csp111358oer;        Tue, 7 Oct 2014 00:58:37 -0700 (PDT)"
    },{
      "name":"X-Received",
      "value":"by 10.70.25.193 with SMTP id e1mr1133884pdg.109.1412668716886;        Tue, 07 Oct 2014 00:58:36 -0700 (PDT)"
    },{
      "name":"Return-Path",
      "value":"<user-return-17515-sportano=gmail.com@spark.apache.org>"
    },{
      "name":"Received",
      "value":"from mail.apache.org (hermes.apache.org. [140.211.11.3])        by mx.google.com with SMTP id zs1si15025755pbc.225.2014.10.07.00.58.36        for <sportano@gmail.com>;        Tue, 07 Oct 2014 00:58:36 -0700 (PDT)"
    },{
      "name":"Received-SPF",
      "value":"pass (google.com: domain of user-return-17515-sportano=gmail.com@spark.apache.org designates 140.211.11.3 as permitted sender) client-ip=140.211.11.3;"
    },{
      "name":"Authentication-Results",
      "value":"mx.google.com;       spf=pass (google.com: domain of user-return-17515-sportano=gmail.com@spark.apache.org designates 140.211.11.3 as permitted sender) smtp.mail=user-return-17515-sportano=gmail.com@spark.apache.org"
    },{
      "name":"Received",
      "value":"(qmail 79414 invoked by uid 500); 7 Oct 2014 07:58:36 -0000"
    },{
      "name":"Mailing-List",
      "value":"contact user-help@spark.apache.org; run by ezmlm"
    },{
      "name":"Precedence",
      "value":"bulk"
    },{
      "name":"List-Help",
      "value":"<mailto:user-help@spark.apache.org>"
    },{
      "name":"List-Unsubscribe",
      "value":"<mailto:user-unsubscribe@spark.apache.org>"
    },{
      "name":"List-Post",
      "value":"<mailto:user@spark.apache.org>"
    },{
      "name":"List-Id",
      "value":"<user.spark.apache.org>"
    },{
      "name":"Delivered-To",
      "value":"mailing list <user@spark.apache.org>"
    },{
      "name":"Received",
      "value":"(qmail 79404 invoked by uid 99); 7 Oct 2014 07:58:36 -0000"
    },{
      "name":"Received",
      "value":"from nike.apache.org (HELO nike.apache.org) (192.87.106.230)    by apache.org (qpsmtpd/0.29) with ESMTP; Tue, 07 Oct 2014 07:58:36 +0000"
    },{
      "name":"X-ASF-Spam-Status",
      "value":"No, hits=2.0 required=5.0 tests=SPF_NEUTRAL,URI_HEX"
    },{
      "name":"X-Spam-Check-By",
      "value":"apache.org"
    },{
      "name":"Received-SPF",
      "value":"neutral (nike.apache.org: local policy)"
    },{
      "name":"Received",
      "value":"from [216.139.236.26] (HELO sam.nabble.com) (216.139.236.26)    by apache.org (qpsmtpd/0.29) with ESMTP; Tue, 07 Oct 2014 07:58:10 +0000"
    },{
      "name":"Received",
      "value":"from ben.nabble.com ([192.168.236.152]) by sam.nabble.com with esmtp (Exim 4.72) (envelope-from <deenar.toraskar@db.com>) id 1XbPer-0005TG-7M for user@spark.incubator.apache.org; Tue, 07 Oct 2014 00:58:09 -0700"
    },{
      "name":"Date",
      "value":"Tue, 7 Oct 2014 00:58:09 -0700 (PDT)"
    },{
      "name":"From",
      "value":"\"deenar.toraskar\" <deenar.toraskar@db.com>"
    },{
      "name":"To",
      "value":"user@spark.incubator.apache.org"
    },{
      "name":"Message-ID",
      "value":"<1412668689216-15835.post@n3.nabble.com>"
    },{
      "name":"In-Reply-To",
      "value":"<8FB91C6E-21AF-4073-9623-8631A0E6858C@gmail.com>"
    },{
      "name":"References",
      "value":"<CADH7zthf72x6TO6C=AyAsWdd=GHD8uapBiiFXgqhviPmz2DWsg@mail.gmail.com> <CAOhmDzeJquE271aecK+60Fpaxo+Br3c1AHLu9WVNfT6i45Xp9A@mail.gmail.com> <CADH7ztiB+0e=axxhM+Vojk-rgLoJdzK8wiku=ts+e7JvOfoxww@mail.gmail.com> <CAOhmDzeherBzuQAV-P6S8qiST5ivZhq9iOcNdqLST5rGurpyzg@mail.gmail.com> <8FB91C6E-21AF-4073-9623-8631A0E6858C@gmail.com>"
    },{
      "name":"Subject",
      "value":"Re: Strategies for reading large numbers of files"
    },{
      "name":"MIME-Version",
      "value":"1.0"
    },{
      "name":"Content-Type",
      "value":"text/plain; charset=us-ascii"
    },{
      "name":"Content-Transfer-Encoding",
      "value":"7bit"
    },{
      "name":"X-Virus-Checked",
      "value":"Checked by ClamAV on apache.org"
    }],
    "body":{
      "size":1449,
      "data":"SGkgTGFuZG9uIA0KDQpJIGhhZCBhIHByb2JsZW0gdmVyeSBzaW1pbGFyIHRvIHlvdXIsIHdoZXJlIHdlIGhhdmUgdG8gcHJvY2VzcyBhcm91bmQgNQ0KbWlsbGlvbiByZWxhdGl2ZWx5IHNtYWxsIGZpbGVzIG9uIE5GUy4gQWZ0ZXIgdHJ5aW5nIHZhcmlvdXMgb3B0aW9ucywgd2UgZGlkDQpzb21ldGhpbmcgc2ltaWxhciB0byB3aGF0IE1hdGVpIHN1Z2dlc3RlZC4NCg0KMSkgdGFrZSB0aGUgb3JpZ2luYWwgcGF0aCBhbmQgZmluZCB0aGUgc3ViZGlyZWN0b3JpZXMgdW5kZXIgdGhhdCBwYXRoIGFuZA0KdGhlbiBwYXJhbGxlbGl6ZSB0aGUgcmVzdWx0aW5nIGxpc3QuIHlvdSBjYW4gY29uZmlndXJlIHRoZSBkZXB0aCB5b3Ugd2FudCB0bw0KZ28gZG93biB0byBiZWZvcmUgc2VuZGluZyB0aGUgcGF0aHMgYWNyb3NzIHRoZSBjbHVzdGVyLg0KDQogIGRlZiBnZXRGaWxlTGlzdChzcmNEaXI6RmlsZSwgZGVwdGg6SW50KSA6IExpc3RbRmlsZV0gPSB7IA0KICAgIHZhciBsaXN0IDogTGlzdEJ1ZmZlcltGaWxlXSA9IG5ldyBMaXN0QnVmZmVyW0ZpbGVdKCkgDQogICAgaWYgKHNyY0Rpci5pc0RpcmVjdG9yeSgpKSB7IA0KICAgIHNyY0Rpci5saXN0RmlsZXMoKSAuZm9yZWFjaCgoZmlsZTogRmlsZSkgPT4gDQogICAgICAgaWYgKGZpbGUuaXNGaWxlKCkpIHsgDQogICAgICAgICAgbGlzdCArPShmaWxlKSANCiAgICAgICB9IGVsc2UgeyANCiAgICAgICAgICBpZiAoZGVwdGggPiAwICkgeyANCiAgICAgICAgICAgICBsaXN0ICsrPSBnZXRGaWxlTGlzdChmaWxlLCAoZGVwdGgtIDEgKSkgDQogICAgICAgICAgfSANCiAgIGVsc2UgaWYgKGRlcHRoIDwgMCkgew0KICAgICAgICBsaXN0ICsrPSBnZXRGaWxlTGlzdChmaWxlLCAoZGVwdGgpKSANCiAgICAgICAgICB9DQogICAgICAgZWxzZSB7IA0KICAgICAgICAgIGxpc3QgKz0gZmlsZSANCiAgICAgICB9IA0KICAgIH0pIA0KICAgIH0NCiAgICBlbHNlIHsNCiAgICAgICBsaXN0ICs9IHNyY0Rpcg0KICAgIH0NCiAgICBsaXN0IC50b0xpc3QgDQogIH0NCg0KDQoNCg0KDQotLQ0KVmlldyB0aGlzIG1lc3NhZ2UgaW4gY29udGV4dDogaHR0cDovL2FwYWNoZS1zcGFyay11c2VyLWxpc3QuMTAwMTU2MC5uMy5uYWJibGUuY29tL1N0cmF0ZWdpZXMtZm9yLXJlYWRpbmctbGFyZ2UtbnVtYmVycy1vZi1maWxlcy10cDE1NjQ0cDE1ODM1Lmh0bWwNClNlbnQgZnJvbSB0aGUgQXBhY2hlIFNwYXJrIFVzZXIgTGlzdCBtYWlsaW5nIGxpc3QgYXJjaGl2ZSBhdCBOYWJibGUuY29tLg0KDQotLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0NClRvIHVuc3Vic2NyaWJlLCBlLW1haWw6IHVzZXItdW5zdWJzY3JpYmVAc3BhcmsuYXBhY2hlLm9yZw0KRm9yIGFkZGl0aW9uYWwgY29tbWFuZHMsIGUtbWFpbDogdXNlci1oZWxwQHNwYXJrLmFwYWNoZS5vcmcNCg0K"
    }
  },
  "sizeEstimate":4358
}


 """
      @tailrec def findJsonObjects(str: String, json: List[JValue] = List(), currentIndex: Int = 0, startingIndex: Int = 0, openingBrackets: Int = 0, closingBrackets: Int = 0): List[JValue] = {
        str.slice(currentIndex, currentIndex + 1) match {
          case "{" if openingBrackets == 0 => findJsonObjects(str, json, currentIndex + 1, currentIndex, 1, 0)
          case "{" if openingBrackets > 0 => findJsonObjects(str, json, currentIndex + 1, startingIndex, openingBrackets + 1, closingBrackets)
          case "}" if closingBrackets < (openingBrackets - 1) => findJsonObjects(str, json, currentIndex + 1, startingIndex, openingBrackets, closingBrackets + 1)
          case "}" if closingBrackets == (openingBrackets - 1) => findJsonObjects(str, JsonParser.parse(str.slice(startingIndex, currentIndex + 1)) :: json, currentIndex + 1)
          case x if currentIndex == str.length => json
          case x  => findJsonObjects(str, json, currentIndex + 1, startingIndex, openingBrackets, closingBrackets)
        }
      }
      
      def findMessageMetaData(json: JValue) = {
    val payload = for {
      payload <- json.values.asInstanceOf[Map[String,Any]].get("payload")
    } yield payload
    
    val headers = for {
      payload <- payload
      headers <- payload.asInstanceOf[Map[String,Any]].get("headers")
    } yield headers
    
    val subject = for {
      headers <- headers
      subjectOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("Subject"))
      subject <- subjectOpt.get("value")
    } yield subject
    println(s"############## subject $subject")
    
    val to = (for {
      headers <- headers
      toOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("To"))
      to <- toOpt.get("value")
    } yield to) match {
      case Some(s) => Some(s)
      case None => {
        (for {
          headers <- headers
          toOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("Delivered-To"))
          to <- toOpt.get("value")
        } yield to)
      }
    }
    println(s"############## to $to")
    
    val from = for {
      headers <- headers
      fromOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("From"))
      from <- fromOpt.get("value")
    } yield from
    println(s"############## from $from")
    
    val threadId = for {
      threadId <- json.values.asInstanceOf[Map[String,Any]].get("threadId")
    } yield threadId
    println(s"############## threadId $threadId")
    
    val messageId = for {
      messageId <- json.values.asInstanceOf[Map[String,Any]].get("id")
    } yield messageId
    println(s"############## messageId $messageId")
    
    val cc = for {
      headers <- headers
      ccOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("Cc"))
      cc <- ccOpt.get("value")
    } yield cc
    println(s"############## cc $cc")
    
    val time = (for {
      headers <- headers
      timeOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("Received"))
      time <- timeOpt.get("value")
    } yield time) match {
      case Some(s) => Some(s)
      case None => {
        (for {
          headers <- headers
          toOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("X-Received"))
          to <- toOpt.get("value")
        } yield to)
      }
    }
    println(s"############## time $time")
 
    for {
      headers <- headers
      subjectOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("Subject"))
      subject <- subjectOpt.get("value")
      fromOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("From"))
      from <- fromOpt.get("value")
      messageId <- json.values.asInstanceOf[Map[String,Any]].get("id")
      threadId <- json.values.asInstanceOf[Map[String,Any]].get("threadId")
      timeOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("Received"))
      time <- timeOpt.get("value")
    } yield(Map("subject" -> subject, "to" -> to, "from" -> from, "threadId" -> threadId, "messageId" -> messageId, "cc" -> cc, "time" -> time))
  }
      
      def getBodies1(json: JValue): Option[Map[String,String]] = {
        (for {
          payload <- json.values.asInstanceOf[Map[String,Any]].get("payload")
          body <- payload.asInstanceOf[Map[String,Any]].get("body")
//          headers <- payload.asInstanceOf[Map[String,Any]].get("headers")
        } yield(body, payload)) match {
        case Some(m) => {
          (for {
            parts <- m._2.asInstanceOf[Map[String,Any]].get("parts")
            textMap <- parts.asInstanceOf[List[Map[String,Any]]].find(part => part.get("mimeType") == Some("text/plain"))
            text <- textMap.get("body").get.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
            htmlMap <- parts.asInstanceOf[List[Map[String,Any]]].find(part => part.get("mimeType") == Some("text/html"))
            html <- htmlMap.get("body").get.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
          } yield(text, html)) match {
            case Some(b) => Some(Map("text" -> StringUtils.newStringUtf8(Base64.decodeBase64(b._1)), "html" -> StringUtils.newStringUtf8(Base64.decodeBase64(b._2))))
            case None => None
          }
        }
        case None => None
        }
      }
      
      def getBodies2(json: JValue): Option[Map[String,String]] = {
        (for {
          payload <- json.values.asInstanceOf[Map[String,Any]].get("payload")
          parts1 <- payload.asInstanceOf[Map[String,Any]].get("parts")
          parts2Option = parts1.asInstanceOf[List[Map[String,Any]]].find(_.get("parts").isDefined).flatMap(_.get("parts"))
          parts2 <- parts2Option
        } yield(parts2.asInstanceOf[List[Map[String,Any]]])) match {
          case Some(np) => {
            (for {
              textMap <- np.find(_.get("mimeType") == Some("text/plain"))
              text <- textMap.get("body").get.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
              htmlMap <- np.find(_.get("mimeType") == Some("text/html"))
              html <- htmlMap.get("body").get.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
            } yield(text, html)) match {
              case Some(b) => Some(Map("textBody" -> StringUtils.newStringUtf8(Base64.decodeBase64(b._1)), "htmlBody" -> StringUtils.newStringUtf8(Base64.decodeBase64(b._2))))
              case None => None
            }
          }
          case None => None
        }
      }
      
      def getBodies3(json: JValue): Option[Map[String,String]] = {
        (for {
          payload <- json.values.asInstanceOf[Map[String,Any]].get("payload")
          body <- payload.asInstanceOf[Map[String,Any]].get("body")
          data <- body.asInstanceOf[Map[String,Any]].get("data")
        } yield(data.toString)) match {
          case Some(b) => Some(Map("textBody" -> StringUtils.newStringUtf8(Base64.decodeBase64(b))))
          case None => None
        }
      }
        
        def getMessageBodies(json: JValue): Option[Map[String,String]] = {
          getBodies1(json) match {
            case Some(b) => Some(b)
            case None => getBodies2(json) match {
              case Some(b) => Some(b)
              case None => getBodies3(json)
            }
          }
        }
        
        val results = findJsonObjects(json).map(j => {
//          println(s"################ ${findMessageMetaData(j)}")
//          val x = j.values.asInstanceOf[Map[String,Any]].get("id")
//          val x = getMessageBodies(j)
//          println(s"################ JSON 1 ${x}")
        })
    }
    
    "k" in {
//      def getRecipientInfo(recipients: String): List[(String,String)] = recipients.split(",").toList.map(u => u.split(" <").toList.map(c => c.replaceAll(">", "").trim)).map(list => (list(0), list(1)))
//      val cc = """Shaikh Riyaz <shaikh.r.a@gmail.com>, "user@spark.apache.org" <user@spark.apache.org>, Dibyendu Bhattacharya <dibyendu.bhattachary@gmail.com>"""
//      val users = cc.split(",").toList
//      val y = users.map(u => u.split(" <").toList.map(c => c.replaceAll(">", "").trim)).map(list => (list(0), list(1)))
      
//      println(s"######### users ${getRecipientInfo(cc)}")
    }

    "datetime parser" in {
//      val x = DateTime.parse("Wed, 1 Oct 2014 22:12:45 -0700 (PDT)")
//      val fmt = DateTimeFormat.forPattern("dddd, dd MMMM yyyy HH:mm:ss Z");
//      implicit val fmt = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z");
//      def findTime(time: String)(implicit fmt: DateTimeFormatter): Try[Long] = {
//        val sanitizedTime = time.split("\\(")
//        Try{DateTime.parse(sanitizedTime(0).trim, fmt).getMillis}
//      }
//      val x = findTime("Sun, 28 Sep 2014 17:48:39 -0700")
//      println(s"######## x $x")
//      val y = findTime("Thu, 02 Oct 2014 00:57:06 +0000 (UTC)")
//      println(s"######## y $y")
    }
  }
  
  "sending emails" should {
    "do something" in {
//      val email = Email("123", "someUserId", "4535335", "recipients", Some(Map("Stephen Portanova" -> "portanova@me.com", "Elizabeth Portanova" -> "elizabethportanova@gmail.com")), 234243l, "subject", Map("sportano@gmail.com" -> "sportano@gmail.com"), "cc","bcc","body", "emailBodyHtml")
      val email = Email("123", "someUserId", Some("4535335"), "recipients", Some(Map("Stephen Portanova" -> "portanova@me.com")), 234243l, "subject", Map("sportano@gmail.com" -> "sportano@gmail.com"), "cc","bcc","body", "emailBodyHtml", "msgId")
      def createRawMessage(email: Email): String = {
        val fmt = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z")

        val from = s"From: ${formatRawMessageRecipients(email.sender)}\n"
        val to = s"To: ${formatRawMessageRecipients(email.recipients.get)}\n"
        val subject = "Subject: " + email.subject + "\n"
        val date = "Date: " + fmt.print(new DateTime(email.ts)) + "\n\n"
        val message = email.textBody
        
        Base64.encodeBase64URLSafeString((from + to + subject + date + message).getBytes)
      }
      def formatRawMessageRecipients(recipients: Map[String,String]): String = {
        val str = recipients.foldLeft("")((acc, recipients) => {
          acc + " " + recipients._1 + " <" + recipients._2 + ">," 
        })
        str.slice(0, str.length - 1)
      }
      
      def createHeaders(email: Email): List[Map[String,String]] = {
        val formattedTo = email.recipients.get.foldLeft("")((acc, recipient) => {
          acc + " " + recipient._1 + " " + recipient._2 + ","
        }) match {
          case x => x.slice(0, x.length - 1)
        }
        val to = Map("name" -> "to", "value" -> formattedTo)

        val formattedFrom = email.sender.toList.headOption match {
          case Some(sender) => sender._1 + " " + sender._2
          case None => throw new IllegalArgumentException("this message has no sender"); ""
        }
        val from = Map("name" -> "from", "value" -> formattedFrom)
        
        val subject = Map("name" -> "subject", "value" -> email.subject)
        
        List(to, from, subject)
      }
      val headers = createHeaders(email)
//      println(s"@@@@@@@@@@@ headers $headers")
      implicit val formats = net.liftweb.json.DefaultFormats
      
      def createSendHTTPBody(email: Email): String = {
        val start = "--narmal_send\nContent-Type: application/json; charset=UTF-8\n\n"
        val end = "\n\n--narmal_send\nContent-Type: message/rfc822\n\n--narmal_send--"

        val rawEmail = createRawMessage(email)
        val headers = createHeaders(email)
        val payload = Map("headers" -> headers, "mimeType" -> "text/plain")
        val json = compact(render(decompose(Map("raw" -> rawEmail, "payload" -> payload))))
        
        start + json + end
      }
      val x = createSendHTTPBody(email)
//      println(s"########## body $x")
//      val message = "From: John Doe <sportano@gmail.com>\nTo: Mary Smith <sportano@gmail.com>, Stephen Portanova <portanova@me.com>\nSubject: Saying Hello\nDate: Fri, 21 Nov 1997 09:55:06 -0600\nMessage-ID: <sportano@gmail.com>\n\nThis is a message just to say hello. So, Hello."
        
//    var bytes: ByteArrayOutputStream = new ByteArrayOutputStream();
//    email.writeTo(bytes);
//    String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());

      val rawMessage = createRawMessage(email)
      
//      {
//    "name": "To",
//    "value": "\"dev@spark.apache.org\" \u003cdev@spark.apache.org\u003e, \"user@spark.apache.org\" \u003cuser@spark.apache.org\u003e, Xiangrui Meng \u003cmengxr@gmail.com\u003e, DB Tsai \u003cdbtsai@dbtsai.com\u003e"
//   }
      
      val json = s"""{    
       "raw": "$rawMessage",  
       "payload": { 
         "headers": [ 
           { "name": "to", "value": "portanova@me.com"}, 
           { "name": "from","value": "sportano@gmail.com" }, 
           { "name": "subject", "value": "Saying Hello"  } 
         ],
         "mimeType": "text/plain" 
       }
      }"""
       
//     println(s"########## json $json")

//      val email1 = Email("123l", "someUserId", Some("4535335l"), "recipients", Some(Map("Stephen Portanova" -> "sportano@gmail.com")), 234243l, "subject", Map("Stephen Portanova" -> "sportano@gmail.com"), "cc","bcc","body", "emailBodyHtml", "msgId")
//      SendEmail.send(email1, "100030981325891290860", "ya29.lgBv-SEWcPypOGICGQPvciXqIwwAP8n0w2eHRZj-oQlXS_8Y7LWOl5ts")
       implicit val httpClient = new ApacheHttpClient       
       val url = new URL("https://www.googleapis.com/gmail/v1/users/100030981325891290860/labels?userId=me")
//       val req = GET(url).addHeaders(("Authorization", "Bearer ya29.oQCbSEm5zSKffrZxOW9CoOCYdY225WpmHIwtCtJ4fnK8kYHp843Ep3VI")).apply
//       req.map(res => {
//         val x = res.toJValue.values
//         println(s"@@@@@@@@@@@@ x $x")
//       })
      "" == ""
//       https://www.googleapis.com/upload/gmail/v1/users/userId/messages/send
    }
  }
}
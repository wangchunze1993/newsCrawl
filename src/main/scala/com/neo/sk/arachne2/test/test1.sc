import java.util.Locale
import java.util.regex.Pattern
import com.github.nscala_time.time.Imports.DateTimeFormat
import com.neo.sk.arachne2.jobs.smthlogin.SmthloginArticle
import com.neo.sk.arachne2.utils.http.HttpClientUtil


val t = " SYSRP (浮生无处闲)".trim.split("\\(")(0).trim
println("="+t+"=")
/*val article = new SmthloginArticle()
val content = new HttpClientUtil().getResponseContent("http://www.newsmth.net/bbscon.php?bid=953&id=15584875","gb2312")
val contRegex = "conWriter\\(\\d+,\\s*'(.*)',\\s*\\d+,\\s*(\\d+),\\s*(\\d+),\\s*(\\d+),\\s*'.*',\\s*(\\d+),\\s*\\d+,'(.*)'\\);"
val contMatch = Pattern.compile(contRegex,Pattern.MULTILINE).matcher(content)
val attRegex = "prints\\('发信人:(.*), 信区:(.*)\\\\n标  题:(.*)\\\\n发信站: 水木社区 \\((.*)\\), 站内\\\\n\\\\n(.*)\\\\n(【.*)\\[FROM:(.*)\\]\\\\r(.*);o\\.h\\(0\\);o\\.t\\(\\);"
val attMatch = Pattern.compile(attRegex,Pattern.MULTILINE).matcher(content)
if(contMatch.find() && attMatch.find()){
  article.url = "url"
  article.id = "1111"
  article.boardId = "222"
  article.boardName = "333"
  article.firstId = contMatch.group(3)
  article.reId = contMatch.group(4)
  article.authorId = attMatch.group(1)
  article.title = attMatch.group(3)
  article.pureContent = attMatch.group(5)
  article.allContent = article.pureContent + """\n""" + attMatch.group(6)
  article.postTimeMs = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss yyyy").withLocale(Locale.ENGLISH).parseDateTime(attMatch.group(4)).getMillis.toString
  article.fromIp = attMatch.group(7)
  println(article.toString)
  println("pure=" + article.pureContent)
}*/
/*//http://www.newsmth.net/bbsdoc.php?board=NewExpress
//http://www.newsmth.net/bbsdoc.php?board=NewExpress&ftype=0&page=1447
//http://www.newsmth.net/bbscon.php?bid=1348&id=6651911&board=NewExpress
val url1 = "http://www.newsmth.net/bbsdoc.php?board=NewExpress"
val url2 = "http://www.newsmth.net/bbsdoc.php?board=NewExpress&ftype=0&page=1447"
val url3 = "http://www.newsmth.net/bbscon.php?bid=1348&id=6651911&board=NewExpress"
val boardRegex = "http://www\\.newsmth\\.net/bbsdoc\\.php\\?board=(.*)".r
val articleRegex = "http://www\\.newsmth\\.net/bbscon\\.php\\?bid=(\\d+)&id=(\\d+)&board=(.*)".r
url3 match {
  case articleRegex(bid,id,boardName) =>{
    println(s"artile and bid=$bid,id=$id,boardName=$boardName")
  }
  case boardRegex(postfix) =>{
    val boardName =  if (postfix.contains("page=")) postfix.split("&")(0) else postfix
    println(s"board and boardName = $boardName")
  }
  case unknow@_ =>{
    println(s"can not find regex in $unknow")
  }
}*/
/*val date = "Mon Oct 26 11:08:32 2015".split(" ")
val month = 10
val dateStr = date(4) + "-" + month + "-" + date(2) + " " + date(3)
val d = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(dateStr)
println("==="+ d.getMillis)*/
/*val url1 = "http://www.cnblogs.com/muluobo123/archive/2015/10/17/4887855.html"
val url2 = "http://www.cnblogs.com/cate/4/1"
val url3 = "www.baidu.com"
val cateRegex = "(http://www.cnblogs.com/cate/)(\\d+)/(.*)".r
val detailRegex = "(.*)(\\d+)(\\.html)".r
url3 match {
  case cateRegex(cateBaseUrl,cate,page) =>{
   println("is cate url .........")
  }
  case detailRegex(prefix,articleId,postfix)=>{
    println("is article detail url ......")
  }
  case u@_=> println(s"unknow url regex : $u")
}*/
/*
val url1 = "http://m.byr.cn/board/Java/0?p=1"
val url2 = "http://m.byr.cn/article/Java/single/44814/0"
val boardRegex  = "http://m.byr.cn/board/(.*)/0\\?p=(\\d+)".r
val detailRegex = "http://m.byr.cn/article/(.*)/single/(\\d+)/0".r
url2 match {
  case boardRegex(boardName,page) =>{
   println("board = ")
  }
  case detailRegex(boardName,id) =>{
println("detail = ")
  }
  case unknow@_ => println("unknow..")
}*/
/*val text = "【 在 crazy0602 的大作中提到: 】\n: 靠。才看到..十一的时候刚好在青海。错过了带你飞得机会\n: 发自「贵邮」\nO(∩_∩)O谢谢啊\n--\nFROM 114.241.14.* [北京市 联通ADSL]"
val ipRegex = "--\nFROM ((\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d*))(.*)"
val pattern =Pattern.compile(ipRegex,Pattern.MULTILINE)
val ipMatch = pattern.matcher(text)
if(ipMatch.find()){
  println("==="+ipMatch.group(1))
  val text1 = ipMatch.replaceAll("")
  println(s"\n==$text1")
  val replaceRegex = "^:(.*)\n"
  val replacePattern = Pattern.compile(replaceRegex,Pattern.MULTILINE)
  val replaceMatch =replacePattern.matcher(text1)
  if(replaceMatch.find()){
    val text2 = replaceMatch.replaceAll("")
    println(s"test2=$text2")
    val regex2 = "^【(.*)\n"
    val mat2 = Pattern.compile(regex2,Pattern.MULTILINE).matcher(text2)
    if(mat2.find){
      val text3 = mat2.replaceAll("")
      println(s"text3==$text3")
    }
  }
}*/
/*val text = "【 在 crazy0602 的大作中提到: 】\n: 靠。才看到..十一的时候刚好在青海。错过了带你飞得机会\n: 发自「贵邮」\nO(∩_∩)O谢谢啊\n--\nFROM 114.241.14.* [北京市 联通ADSL]"
val replaceRegex = "^:(.*)\n"
val replacePattern = Pattern.compile(replaceRegex,Pattern.MULTILINE)
val replaceMatch =replacePattern.matcher(text)
if(replaceMatch.find()){
  val text1 = replaceMatch.replaceAll("")
  println(s"result===$text1")
  val regex2 = "^【(.*)\n"
  val mat2 = Pattern.compile(regex2,Pattern.MULTILINE).matcher(text1)
  if(mat2.find){
    val text2 = mat2.replaceAll("").replaceAll("--\n","").replaceAll("^FROM (.*)\n","")
    println(s"text2==$text2")
  }
}*/
/*var content = "【 在 crazy0602 的大作中提到: 】\n: 靠。才看到..十一的时候刚好在青海。错过了带你飞得机会\n: 发自「贵邮」\nO(∩_∩)O谢谢啊\n--\nFROM 114.241.14.* [北京市 联通ADSL]"
val ipRegex = "FROM ((\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d*))(.*)"
val pattern =Pattern.compile(ipRegex,Pattern.MULTILINE)
content = content.replaceAll("--\n","")
val ipMatch = pattern.matcher(content)
if(ipMatch.find()){
  println("ip========"+ipMatch.group(1))
  content = ipMatch.replaceAll("")
}
val startRegex = "^:(.*)\n"
val startRegexPattern = Pattern.compile(startRegex,Pattern.MULTILINE)
val startRegexMatch =startRegexPattern.matcher(content)
if(startRegexMatch.find){
  content = startRegexMatch.replaceAll("")
}
val sayRegex = "^【(.*)\n"
val sayMatch = Pattern.compile(sayRegex,Pattern.MULTILINE).matcher(content)
if(sayMatch.find){
  content = sayMatch.replaceAll("")
}
println(s"content=\n$content")*/
/*
val postIdText = "/article/Java/single/44829/0"
val postIdRegex = "(.*)/single/(\\d+)(.*)".r
postIdText match {
  case postIdRegex(prefix,postId, postfix) => {
   println(postId)
  }
}*/
/*//http://www.newsmth.net/bbsdoc.php?board=NewExpress
//http://www.newsmth.net/bbsdoc.php?board=NewExpress&ftype=0&page=1447
//http://www.newsmth.net/bbscon.php?bid=1348&id=6651911&board=NewExpress
val url = "http://www.newsmth.net/bbsdoc.php?board=NewExpress"
val url2 = "http://www.newsmth.net/bbsdoc.php?board=NewExpress&ftype=0&page=1447"
val url3 = "http://www.newsmth.net/bbscon.php?bid=1348&id=6651911&board=NewExpress"
val boardRegex = "http://www\\.newsmth\\.net/bbsdoc\\.php\\?board=(.*)".r
val articleRegex = "http://www\\.newsmth\\.net/bbscon\\.php\\?bid=(\\d+)&id=(\\d+)&board=(.*)".r
url3 match {
  case articleRegex(bid,id,boardName) =>{
  println(s"$bid+$id+$boardName")
  }
  case boardRegex(postfix) =>{
    val boardName =  if (postfix.contains("page=")) postfix.split("&")(0) else postfix
println(s"$boardName")
  }
  case unknow@_ =>{
    println(s"can not find regex in $url")
  }
}*/


/*
val content = """<script type="text/javascript"><!--
                var o = new conWriter(0, 'NewExpress', 1348, 6669083, 6668361, 6668361, '<a href=\"bbssfav.php?act=choose&title=Re%3A%20%D2%BB%D6%B1%CE%DE%B7%A8%C0%ED%BD%E2%D2%FD%C1%A6%B3%A1&url=bbscon.php%3Fbid%3D1348%26id%3D6669083&type=0\">百宝箱</a>', 56534, 0,'Re: 一直无法理解引力场');
                o.h(1);
                att = new attWriter(1348,6669083,0,56534,0);
                prints('发信人: zidantou (子弹头), 信区: NewExpress\n标  题: Re: 一直无法理解引力场\n发信站: 水木社区 (Mon Oct 26 11:08:32 2015), 站内\n\n问题在于是什么\n而不是为什么\n\n【 在 rallumer 的大作中提到: 】\n: 为什么有质量就有引力？黑洞吞噬恒星\n--\n\n\r[m\r[35m※ 来源:·水木社区 http:\/\/m.newsmth.net·[FROM: 123.123.192.*]\r[m\n');o.h(0);o.t();
                //-->
                </script>"""


val contRegex = "conWriter\\(\\d+,\\s*'(.*)',\\s*\\d+,\\s*(\\d+),\\s*(\\d+),\\s*(\\d+),\\s*'.*',\\s*(\\d+),\\s*\\d+,'(.*)'\\);"
val contMatch = Pattern.compile(contRegex,Pattern.MULTILINE).matcher(content)
val attRegex = "prints\\('发信人:(.*), 信区:(.*)\\\\n标  题:(.*)\\\\n发信站: 水木社区 \\((.*)\\), 站内\\\\n\\\\n(.*)\\\\n\\\\n【(.*)·\\[FROM:(.*)\\](.*);o\\.h\\(0\\);o\\.t\\(\\);"
val attMatch = Pattern.compile(attRegex,Pattern.MULTILINE).matcher(content)

if(contMatch.find){
  println("="+contMatch.group(0))
}

if(attMatch.find){
  println("==" + attMatch.group(0))
}*/

val href = "https://bbs.sjtu.edu.cn/bbscon,board,Accounting,file,M.1435742509.A.html"
//val board = url.substring(url.indexOf("board")+1,url.indexOf("html")-1)
//var boardName = ""
//if(board.contains("page")){
//   boardName = board.substring(board.indexOf(",")+1,board.indexOf("page")-1)
//}else{
//   boardName = board.substring(board.indexOf(",")+1)
//}
val sub = href.substring(href.indexOf("board"),href.indexOf("file")-1)
val time = sub.substring(sub.indexOf(",")+1)


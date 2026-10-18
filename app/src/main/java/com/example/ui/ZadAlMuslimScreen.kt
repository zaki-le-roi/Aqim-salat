package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

private const val ZAD_CATALOG = "https://raw.githubusercontent.com/zaki-le-roi/Aqim-salat/main/library/catalog.json"
private data class ZadBook(val id:String,val title:String,val author:String,val description:String,val pages:Int,val url:String,val source:String)
private data class ZadCategory(val id:String,val title:String,val description:String,val books:List<ZadBook>)
private fun parseZad(raw:String):List<ZadCategory> {
    val arr=JSONObject(raw).optJSONArray("categories")?:JSONArray()
    return buildList {
        for(i in 0 until arr.length()) {
            val c=arr.optJSONObject(i)?:continue
            val ba=c.optJSONArray("books")?:JSONArray()
            val books=buildList {
                for(j in 0 until ba.length()) {
                    val b=ba.optJSONObject(j)?:continue
                    val id=b.optString("id");val title=b.optString("title");val url=b.optString("downloadUrl")
                    if(id.isNotBlank()&&title.isNotBlank()&&url.startsWith("https://"))
                        add(ZadBook(id,title,b.optString("author","غير محدد"),b.optString("description",""),b.optInt("pages",0),url,b.optString("sourceUrl",url)))
                }
            }
            if(c.optString("id").isNotBlank()&&c.optString("title").isNotBlank())
                add(ZadCategory(c.getString("id"),c.getString("title"),c.optString("description",""),books))
        }
    }
}
private fun zadFile(ctx:Context,b:ZadBook)=File(File(ctx.filesDir,"zad_al_muslim"),"${b.id}.pdf")
private suspend fun zadCatalog(ctx:Context,remote:Boolean):List<ZadCategory> = withContext(Dispatchers.IO) {
    val raw=if(remote) {
        val con=URL(ZAD_CATALOG).openConnection() as HttpURLConnection
        try { con.connectTimeout=9000;con.readTimeout=15000;con.inputStream.bufferedReader(Charsets.UTF_8).use{it.readText()} }
        finally { con.disconnect() }
    } else ctx.assets.open("zad_al_muslim_catalog.json").bufferedReader(Charsets.UTF_8).use{it.readText()}
    parseZad(raw)
}


@Composable
private fun ZadBookCover(title:String, author:String, bookId:String, modifier:Modifier=Modifier) {
    val palette = when {
        bookId.contains("prayer") || title.contains("الصلاة") -> listOf(Color(0xFF174A3B), Color(0xFF0B2C25))
        bookId.contains("aqidah") || title.contains("التوحيد") || title.contains("العقيدة") -> listOf(Color(0xFF294A73), Color(0xFF142B48))
        bookId.contains("seerah") || title.contains("الرحيق") || title.contains("السيرة") -> listOf(Color(0xFF7C4934), Color(0xFF45291F))
        bookId.contains("hadith") || title.contains("رياض") || title.contains("شرح") -> listOf(Color(0xFF725329), Color(0xFF3E2C18))
        else -> listOf(Color(0xFF3C6650), Color(0xFF193A2D))
    }
    Box(modifier.clip(RoundedCornerShape(10.dp)).background(Brush.verticalGradient(palette)).border(1.dp,Color(0xFFD6B66A).copy(alpha=.75f),RoundedCornerShape(10.dp)),contentAlignment=Alignment.Center) {
        Column(Modifier.fillMaxSize().padding(6.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceBetween) {
            Icon(Icons.Filled.AutoStories,null,tint=Color(0xFFE7CA80),modifier=Modifier.size(25.dp))
            Text(title,color=Color.White,fontSize=10.sp,fontWeight=FontWeight.Bold,lineHeight=13.sp,maxLines=4,overflow=TextOverflow.Ellipsis,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
            Text(author,color=Color(0xFFE7D9B0),fontSize=7.sp,maxLines=2,overflow=TextOverflow.Ellipsis,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZadAlMuslimScreen(onBack:()->Unit,modifier:Modifier=Modifier) {
    val ctx=LocalContext.current
    val scope=rememberCoroutineScope()
    var categories by remember { mutableStateOf(emptyList<ZadCategory>()) }
    var category by remember { mutableStateOf<ZadCategory?>(null) }
    var book by remember { mutableStateOf<ZadBook?>(null) }
    var reader by remember { mutableStateOf<ZadBook?>(null) }
    var search by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("جارٍ تجهيز المكتبة...") }
    var downloading by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableIntStateOf(0) }
    var page by remember { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    fun downloaded(b:ZadBook):Boolean { refresh; return zadFile(ctx,b).let{it.isFile&&it.length()>100} }

    LaunchedEffect(Unit) {
        try { categories=zadCatalog(ctx,false);message="" } catch(_:Exception){message="تعذر فتح فهرس الكتب المحفوظ."}
        try { val online=zadCatalog(ctx,true);if(online.isNotEmpty())categories=online;message="" } catch(_:Exception){if(categories.isEmpty())message="تعذر تحميل الفهرس؛ تحقق من الإنترنت."}
    }
    LaunchedEffect(reader?.id,page) {
        val b=reader?:return@LaunchedEffect
        try {
            val result=withContext(Dispatchers.IO) {
                ParcelFileDescriptor.open(zadFile(ctx,b),ParcelFileDescriptor.MODE_READ_ONLY).use{fd->
                    PdfRenderer(fd).use{renderer->
                        renderer.openPage(page.coerceIn(0,renderer.pageCount-1)).use{p->
                            val w=1100;val h=(p.height.toFloat()*w/p.width).toInt().coerceAtLeast(1)
                            Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888).also{p.render(it,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)} to renderer.pageCount
                        }
                    }
                }
            }
            bitmap=result.first;pageCount=result.second;page=page.coerceIn(0,result.second-1)
            ctx.getSharedPreferences("zad_al_muslim_reader",Context.MODE_PRIVATE).edit().putInt("page_${b.id}",page).apply()
        } catch(_:Exception){bitmap=null;message="تعذر فتح ملف PDF. أعد تنزيل الكتاب."}
    }
    val title=when{reader!=null->reader!!.title;book!=null->book!!.title;category!=null->category!!.title;else->"زاد المسلم"}
    Scaffold(modifier=modifier.fillMaxSize(),topBar={TopAppBar(title={Text(title,maxLines=1,overflow=TextOverflow.Ellipsis,fontWeight=FontWeight.Bold)},navigationIcon={
        IconButton(onClick={when{reader!=null->{reader=null;bitmap=null};book!=null->book=null;category!=null->category=null;else->onBack()}}){Icon(Icons.Filled.ArrowBack,"رجوع")}
    },actions={if(reader==null&&book==null&&category==null)IconButton(onClick={scope.launch{try{categories=zadCatalog(ctx,true);message=""}catch(_:Exception){message="تعذر تحديث الفهرس."}}}){Icon(Icons.Filled.Refresh,"تحديث")}})}) { padding ->
        when {
            reader!=null -> Column(Modifier.fillMaxSize().padding(padding).padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                if(bitmap==null){CircularProgressIndicator();Text("جارٍ فتح الصفحة...")}else Image(bitmap!!.asImageBitmap(),"صفحة من الكتاب",Modifier.weight(1f).fillMaxWidth())
                Row(Modifier.fillMaxWidth().padding(8.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                    OutlinedButton(onClick={if(page>0)page--},enabled=page>0){Text("السابقة")}
                    Text("${page+1} / $pageCount")
                    Button(onClick={if(page<pageCount-1)page++},enabled=pageCount>0&&page<pageCount-1){Text("التالية")}
                }
            }
            book!=null -> {
                val b=book!!
                Column(Modifier.fillMaxSize().padding(padding).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    Card { Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                        ZadBookCover(b.title,b.author,b.id,Modifier.width(118.dp).height(164.dp).align(Alignment.CenterHorizontally))
                        Text(b.title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
                        Text("المؤلف: ${b.author}")
                        Text(b.description)
                        if(b.pages>0)Text("عدد الصفحات: ${b.pages}")
                    }}
                    if(downloaded(b)) {
                        Button(onClick={page=ctx.getSharedPreferences("zad_al_muslim_reader",Context.MODE_PRIVATE).getInt("page_${b.id}",0);reader=b;message=""},modifier=Modifier.fillMaxWidth()){Icon(Icons.Filled.MenuBook,null);Spacer(Modifier.width(8.dp));Text("اقرأ دون إنترنت")}
                        OutlinedButton(onClick={zadFile(ctx,b).delete();refresh++;message="حُذفت النسخة من الهاتف."},modifier=Modifier.fillMaxWidth()){Icon(Icons.Filled.Delete,null);Spacer(Modifier.width(8.dp));Text("حذف النسخة")}
                    } else {
                        Button(enabled=downloading==null,onClick={
                            downloading=b.id;progress=0;message=""
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) {
                                        val dir=File(ctx.filesDir,"zad_al_muslim").apply{mkdirs()}
                                        val tmp=File(dir,"${b.id}.pdf.part")
                                        val candidates=listOf(b.url,b.source).filter{it.startsWith("https://")}.distinct()
                                        var lastFailure:Exception?=null
                                        var validPdf=false
                                        for(candidate in candidates) {
                                            tmp.delete()
                                            try {
                                                val con=URL(candidate).openConnection() as HttpURLConnection
                                                try {
                                                    con.connectTimeout=20000
                                                    con.readTimeout=60000
                                                    con.instanceFollowRedirects=true
                                                    con.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36")
                                                    con.setRequestProperty("Accept","application/pdf,application/octet-stream,*/*")
                                                    con.setRequestProperty("Referer","https://shamela.org/")
                                                    con.connect()
                                                    val code=con.responseCode
                                                    if(code !in 200..299) throw IllegalStateException("HTTP $code")
                                                    val total=con.contentLengthLong
                                                    con.inputStream.use{input->FileOutputStream(tmp).use{out->
                                                        val buf=ByteArray(32768);var got=0L
                                                        while(true) {
                                                            val n=input.read(buf);if(n<0)break;if(n==0)continue
                                                            out.write(buf,0,n);got+=n
                                                            if(total>0)withContext(Dispatchers.Main){progress=((got*100/total).toInt()).coerceIn(0,99)}
                                                        }
                                                    }}
                                                } finally { con.disconnect() }
                                                val head=ByteArray(5)
                                                val headerCount=tmp.inputStream().use{it.read(head)}
                                                if(headerCount<5||String(head,Charsets.US_ASCII)!="%PDF-") throw IllegalStateException("الرابط لا يعيد ملف PDF")
                                                validPdf=true
                                                break
                                            } catch(e:Exception) {
                                                lastFailure=e
                                                tmp.delete()
                                            }
                                        }
                                        if(!validPdf) throw IllegalStateException(lastFailure?.message ?: "فشل الاتصال بمصدر الكتاب")
                                        val target=zadFile(ctx,b);target.delete()
                                        if(!tmp.renameTo(target)){tmp.copyTo(target,true);tmp.delete()}
                                    }
                                    refresh++;progress=100;message="اكتمل التنزيل؛ أصبح الكتاب متاحًا دون إنترنت."
                                }catch(e:Exception){message="تعذر التنزيل: "+(e.message?.take(100) ?: "تحقق من اتصال الإنترنت أو رابط الكتاب") }
                                finally{downloading=null}
                            }
                        },modifier=Modifier.fillMaxWidth()){Icon(Icons.Filled.CloudDownload,null);Spacer(Modifier.width(8.dp));Text(if(downloading==b.id)"جارٍ التنزيل $progress%" else "تنزيل الكتاب")}
                        if(downloading==b.id)LinearProgressIndicator(progress={progress/100f},modifier=Modifier.fillMaxWidth())
                    }
                    if(message.isNotBlank())Text(message,color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.bodySmall)
                }
            }
            category!=null -> {
                val c=categories.firstOrNull{it.id==category!!.id}?:category!!
                LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                    item{Text(c.description,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("${c.books.size} كتاب",color=MaterialTheme.colorScheme.primary)}
                    items(c.books, key = { it.id }){b->Card(Modifier.fillMaxWidth().clickable{book=b},shape=RoundedCornerShape(16.dp)){Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){ZadBookCover(b.title,b.author,b.id,Modifier.width(66.dp).height(92.dp));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(b.title,fontWeight=FontWeight.Bold);Text(b.author,style=MaterialTheme.typography.bodySmall);Text(if(downloaded(b))"محفوظ دون إنترنت" else "متاح للتنزيل",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)};Icon(if(downloaded(b))Icons.Filled.CheckCircle else Icons.Filled.CloudDownload,null)}}}
                }
            }
            else -> {
                val filtered=categories.filter{search.isBlank()||it.title.contains(search,true)||it.books.any{b->b.title.contains(search,true)||b.author.contains(search,true)}}
                LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(16.dp,12.dp,16.dp,90.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                    item{Card(shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFF153D30))){Column(Modifier.fillMaxWidth().padding(18.dp)){Text("زاد المسلم",color=Color(0xFFD4AF37),fontSize=22.sp,fontWeight=FontWeight.ExtraBold);Text("مكتبتك الإسلامية للعلم والمعرفة",color=Color.White);Text("نزّل الكتاب مرة واقرأه لاحقًا دون إنترنت.",color=Color(0xFFE7D9B0))}}}
                    item{OutlinedTextField(search,{search=it},modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text("ابحث عن تخصص أو كتاب أو مؤلف")},leadingIcon={Icon(Icons.Filled.Search,null)})}
                    if(message.isNotBlank())item{Text(message,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                    items(filtered, key = { it.id }){c->Card(Modifier.fillMaxWidth().clickable{category=c},shape=RoundedCornerShape(16.dp)){Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Filled.LibraryBooks,null,tint=Color(0xFF205A42),modifier=Modifier.size(36.dp));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(c.title,fontWeight=FontWeight.Bold);Text(c.description,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("${c.books.size} كتاب",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)};Icon(Icons.Filled.ArrowBack,null)}}}
                }
            }
        }
    }
}

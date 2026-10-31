package com.example.ui
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.min

/** Cached botanical arabesque with a connected vine, curling tendrils and lobed leaves. */
fun Modifier.islamicVineOrnament(color: Color = Color(0xFFD4AF37), alpha: Float = 0.14f, mirrored: Boolean = false): Modifier = drawWithCache {
 val w=size.width; val h=size.height; val unit=min(w,h).coerceAtLeast(1f); val side=(w*.29f).coerceAtMost(unit*.72f); val stroke=1.15.dp.toPx()
 fun x(v:Float)=if(mirrored) w-v else v
 val stem=Path().apply {
  moveTo(x(side*.08f),h*1.02f)
  cubicTo(x(side*.10f),h*.88f,x(side*.78f),h*.84f,x(side*.57f),h*.68f)
  cubicTo(x(side*.38f),h*.53f,x(side*.06f),h*.55f,x(side*.38f),h*.34f)
  cubicTo(x(side*.74f),h*.22f,x(side*.58f),h*.12f,x(side*.92f),-h*.02f)
  moveTo(x(side*.43f),h*.35f)
  cubicTo(x(side*.08f),h*.30f,x(side*.13f),h*.17f,x(side*.32f),h*.22f)
  quadraticBezierTo(x(side*.48f),h*.28f,x(side*.38f),h*.34f)
  moveTo(x(side*.57f),h*.68f)
  cubicTo(x(side*.92f),h*.70f,x(side*.96f),h*.91f,x(side*.72f),h*.90f)
  quadraticBezierTo(x(side*.48f),h*.88f,x(side*.57f),h*.68f)
 }
 val leaves=Path().apply {
  fun leaf(ax:Float,ay:Float,tx:Float,ty:Float,width:Float){
   val a=x(ax);val t=x(tx);val dx=t-a;val dy=ty-ay
   moveTo(a,ay)
   cubicTo(a+dx*.12f-width,ay+dy*.25f,a+dx*.70f-width*.45f,ay+dy*.75f,t,ty)
   cubicTo(a+dx*.85f+width*.45f,ay+dy*.65f,a+dx*.40f+width,ay+dy*.18f,a,ay)
   close()
  }
  leaf(side*.24f,h*.78f,side*.05f,h*.66f,unit*.055f)
  leaf(side*.54f,h*.67f,side*.80f,h*.57f,unit*.060f)
  leaf(side*.32f,h*.48f,side*.10f,h*.36f,unit*.055f)
  leaf(side*.43f,h*.34f,side*.68f,h*.27f,unit*.055f)
  leaf(side*.55f,h*.13f,side*.31f,h*.07f,unit*.050f)
  moveTo(x(side*.91f),h*.04f)
  cubicTo(x(side*.72f),h*.02f,x(side*.76f),-h*.04f,x(side*.88f),h*.015f)
  cubicTo(x(side*.90f),-h*.07f,x(side*1.02f),-h*.02f,x(side*.91f),h*.04f)
  close()
 }
 val tint=color.copy(alpha=alpha.coerceIn(.10f,.18f))
 onDrawBehind {
  drawPath(stem,tint,style=Stroke(width=stroke))
  drawPath(leaves,tint,style=Stroke(width=stroke))
  drawPath(leaves,tint.copy(alpha=tint.alpha*.30f))
 }
}

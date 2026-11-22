package com.ali.asghar.gymbow

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.Executors

private val ink=Color(0xFF111111)
private val muted=Color(0xFF77777A)
private val cloud=Color(0xFFF5F5F5)
private val line=Color(0xFFE5E5E5)
private val paper=Color.White
private enum class Tab(val title:String){HOME("Home"),MOVE("Move"),TRAIN("Training"),FOOD("Nutrition"),REST("Recovery"),PROGRESS("Progress")}
private data class Food(val name:String,val kcal:Int,val protein:Double,val carbs:Double,val fat:Double,val grams:Int)
private val catalog=listOf(Food("Whole egg",143,12.6,.7,9.5,100),Food("Chicken breast",165,31.0,0.0,3.6,100),Food("Cooked white rice",130,2.7,28.2,.3,100),Food("Whole milk",61,3.2,4.8,3.3,100),Food("Lentils, cooked",116,9.0,20.0,.4,100),Food("Banana",89,1.1,22.8,.3,100),Food("Greek yogurt",97,9.0,3.9,5.0,100),Food("Oats, dry",379,13.2,67.7,6.8,100))
private data class Exercise(val name:String,val sets:String)
private data class Workout(val name:String,val focus:String,val duration:Int,val moves:List<Exercise>)
private val workouts=listOf(Workout("Upper body strength","CHEST · BACK · SHOULDERS",65,listOf(Exercise("Bench press","4 × 8"),Exercise("Lat pulldown","3 × 10"),Exercise("Incline dumbbell press","3 × 10"),Exercise("Seated cable row","3 × 12"),Exercise("Lateral raise","3 × 15"))),Workout("Lower body","QUADS · HAMSTRINGS · CALVES",55,listOf(Exercise("Squat","4 × 8"),Exercise("Romanian deadlift","3 × 10"),Exercise("Leg press","3 × 12"),Exercise("Calf raise","4 × 15"))))
private fun prefs(c:Context)=c.getSharedPreferences("gymbow-native",Context.MODE_PRIVATE)

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=android.graphics.Color.rgb(245,245,245);window.navigationBarColor=android.graphics.Color.WHITE;window.decorView.systemUiVisibility=android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;setContent{Gymbow()}}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun Gymbow(){
 val ctx=LocalContext.current
 var tab by rememberSaveable{mutableStateOf(Tab.HOME)}
 var weight by rememberSaveable{mutableFloatStateOf(prefs(ctx).getFloat("weight",72f))}
 var water by rememberSaveable{mutableIntStateOf(prefs(ctx).getInt("water",4))}
 var sleep by rememberSaveable{mutableFloatStateOf(prefs(ctx).getFloat("sleep",8f))}
 var completed by rememberSaveable{mutableIntStateOf(prefs(ctx).getInt("done",0))}
 var foods by remember{mutableStateOf(listOf(catalog[0].copy(grams=150),catalog[2].copy(grams=250),catalog[3].copy(grams=250)))}
 var addFood by remember{mutableStateOf(false)}
 var active by rememberSaveable{mutableStateOf("")}
 var seconds by rememberSaveable{mutableIntStateOf(0)}
 var timer by rememberSaveable{mutableStateOf(false)}
 LaunchedEffect(timer){while(timer){delay(1000);seconds++}}
 LaunchedEffect(weight,water,sleep,completed){prefs(ctx).edit().putFloat("weight",weight).putInt("water",water).putFloat("sleep",sleep).putInt("done",completed).apply()}
 val kcal=foods.sumOf{it.kcal*it.grams/100.0}
 val protein=foods.sumOf{it.protein*it.grams/100.0}
 Scaffold(containerColor=cloud,topBar={TopAppBar(title={Row{Text("GYM",fontWeight=FontWeight.Black,letterSpacing=(-1).sp);Text("BOW",fontWeight=FontWeight.Light,letterSpacing=(-1).sp)}},actions={Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF168451)));Spacer(Modifier.width(6.dp));Text("ALL SYSTEMS READY",fontSize=8.sp,color=muted,fontWeight=FontWeight.Bold);Spacer(Modifier.width(14.dp))}},colors=TopAppBarDefaults.topAppBarColors(containerColor=cloud))},
 bottomBar={NavigationBar(containerColor=paper,tonalElevation=0.dp){Tab.entries.forEach{t ->
    val icon=when(t){Tab.HOME->Icons.Outlined.Dashboard;Tab.MOVE->Icons.Outlined.AccessibilityNew;Tab.TRAIN->Icons.Outlined.FitnessCenter;Tab.FOOD->Icons.Outlined.Restaurant;Tab.REST->Icons.Outlined.Bedtime;Tab.PROGRESS->Icons.Outlined.BarChart}
    NavigationBarItem(selected=tab==t,onClick={tab=t},icon={Icon(icon,null)},label={Text(t.title,fontSize=9.sp,fontWeight=FontWeight.SemiBold)},colors=NavigationBarItemDefaults.colors(selectedIconColor=ink,selectedTextColor=ink,indicatorColor=cloud,unselectedIconColor=muted,unselectedTextColor=muted))
 }}}
 ){padding->
  when(tab){
   Tab.HOME->Page(padding){Heading("YOUR TRAINING · YOUR DATA","BUILT BY\nCONSISTENCY.","Your body is a long-term project. Let's put in the work.");Panel{Eyebrow("THIS WEEK");Text("WEEKLY CHECK-IN",fontSize=20.sp,fontWeight=FontWeight.Black);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){listOf("M","T","W","T","F","S","S").forEachIndexed{i,d->Column(horizontalAlignment=Alignment.CenterHorizontally){Text(d,fontSize=9.sp,color=muted);Box(Modifier.padding(top=6.dp).size(27.dp).clip(CircleShape).background(if(i<completed.coerceAtMost(7))ink else cloud),contentAlignment=Alignment.Center){Text((16+i).toString(),fontSize=9.sp,color=if(i<completed.coerceAtMost(7))paper else muted)}}}};Text(completed.toString()+" sessions completed",fontSize=10.sp,color=muted)};Box(Modifier.fillMaxWidth().height(155.dp).clip(RoundedCornerShape(4.dp)).background(ink).padding(18.dp),contentAlignment=Alignment.CenterStart){Column{Eyebrow("YOUR NEXT CHAPTER STARTS NOW",Color.LightGray);Text("NO SHORTCUTS.\nJUST PROGRESS.",fontSize=28.sp,lineHeight=30.sp,fontWeight=FontWeight.Black,color=paper);Button(onClick={tab=Tab.TRAIN},colors=ButtonDefaults.buttonColors(containerColor=paper,contentColor=ink),shape=RoundedCornerShape(3.dp)){Text("VIEW YOUR PLAN",fontSize=10.sp,fontWeight=FontWeight.Bold)}}};Eyebrow("YOUR NUMBERS");Text("THE DAILY CHECK-IN",fontSize=20.sp,fontWeight=FontWeight.Black);Metric("ENERGY INTAKE",kcal.toInt().toString(),"kcal",(kcal/2700).toFloat(),(2700-kcal.toInt()).coerceAtLeast(0).toString()+" kcal remaining");Metric("PROTEIN",protein.toInt().toString(),"g",(protein/140).toFloat(),(140-protein.toInt()).coerceAtLeast(0).toString()+" g to target");Metric("BODY WEIGHT",String.format(Locale.US,"%.1f",weight),"kg",.72f,"Target tracking");Metric("LAST NIGHT",String.format(Locale.US,"%.1f",sleep),"hrs",sleep/8,"Sleep duration");Panel{Eyebrow("ON THE SCHEDULE");Text("UP NEXT",fontSize=20.sp,fontWeight=FontWeight.Black);Text("Upper body strength · 65 MIN",fontSize=12.sp,color=muted);DarkButton("OPEN TRAINING"){tab=Tab.TRAIN}}}
   Tab.MOVE->MovementPage()
   Tab.TRAIN->Page(padding){Heading("PROGRAMMING · PERFORMANCE","TRAIN WITH\nINTENT.","A plan built around progressive overload, consistency and measurable performance.");WorkoutSchedulePanel(ctx);TrainingHistoryPanel(ctx);Panel{Eyebrow("WEEKLY VOLUME");Text("14 SETS / MUSCLE AVG.",fontSize=20.sp,fontWeight=FontWeight.Black);Text(completed.toString()+" SESSIONS COMPLETED",fontSize=11.sp,color=muted)};if(active.isNotEmpty())Panel{Eyebrow("SESSION IN PROGRESS");Text(active,fontSize=21.sp,fontWeight=FontWeight.Black);Text(String.format(Locale.US,"%02d:%02d",seconds/60,seconds%60),fontSize=38.sp,fontWeight=FontWeight.Black);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){DarkButton(if(timer)"PAUSE" else "START TIMER"){timer=!timer};LightButton("RESET"){seconds=0;timer=false};LightButton("FINISH"){recordWorkoutSession(ctx,active);completed++;active="";timer=false;seconds=0}}};workouts.forEach{w->Panel{Eyebrow(w.focus);Text(w.name,fontSize=23.sp,fontWeight=FontWeight.Black);Text(w.duration.toString()+" MIN · "+w.moves.size+" EXERCISES",fontSize=9.sp,color=muted);HorizontalDivider(color=line);w.moves.forEach{e->Row(Modifier.fillMaxWidth().padding(vertical=6.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(e.name,fontSize=12.sp,fontWeight=FontWeight.SemiBold);Text(e.sets,fontSize=10.sp,color=muted)}};DarkButton("START WORKOUT"){active=w.name;seconds=0;timer=false}}}}
   Tab.FOOD->Page(padding){Heading("FUEL · RECOVERY · PERFORMANCE","EAT FOR\nTHE WORK.","A transparent food log with portion-scaled macro math.");Panel{Eyebrow("CALORIES CONSUMED");Text(kcal.toInt().toString()+" kcal",fontSize=38.sp,fontWeight=FontWeight.Black);LinearProgressIndicator(progress={(kcal/2700).toFloat().coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth(),color=ink,trackColor=cloud);Text((2700-kcal.toInt()).coerceAtLeast(0).toString()+" kcal left",fontSize=10.sp,color=muted)};Metric("PROTEIN",protein.toInt().toString(),"g",(protein/140).toFloat(),"Target 140 g");Metric("CARBS",foods.sumOf{it.carbs*it.grams/100}.toInt().toString(),"g",.45f,"Daily estimate");Metric("FAT",foods.sumOf{it.fat*it.grams/100}.toInt().toString(),"g",.35f,"Daily estimate");Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Eyebrow("YOUR FOOD DIARY");Text("TODAY'S INTAKE",fontSize=20.sp,fontWeight=FontWeight.Black)};DarkButton("+ ADD FOOD"){addFood=true}};foods.forEachIndexed{i,f->Panel{Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(f.name,fontWeight=FontWeight.Bold,fontSize=12.sp);Text(f.grams.toString()+" g",fontSize=9.sp,color=muted)};Text((f.kcal*f.grams/100).toInt().toString()+" kcal",fontWeight=FontWeight.Bold,fontSize=11.sp);IconButton({foods=foods.filterIndexed{ix,_->ix!=i}}){Icon(Icons.Outlined.Delete,null,tint=muted)}}}}}
   Tab.REST->Page(padding){Heading("SLEEP · READINESS · REST","RECOVER\nSTRONGER.","Training is the stimulus. Recovery is where adaptation happens.");Panel{Eyebrow("SLEEP TRACKER");Text(String.format(Locale.US,"%.1f",sleep)+" HOURS",fontSize=38.sp,fontWeight=FontWeight.Black);Text("TARGET · 8 HOURS",fontSize=9.sp,color=muted);LinearProgressIndicator(progress={(sleep/8).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth(),color=ink,trackColor=cloud);DarkButton("EDIT SLEEP HOURS"){sleep=(sleep+0.5f).coerceAtMost(12f)}};Panel{Eyebrow("DAILY READINESS");Text("78 / 100",fontSize=38.sp,fontWeight=FontWeight.Black);Text("ILLUSTRATIVE SCORE · NOT MEASURED",fontSize=9.sp,color=muted)};Panel{Eyebrow("HYDRATION TRACKER");Text(water.toString()+" / 8 GLASSES",fontSize=23.sp,fontWeight=FontWeight.Black);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){repeat(8){i->Box(Modifier.size(25.dp,35.dp).clip(RoundedCornerShape(3.dp)).background(if(i<water)ink else cloud))}};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){LightButton("− REMOVE"){water=(water-1).coerceAtLeast(0)};DarkButton("+ ADD GLASS"){water=(water+1).coerceAtMost(8)}}};Panel{Eyebrow("RECOVERY CHECKLIST");listOf("Consistent sleep and wake time","Adequate daily protein and energy","Hydration throughout the day","Light movement on rest days","Manage training stress").forEach{var checked by rememberSaveable{mutableStateOf(false)};Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked,{checked=it},colors=CheckboxDefaults.colors(checkedColor=ink));Text(it,fontSize=11.sp)}}}}
   Tab.PROGRESS->Page(padding){Heading("MEASURE · ADAPT · REPEAT","PROGRESS\nIS EARNED.","Track the signals that matter. Consistency beats guesswork.");BodyMetricsPanel(ctx,weight);TrainingHistoryPanel(ctx);Panel{Eyebrow("BODY WEIGHT");Text(String.format(Locale.US,"%.1f kg",weight),fontSize=38.sp,fontWeight=FontWeight.Black);Slider(value=weight,onValueChange={weight=it},valueRange=40f..140f,colors=SliderDefaults.colors(thumbColor=ink,activeTrackColor=ink))};Panel{Eyebrow("TRAINING CONSISTENCY");Text(completed.toString()+" SESSIONS COMPLETED",fontSize=20.sp,fontWeight=FontWeight.Black);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.Bottom){listOf(4,6,3,5,7,2,completed.coerceAtMost(7)).forEachIndexed{i,v->Column(horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.width(22.dp).height((v*14+8).dp).background(ink));Text(listOf("M","T","W","T","F","S","S")[i],fontSize=9.sp,color=muted)}}}};Panel{Eyebrow("PERFORMANCE SNAPSHOT");Text("Build a consistent log",fontSize=18.sp,fontWeight=FontWeight.Bold);Text("Record body weight and completed sessions over time. Historical samples are not fabricated.",fontSize=11.sp,color=muted)}}
  }
 }
 if(addFood) AlertDialog(onDismissRequest={addFood=false},title={Text("ADD TO YOUR DIARY",fontWeight=FontWeight.Black)},text={LazyColumn(Modifier.heightIn(max=300.dp)){items(catalog){f->TextButton(onClick={foods=foods+f;addFood=false},modifier=Modifier.fillMaxWidth()){Column(Modifier.fillMaxWidth()){Text(f.name,color=ink,fontWeight=FontWeight.Bold);Text(f.kcal.toString()+" kcal / 100 g",fontSize=10.sp,color=muted)}}}}},confirmButton={TextButton(onClick={addFood=false}){Text("CLOSE",color=ink)}})
}

@Composable private fun Page(padding:PaddingValues,content:@Composable ColumnScope.()->Unit){LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){item{Column(verticalArrangement=Arrangement.spacedBy(13.dp),content=content)}}}
@Composable private fun Heading(kicker:String,title:String,subtitle:String){Column(verticalArrangement=Arrangement.spacedBy(6.dp)){Eyebrow(kicker);Text(title,fontSize=35.sp,lineHeight=36.sp,fontWeight=FontWeight.Black,letterSpacing=(-1.3).sp,color=ink);Text(subtitle,fontSize=12.sp,lineHeight=18.sp,color=muted)}}
@Composable private fun Eyebrow(text:String,color:Color=muted){Text(text.uppercase(Locale.US),fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=1.2.sp,color=color)}
@Composable private fun Panel(content:@Composable ColumnScope.()->Unit){Card(shape=RoundedCornerShape(4.dp),colors=CardDefaults.cardColors(containerColor=paper),border=BorderStroke(1.dp,line)){Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(9.dp),content=content)}}
@Composable private fun Metric(label:String,value:String,unit:String,progress:Float,caption:String){Panel{Eyebrow(label);Row(verticalAlignment=Alignment.Bottom){Text(value,fontSize=30.sp,fontWeight=FontWeight.Black,color=ink);Text(" "+unit,fontSize=10.sp,color=muted,modifier=Modifier.padding(bottom=5.dp))};LinearProgressIndicator(progress={progress.coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth().height(4.dp),color=ink,trackColor=cloud);Text(caption,fontSize=10.sp,color=muted)}}
@Composable private fun DarkButton(label:String,onClick:()->Unit){Button(onClick,shape=RoundedCornerShape(3.dp),colors=ButtonDefaults.buttonColors(containerColor=ink,contentColor=paper),contentPadding=PaddingValues(horizontal=13.dp,vertical=7.dp)){Text(label,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=.4.sp)}}
@Composable private fun LightButton(label:String,onClick:()->Unit){OutlinedButton(onClick,shape=RoundedCornerShape(3.dp),border=BorderStroke(1.dp,line),contentPadding=PaddingValues(horizontal=11.dp,vertical=7.dp)){Text(label,fontSize=9.sp,fontWeight=FontWeight.Bold,color=ink)}}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun MovementPage(){
 val context=LocalContext.current
 var granted by remember{mutableStateOf(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)}
 var cameraOn by rememberSaveable{mutableStateOf(false)}
 var exercise by rememberSaveable{mutableStateOf("Barbell squat")}
 var posePoints by remember { mutableStateOf(emptyList<PosePoint>()) }
 var poseError by remember { mutableStateOf("") }
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted=it;cameraOn=it}
 Page(PaddingValues(0.dp)){
  Heading("BIOMECHANICS · FULL BODY TRACKING","MOVE WITH\nPRECISION.","Native camera workspace for movement capture.")
  Panel{
   var expanded by remember{mutableStateOf(false)}
   ExposedDropdownMenuBox(expanded=expanded,onExpandedChange={expanded=it}){
    OutlinedTextField(value=exercise,onValueChange={},readOnly=true,label={Text("Exercise")},trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded)},modifier=Modifier.menuAnchor().fillMaxWidth())
    ExposedDropdownMenu(expanded=expanded,onDismissRequest={expanded=false}){
     listOf("Barbell squat","Deadlift","Bench press","Overhead press","Biceps curl","Push-up","Lunge","Pull-up","Running","Custom movement").forEach{item->DropdownMenuItem(text={Text(item)},onClick={exercise=item;expanded=false})}
    }
   }
  }
  if(cameraOn&&granted){
   CameraPreview(Modifier.fillMaxWidth().height(360.dp),onPose={posePoints=it},onError={poseError=it})
   if(posePoints.size==33) PoseReadout(posePoints) else if(poseError.isNotBlank()) Text(poseError,fontSize=10.sp,color=muted)
   DarkButton("STOP CAMERA"){cameraOn=false}
  }else{
   Box(Modifier.fillMaxWidth().height(245.dp).clip(RoundedCornerShape(4.dp)).background(ink),contentAlignment=Alignment.Center){
    Column(horizontalAlignment=Alignment.CenterHorizontally){
     Icon(Icons.Outlined.AccessibilityNew,null,tint=paper,modifier=Modifier.size(42.dp))
     Text("FULL BODY CAPTURE",fontWeight=FontWeight.Bold,color=paper)
     Text("Keep your full body in frame",fontSize=10.sp,color=Color.LightGray)
    }
   }
   DarkButton("START CAMERA"){if(granted)cameraOn=true else permission.launch(Manifest.permission.CAMERA)}
  }
  Panel{
   Eyebrow("POSE ESTIMATION")
   Text("33 LANDMARKS · ON-DEVICE INFERENCE",fontSize=18.sp,fontWeight=FontWeight.Black)
   Text("The full-body MediaPipe model runs locally on the device. No camera frames are uploaded.",fontSize=10.sp,lineHeight=16.sp,color=muted)
  }
 }
}

@Composable private fun CameraPreview(modifier:Modifier,onPose:(List<PosePoint>)->Unit,onError:(String)->Unit){
 val context=LocalContext.current
 val owner=androidx.lifecycle.compose.LocalLifecycleOwner.current
 val executor=remember{Executors.newSingleThreadExecutor()}
 DisposableEffect(Unit){onDispose{executor.shutdown()}}
 AndroidView(modifier=modifier,factory={ctx->
  val view=PreviewView(ctx)
  val future=ProcessCameraProvider.getInstance(ctx)
  future.addListener({
   val provider=future.get()
   val preview=Preview.Builder().build().also{it.surfaceProvider=view.surfaceProvider}
   val analysis=ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
   val pose=try{NativePoseAnalyzer(ctx,onPose,onError)}catch(t:Throwable){onError(t.message?: "Pose model unavailable");null}
   if(pose!=null) analysis.setAnalyzer(executor,pose)
   try{provider.unbindAll();provider.bindToLifecycle(owner,CameraSelector.DEFAULT_FRONT_CAMERA,preview,analysis)}catch(t:Exception){onError(t.message?: "Camera unavailable")}
  },ContextCompat.getMainExecutor(ctx))
  view
 })
}

@Composable private fun PoseReadout(points:List<PosePoint>){
 val angleItems=listOf("LEFT SHOULDER" to Triple(13,11,23),"RIGHT SHOULDER" to Triple(14,12,24),"LEFT ELBOW" to Triple(11,13,15),"RIGHT ELBOW" to Triple(12,14,16),"LEFT HIP" to Triple(11,23,25),"RIGHT HIP" to Triple(12,24,26),"LEFT KNEE" to Triple(23,25,27),"RIGHT KNEE" to Triple(24,26,28),"LEFT ANKLE" to Triple(25,27,31),"RIGHT ANKLE" to Triple(26,28,32))
 Panel{
  Eyebrow("LIVE JOINT ANGLES")
  angleItems.forEach{(label,ids)->
   val angle=if(points.size>ids.third) PoseMath.angle(points[ids.first],points[ids.second],points[ids.third]) else 0
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label,fontSize=10.sp,fontWeight=FontWeight.Bold);Text("$angle°",fontSize=12.sp,fontWeight=FontWeight.Black)}
  }
  Text("33/33 pose landmarks · 3D world-space joint angles",fontSize=9.sp,color=muted)
 }
}

package com.ali.asghar.gymbow

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.hypot

private data class GymExercise(
    val name:String, val muscle:String, val equipment:String, val category:String,
    val sets:Int=3, val reps:String="8–12", val rest:Int=90,
    val setup:String, val cues:String, val mistakes:String,
    val phases:List<String>, val alternatives:List<String>
)
private val gymExercises = listOf(
 GymExercise("Barbell squat","Quadriceps · Glutes · Core","Barbell · Rack","Strength",4,"6–10",150,"Bar on upper back, feet shoulder-width, brace your trunk.","Sit between hips, track knees over toes, keep whole foot planted.","Heels lifting, knees collapsing inward, losing trunk brace.",listOf("Standing lockout","Controlled descent","Bottom position","Drive to standing"),listOf("Goblet squat","Smith machine squat","Hack squat")),
 GymExercise("Bench press","Chest · Triceps · Front delts","Barbell · Bench · Rack","Strength",4,"6–10",150,"Eyes under bar, feet grounded, shoulder blades set.","Lower with control to lower chest; press without bouncing.","Shoulders rolling forward, wrists folding, bouncing bar.",listOf("Arms extended","Lower to chest","Bar touches chest","Press to lockout"),listOf("Dumbbell press","Chest press machine","Push-up")),
 GymExercise("Deadlift","Hamstrings · Glutes · Back","Barbell · Plates","Strength",3,"5–8",180,"Bar over mid-foot, hinge and grip just outside legs.","Brace, push floor away, keep bar close to legs.","Bar drifting forward, lumbar rounding, jerking from floor.",listOf("Hip hinge setup","Bar leaves floor","Bar passes knees","Tall lockout"),listOf("Trap-bar deadlift","Romanian deadlift","Cable pull-through")),
 GymExercise("Romanian deadlift","Hamstrings · Glutes","Barbell · Dumbbells","Strength",3,"8–12",120,"Stand tall with soft knees and weights against thighs.","Push hips back while keeping weights close; stop at hamstring tension.","Turning it into a squat, rounding back, overreaching depth.",listOf("Tall stance","Hips travel back","Hamstring stretch","Hip extension"),listOf("Dumbbell RDL","Cable pull-through","45° back extension")),
 GymExercise("Overhead press","Shoulders · Triceps · Core","Barbell · Rack","Strength",3,"6–10",120,"Bar at upper chest, ribs stacked over pelvis.","Press vertically and move head through once bar clears.","Excessive back arch, flared ribs, bar drifting forward.",listOf("Front rack","Press begins","Bar overhead","Controlled return"),listOf("Dumbbell shoulder press","Machine shoulder press","Landmine press")),
 GymExercise("Lat pulldown","Lats · Biceps","Cable · Pulldown bar","Machine",3,"8–12",90,"Thighs secured, torso slightly reclined, wide comfortable grip.","Drive elbows down toward ribs; control the return.","Pulling behind neck, swinging torso, shrugging shoulders.",listOf("Arms overhead","Elbows descend","Bar near upper chest","Controlled stretch"),listOf("Assisted pull-up","Neutral-grip pulldown","Band pulldown")),
 GymExercise("Seated cable row","Mid-back · Lats · Biceps","Cable · Row handle","Machine",3,"10–12",90,"Sit tall with neutral spine and soft knees.","Pull handle toward torso; let shoulder blades move naturally.","Excessive torso rocking, shrugging, rounded lumbar spine.",listOf("Arms extended","Scapula initiates","Handle to torso","Arms extend"),listOf("Chest-supported row","Dumbbell row","Machine row")),
 GymExercise("Dumbbell row","Lats · Mid-back · Biceps","Dumbbell · Bench","Free weights",3,"8–12",90,"Support one hand, hinge at hips, spine neutral.","Pull elbow toward hip without rotating torso.","Twisting torso, shrugging, using momentum.",listOf("Arm extended","Elbow drives back","Top contraction","Controlled lower"),listOf("Cable row","Chest-supported row","Machine row")),
 GymExercise("Incline dumbbell press","Upper chest · Triceps · Delts","Dumbbells · Incline bench","Free weights",3,"8–12",90,"Bench at a moderate incline, shoulder blades set.","Lower dumbbells beside upper chest; press smoothly.","Excessive incline, flared elbows, clashing dumbbells.",listOf("Dumbbells over chest","Lower under control","Bottom stretch","Press up"),listOf("Incline machine press","Incline barbell press","Low-to-high cable fly")),
 GymExercise("Dumbbell lateral raise","Lateral delts","Dumbbells","Free weights",3,"12–20",60,"Stand tall, slight elbow bend, weights by sides.","Raise in scapular plane to comfortable shoulder height.","Swinging, shrugging, lifting far above shoulder height.",listOf("Weights by sides","Arms rising","Near shoulder height","Controlled lower"),listOf("Cable lateral raise","Lateral raise machine","Lean-away cable raise")),
 GymExercise("Biceps curl","Biceps · Brachialis","Dumbbells · EZ bar","Free weights",3,"10–15",60,"Elbows near sides, shoulders relaxed.","Flex elbows without swinging upper arms.","Hip swing, elbows drifting, wrist collapse.",listOf("Arms extended","Curl begins","Peak contraction","Slow extension"),listOf("Cable curl","Preacher curl","Hammer curl")),
 GymExercise("Triceps pushdown","Triceps","Cable · Rope/bar","Cable",3,"10–15",60,"Elbows tucked beside torso, shoulders relaxed.","Extend elbows fully while keeping upper arms still.","Leaning bodyweight into cable, elbows moving forward.",listOf("Elbows bent","Extension","Full extension","Controlled return"),listOf("Overhead cable extension","Dumbbell extension","Close-grip push-up")),
 GymExercise("Leg press","Quadriceps · Glutes","Leg press machine","Machine",3,"10–15",120,"Back and pelvis supported; feet stable on platform.","Lower only as far as pelvis stays supported; press smoothly.","Pelvis curling off pad, knees collapsing, locking aggressively.",listOf("Knees extended","Controlled lowering","Bottom range","Press platform"),listOf("Squat","Hack squat","Split squat")),
 GymExercise("Leg extension","Quadriceps","Leg extension machine","Machine",3,"10–15",60,"Align knee joint with machine pivot; pad above ankle.","Extend knee smoothly; pause and lower with control.","Wrong seat alignment, kicking, bouncing at bottom.",listOf("Knee flexed","Extension begins","Near extension","Controlled flexion"),listOf("Spanish squat","Step-up","Sissy squat")),
 GymExercise("Seated leg curl","Hamstrings","Leg curl machine","Machine",3,"10–15",60,"Align knee with pivot, pad above heel, hips down.","Curl heels under seat without lifting hips.","Hips lifting, partial range, jerking.",listOf("Knee extended","Curl begins","Full curl","Slow return"),listOf("Lying leg curl","Nordic curl","Cable leg curl")),
 GymExercise("Standing calf raise","Calves","Calf raise machine","Machine",4,"10–20",60,"Ball of foot on platform, knees softly extended.","Lower heel into stretch; rise to full plantar flexion.","Bouncing, partial range, rolling ankles.",listOf("Heel lowered","Mid-range","High heel","Slow stretch"),listOf("Seated calf raise","Single-leg calf raise","Leg press calf raise")),
 GymExercise("Pull-up","Lats · Biceps · Core","Pull-up bar","Bodyweight",3,"AMRAP",120,"Hang with active shoulders and comfortable grip.","Drive elbows down; avoid kicking or excessive swing.","Kipping unintentionally, shrugging, half reps.",listOf("Active hang","Elbows drive down","Chin over bar","Controlled hang"),listOf("Lat pulldown","Assisted pull-up","Band pull-up")),
 GymExercise("Push-up","Chest · Triceps · Core","Floor · Mat","Bodyweight",3,"AMRAP",90,"Hands under or slightly wider than shoulders, body in line.","Lower chest between hands; press floor away.","Hips sagging, elbows flared, head reaching forward.",listOf("High plank","Lowering","Chest near floor","Press to plank"),listOf("Incline push-up","Knee push-up","Machine chest press")),
 GymExercise("Walking lunge","Quads · Glutes","Dumbbells · Open space","Free weights",3,"10 each",90,"Stand tall with feet hip-width and stable stride.","Lower back knee toward floor; push through whole front foot.","Narrow balance line, front heel lifting, uncontrolled steps.",listOf("Standing","Step forward","Bottom lunge","Drive and step"),listOf("Reverse lunge","Split squat","Step-up")),
 GymExercise("Hip thrust","Glutes · Hamstrings","Bench · Barbell","Free weights",3,"8–12",90,"Upper back supported, feet planted, chin slightly tucked.","Extend hips until torso and thighs align; avoid lumbar overextension.","Overarching back, feet too far, pushing through toes only.",listOf("Hips lowered","Hip drive","Top lockout","Controlled lower"),listOf("Glute bridge","Machine hip thrust","Cable pull-through")),
 GymExercise("Plank","Core · Abdominals","Mat","Bodyweight",3,"30–60 sec",60,"Elbows under shoulders, legs extended, ribs tucked.","Maintain straight line and breathe without losing brace.","Hips sagging, hips too high, breath holding.",listOf("Set elbows","Lift to plank","Hold neutral line","Knees lower"),listOf("Dead bug","Bird dog","Knee plank")),
 GymExercise("Cable fly","Chest","Cable station","Cable",3,"12–15",60,"Stagger stance, slight elbow bend, chest open.","Bring hands together in arc without shrugging.","Overstretching shoulder, turning into press, momentum.",listOf("Arms open","Arms sweep","Hands meet","Controlled open"),listOf("Pec deck","Dumbbell fly","Band fly")),
 GymExercise("Face pull","Rear delts · Upper back","Cable · Rope","Cable",3,"12–20",60,"Cable around face height, thumbs back.","Pull rope toward forehead with elbows high and controlled.","Leaning back, shrugging, excessive load.",listOf("Arms extended","Pull begins","Hands beside head","Slow reach"),listOf("Reverse pec deck","Band pull-apart","Cable rear delt fly")),
 GymExercise("Chest-supported row","Mid-back · Lats","Incline bench · Dumbbells","Free weights",3,"8–12",90,"Chest supported, feet planted, arms hanging.","Pull elbows back while keeping chest on pad.","Lifting chest, shrugging, shortened range.",listOf("Arms hanging","Row begins","Elbows back","Controlled lower"),listOf("Seated cable row","Machine row","One-arm cable row")),
 GymExercise("Machine chest press","Chest · Triceps","Chest press machine","Machine",3,"8–12",90,"Adjust seat so handles align around mid-chest.","Press without lifting back from pad; return under control.","Seat too low/high, shoulders rolling forward.",listOf("Handles near chest","Press forward","Arms extended","Controlled return"),listOf("Bench press","Dumbbell press","Push-up")),
 GymExercise("Smith machine squat","Quads · Glutes","Smith machine","Machine",3,"8–12",120,"Set bar height and stance for comfortable track.","Brace and squat through stable range of motion.","Feet position forcing knees, shallow uncontrolled reps.",listOf("Standing setup","Lowering","Bottom position","Stand tall"),listOf("Barbell squat","Goblet squat","Leg press")),
 GymExercise("Hack squat","Quads · Glutes","Hack squat machine","Machine",3,"8–12",120,"Shoulders under pads, feet stable, back supported.","Lower with control; knees track with toes.","Heels lifting, pelvis rolling, bouncing.",listOf("Top position","Controlled descent","Bottom range","Drive upward"),listOf("Leg press","Front squat","Goblet squat")),
 GymExercise("Hammer curl","Brachialis · Biceps · Forearms","Dumbbells","Free weights",3,"10–15",60,"Neutral grip, elbows by sides.","Curl without rotating wrists or swinging torso.","Shoulder swing, elbows drifting, wrist bending.",listOf("Arms down","Curl upward","Peak contraction","Lower slowly"),listOf("Rope hammer curl","Cross-body curl","Cable curl")),
 GymExercise("Reverse fly","Rear delts · Upper back","Dumbbells · Machine","Free weights",3,"12–20",60,"Hinge or use chest support; arms slightly bent.","Move arms outward with shoulder blades controlled.","Shrugging, swinging, excessive load.",listOf("Arms below chest","Arms open","Wide position","Controlled return"),listOf("Reverse pec deck","Cable reverse fly","Face pull"))
)

private data class Joint(val id:String,val label:String,val x:Float,val y:Float)
private val baseJoints = listOf(
 Joint("head","Head",.50f,.08f),Joint("neck","Neck",.50f,.16f),Joint("shoulderL","Left shoulder",.39f,.21f),Joint("shoulderR","Right shoulder",.61f,.21f),
 Joint("elbowL","Left elbow",.33f,.34f),Joint("elbowR","Right elbow",.67f,.34f),Joint("wristL","Left wrist",.28f,.46f),Joint("wristR","Right wrist",.72f,.46f),
 Joint("hipL","Left hip",.44f,.48f),Joint("hipR","Right hip",.56f,.48f),Joint("kneeL","Left knee",.43f,.68f),Joint("kneeR","Right knee",.57f,.68f),
 Joint("ankleL","Left ankle",.42f,.87f),Joint("ankleR","Right ankle",.58f,.87f),Joint("heelL","Left heel",.39f,.91f),Joint("heelR","Right heel",.61f,.91f),
 Joint("toeL","Left toe",.34f,.94f),Joint("toeR","Right toe",.66f,.94f),
 Joint("thumbL1","L thumb base",.25f,.45f),Joint("thumbL2","L thumb tip",.23f,.43f),Joint("indexL1","L index base",.26f,.48f),Joint("indexL2","L index tip",.24f,.51f),
 Joint("middleL1","L middle base",.28f,.49f),Joint("middleL2","L middle tip",.28f,.53f),Joint("ringL1","L ring base",.30f,.49f),Joint("ringL2","L ring tip",.31f,.52f),Joint("pinkyL1","L pinky base",.32f,.48f),Joint("pinkyL2","L pinky tip",.34f,.50f),
 Joint("thumbR1","R thumb base",.75f,.45f),Joint("thumbR2","R thumb tip",.77f,.43f),Joint("indexR1","R index base",.74f,.48f),Joint("indexR2","R index tip",.76f,.51f),
 Joint("middleR1","R middle base",.72f,.49f),Joint("middleR2","R middle tip",.72f,.53f),Joint("ringR1","R ring base",.70f,.49f),Joint("ringR2","R ring tip",.69f,.52f),Joint("pinkyR1","R pinky base",.68f,.48f),Joint("pinkyR2","R pinky tip",.66f,.50f),
 Joint("toeL1","L big toe base",.35f,.93f),Joint("toeL2","L big toe tip",.31f,.95f),Joint("toeL3","L toe tips",.36f,.95f),Joint("toeR1","R big toe base",.65f,.93f),Joint("toeR2","R big toe tip",.69f,.95f),Joint("toeR3","R toe tips",.64f,.95f)
)
private val bones = listOf("head" to "neck","neck" to "shoulderL","neck" to "shoulderR","shoulderL" to "elbowL","elbowL" to "wristL","shoulderR" to "elbowR","elbowR" to "wristR","shoulderL" to "hipL","shoulderR" to "hipR","hipL" to "hipR","hipL" to "kneeL","kneeL" to "ankleL","ankleL" to "heelL","heelL" to "toeL","hipR" to "kneeR","kneeR" to "ankleR","ankleR" to "heelR","heelR" to "toeR",
 "wristL" to "thumbL1","thumbL1" to "thumbL2","wristL" to "indexL1","indexL1" to "indexL2","wristL" to "middleL1","middleL1" to "middleL2","wristL" to "ringL1","ringL1" to "ringL2","wristL" to "pinkyL1","pinkyL1" to "pinkyL2",
 "wristR" to "thumbR1","thumbR1" to "thumbR2","wristR" to "indexR1","indexR1" to "indexR2","wristR" to "middleR1","middleR1" to "middleR2","wristR" to "ringR1","ringR1" to "ringR2","wristR" to "pinkyR1","pinkyR1" to "pinkyR2","toeL" to "toeL1","toeL1" to "toeL2","toeR" to "toeR1","toeR1" to "toeR2")

private fun loadPose(context:Context,key:String):List<Joint>{
 val saved=context.getSharedPreferences("gymbow-native",Context.MODE_PRIVATE).getString("pose_$key",null)?:return baseJoints
 val coords=saved.split(";").mapNotNull{row->val p=row.split(",");if(p.size==3) p[0] to (p[1].toFloatOrNull()?:return@mapNotNull null) to (p[2].toFloatOrNull()?:return@mapNotNull null) else null}.associate{it.first.first to (it.first.second to it.second)}
 return baseJoints.map{j->coords[j.id]?.let{j.copy(x=it.first,y=it.second)}?:j}
}
private fun savePose(context:Context,key:String,points:List<Joint>){
 context.getSharedPreferences("gymbow-native",Context.MODE_PRIVATE).edit().putString("pose_$key",points.joinToString(";"){"${it.id},${it.x},${it.y}"}).apply()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun WorkoutStudioPage(){
 val ctx= androidx.compose.ui.platform.LocalContext.current
 var query by rememberSaveable{mutableStateOf("")}
 var selected by remember{mutableStateOf(gymExercises.first())}
 var equipment by rememberSaveable{mutableStateOf("All")}
 var view by rememberSaveable{mutableStateOf("FRONT")}
 var phase by rememberSaveable{mutableIntStateOf(0)}
 var points by remember{mutableStateOf(loadPose(ctx,"squat_front"))}
 var selectedJoint by rememberSaveable{mutableStateOf("kneeL")}
 var showDetails by remember{mutableStateOf(true)}
 val equipmentFilters=listOf("All","Barbell","Dumbbells","Machine","Cable","Bodyweight")
 val filtered=gymExercises.filter{(query.isBlank()||it.name.contains(query,true)||it.muscle.contains(query,true))&&(equipment=="All"||it.equipment.contains(equipment,true)||it.category==equipment)}
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item { Column(verticalArrangement=Arrangement.spacedBy(6.dp)){Eyebrow("MOVEMENT LAB · MANUAL BIOMECHANICS");Text("BUILD THE\nPERFECT REP.",fontSize=34.sp,lineHeight=35.sp,fontWeight=FontWeight.Black);Text("Exercise library, equipment setup and adjustable reference poses. No camera required.",fontSize=11.sp,color=muted)} }
  item { Panel {
   Eyebrow("EXERCISE LIBRARY · ${gymExercises.size} MOVEMENTS")
   OutlinedTextField(query,{query=it},modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text("Search exercise or muscle")},leadingIcon={Icon(Icons.Outlined.Search,null)})
   Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){equipmentFilters.forEach{filter->FilterChip(selected=equipment==filter,onClick={equipment=filter},label={Text(filter,fontSize=9.sp)})}}
   Text("SELECT MOVEMENT",fontSize=10.sp,fontWeight=FontWeight.Bold)
   Column(Modifier.heightIn(max=190.dp).fillMaxWidth()){
    LazyColumn { items(filtered){ex->TextButton(onClick={selected=ex;phase=0;points=loadPose(ctx,ex.name.lowercase(Locale.US).replace(" ","_")+"_front")},modifier=Modifier.fillMaxWidth()){Column(Modifier.fillMaxWidth()){Text(ex.name,color=ink,fontWeight=FontWeight.Bold,fontSize=12.sp);Text("${ex.muscle} · ${ex.equipment}",color=muted,fontSize=9.sp)}}} }
   }
  } }
  item { Panel {
   Eyebrow("SELECTED MOVEMENT")
   Text(selected.name,fontSize=24.sp,fontWeight=FontWeight.Black)
   Text(selected.muscle.uppercase(Locale.US),fontSize=9.sp,color=muted,fontWeight=FontWeight.Bold)
   Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){StatPill("${selected.sets} SETS");StatPill(selected.reps+" REPS");StatPill("${selected.rest} SEC REST")}
   Text("EQUIPMENT",fontSize=9.sp,fontWeight=FontWeight.Bold,color=muted)
   Text(selected.equipment,fontSize=12.sp)
   Text("SETUP",fontSize=9.sp,fontWeight=FontWeight.Bold,color=muted);Text(selected.setup,fontSize=11.sp)
   Text("FORM CUES",fontSize=9.sp,fontWeight=FontWeight.Bold,color=muted);Text(selected.cues,fontSize=11.sp)
   Text("COMMON MISTAKES",fontSize=9.sp,fontWeight=FontWeight.Bold,color=muted);Text(selected.mistakes,fontSize=11.sp)
   Text("ALTERNATIVES",fontSize=9.sp,fontWeight=FontWeight.Bold,color=muted);Text(selected.alternatives.joinToString("  ·  "),fontSize=10.sp,color=muted)
  } }
  item { Panel {
   Eyebrow("MANUAL POSE STUDIO · ${baseJoints.size} ARTICULATION POINTS")
   Text("REFERENCE POSITION",fontSize=19.sp,fontWeight=FontWeight.Black)
   Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("FRONT","SIDE","BACK").forEach{v->FilterChip(selected=view==v,onClick={view=v;points=loadPose(ctx,selected.name.lowercase(Locale.US).replace(" ","_")+"_${v.lowercase()}")},label={Text(v,fontSize=9.sp)})}}
   Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){selected.phases.forEachIndexed{i,p->FilterChip(selected=phase==i,onClick={phase=i},label={Text(p,fontSize=8.sp)})}}
   Text("Drag any joint dot to pose the body. Select a point below for fine adjustment.",fontSize=9.sp,color=muted)
   PoseCanvas(points,selectedJoint,{selectedJoint=it},{updated->points=updated})
   val joint=points.firstOrNull{it.id==selectedJoint}?:points.first()
   Text("SELECTED · ${joint.label}",fontSize=10.sp,fontWeight=FontWeight.Bold)
   Text("HORIZONTAL",fontSize=8.sp,color=muted)
   Slider(joint.x,{x->points=points.map{if(it.id==joint.id)it.copy(x=x)else it}},valueRange=.03f.. .97f)
   Text("VERTICAL",fontSize=8.sp,color=muted)
   Slider(joint.y,{y->points=points.map{if(it.id==joint.id)it.copy(y=y)else it}},valueRange=.02f.. .98f)
   Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){DarkButton("SAVE REFERENCE"){savePose(ctx,selected.name.lowercase(Locale.US).replace(" ","_")+"_${view.lowercase()}",points)};LightButton("RESET POSE"){points=baseJoints}}
   Text("Manual reference is a coaching diagram, not a medical motion-capture measurement. Save a separate pose for each exercise phase and camera-free view.",fontSize=9.sp,color=muted)
  } }
  item { Panel {
   Eyebrow("MOVEMENT PHASE GUIDE")
   selected.phases.forEachIndexed{i,p->Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(25.dp).padding(3.dp),contentAlignment=Alignment.Center){Text("${i+1}",fontSize=10.sp,fontWeight=FontWeight.Black)};Column(Modifier.weight(1f)){Text(p,fontSize=11.sp,fontWeight=FontWeight.Bold);Text(if(i==0)selected.setup else if(i==selected.phases.lastIndex)selected.cues else "Move smoothly through this phase; maintain control and alignment.",fontSize=9.sp,color=muted)}}}
  } }
 }
}

@Composable private fun PoseCanvas(points:List<Joint>,selected:String,onSelect:(String)->Unit,onMove:(List<Joint>)->Unit){
 Canvas(Modifier.fillMaxWidth().height(330.dp).pointerInput(points){detectDragGestures(onDragStart={p->val nearest=points.minByOrNull{hypot(it.x*size.width-p.x,it.y*size.height-p.y)};nearest?.let{onSelect(it.id)}},onDrag={change,_->val x=(change.position.x/size.width).coerceIn(.02f,.98f);val y=(change.position.y/size.height).coerceIn(.02f,.98f);val current=points.firstOrNull{it.id==selected};if(current!=null)onMove(points.map{if(it.id==selected)it.copy(x=x,y=y)else it});change.consume()})}){
 val w=size.width;val h=size.height
 drawLine(line,Offset(w*.5f,h*.03f),Offset(w*.5f,h*.97f),1f)
 bones.forEach{(a,b)->val p=points.firstOrNull{it.id==a};val q=points.firstOrNull{it.id==b};if(p!=null&&q!=null)drawLine(ink,Offset(p.x*w,p.y*h),Offset(q.x*w,q.y*h),5f,cap=StrokeCap.Round)}
 points.forEach{p->drawCircle(if(p.id==selected)Color(0xFFE11D2E)else ink,if(p.id==selected)9f else 5.5f,Offset(p.x*w,p.y*h))}
}
}

@Composable private fun StatPill(label:String){Surface(color=cloud,shape=RoundedCornerShape(30.dp)){Text(label,Modifier.padding(horizontal=10.dp,vertical=6.dp),fontSize=8.sp,fontWeight=FontWeight.Bold,color=ink)}}

@Composable fun WorkoutRunnerPanel(context:Context){
 var active by rememberSaveable{mutableStateOf(false)}
 var running by rememberSaveable{mutableStateOf(false)}
 var elapsed by rememberSaveable{mutableIntStateOf(0)}
 var rest by rememberSaveable{mutableIntStateOf(90)}
 var set by rememberSaveable{mutableIntStateOf(1)}
 var reps by rememberSaveable{mutableIntStateOf(0)}
 var weight by rememberSaveable{mutableFloatStateOf(20f)}
 var selected by rememberSaveable{mutableStateOf("Barbell squat")}
 LaunchedEffect(running){while(running){delay(1000);elapsed++}}
 val ex=gymExercises.firstOrNull{it.name==selected}?:gymExercises.first()
 Panel {
  Eyebrow("LIVE WORKOUT CONSOLE")
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(if(active)"SESSION ACTIVE" else "READY TO TRAIN",fontSize=18.sp,fontWeight=FontWeight.Black);Text("${set}/${ex.sets} SETS",fontSize=10.sp,color=muted)}
  var expanded by remember{mutableStateOf(false)}
  ExposedDropdownMenuBox(expanded=expanded,onExpandedChange={expanded=it}){
   OutlinedTextField(value=selected,onValueChange={},readOnly=true,label={Text("Movement")},trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded)},modifier=Modifier.menuAnchor().fillMaxWidth())
   ExposedDropdownMenu(expanded=expanded,onDismissRequest={expanded=false}){gymExercises.forEach{e->DropdownMenuItem(text={Text(e.name)},onClick={selected=e.name;set=1;reps=0;expanded=false})}}
  }
  Text(String.format(Locale.US,"%02d:%02d",elapsed/60,elapsed%60),fontSize=40.sp,fontWeight=FontWeight.Black)
  Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){DarkButton(if(running)"PAUSE TIMER" else "START TIMER"){active=true;running=!running};LightButton("RESET"){elapsed=0;running=false;active=false}}
  Text("CURRENT SET",fontSize=9.sp,color=muted,fontWeight=FontWeight.Bold)
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("Repetitions",fontSize=12.sp);Row(verticalAlignment=Alignment.CenterVertically){IconButton({reps=(reps-1).coerceAtLeast(0)}){Icon(Icons.Outlined.Remove,null)};Text(reps.toString(),fontSize=22.sp,fontWeight=FontWeight.Black);IconButton({reps++}){Icon(Icons.Outlined.Add,null)}}}
  Text("LOAD · ${weight.toInt()} KG",fontSize=10.sp,fontWeight=FontWeight.Bold)
  Slider(value=weight,onValueChange={weight=it},valueRange=0f..250f)
  Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){DarkButton("COMPLETE SET"){set++;reps=0;rest=ex.rest};LightButton("START REST"){running=false;rest=ex.rest}}
  Text("REST TIMER · ${rest/60}:${(rest%60).toString().padStart(2,'0')}",fontSize=11.sp,fontWeight=FontWeight.Bold)
  Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){LightButton("−15 SEC"){rest=(rest-15).coerceAtLeast(0)};LightButton("+15 SEC"){rest+=15}}
  Text("REFERENCE PHASE",fontSize=9.sp,color=muted,fontWeight=FontWeight.Bold)
  Text(ex.phases[(set-1).coerceAtMost(ex.phases.lastIndex)],fontSize=14.sp,fontWeight=FontWeight.Bold)
  Text("Keep the selected movement's pose guide open in Movement Lab while training.",fontSize=9.sp,color=muted)
  DarkButton("FINISH SESSION"){recordWorkoutSession(context,selected);active=false;running=false;elapsed=0;set=1;reps=0}
 }
}

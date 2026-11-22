# GYMBOW — Product scope

## Product promise
A single athlete workspace for planning training, understanding movement, recording nutrition, tracking recovery and measuring progress. The interface follows the Nike-inspired system at getdesign.md: monochrome UI, editorial athletic photography, oversized condensed headlines, restrained surfaces and pill CTAs.

## Product areas

1. **Athlete profile** — goals, body metrics, training age, equipment, constraints, preferences and measurement history.
2. **Movement Lab** — 33-landmark MediaPipe Pose Landmarker Full capture from camera/video, face and limb landmarks, per-point confidence, relative depth, bilateral 2D joint-angle readouts, pose keyframes and JSON export. 3D reconstruction, calibrated multi-view capture and exercise-specific form scoring remain future work.
3. **Training** — split builder, exercise prescription, sets/reps/load/RIR/RPE, rest timers, supersets, progressive overload, periodization, deloads, session logs and personal records.
4. **Nutrition** — searchable food composition database, branded food labels, recipes, serving scaling, barcode capture, meal templates, calories, macros, micros, water and targets.
5. **Recovery** — manual sleep log, wearable sleep import, HR/HRV/resting HR, readiness, fatigue, soreness and hydration.
6. **Progress** — weight and circumference trends, photos, strength records, volume, adherence, nutrition and recovery correlations, exportable reports.
7. **Platform** — localization (Persian RTL and English), accessibility, offline-first capture, secure accounts, sync, data export/deletion and integrations.

## Accuracy boundaries
The initial UI uses clearly labelled illustrative readiness and chart samples. Food values are generic estimates and should be replaced by verified provider data or package labels. A true motion coach requires validated anatomical models, calibrated camera input, pose-estimation inference and exercise-specific biomechanical rules. Sleep stages and HRV require supported sensor integrations. The app must never present estimates as measured facts or medical advice.

## Current vertical slice
A responsive React app with persistent local food/workout/profile logs; editable training sessions and timer; macro scaling; manual sleep session and hydration; progress inputs; and an interactive 2D joint rig with draggable landmarks, angle readouts, keyframes and JSON export.

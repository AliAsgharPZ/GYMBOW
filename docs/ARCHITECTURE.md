# Architecture

## Current frontend
- React 18 + TypeScript + Vite.
- Local-first browser persistence via namespaced localStorage keys.
- SVG skeletal editor with editable landmark coordinates and keyframe snapshots.
- Domain boundaries are intentionally visible in the UI: training, nutrition, recovery, movement and analytics.

## Target system
- **Client:** React Native/Expo or Flutter for native mobile sensors, with a shared TypeScript web client if desired.
- **API:** versioned REST/typed RPC service; authenticated athlete, plan, session, food-log, sleep-log and measurement resources.
- **Database:** PostgreSQL for accounts and canonical records; object storage for media; Redis/queue for jobs and sync.
- **Movement engine:** calibrated camera capture → pose estimation → landmark normalization → temporal smoothing → joint-angle computation → exercise-specific quality rules. Keep raw video opt-in and process locally where possible.
- **Food data:** provider adapter layer for verified food composition, branded labels and barcode lookup; preserve provenance and serving units.
- **Wearables:** explicit adapters for Apple Health, Health Connect and vendor APIs. Distinguish imported sensor observations from user-entered values.
- **Analytics:** derived metrics with formula version, source observations, confidence and timestamp.

## Data integrity principles
- Store units and timestamps explicitly; normalize time to UTC while preserving the athlete's timezone.
- Never silently overwrite user logs during sync; use stable IDs and conflict resolution.
- Label demo data and estimated values. Do not fabricate sensor-derived metrics.
- Provide export and deletion paths for personal data.
- Validate all inputs server-side; enforce per-user authorization on every resource.

## Movement schema direction
A motion clip contains an exercise reference, coordinate-space metadata, frame rate, duration and ordered keyframes. Each pose stores named landmarks with x/y/z coordinates, visibility/confidence and optional joint rotations. Angle values are derived from landmarks and must include the joint definition and coordinate frame. Version schemas so new pose-estimation models remain reproducible.

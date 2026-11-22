# GYMBOW

**Train with intention. Move with precision.**

GYMBOW is an athlete workspace for training plans, movement analysis, nutrition, recovery and performance tracking.

## Experience

- **Overview** — daily energy and protein, weekly consistency, upcoming sessions and performance snapshot.
- **Movement Lab** — interactive 2D skeletal rig, draggable landmarks, joint-angle calculations, keyframe timeline, pose playback controls and motion JSON export.
- **Training** — editable workout library, exercise prescriptions, session timer and completion tracking.
- **Nutrition** — portion-scaled calorie and macro diary, searchable starter food composition list, adjustable targets and food logging.
- **Recovery** — manual sleep sessions, sleep-duration target, hydration tracker and recovery checklist.
- **Progress** — body-weight and goal inputs, performance snapshots and trend visualizations.

## Run locally

```bash
npm install
npm run dev
```

Production build:

```bash
npm run build
npm run preview
```

## Stack

React 18 · TypeScript · Vite · Lucide · CSS · local-first browser persistence.

## Product and engineering notes

- [Product scope](docs/PRODUCT.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)

## Accuracy and current limitations

This repository is an actively developed foundation, not a clinically validated coaching system. Food composition entries are generic estimates; use package labels or a verified food-data source for exact values. The readiness score and sample chart are illustrative. Sleep tracking is manual until a supported health-data integration is connected. Movement Lab currently edits a 2D landmark rig; camera-based pose estimation, anatomical 3D reconstruction, calibrated motion capture and exercise-specific technique scoring are roadmap items. The app does not provide medical advice.

User-entered data currently persists in the current browser via localStorage. Authentication, secure cloud sync, provider integrations and multi-device backup are not yet implemented.

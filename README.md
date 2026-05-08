# 📖 Documentation Index - Where to Start?

## 🎯 Reading Order (by User Type)

### For **Server Admins / Users** who want to **install & use**:
1. **START**: [`QUICKSTART.md`](QUICKSTART.md) (5 min read)
   - Build & deploy instructions
   - Basic testing
   - Feature table
   - Troubleshooting

2. **CONFIG**: [`example-furniture.yml`](src/main/resources/example-furniture.yml) (copy-paste ready)
   - Minimal config
   - Full config example
   - Comments for each field

3. **DETAILS**: [`SETUP.md`](SETUP.md) (15 min read)
   - Complete installation guide
   - Feature explanations
   - Testing procedures
   - Configuration reference

---

### For **Developers** who want to **understand & extend**:
1. **START**: [`FINAL_SUMMARY.md`](FINAL_SUMMARY.md) (10 min read)
   - Top-level overview
   - What was implemented
   - Architecture diagram
   - File structure

2. **DEEP DIVE**: [`IMPLEMENTATION.md`](IMPLEMENTATION.md) (30 min read)
   - Each feature explained in detail
   - Code snippets
   - API reference
   - Known limitations
   - Roadmap for improvements

3. **CODE**: [`src/main/java/me/chyxelmc/ceaddon/`](src/main/java/me/chyxelmc/ceaddon/) (review actual code)
   - BedBlockBehavior.java — Main placement logic
   - BedBehavior.java — Sleep interaction logic
   - FacingHelper.java — Direction rotation
   - OriginalStateStorage.java — State persistence
   - NmsSleepAdapter.java — NMS reflection

---

## 📚 File Guide

### 📖 Documentation Files (READ FIRST)
| File | Purpose | Read Time | Audience |
|------|---------|-----------|----------|
| [`FINAL_SUMMARY.md`](FINAL_SUMMARY.md) | Executive summary, architecture overview | 10 min | Everyone |
| [`QUICKSTART.md`](QUICKSTART.md) | Build, deploy, test in 5 minutes | 5 min | Server admins |
| [`SETUP.md`](SETUP.md) | Complete setup guide, features, troubleshooting | 15 min | Server admins |
| [`IMPLEMENTATION.md`](IMPLEMENTATION.md) | Technical architecture, API reference, roadmap | 30 min | Developers |

### 🔧 Configuration Files
| File | Purpose | Use Case |
|------|---------|----------|
| [`example-furniture.yml`](src/main/resources/example-furniture.yml) | Example CraftEngine config | Copy into your resources |
| [`paper-plugin.yml`](src/main/resources/paper-plugin.yml) | Plugin metadata | Auto-generated |

### 💻 Source Code (Read for Implementation)
| File | Purpose | Lines | Key Class(es) |
|------|---------|-------|---|
| [`BedBlockBehavior.java`](src/main/java/me/chyxelmc/ceaddon/behavior/BedBlockBehavior.java) | **Main** block behavior (multi-part placement) | ~220 | `BedBlockBehavior` |
| [`BedBehavior.java`](src/main/java/me/chyxelmc/ceaddon/behavior/BedBehavior.java) | Furniture behavior (sleep interaction) | ~80 | `BedBehavior` |
| [`FacingHelper.java`](src/main/java/me/chyxelmc/ceaddon/behavior/FacingHelper.java) | Facing rotation utilities | ~60 | `FacingHelper` |
| [`OriginalStateStorage.java`](src/main/java/me/chyxelmc/ceaddon/behavior/OriginalStateStorage.java) | State persistence cache | ~80 | `OriginalStateStorage` |
| [`NmsSleepAdapter.java`](src/main/java/me/chyxelmc/ceaddon/nms/NmsSleepAdapter.java) | NMS reflection adapters | ~100 | `NmsSleepAdapter` |
| [`BedConfig.java`](src/main/java/me/chyxelmc/ceaddon/behavior/BedConfig.java) | Config parser | ~60 | `BedConfig` |
| [`CraftEngineSleepPlugin.java`](src/main/java/me/chyxelmc/ceaddon/CraftEngineSleepPlugin.java) | Plugin main (behavior registration) | ~15 | `CraftEngineSleepPlugin` |
| Factory classes | Behavior factories | ~25 ea. | `BedBlockBehaviorFactory`, `BedBehaviorFactory` |

---

## 🎯 Quick Navigation

### "I want to..." → Read:
| Goal | File | Time |
|------|------|------|
| ...get started now | `QUICKSTART.md` | 5 min |
| ...understand what was built | `FINAL_SUMMARY.md` | 10 min |
| ...add to my server | `SETUP.md` + `example-furniture.yml` | 15 min |
| ...modify the code | `IMPLEMENTATION.md` | 30 min |
| ...understand facing rotation | `IMPLEMENTATION.md` → Feature #2 | 5 min |
| ...understand state persistence | `IMPLEMENTATION.md` → Feature #3 | 10 min |
| ...understand monster radius check | `IMPLEMENTATION.md` → Feature #1 | 5 min |
| ...troubleshoot an issue | `SETUP.md` → Troubleshooting section | varies |

---

## 📊 Summary Matrix

```
┌─────────────────────────────────────────────────────────────────┐
│                    DOCUMENTATION TREE                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─ Installation Path (Admin) ─────────────────┐               │
│  │ 1. QUICKSTART.md ──→ Build & Deploy (5m)    │               │
│  │ 2. example-furniture.yml ──→ Config example  │               │
│  │ 3. SETUP.md ──→ Detailed setup (15m)        │               │
│  └────────────────────────────────────────────┘               │
│                                                                  │
│  ┌─ Development Path (Developer) ──────────────┐               │
│  │ 1. FINAL_SUMMARY.md ──→ Overview (10m)      │               │
│  │ 2. IMPLEMENTATION.md ──→ Deep dive (30m)    │               │
│  │ 3. Source code ──→ Review & modify          │               │
│  └────────────────────────────────────────────┘               │
│                                                                  │
│  ┌─ Quick Reference ──────────────────────────┐               │
│  │ • Features table ──→ FINAL_SUMMARY.md       │               │
│  │ • API reference ──→ IMPLEMENTATION.md       │               │
│  │ • Config syntax ──→ example-furniture.yml   │               │
│  │ • Build steps ──→ QUICKSTART.md             │               │
│  └────────────────────────────────────────────┘               │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🏃 Quick Start (TL;DR)

```powershell
# 1. Read (2 min)
# → Read QUICKSTART.md (Steps 1-3)

# 2. Build (1 min)
.\gradlew.bat --no-daemon build -x test

# 3. Deploy (1 min)
Copy-Item "build/libs/CraftEngineSleep-1.0.0.jar" -Destination "plugins/"

# 4. Configure (2 min)
# → Copy example-furniture.yml into your CraftEngine resources
# → Customize as needed

# 5. Test (1 min)
# → Start server
# → /give @s furniture:single_bed
# → Place bed & click to sleep!
```

---

## 💡 Tips for Reading

- **Time-constrained?** → Start with `QUICKSTART.md` (5 min)
- **Want full details?** → Read `SETUP.md` (15 min)
- **Need to customize?** → Read `IMPLEMENTATION.md` (30 min)
- **Just deploying?** → Copy `example-furniture.yml` + read `SETUP.md`
- **Want to extend?** → Read all docs + review source code

---

## 📋 Checklist Before Deployment

- [ ] Read QUICKSTART.md or SETUP.md
- [ ] Run `.\gradlew.bat build -x test`
- [ ] Copy JAR to plugins/
- [ ] Copy/adapt example-furniture.yml to your resources
- [ ] Restart server
- [ ] Test: /give @s furniture:single_bed
- [ ] Check server logs: "[CraftEngineSleepPlugin] Registered..."
- [ ] Test sleep interaction (place bed, right-click)

---

**Happy reading!** 📖

Need help? Check:
- `SETUP.md` → Troubleshooting section
- `IMPLEMENTATION.md` → Known Limitations section


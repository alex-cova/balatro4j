# 🃏 Balatro4j

> A high-performance Balatro seed analyzer and finder, coded in pure Java — based on [Immolate](https://github.com/ImogenBits/immolate).

[![Build](https://github.com/alex-cova/balatro4j/actions/workflows/compile-native.yaml/badge.svg)](https://github.com/alex-cova/balatro4j/actions/workflows/compile-native.yaml)
[![Version](https://img.shields.io/badge/version-2.0.1-blue)](https://github.com/alex-cova/balatro4j/releases)
[![Java](https://img.shields.io/badge/Java-26%2B-orange?logo=openjdk)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-9.4-02303A?logo=gradle)](https://gradle.org/)
[![GitHub Packages](https://img.shields.io/badge/GitHub%20Packages-available-green?logo=github)](https://github.com/alex-cova/balatro4j/packages)

---

## ✨ Features

- **🔍 Seed Finder** — Fully customizable multi-threaded seed search API with auto-configuration for maximum speed (~6M seeds/sec)
- **⚡ Vector Seed Finder** — SIMD-accelerated search using the JDK Vector API (`jdk.incubator.vector`); evaluates multiple seeds per CPU instruction for PRNG-compatible filters
- **🖼️ Seed Renderer** — Convert any seed to a PNG image with game-accurate sprite rendering
- **📊 Seed Scorer** — Evaluate and rank seeds programmatically
- **🗂️ Perkeo Database** — Pre-processed seed database for instant query resolution
- **⚡ Canio Database** — Ultra-compressed seed store (256 bits/seed) enabling instant search across up to 20 million seeds
- **🖥️ Swing UI** — Basic graphical interface to search for and visualize seeds
- **📦 JSON Export** — Serialize any seed run into structured JSON
- **🔧 GraalVM Ready** — Native image compilation support for standalone deployment

---

## 📸 Demo

<!-- TODO: Add screenshot of the Swing UI here -->
> _Screenshot placeholder — launch the UI with `./gradlew :ui:run` to see it in action._

---

## 📋 Prerequisites

| Requirement | Version |
|---|---|
| Java (JDK) | 26 or higher |
| Gradle | 9.4 (wrapper included) |
| GraalVM _(optional, for native build)_ | 23+ |

> **Note:** The JVM version is generally faster than the native image compilation. Native compilation is recommended only for distribution purposes.
>
> **Vector search** requires JDK 26+ with the `jdk.incubator.vector` module enabled. The Gradle build configures this automatically via `--add-modules jdk.incubator.vector`.

---

## 🚀 Installation

### Clone the repository

```bash
git clone https://github.com/alex-cova/balatro4j.git
cd balatro4j
```

### Build the project

```bash
./gradlew build
```

### Use as a library (GitHub Packages)

Add the `perkeo` module as a dependency via GitHub Packages:

```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/alex-cova/balatro4j")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.balatro:balatro4j:2.0.1")
}
```

### Compile to native image (optional)

```bash
./gradlew nativeCompile
```

> The compiled binary will be located at `build/native/nativeCompile/`.

---

## ⚙️ Configuration

The project is self-contained and requires no external configuration files. Gradle build behavior can be tuned through `gradle.properties`:

```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.jvmargs=-Xmx8192m
org.gradle.configuration-cache.parallel=true
```

For CI/CD publishing, the following environment variables are required:

| Variable | Description |
|---|---|
| `MAVEN_USER` | GitHub username for publishing to GitHub Packages |
| `MAVEN_SECRET` | GitHub token/secret for publishing |

---

## 🧑‍💻 Usage & Examples

### 1. Analyze a specific seed

```java
import com.balatro.api.Balatro;
import com.balatro.api.Run;

Run run = Balatro.builder("2K9H9HN", 8)
        .analyzeAll()
        .analyze();

System.out.println(run.toJson());
```

### 2. Analyze a random seed (up to ante 8)

```java
Run run = Balatro.random(8).analyzeAll();
```

### 3. Search for seeds with specific jokers (scalar)

The standard `Balatro.search()` API runs a full seed analysis for every candidate. Use this when filters require complete run inspection (shop queues, multiple antes, complex AND/OR logic).

```java
import com.balatro.api.Balatro;
import com.balatro.api.Run;
import com.balatro.enums.*;

var seeds = Balatro.search(10, 1_000_000)
        .configuration(config -> config
                .maxAnte(1)
                .disablePack(PackKind.Buffoon))
        .filter(LegendaryJoker.Perkeo.inPack()
                .and(LegendaryJoker.Triboulet.inPack())
                .and(RareJoker.Blueprint.inShop())
                .and(RareJoker.Brainstorm.inShop()))
        .find();

System.out.println("Seeds found: " + seeds.size());
seeds.forEach(System.out::println);
```

**Example output:**
```
ECGC4XT
NYA8CXV
LK2LWI8
2MFLPG6
1LFG6WV
5116R1D
U7ZYC85
KNIGXTT
```

Speed on a Mac M5 Max (max depth 1 legendary joker search)
- Average speed: **22,889,988 ops/s**
- (Vector) Average speed: **98,482,860 ops/s** 

### 4. Vector-accelerated seed search (SIMD)

`Balatro.vectorSearch()` uses a two-stage pipeline:

1. **Vector prefilter** (`vectorFilter`) — evaluates many seeds in parallel via SIMD lanes, replaying only the PRNG streams needed for the filter.
2. **Scalar verifier** (`filter`) — optional full analysis pass on candidates that survive the prefilter.

This is significantly faster than scalar search when a vector-compatible prefilter exists, because most seeds are rejected without running a full `Balatro.analyze()`.

```java
import com.balatro.api.Balatro;
import com.balatro.enums.*;
import com.balatro.vector.VectorFilters;

var negativeLegendaryFilter = LegendaryJoker.Perkeo.inPack(Edition.Negative)
        .or(LegendaryJoker.Triboulet.inPack(Edition.Negative))
        .or(LegendaryJoker.Canio.inPack(Edition.Negative))
        .or(LegendaryJoker.Yorick.inPack(Edition.Negative))
        .or(LegendaryJoker.Chicot.inPack(Edition.Negative));

var seeds = Balatro.vectorSearch(100_000_000)
        .vectorFilter(VectorFilters.negativeLegendaryInAnteOnePacks())
        .configuration(config -> config
                .maxAnte(1)
                .disableShopQueue()
                .disablePack(PackKind.Buffoon))
        .filter(negativeLegendaryFilter)
        .find();

System.out.println("Seeds found: " + seeds.size());
```

**Built-in vector filters** (`VectorFilters`):

| Filter | Description |
|---|---|
| `findAll()` | Accept every seed (default; useful for benchmarking throughput) |
| `negativeTagRange(minAnte, maxAnte)` | Both tags in the given ante range are `Negative_Tag` |
| `negativeLegendaryInAnteOnePacks()` | A negative-edition legendary soul card can appear in ante 1 Arcana or Spectral packs |

> Vector filters operate on the game's Lua PRNG directly. For filters that cannot be expressed as PRNG stream checks, use `Balatro.search()` instead, or combine a vector prefilter with a scalar `.filter()` verifier as shown above.

### 5. Perkeo — Cached seed searching (instant lookup)

```java
import com.balatro.cache.PreProcessedSeeds;
import com.balatro.enums.*;

var p = new PreProcessedSeeds();
// Build or load the database (run once; subsequent queries resolve instantly)
p.start(Runtime.getRuntime().availableProcessors(), 2_000_000_000);

var result = p.search(List.of(
        LegendaryJoker.Perkeo,
        LegendaryJoker.Triboulet,
        RareJoker.Blueprint,
        RareJoker.Brainstorm,
        UnCommonJoker.Sock_and_Buskin,
        CommonJoker.Hanging_Chad,
        RareJoker.Invisible_Joker
));
```

### 6. Render a seed to PNG

```java
import com.balatro.api.Balatro;
import com.balatro.ui.SeedRenderer;
import javax.imageio.ImageIO;
import java.io.File;

var run = Balatro.random(8).analyzeAll();

var image = new SeedRenderer(run).render();

ImageIO.write(image, "PNG", new File("rendered.png"));
```

### 7. Export a seed to JSON

```java
Run run = Balatro.builder("2K9H9HN", 8)
        .analyzeAll()
        .analyze();

System.out.println(run.toJson());
```

<details>
<summary>📄 Example JSON output (click to expand)</summary>

```json
{
  "seed": "2K9H9HN",
  "antes": [
    {
      "ante": 1,
      "shopQueue": [
        { "item": "Drunkard" },
        { "item": "Half Joker" },
        { "item": "Eri" },
        { "item": "Burglar" },
        { "item": "Blackboard" },
        { "item": "The Emperor" },
        { "item": "Drunkard" },
        { "item": "Splash" },
        { "item": "Justice" },
        { "item": "To the Moon" },
        { "item": "The Devil" },
        { "item": "Eri" },
        { "item": "Even Steven" },
        { "item": "Gift Card" },
        { "item": "Mercury" }
      ],
      "tags": ["D6_Tag", "Boss_Tag"],
      "voucher": "Directors_Cut",
      "boss": "The_Club",
      "packs": [
        {
          "type": "Buffoon_Pack",
          "size": 2,
          "choices": 1,
          "options": [
            { "name": "Raised Fist" },
            { "name": "Baseball Card" }
          ],
          "kind": "Buffoon"
        },
        {
          "type": "Arcana_Pack",
          "size": 3,
          "choices": 1,
          "options": [
            { "name": "The Lovers" },
            { "name": "The Moon" },
            { "name": "The World" }
          ],
          "kind": "Arcana"
        },
        {
          "type": "Standard_Pack",
          "size": 3,
          "choices": 1,
          "options": [
            { "name": "Gold 3 of Clubs" },
            { "name": "Bonus 5 of Hearts" },
            { "name": "Gold Queen of Spades" }
          ],
          "kind": "Standard"
        }
      ]
    }
  ]
}
```
</details>

### 8. Custom filter with OR / AND logic

```java
var seeds = Balatro.search(1, 1_000_000)
        .configuration(config -> config.maxAnte(1))
        .filter(LegendaryJoker.Perkeo.inPack(1)
                .or(LegendaryJoker.Triboulet.inPack(1))
                .and(RareJoker.Blueprint.inShop(1)))
        .find();
```

---

## 📚 API Reference

### `Balatro` — Entry Point

| Method | Description |
|---|---|
| `Balatro.builder(String seed)` | Create an analyzer for a specific seed (default 8 antes) |
| `Balatro.builder(String seed, int maxAnte)` | Create an analyzer for a specific seed up to `maxAnte` |
| `Balatro.random(int maxAnte)` | Create an analyzer with a randomly generated seed |
| `Balatro.search()` | Create a scalar `SeedFinder` using all CPU cores, 1M seeds/thread |
| `Balatro.search(int parallelism, int seedsPerThread)` | Create a scalar `SeedFinder` with explicit settings |
| `Balatro.vectorSearch()` | Create a SIMD `VectorSeedFinder` using all CPU cores, 1M seeds/thread |
| `Balatro.vectorSearch(int parallelism, int seedsPerThread)` | Create a `VectorSeedFinder` with explicit settings |
| `.analyzeAll()` | Enable analysis of all game components |
| `.analyze()` | Run the analysis and return a `Run` |
| `.maxAnte(int)` | Limit analysis to a maximum ante |
| `.deck(Deck)` | Set the starting deck |
| `.stake(Stake)` | Set the stake level |
| `.disablePack(PackKind)` | Skip a specific pack type during analysis |

### `SeedFinder` / `VectorSeedFinder` — Search Builder

Both `Balatro.search()` and `Balatro.vectorSearch()` return a fluent search builder implementing `SeedFinder`. `VectorSeedFinder` adds an extra SIMD prefilter stage.

| Method | Description |
|---|---|
| `.filter(Filter)` | Set the scalar filter (full analysis per candidate; also used as verifier in vector search) |
| `.vectorFilter(VectorSeedFilter)` | _(Vector only)_ Set the SIMD prefilter that rejects candidates before full analysis |
| `.configuration(Consumer<Balatro>)` | Configure the analyzer for each candidate seed |
| `.autoConfigure()` | Let the library infer the optimal configuration from the filter |
| `.progressListener(BiConsumer<String, Integer>)` | Register a progress callback |
| `.find()` | Execute the search and return matching seeds |

### `VectorSeedFilter` — SIMD Prefilters

Implement or use built-in filters from `VectorFilters`. Each filter replays specific PRNG streams across all SIMD lanes:

```java
VectorFilters.findAll()                              // no prefiltering
VectorFilters.negativeTagRange(1, 3)                 // Negative_Tag on both tags, antes 1–3
VectorFilters.negativeLegendaryInAnteOnePacks()      // negative soul legendary in ante 1 packs
```

Custom filters implement `VectorSeedFilter` and return a `VectorMaskBits` lane mask from `filter(VectorSearchContext)`. Filters that also implement `ScalarSeedPrefilter` can fall back to a scalar batch path on hardware with fewer than 4 SIMD lanes.

### `Filter` — Composable Filters

```java
// Built-in filter factories on all Item / Joker enums
LegendaryJoker.Perkeo.inPack()          // joker appears in any Buffoon pack
RareJoker.Blueprint.inShop()            // joker appears in any shop slot
LegendaryJoker.Triboulet.inPack(2)      // joker appears in Buffoon pack at ante 2
Tag.Negative_Tag.inAnte()               // tag appears in any ante

// Combine filters
Filter f = filterA.and(filterB).or(filterC);

// Logical helpers
Filter.compound(List.of(f1, f2, f3));   // ALL must match
Filter.findAll();                        // match every seed (debug/export use case)
```

### `Run` — Seed Analysis Result

| Method | Description |
|---|---|
| `run.seed()` | The seed string |
| `run.getAnte(int)` | Get a specific ante's data |
| `run.antes()` | List of all analyzed antes |
| `run.getJokers()` | All joker names across all antes |
| `run.getRareJokers()` | Rare joker names across all antes |
| `run.getTags()` | All tag names |
| `run.getVouchers()` | All vouchers |
| `run.getBosses()` | All boss names |
| `run.getLegendaryJokers()` | Legendary joker names |
| `run.getScore()` | Computed numeric score |
| `run.toJson()` | Serialize the full run to JSON |

---

## 🏗️ Project Structure

```
Balatro4j/
├── perkeo/                                # Core analysis library (publishable JAR)
│   └── src/main/java/com/balatro/
│       ├── api/                           # Public interfaces (Balatro, Run, SeedFinder, Filter…)
│       │   └── filter/                    # Composable filter implementations
│       ├── impl/                          # Concrete implementations
│       ├── enums/                         # Game element enums (Joker, Deck, Stake, Boss, Tag…)
│       ├── structs/                       # Data transfer objects (JokerData, Pack, ShopItem…)
│       ├── cache/                         # Perkeo / Canio caching layer
│       ├── vector/                        # SIMD seed finder (VectorSeedFinder, VectorFilters, PRNG streams)
│       └── jackson/                       # Custom Jackson serializers
│
├── ui/                                    # Swing desktop UI module
│   └── src/main/java/com/balatro/ui/
│       ├── Main.java                      # Application entry point
│       ├── SeedRenderer.java              # Renders a Run to a BufferedImage / PNG
│       └── ...                            # Layout managers, sprite utilities, components
│
├── src/test/java/tests/                   # Integration & performance test harness
│   ├── Perkeo.java                        # Perkeo database tests
│   ├── CanioGenerator.java
│   ├── Differentiator.java
│   └── UITest.java
│
├── .github/workflows/
│   ├── compile-native.yaml               # GraalVM native image CI + EC2 deploy
│   └── release.yaml                      # Tag-triggered publish to GitHub Packages
│
├── build.gradle.kts                       # Root build (aggregates modules)
├── settings.gradle.kts                    # Module declarations
├── gradle/shared.versions.toml           # Centralized dependency versions
└── gradle.properties                      # JVM / caching / parallelism settings
```

---

## 🧪 Tests

Run the full test suite with:

```bash
./gradlew test
```

Run tests for the core library only:

```bash
./gradlew :perkeo:test
```

Tests cover:
- **RNG correctness** — Lua random number generator fidelity (`RNGTests`, `VectorLuaRandomTests`)
- **Seed analysis** — Pack contents and shop queues verified against known seeds (`BalatroTests`)
- **Vector filters** — SIMD prefilters match scalar analysis on known seeds (`VectorFiltersTest`)
- **Cache round-trips** — Serialization/deserialization of `Query` and `QueryResult`
- **Lock mechanics** — Voucher and joker unlock sequencing (`LockTests`)
- **Scoring** — Ante-level score calculation (`ScoringTest`)
- **32-bit seed encoding** — Compact seed representation validation (`Seed32Tests`)

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m "Add my feature"`
4. Push to your fork: `git push origin feature/my-feature`
5. Open a Pull Request

**Guidelines:**
- Target Java 26 source/target compatibility
- Add or update tests for any new functionality
- Follow existing package structure (`api` for interfaces, `impl` for implementations)
- Use `@NotNull` / `@Nullable` JetBrains annotations consistently

---

## 🗺️ Roadmap

- [ ] Full ante 8 analysis coverage
- [ ] Web API / REST endpoint for remote seed queries
- [ ] Expand Canio database capacity beyond 20M seeds
- [ ] Additional deck / stake configurations
- [ ] Improved UI with filtering history and seed comparison

---

## 📄 License

No explicit license file is currently included in this repository. All rights reserved by the author unless otherwise stated.

---

## 👤 Author

**alex-cova**
GitHub: [@alex-cova](https://github.com/alex-cova)
Repository: [github.com/alex-cova/balatro4j](https://github.com/alex-cova/balatro4j)

---

*Balatro4j is a fan-made tool and is not affiliated with or endorsed by LocalThunk or the official Balatro game.*

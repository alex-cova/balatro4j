# balatro4j

A balatro seed analyzer and finder, coded in pure Java based on Immolate

### Features

- Seed render, convert any seed to PNG
- Seed scoring
- Fully customizable seed searching API with autoconfiguration for max speed
- Basic UI to find and view seeds
- Perkeo a seed database able to find any seed instantly
- Canio an ultra compressed seed database (256bit per seed) to instant seed searching up to 20 million seeds

Requeriments:

- Java 23 or GraalVM 23 if you want to compile the project to native image
- Gradle 8.12

### Native compilation

```shell
./gradlew nativeCompile
```

> I've seen more speed running the JVM version than the native one, so I recommend using the JVM version

### Examples

```java
void find() {
    var seeds = Balatro.search(10, 1_000_000)
            .configuration(config -> config.maxAnte(1)
                    .disablePack(PackKind.Buffoon))
            .filter(Perkeo.inPack().and(Triboulet.inPack())
                    .and(RareJoker.Blueprint.inShop()).and(RareJoker.Brainstorm.inShop()))
            .find();

    System.out.println("Seeds found: " + seeds.size());

    for (Run seed : seeds) {
        System.out.println(seed.toString());
    }
}
```

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

## Perkeo cached seed searching


```java
void cachedSearch(){
    var p = new PreProcessedSeeds();
    p.start(Runtime.getRuntime().availableProcessors(), 2_000_000_000);
    //After created the db, subsequent queries will be instant resolved    
    
    var result = p.search(List.of(Perkeo, Triboulet, RareJoker.Blueprint, RareJoker.Brainstorm,
            UnCommonJoker.Sock_and_Buskin, CommonJoker.Hanging_Chad, RareJoker.Invisible_Joker));
}
```


## Convert a seed into a Image

```java
void seedToImage(){
    var run = Balatro.random(8)
            .analyzeAll();

    var image = new SeedRenderer(run)
            .render();

    try {
        ImageIO.write(image, "PNG", new File("rendered.png"));
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
```

## Convert a seed into a JSON

```java
void runToJson() {
    var run = Balatro.builder("2K9H9HN", 8)
            .analyzeAll()
            .analyze();

    System.out.println(run.toJson());
}
```

output:

```json
{
  "seed": "2K9H9HN",
  "antes": [
    {
      "ante": 1,
      "shopQueue": [
        {
          "item": "Drunkard"
        },
        {
          "item": "Half Joker"
        },
        {
          "item": "Eri"
        },
        {
          "item": "Burglar"
        },
        {
          "item": "Blackboard"
        },
        {
          "item": "The Emperor"
        },
        {
          "item": "Drunkard"
        },
        {
          "item": "Splash"
        },
        {
          "item": "Justice"
        },
        {
          "item": "To the Moon"
        },
        {
          "item": "The Devil"
        },
        {
          "item": "Eri"
        },
        {
          "item": "Even Steven"
        },
        {
          "item": "Gift Card"
        },
        {
          "item": "Mercury"
        }
      ],
      "tags": [
        "D6_Tag",
        "Boss_Tag"
      ],
      "voucher": "Directors_Cut",
      "boss": "The_Club",
      "packs": [
        {
          "type": "Buffoon_Pack",
          "size": 2,
          "choices": 1,
          "options": [
            {
              "name": "Raised Fist"
            },
            {
              "name": "Baseball Card"
            }
          ],
          "kind": "Buffoon"
        },
        {
          "type": "Arcana_Pack",
          "size": 3,
          "choices": 1,
          "options": [
            {
              "name": "The Lovers"
            },
            {
              "name": "The Moon"
            },
            {
              "name": "The World"
            }
          ],
          "kind": "Arcana"
        },
        {
          "type": "Buffoon_Pack",
          "size": 2,
          "choices": 1,
          "options": [
            {
              "name": "Jolly Joker"
            },
            {
              "name": "Gift Card"
            }
          ],
          "kind": "Buffoon"
        },
        {
          "type": "Standard_Pack",
          "size": 3,
          "choices": 1,
          "options": [
            {
              "name": "Gold 3 of Clubs"
            },
            {
              "name": "Bonus 5 of Hearts"
            },
            {
              "name": "Gold Queen of Spades"
            }
          ],
          "kind": "Standard"
        }
      ]
    }
  ],
  "firstAnte": {
    "ante": 1,
    "shopQueue": [
      {
        "item": "Drunkard"
      },
      {
        "item": "Half Joker"
      },
      {
        "item": "Eri"
      },
      {
        "item": "Burglar"
      },
      {
        "item": "Blackboard"
      },
      {
        "item": "The Emperor"
      },
      {
        "item": "Drunkard"
      },
      {
        "item": "Splash"
      },
      {
        "item": "Justice"
      },
      {
        "item": "To the Moon"
      },
      {
        "item": "The Devil"
      },
      {
        "item": "Eri"
      },
      {
        "item": "Even Steven"
      },
      {
        "item": "Gift Card"
      },
      {
        "item": "Mercury"
      }
    ],
    "tags": [
      "D6_Tag",
      "Boss_Tag"
    ],
    "voucher": "Directors_Cut",
    "boss": "The_Club",
    "packs": [
      {
        "type": "Buffoon_Pack",
        "size": 2,
        "choices": 1,
        "options": [
          {
            "name": "Raised Fist"
          },
          {
            "name": "Baseball Card"
          }
        ],
        "kind": "Buffoon"
      },
      {
        "type": "Arcana_Pack",
        "size": 3,
        "choices": 1,
        "options": [
          {
            "name": "The Lovers"
          },
          {
            "name": "The Moon"
          },
          {
            "name": "The World"
          }
        ],
        "kind": "Arcana"
      },
      {
        "type": "Buffoon_Pack",
        "size": 2,
        "choices": 1,
        "options": [
          {
            "name": "Jolly Joker"
          },
          {
            "name": "Gift Card"
          }
        ],
        "kind": "Buffoon"
      },
      {
        "type": "Standard_Pack",
        "size": 3,
        "choices": 1,
        "options": [
          {
            "name": "Gold 3 of Clubs"
          },
          {
            "name": "Bonus 5 of Hearts"
          },
          {
            "name": "Gold Queen of Spades"
          }
        ],
        "kind": "Standard"
      }
    ]
  }
}
```

```java
var seeds = Balatro.finder(1, 1_000_000)
        .configuration(config -> config.maxAnte(1))
        .filter(LegendaryJoker.Perkeo.inPack(1)
                .or(LegendaryJoker.Triboulet.inPack(1))
                .and(RareJoker.Blueprint.inShop(1)))
        .find();
```

## Example seeds found with Perkeo And Blueprint at Ante 1

```
CL7GH6H
X7CXUJP
WNL26QF
3K2YXQU
S258PHZ
L3F8RIJ
TOB4W98
AJSOD4X
N2DLGEJ
P36G9DH
E74BJ11
NG5OCZP
H7ENEO3
U5KN877
ZG4PCNG
9D7T4NL
MG7LPGN
JVT8XAK
JRMGDQ1
LPQW89R
JACU3YR
U7JZ2IP
Q6NVSEK
PDOFM57
R2GYU2O
4O5N1WE
LOO1XGD
CY8GJHV
D56BSVH
T41RPVX
K6ORMCO
OHME2GT
XE13DOC
JQ7UY5Q
VVBS744
BNSJKVT
SAVPR55
GNAL7N5
27LFAKN
M3NHKVF
A9WBD1R
8CWQ9HF
3YZF6BN
XKLMQWV
N9E9D32
IXMJ3LP
2A55NLM
9WPH7DG
WHOQQZD
3WBK1NK
```

> Average speed: 6,114,471 seeds/sec, max depth 1 legendary joker search
